package net.claustra01.clayium.gametest.compat;

import java.util.Set;
import java.util.stream.Collectors;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.compat.RecipeViewerCatalysts;
import net.claustra01.clayium.compat.RecipeViewerData;
import net.claustra01.clayium.compat.RecipeViewerDescriptions;
import net.claustra01.clayium.compat.RecipeViewerLayout;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.claustra01.clayium.workbench.ClayWorkTableBlockEntity;
import net.claustra01.clayium.workbench.ClayWorkTableMenu;
import net.claustra01.clayium.workbench.ClayCraftingBoardBlockEntity;
import net.claustra01.clayium.workbench.ClayCraftingBoardMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RecipeViewerGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalNeiCoordinatesDriveEveryMachineViewerLayout(GameTestHelper helper) {
        ClayiumMachineRecipe recipe = recipe(helper, "gametest/machine_recipe");
        RecipeViewerLayout layout = RecipeViewerLayout.machine(recipe);
        require(layout.width() == 166 && layout.height() == 65,
                "The undecorated 176 by 103 viewer layout is still in use");
        require(layout.background().texture().equals(ClayiumMod.id("textures/gui/nei.png"))
                        && layout.background().u() == 5 && layout.background().v() == 11,
                "The original NEI texture region is not the viewer background");
        require(layout.text().get(0).x() == 6 && layout.text().get(0).y() == 43
                        && layout.text().get(1).x() == 6 && layout.text().get(1).y() == 52,
                "Tier or CE text moved away from the original NEI coordinates");
        require(layout.progress().x() == 71 && layout.progress().y() == 21
                        && layout.progress().width() == 24 && layout.progress().height() == 17,
                "The progress indicator does not stay between the original input and output wells");
        require(layout.slots().stream().anyMatch(slot -> slot.kind() == RecipeViewerLayout.SlotKind.FLUID_INPUT)
                        && layout.slots().stream().anyMatch(
                                slot -> slot.kind() == RecipeViewerLayout.SlotKind.FLUID_OUTPUT),
                "Direct fluid slots were omitted from the shared layout");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void everyRecipeMethodAndOriginalDescriptionPageIsCovered(GameTestHelper helper) {
        for (ClayiumRecipeMethod method : ClayiumRecipeMethod.values()) {
            require(RecipeRegistration.type(method) != null, "Missing viewer recipe type for " + method.id());
            require(!RecipeViewerCatalysts.forMethod(method).isEmpty(),
                    "Missing viewer catalyst for " + method.id());
            helper.getLevel().getRecipeManager().getAllRecipesFor(RecipeRegistration.type(method)).forEach(holder -> {
                ClayiumMachineRecipe recipe = holder.value();
                RecipeViewerLayout layout = RecipeViewerLayout.machine(recipe);
                require(count(layout, RecipeViewerLayout.SlotKind.ITEM_INPUT) == recipe.itemInputs().size()
                                && count(layout, RecipeViewerLayout.SlotKind.FLUID_INPUT) == recipe.fluidInputs().size()
                                && count(layout, RecipeViewerLayout.SlotKind.ITEM_OUTPUT) == recipe.itemOutputs().size()
                                && count(layout, RecipeViewerLayout.SlotKind.FLUID_OUTPUT) == recipe.fluidOutputs().size()
                                && (method == ClayiumRecipeMethod.CHEMICAL_METAL_SEPARATOR
                                        ? count(layout, RecipeViewerLayout.SlotKind.WEIGHTED_OUTPUTS) == 1
                                        && !recipe.randomOutputs().isEmpty()
                                        : count(layout, RecipeViewerLayout.SlotKind.RANDOM_OUTPUT)
                                                == recipe.randomOutputs().size()),
                        "Viewer omitted a recipe input or output: " + holder.id());
                require(layout.slots().stream().allMatch(slot -> slot.x() >= 0 && slot.y() >= 0
                                && slot.x() + 16 <= layout.width() && slot.y() + 16 <= layout.height()),
                        "Viewer slot lies outside the original NEI content: " + holder.id());
            });
        }
        var decomposition = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(RecipeRegistration.type(ClayiumRecipeMethod.EC_DECOMPOSER));
        require(decomposition.size() == 14 && decomposition.stream().allMatch(holder -> holder.value().requiredTier() == 13)
                        && recipe(helper, "progression/machines/energetic_clay_decomposer").itemOutputs().getFirst()
                                .is(RecipeViewerCatalysts.forMethod(ClayiumRecipeMethod.EC_DECOMPOSER).getFirst().getItem()),
                "The original tier 13 decomposer, its assembly recipe and fourteen decompositions must be discoverable");
        Set<String> descriptions = RecipeViewerDescriptions.pages().stream()
                .peek(page -> require(!page.stacks().isEmpty(), "Description has no searchable item: " + page.id()))
                .map(RecipeViewerDescriptions.Page::id)
                .collect(Collectors.toSet());
        require(descriptions.equals(Set.of(
                        "cobblestone_generator",
                        "salt_extractor",
                        "quartz_crucible",
                        "solar_clay_fabricator",
                        "clay_tree")),
                "The five original NEI description pages are not covered exactly");
        require(RecipeViewerCatalysts.forMethod(ClayiumRecipeMethod.MILLING_MACHINE).stream()
                        .anyMatch(stack -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())
                                .equals(ClayiumMod.id("elemental_milling_machine"))),
                "The Elemental Milling Machine is missing from milling catalysts");
        require(RecipeViewerCatalysts.forMethod(ClayiumRecipeMethod.CLAY_WORK_TABLE).stream()
                        .allMatch(stack -> stack.is(WorkbenchRegistration.CLAY_WORK_TABLE_ITEM.get())),
                "Only the work table can perform manual tool operations");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void workToolsAndMetalProbabilitiesUseServerRecipeInputs(GameTestHelper helper) {
        BlockPos tablePos = new BlockPos(1, 2, 1);
        helper.setBlock(tablePos, WorkbenchRegistration.CLAY_WORK_TABLE.get());
        ClayWorkTableBlockEntity table = helper.getBlockEntity(tablePos);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ClayWorkTableMenu menu = new ClayWorkTableMenu(0, player.getInventory(), table);
        require(menu.slots.get(0).getSlotIndex() == ClayWorkTableBlockEntity.INPUT_SLOT
                        && menu.slots.get(1).getSlotIndex() == ClayWorkTableBlockEntity.TOOL_SLOT,
                "Viewer input order does not match work table transfer slots");
        require(!menu.slots.get(1).mayPlace(new ItemStack(Items.DIRT))
                        && !menu.slots.get(2).mayPlace(new ItemStack(Items.CLAY))
                        && !menu.slots.get(3).mayPlace(new ItemStack(Items.CLAY)),
                "Work table accepts invalid transfer into tool or output slots");
        player.getInventory().setItem(9, new ItemStack(WorkbenchRegistration.CLAY_ROLLING_PIN.get()));
        require(!menu.quickMoveStack(player, 4).isEmpty() && menu.getSlot(1).hasItem()
                        && !menu.getSlot(0).hasItem(), "Shift-click must equip the tool");
        player.getInventory().setItem(9, new ItemStack(Items.CLAY, 2));
        menu.clicked(4, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        menu.clicked(1, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        require(menu.getCarried().is(Items.CLAY) && menu.getCarried().getCount() == 2
                        && menu.getSlot(1).getItem().is(WorkbenchRegistration.CLAY_ROLLING_PIN.get()),
                "An invalid transfer into the tool slot must leave the cursor and tool intact");
        menu.clicked(0, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        require(menu.getCarried().isEmpty() && menu.getSlot(0).getItem().getCount() == 2,
                "Transferring material must preserve its quantity");
        BlockPos boardPos = new BlockPos(3, 2, 1);
        helper.setBlock(boardPos, WorkbenchRegistration.CLAY_CRAFTING_BOARD.get());
        ClayCraftingBoardBlockEntity board = helper.getBlockEntity(boardPos);
        ClayCraftingBoardMenu boardMenu = new ClayCraftingBoardMenu(1, player.getInventory(), board);
        require(!boardMenu.slots.get(0).mayPlace(new ItemStack(Items.CLAY))
                        && java.util.stream.IntStream.range(1, 10)
                                .allMatch(index -> boardMenu.slots.get(index).getSlotIndex() == index - 1),
                "Crafting board transfer grid does not match the vanilla recipe order");
        var workRecipes = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(RecipeRegistration.type(ClayiumRecipeMethod.CLAY_WORK_TABLE));
        require(workRecipes.stream().anyMatch(holder -> {
            RecipeViewerData data = RecipeViewerData.from(holder.value());
            RecipeViewerLayout layout = RecipeViewerLayout.machine(holder.value());
            return data.requiredTier() == 3
                    && layout.operation() == 3 && layout.background().texture().equals(ClayiumMod.id("textures/gui/clayworktable.png"))
                    && layout.slots().getFirst().x() == 12 && layout.slots().getFirst().y() == 19
                    && data.workToolItems().size() == 1
                    && layout.slots().stream().anyMatch(slot -> slot.kind() == RecipeViewerLayout.SlotKind.TOOL);
        }), "The work-table viewer does not expose the required server-side tool");

        ClayiumMachineRecipe separator = recipe(helper, "chemical_metal_separator/industrial_clay");
        require(RecipeViewerLayout.totalRandomWeight(separator) == 488,
                "The metal-separator viewer changed the original weight denominator");
        double totalPercent = separator.randomOutputs().stream()
                .mapToDouble(output -> RecipeViewerLayout.randomOutputPercent(separator, output)).sum();
        require(Math.abs(totalPercent - 100.0D) < 0.0001D,
                "The metal-separator probabilities do not total 100 percent");
        require(RecipeViewerLayout.machine(separator).slots().stream()
                        .filter(slot -> slot.kind() == RecipeViewerLayout.SlotKind.WEIGHTED_OUTPUTS).count() == 1,
                "Metal-separator results are not presented in the original cycling output position");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void loadedDatapackValuesRemainTheSingleViewerSource(GameTestHelper helper) {
        ClayiumMachineRecipe recipe = recipe(helper, "gametest/machine_recipe");
        RecipeViewerData data = RecipeViewerData.from(recipe);
        require(data.requiredTier() == recipe.requiredTier()
                        && data.processingTicks() == 40
                        && data.clayEnergyPerTick() == 750_000L
                        && data.clayEnergy() == 30_000_000L,
                "Tier, time, CE/t, or total CE diverges from the loaded datapack recipe");
        require(recipe.fluidInputs().getFirst().getAmount() == 250
                        && recipe.fluidOutputs().getFirst().getAmount() == 125,
                "The viewer source lost direct fluid mB values");
        helper.succeed();
    }

    private static ClayiumMachineRecipe recipe(GameTestHelper helper, String id) {
        return (ClayiumMachineRecipe)helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id(id)).orElseThrow().value();
    }

    private static long count(RecipeViewerLayout layout, RecipeViewerLayout.SlotKind kind) {
        return layout.slots().stream().filter(slot -> slot.kind() == kind).count();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private RecipeViewerGameTests() {
    }
}
