package net.claustra01.clayium.gametest.recipe;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.gadget.GadgetType;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.NbtPreservingAutoEatRecipe;
import net.claustra01.clayium.registry.FoundationItemRegistration;
import net.claustra01.clayium.registry.GadgetRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OriginalRecipeRepairGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalAssemblerCondenserAndDecomposerValuesAreExact(GameTestHelper helper) {
        assertMachine(helper, "assembler/clay_io_configurator", 6, 10_000L, 20,
                WorkbenchRegistration.CLAY_IO_CONFIGURATOR.get(), 1,
                input(WorkbenchRegistration.CLAY_ROLLING_PIN.get(), 1),
                input(WorkbenchRegistration.CLAY_SLICER.get(), 1));
        assertMachine(helper, "assembler/clay_piping_configurator", 6, 10_000L, 20,
                WorkbenchRegistration.CLAY_PIPING_CONFIGURATOR.get(), 1,
                input(WorkbenchRegistration.CLAY_SPATULA.get(), 1),
                input(FoundationItemRegistration.CLAY_WRENCH.get(), 1));
        assertMachine(helper, "assembler/memory_card", 6, 10_000L, 20,
                WorkbenchRegistration.MEMORY_CARD.get(), 1,
                input(WorkbenchRegistration.CLAY_IO_CONFIGURATOR.get(), 1),
                input(component("precision_circuit"), 2));
        assertMachine(helper, "assembler/direction_memory_card", 6, 10_000L, 20,
                WorkbenchRegistration.DIRECTION_MEMORY_CARD.get(), 1,
                input(material("dense_clay_spindle"), 1), input(component("precision_circuit"), 2));

        assertMachine(helper, "condenser/clay_to_dense_clay", 0, 1L, 4,
                compression("dense_clay"), 1, input(Items.CLAY, 9));
        assertMachine(helper, "condenser/dense_to_compressed_clay", 0, 1L, 4,
                compression("compressed_clay"), 1, input(compression("dense_clay"), 9));
        assertMachine(helper, "condenser/industrial_to_advanced_industrial_clay", 0, 100L, 4,
                compression("advanced_industrial_clay"), 1, input(compression("industrial_clay"), 9));

        assertMachine(helper, "decomposer/clay_block", 0, 1L, 3,
                Items.CLAY_BALL, 4, input(Items.CLAY, 1));
        assertMachine(helper, "decomposer/compressed_clay", 0, 1L, 3,
                compression("dense_clay"), 9, input(compression("compressed_clay"), 1));
        assertMachine(helper, "decomposer/industrial_clay", 0, 1L, 10,
                compression("compressed_clay"), 9, input(compression("industrial_clay"), 1));
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalCraftingLayoutsAndAutoEatDataArePreserved(GameTestHelper helper) {
        assertCraftingMatch(helper, "forming_parts/clay_wrench_original", CraftingInput.of(3, 3, List.of(
                stack(material("dense_clay_blade")), ItemStack.EMPTY, stack(material("dense_clay_blade")),
                ItemStack.EMPTY, stack(material("dense_clay_spindle")), ItemStack.EMPTY,
                ItemStack.EMPTY, stack(material("dense_clay_stick")), ItemStack.EMPTY)));
        assertCraftingMatch(helper, "forming_parts/large_clay_plate", CraftingInput.of(3, 3,
                java.util.Collections.nCopies(9, stack(material("clay_plate")))));
        assertCraftingMatch(helper, "forming_parts/clay_wheel", wheelInput("clay"));
        assertCraftingMatch(helper, "forming_parts/dense_clay_wheel", wheelInput("dense_clay"));

        NbtPreservingAutoEatRecipe recipe = (NbtPreservingAutoEatRecipe)helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id("progression/equipment/clay_gadget_auto_eat")).orElseThrow().value();
        CompoundTag tag = new CompoundTag();
        tag.putInt("original_marker", 73);
        ItemStack economical = stack(gadget(GadgetType.ECONOMICAL_AUTO_EAT));
        economical.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        ItemStack auto = recipe.assemble(CraftingInput.of(1, 1, List.of(economical)),
                helper.getLevel().registryAccess());
        require(auto.is(gadget(GadgetType.AUTO_EAT)) && customMarker(auto) == 73,
                "Economical Auto Eat conversion did not preserve components");
        ItemStack economicalAgain = recipe.assemble(CraftingInput.of(1, 1, List.of(auto)),
                helper.getLevel().registryAccess());
        require(economicalAgain.is(gadget(GadgetType.ECONOMICAL_AUTO_EAT)) && customMarker(economicalAgain) == 73,
                "Reverse Auto Eat conversion did not preserve components");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalClayPartAssemblerAndGrinderRoutesAreExact(GameTestHelper helper) {
        assertMachine(helper, "assembler/clay_spindle", 3, 10L, 20,
                material("clay_spindle"), 1,
                input(material("large_clay_plate"), 1), input(Items.CLAY_BALL, 8));
        assertMachine(helper, "assembler/clay_grinding_head", 3, 10L, 20,
                material("clay_grinding_head"), 1,
                input(material("large_clay_plate"), 1), input(Items.CLAY, 8));
        assertMachine(helper, "assembler/clay_cutting_head", 3, 10L, 20,
                material("clay_cutting_head"), 1,
                input(material("large_clay_plate"), 1), input(material("clay_plate"), 8));
        assertMachine(helper, "assembler/dense_clay_gear_from_sticks", 3, 10L, 20,
                material("dense_clay_gear"), 1, input(material("dense_clay_stick"), 5));
        assertMachine(helper, "assembler/dense_clay_gear_from_short_sticks", 3, 10L, 20,
                material("dense_clay_gear"), 1, input(material("short_dense_clay_stick"), 9));
        assertMachine(helper, "assembler/dense_clay_grinding_head", 3, 100L, 20,
                material("dense_clay_grinding_head"), 1,
                input(material("large_dense_clay_plate"), 1), input(compression("dense_clay"), 8));
        assertMachine(helper, "assembler/dense_clay_cutting_head", 3, 100L, 20,
                material("dense_clay_cutting_head"), 1,
                input(material("large_dense_clay_plate"), 1), input(material("dense_clay_plate"), 8));

        String[] shapes = {"plate", "stick", "short_stick", "ring", "small_ring", "gear", "blade",
                "needle", "disc", "small_disc", "cylinder", "pipe", "large_plate", "grinding_head",
                "bearing", "spindle", "cutting_head"};
        int[] inputs = {1, 4, 8, 4, 8, 8, 1, 1, 2, 4, 1, 1, 1, 1, 4, 1, 1};
        int[] outputs = {1, 1, 1, 5, 1, 9, 1, 2, 3, 1, 2, 1, 4, 16, 5, 4, 9};
        int[] ticks = {3, 3, 3, 15, 3, 27, 3, 6, 9, 3, 6, 3, 12, 48, 15, 12, 27};
        for (String prefix : new String[] {"clay", "dense_clay"}) {
            int multiplier = prefix.equals("clay") ? 1 : 4;
            for (int index = 0; index < shapes.length; index++) {
                String inputId = clayShapeId(prefix, shapes[index]);
                assertMachine(helper, "grinder/" + inputId + "_to_dust",
                        0, 1L, (long)ticks[index] * multiplier, material(prefix + "_dust"), outputs[index],
                        input(material(inputId), inputs[index]));
            }
        }
        helper.succeed();
    }

    private static String clayShapeId(String prefix, String shape) {
        return switch (shape) {
            case "short_stick" -> "short_" + prefix + "_stick";
            case "small_disc" -> "small_" + prefix + "_disc";
            case "small_ring" -> "small_" + prefix + "_ring";
            case "large_plate" -> "large_" + prefix + "_plate";
            default -> prefix + "_" + shape;
        };
    }

    @SuppressWarnings("unchecked")
    private static void assertCraftingMatch(GameTestHelper helper, String path, CraftingInput input) {
        Recipe<CraftingInput> recipe = (Recipe<CraftingInput>)helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id(path)).orElseThrow().value();
        require(recipe.matches(input, helper.getLevel()), path + " does not match the original layout");
    }

    private static CraftingInput wheelInput(String prefix) {
        ItemStack plate = stack(material(prefix + "_plate"));
        return CraftingInput.of(3, 3, List.of(
                plate.copy(), plate.copy(), plate.copy(),
                plate.copy(), stack(material(prefix + "_ring")), plate.copy(),
                plate.copy(), plate.copy(), plate.copy()));
    }

    private static void assertMachine(GameTestHelper helper, String path, int tier, long energy, long ticks,
            Item output, int outputCount, ExpectedInput... inputs) {
        ClayiumMachineRecipe recipe = (ClayiumMachineRecipe)helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id(path)).orElseThrow().value();
        require(recipe.requiredTier() == tier && recipe.clayEnergy() == energy && recipe.processingTicks() == ticks,
                path + " has different tier, CE, or ticks");
        require(recipe.itemOutputs().size() == 1 && recipe.itemOutputs().getFirst().is(output)
                        && recipe.itemOutputs().getFirst().getCount() == outputCount,
                path + " has a different output");
        require(recipe.itemInputs().size() == inputs.length, path + " has a different input count");
        for (int index = 0; index < inputs.length; index++) {
            ExpectedInput expected = inputs[index];
            require(recipe.itemInputs().get(index).count() == expected.count()
                            && recipe.itemInputs().get(index).ingredient().test(stack(expected.item())),
                    path + " input " + index + " differs from the original");
        }
    }

    private static ExpectedInput input(Item item, int count) {
        return new ExpectedInput(item, count);
    }

    private static Item material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static Item component(String id) {
        return MaterialRegistration.COMPONENTS.get(id).get();
    }

    private static Item compression(String id) {
        return MaterialRegistration.COMPRESSION_BLOCKS.get(id).get().asItem();
    }

    private static Item gadget(GadgetType type) {
        return GadgetRegistration.GADGETS_BY_TYPE.get(type).get();
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(item);
    }

    private static int customMarker(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("original_marker");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private record ExpectedInput(Item item, int count) {
    }

    private OriginalRecipeRepairGameTests() {
    }
}
