package net.claustra01.clayium.gametest.workbench;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.FoundationItemRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.claustra01.clayium.workbench.ClayCraftingBoardBlockEntity;
import net.claustra01.clayium.workbench.ClayCraftingBoardMenu;
import net.claustra01.clayium.workbench.ClayWorkTableBlockEntity;
import net.claustra01.clayium.workbench.ClayWorkTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.inventory.ClickType;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WorkbenchGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void vanillaResourcesReachTierOneParts(GameTestHelper helper) {
        Set<Item> reachable = new HashSet<>(List.of(Items.CLAY_BALL, Items.CLAY, Items.OAK_LOG));
        boolean changed;
        do {
            changed = false;
            for (RecipeHolder<?> holder : helper.getLevel().getRecipeManager().getRecipes()) {
                if (holder.value().getType() != RecipeType.CRAFTING
                        && holder.value().getType() != RecipeType.SMELTING
                        && holder.value().getType() != net.claustra01.clayium.registry.RecipeRegistration.type(
                                ClayiumRecipeMethod.CLAY_WORK_TABLE)) {
                    continue;
                }
                if (holder.value() instanceof ClayiumMachineRecipe workRecipe
                        && !workToolReachable(workRecipe.requiredTier(), reachable)) {
                    continue;
                }
                ItemStack result = holder.value().getResultItem(helper.getLevel().registryAccess());
                if (result.isEmpty() || reachable.contains(result.getItem())) {
                    continue;
                }
                if (holder.value().getIngredients().stream().allMatch(ingredient -> canSupply(ingredient, reachable))) {
                    changed |= reachable.add(result.getItem());
                }
            }
        } while (changed);

        List<Item> required = List.of(
                WorkbenchRegistration.CLAY_WORK_TABLE_ITEM.get(),
                WorkbenchRegistration.CLAY_CRAFTING_BOARD_ITEM.get(),
                WorkbenchRegistration.RAW_CLAY_ROLLING_PIN.get(),
                WorkbenchRegistration.CLAY_ROLLING_PIN.get(),
                WorkbenchRegistration.RAW_CLAY_SLICER.get(),
                WorkbenchRegistration.CLAY_SLICER.get(),
                WorkbenchRegistration.RAW_CLAY_SPATULA.get(),
                WorkbenchRegistration.CLAY_SPATULA.get(),
                FoundationItemRegistration.CLAY_WRENCH.get(),
                WorkbenchRegistration.CLAY_IO_CONFIGURATOR.get(),
                WorkbenchRegistration.CLAY_PIPING_CONFIGURATOR.get(),
                WorkbenchRegistration.MEMORY_CARD.get(),
                WorkbenchRegistration.DIRECTION_MEMORY_CARD.get(),
                WorkbenchRegistration.RAW_CLAY_MACHINE_HULL_ITEM.get(),
                WorkbenchRegistration.CLAY_MACHINE_HULL_ITEM.get(),
                MaterialRegistration.MATERIAL_ITEMS.get("clay_plate").get(),
                MaterialRegistration.MATERIAL_ITEMS.get("clay_stick").get(),
                MaterialRegistration.MATERIAL_ITEMS.get("clay_pipe").get(),
                MaterialRegistration.MATERIAL_ITEMS.get("clay_gear").get());
        for (Item item : required) {
            require(reachable.contains(item), "Tier 1 progression is not reachable from vanilla resources: "
                    + BuiltInRegistries.ITEM.getKey(item));
        }
        helper.setBlock(new BlockPos(1, 1, 1), WorkbenchRegistration.CLAY_WORK_TABLE.get());
        helper.setBlock(new BlockPos(3, 1, 1), WorkbenchRegistration.CLAY_CRAFTING_BOARD.get());
        helper.getLevel().getRecipeManager().getRecipeIds()
                .filter(id -> id.getNamespace().equals(ClayiumMod.MOD_ID) && id.getPath().startsWith("workbench/"))
                .forEach(id -> assertNoExternalItems(helper, id));
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void clayWorkTablePressesConsumeInputsAndDamageTools(GameTestHelper helper) {
        BlockPos position = new BlockPos(1, 1, 1);
        helper.setBlock(position, WorkbenchRegistration.CLAY_WORK_TABLE.get());
        ClayWorkTableBlockEntity table = helper.getBlockEntity(position);
        table.inventory().setStackInSlot(ClayWorkTableBlockEntity.INPUT_SLOT, new ItemStack(Items.CLAY));
        table.inventory().setStackInSlot(
                ClayWorkTableBlockEntity.TOOL_SLOT,
                new ItemStack(WorkbenchRegistration.CLAY_ROLLING_PIN.get()));
        for (int press = 0; press < 4; press++) {
            require(table.press(3), "A valid rolling-pin press was rejected");
        }
        require(table.inventory().getStackInSlot(ClayWorkTableBlockEntity.INPUT_SLOT).isEmpty(),
                "Completed work did not consume the clay block");
        ItemStack output = table.inventory().getStackInSlot(ClayWorkTableBlockEntity.FIRST_OUTPUT_SLOT);
        require(output.is(MaterialRegistration.MATERIAL_ITEMS.get("clay_plate").get()) && output.getCount() == 4,
                "Completed work did not produce four clay plates");
        require(table.inventory().getStackInSlot(ClayWorkTableBlockEntity.TOOL_SLOT).getDamageValue() == 1,
                "The forming tool did not take exactly one point of damage");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void clayCraftingBoardUsesAdjacentItemHandlerWithoutLoss(GameTestHelper helper) {
        BlockPos boardPosition = new BlockPos(1, 1, 1);
        BlockPos chestPosition = boardPosition.east();
        helper.setBlock(boardPosition, WorkbenchRegistration.CLAY_CRAFTING_BOARD.get());
        helper.setBlock(chestPosition, Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(chestPosition);
        chest.setItem(0, new ItemStack(Items.CLAY_BALL, 8));
        ClayCraftingBoardBlockEntity board = helper.getBlockEntity(boardPosition);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ClayCraftingBoardMenu menu = new ClayCraftingBoardMenu(1, player.getInventory(), board);
        for (int slot : List.of(1, 2, 4, 5)) {
            menu.slots.get(slot).set(new ItemStack(Items.CLAY_BALL));
        }
        require(menu.slots.get(0).getItem().is(Items.CLAY), "The board did not match the clay recipe");
        menu.clicked(0, 0, ClickType.PICKUP, player);
        require(menu.getCarried().is(Items.CLAY) && menu.getCarried().getCount() == 1,
                "The board did not deliver the crafted clay");
        require(chest.getItem(0).getCount() == 4, "The board did not draw exactly four clay balls from the chest");
        for (int slot : List.of(1, 2, 4, 5)) {
            require(menu.slots.get(slot).getItem().is(Items.CLAY_BALL), "The pattern was not replenished");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void workbenchMenusExposeOperableSlots(GameTestHelper helper) {
        BlockPos workTablePosition = new BlockPos(1, 1, 1);
        BlockPos boardPosition = new BlockPos(3, 1, 1);
        helper.setBlock(workTablePosition, WorkbenchRegistration.CLAY_WORK_TABLE.get());
        helper.setBlock(boardPosition, WorkbenchRegistration.CLAY_CRAFTING_BOARD.get());
        helper.setBlock(boardPosition.east(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(boardPosition.east());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ClayWorkTableMenu workTable = new ClayWorkTableMenu(
                1, player.getInventory(), helper.getBlockEntity(workTablePosition));
        ClayCraftingBoardMenu board = new ClayCraftingBoardMenu(
                2, player.getInventory(), helper.getBlockEntity(boardPosition));
        require(workTable.slots.size() == 40, "Clay work table menu does not expose four functional slots");
        require(board.adjacentSlotCount() == 27 && board.slots.size() == 73,
                "Clay crafting board menu does not expose the adjacent chest and crafting slots");
        workTable.setCarried(new ItemStack(Items.CLAY));
        workTable.clicked(0, 0, ClickType.PICKUP, player);
        require(workTable.slots.get(0).getItem().is(Items.CLAY), "Work table input rejects placement");
        workTable.setCarried(new ItemStack(WorkbenchRegistration.CLAY_ROLLING_PIN.get()));
        workTable.clicked(1, 0, ClickType.PICKUP, player);
        require(workTable.slots.get(1).getItem().is(WorkbenchRegistration.CLAY_ROLLING_PIN.get()),
                "Work table tool slot rejects placement");
        workTable.setCarried(new ItemStack(Items.STONE));
        workTable.clicked(2, 0, ClickType.PICKUP, player);
        require(workTable.slots.get(2).getItem().isEmpty(), "Work table output accepted placement");
        workTable.setCarried(ItemStack.EMPTY);
        workTable.quickMoveStack(player, 0);
        require(workTable.slots.get(0).getItem().isEmpty(), "Work table shift click did not move its input");
        ClayWorkTableBlockEntity tableEntity = helper.getBlockEntity(workTablePosition);
        tableEntity.inventory().setStackInSlot(ClayWorkTableBlockEntity.FIRST_OUTPUT_SLOT,
                new ItemStack(Items.CLAY_BALL, 2));
        workTable.quickMoveStack(player, 2);
        require(workTable.slots.get(2).getItem().isEmpty(), "Work table output did not shift click out");
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 3));
        board.clicked(10, 0, ClickType.PICKUP, player);
        require(board.getCarried().is(Items.OAK_PLANKS) && chest.getItem(0).isEmpty(),
                "Board chest slot did not allow pickup");
        board.clicked(11, 0, ClickType.PICKUP, player);
        require(board.getCarried().isEmpty() && chest.getItem(1).getCount() == 3,
                "Board chest slot did not allow placement");
        helper.succeed();
    }

    private static boolean canSupply(Ingredient ingredient, Set<Item> reachable) {
        return ingredient.isEmpty()
                || java.util.Arrays.stream(ingredient.getItems()).anyMatch(stack -> reachable.contains(stack.getItem()));
    }

    private static boolean workToolReachable(int action, Set<Item> reachable) {
        return switch (action) {
            case 1, 2 -> true;
            case 3 -> reachable.contains(WorkbenchRegistration.CLAY_ROLLING_PIN.get());
            case 4, 6 -> reachable.contains(WorkbenchRegistration.CLAY_SLICER.get())
                    || reachable.contains(WorkbenchRegistration.CLAY_SPATULA.get());
            case 5 -> reachable.contains(WorkbenchRegistration.CLAY_SPATULA.get());
            default -> false;
        };
    }

    private static void assertNoExternalItems(GameTestHelper helper, ResourceLocation recipeId) {
        RecipeHolder<?> holder = helper.getLevel().getRecipeManager().byKey(recipeId)
                .orElseThrow(() -> new GameTestAssertException("Generated workbench recipe was not loaded: " + recipeId));
        for (Ingredient ingredient : holder.value().getIngredients()) {
            for (ItemStack stack : ingredient.getItems()) {
                String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
                require(namespace.equals("minecraft") || namespace.equals(ClayiumMod.MOD_ID),
                        "Workbench progression requires an external mod item: " + stack);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private WorkbenchGameTests() {
    }
}
