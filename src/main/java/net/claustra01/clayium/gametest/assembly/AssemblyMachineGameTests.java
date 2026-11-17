package net.claustra01.clayium.gametest.assembly;

import java.util.Arrays;
import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.ClayMachineMenu;
import net.claustra01.clayium.machine.series.AssemblySeries;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.OriginalRegistryCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AssemblyMachineGameTests {
    private static final long STARTING_ENERGY = 2_000_000_000L;

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allTenAssemblyMachinesMatchTheOriginalTierLedger(GameTestHelper helper) {
        assertTiers(AssemblySeries.ASSEMBLER, 3, 4, 6, 10);
        assertTiers(AssemblySeries.INSCRIBER, 3, 4);
        assertTiers(AssemblySeries.CENTRIFUGE, 3, 4, 5, 6);
        require(AssemblyMachineRegistration.MACHINES.size() == 10,
                "The assembly registry must contain exactly ten machines");
        require(AssemblySeries.ASSEMBLER.inputSlots() == 2
                        && AssemblySeries.INSCRIBER.inputSlots() == 2
                        && AssemblySeries.CENTRIFUGE.inputSlots() == 1,
                "An assembly series has the wrong original input layout");
        require(AssemblySeries.CENTRIFUGE.outputSlots() == 4
                        && AssemblySeries.ASSEMBLER.outputSlots() == 1
                        && AssemblySeries.INSCRIBER.outputSlots() == 1,
                "An assembly series has the wrong original output layout");

        for (AssemblyMachineRegistration.MachineDefinition definition
                : AssemblyMachineRegistration.MACHINES.values()) {
            require(BuiltInRegistries.BLOCK.containsKey(ClayiumMod.id(definition.id()))
                            && BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(definition.id())),
                    definition.id() + " is not registered as a matching block and item");
            ClayMachineBlock block = definition.block().get();
            require(block.series() == definition.series() && block.tier() == definition.tier(),
                    definition.id() + " does not use the shared processing machine");
            require(helper.getLevel().getRecipeManager().byKey(
                            ClayiumMod.id("assembly_machine/" + definition.id())).isPresent(),
                    definition.id() + " has no construction recipe");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allAssemblyMachinesHaveAnObtainableConstructionPath(GameTestHelper helper) {
        for (int tier : List.of(3, 4, 5, 6, 10)) {
            var hull = helper.getLevel().getRecipeManager()
                    .byKey(ClayiumMod.id("machine_hull/tier_" + tier)).orElseThrow().value();
            require(hull instanceof ShapedRecipe shaped
                            && shaped.getResultItem(helper.getLevel().registryAccess()).is(
                                    AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get().asItem()),
                    "Tier " + tier + " machine hull has no crafting path");
            ShapedRecipe recipe = (ShapedRecipe)hull;
            String plate = switch (tier) {
                case 3 -> "large_industrial_clay_plate";
                case 4 -> "large_advanced_industrial_clay_plate";
                case 5 -> "large_impure_silicon_plate";
                case 6 -> "large_aluminium_plate";
                default -> "large_antimatter_plate";
            };
            String circuit = switch (tier) {
                case 3 -> "simple_circuit";
                case 4 -> "basic_circuit";
                case 5 -> "advanced_circuit";
                case 6 -> "precision_circuit";
                default -> "clay_spirit";
            };
            require(recipe.getIngredients().get(0).test(stack(plate, 1))
                            && recipe.getIngredients().get(4).test(componentStack(circuit, 1))
                            && (tier == 3 || recipe.getIngredients().get(1).test(
                                    componentStack("clay_energy_excitor", 1))),
                    "Tier " + tier + " hull differs from the hotfix2 ingredients");
            CraftingInput hullInput = CraftingInput.of(3, 3, recipe.getIngredients().stream()
                    .map(ingredient -> ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0].copy())
                    .toList());
            require(recipe.matches(hullInput, helper.getLevel()), "Tier " + tier + " hull cannot be crafted");
        }
        for (AssemblyMachineRegistration.MachineDefinition definition
                : AssemblyMachineRegistration.MACHINES.values()) {
            var holder = helper.getLevel().getRecipeManager()
                    .byKey(ClayiumMod.id("assembly_machine/" + definition.id())).orElseThrow();
            if (definition.tier() <= 4) {
                require(holder.value() instanceof ShapedRecipe,
                        definition.id() + " requires an assembler before the first assembler exists");
                ShapedRecipe recipe = (ShapedRecipe)holder.value();
                require(recipe.getIngredients().stream().anyMatch(ingredient ->
                                ingredient.test(new ItemStack(AssemblyMachineRegistration.MACHINE_HULLS
                                        .get(definition.tier()).get()))),
                        definition.id() + " does not use its craftable machine hull");
                ItemStack circuit = componentStack(definition.tier() == 3 ? "simple_circuit" : "basic_circuit", 1);
                require(recipe.getIngredients().get(4).test(new ItemStack(
                                AssemblyMachineRegistration.MACHINE_HULLS.get(definition.tier()).get()))
                                && recipe.getIngredients().get(0).test(stack("dense_clay_gear", 1))
                                && recipe.getIngredients().get(1).test(definition.series() == AssemblySeries.ASSEMBLER
                                        ? circuit : stack("dense_clay_spindle", 1))
                                && recipe.getIngredients().get(7).test(definition.series() == AssemblySeries.CENTRIFUGE
                                        ? stack("dense_clay_spindle", 1) : circuit),
                        definition.id() + " differs from the hotfix2 crafting pattern");
                CraftingInput input = CraftingInput.of(3, 3, recipe.getIngredients().stream()
                        .map(ingredient -> ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0].copy())
                        .toList());
                require(recipe.matches(input, helper.getLevel())
                                && recipe.assemble(input, helper.getLevel().registryAccess()).is(
                                        definition.block().get().asItem()),
                        definition.id() + " cannot be crafted from its listed ingredients");
            } else {
                ClayiumMachineRecipe recipe = (ClayiumMachineRecipe)holder.value();
                ItemStack second = definition.tier() == 10
                        ? new ItemStack(definition(AssemblySeries.ASSEMBLER, 6).block().get())
                        : definition.series() == AssemblySeries.ASSEMBLER
                                ? stack("dense_clay_gear", 4)
                                : stack("dense_clay_spindle", Math.max(definition.tier() - 4, 1));
                require(recipe.processingMethod() == net.claustra01.clayium.recipe.ClayiumRecipeMethod.ASSEMBLER
                                && recipe.requiredTier() == (definition.tier() == 10 ? 6 : 4)
                                && recipe.clayEnergy() == ClayiumRecipeEnergy.e(1.0D, definition.tier())
                                && recipe.processingTicks() == (definition.series() == AssemblySeries.CENTRIFUGE ? 120 : 40)
                                && recipe.itemInputs().size() == 2
                                && recipe.itemInputs().get(0).ingredient().test(new ItemStack(
                                        AssemblyMachineRegistration.MACHINE_HULLS.get(definition.tier()).get()))
                                && recipe.itemInputs().get(1).ingredient().test(second)
                                && recipe.itemInputs().get(1).count() == second.getCount(),
                        definition.id() + " differs from the hotfix2 assembly values");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void assemblyMenuEnergyEqualsConsumedEnergyForAllThreeSeries(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        for (AssemblySeries series : AssemblySeries.values()) {
            ClayMachineBlockEntity machine = machine(helper, series, 4);
            if (series == AssemblySeries.ASSEMBLER) {
                machine.inventory().setStackInSlot(0, stack("clay_stick", 5));
            } else if (series == AssemblySeries.INSCRIBER) {
                machine.inventory().setStackInSlot(0, componentStack("clay_circuit_board", 1));
                machine.inventory().setStackInSlot(1, stack("dense_clay_dust", 6));
            } else {
                machine.inventory().setStackInSlot(0, stack("clay_dust", 9));
            }
            machine.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
            tick(helper, machine);
            require(machine.activeRecipe() != null, series + " did not start");
            ClayMachineMenu menu = (ClayMachineMenu)machine.createMenu(1, player.getInventory(), player);
            long shown = menu.requiredEnergy();
            require(shown == machine.requiredEnergy(), series + " GUI has the wrong synced CE");
            runUntilIdle(helper, machine, (int)machine.requiredProgress() + 2);
            require(STARTING_ENERGY - machine.energyStorage().stored().internalValue() == shown,
                    series + " consumed a different CE amount than its GUI displays");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void everyAssemblyTierProcessesItsRepresentativeRecipe(GameTestHelper helper) {
        for (int tier : AssemblySeries.ASSEMBLER.tiers()) {
            assertProcess(
                    helper,
                    AssemblySeries.ASSEMBLER,
                    tier,
                    List.of(stack("clay_stick", 5)),
                    List.of(stack("clay_gear", 1)),
                    10L,
                    20);
        }
        for (int tier : AssemblySeries.INSCRIBER.tiers()) {
            assertProcess(
                    helper,
                    AssemblySeries.INSCRIBER,
                    tier,
                    List.of(componentStack("clay_circuit_board", 1), stack("dense_clay_dust", 6)),
                    List.of(componentStack("clay_circuit", 1)),
                    2L,
                    20);
        }
        for (int tier : AssemblySeries.CENTRIFUGE.tiers()) {
            assertProcess(
                    helper,
                    AssemblySeries.CENTRIFUGE,
                    tier,
                    List.of(stack("clay_dust", 9)),
                    List.of(stack("dense_clay_dust", 1)),
                    4L,
                    20);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void hotfixTwoAssemblyBalancesAndCeValuesArePreserved(GameTestHelper helper) {
        assertRecipe(helper, "inscriber/advanced_circuit", 0, 100L, 120,
                List.of(stack("impure_silicon_plate", 1), stack("energized_clay_dust", 32)),
                List.of(componentStack("advanced_circuit", 1)));
        assertRecipe(helper, "inscriber/precision_circuit", 0, 1_000L, 120,
                List.of(stack("silicon_plate", 1), stack("energized_clay_dust", 32)),
                List.of(componentStack("precision_circuit", 1)));
        assertRecipe(helper, "assembler/integrated_circuit", 6, 10_000L, 1_200,
                List.of(componentStack("precision_circuit", 1), stack("energized_clay_dust", 32)),
                List.of(componentStack("integrated_circuit", 1)));
        assertRecipe(helper, "assembler/laser_parts", 6, ClayiumRecipeEnergy.e(1.0D, 6), 20,
                List.of(componentStack("clay_energy_excitor", 1), componentStack("integrated_circuit", 1)),
                List.of(componentStack("laser_parts", 1)));
        assertRecipe(helper, "assembler/synchronous_parts", 6, ClayiumRecipeEnergy.e(1.0D, 6), 432_000,
                List.of(stack("beryllium_dust", 8), componentStack("integrated_circuit", 1)),
                List.of(componentStack("synchronous_parts", 1)));
        assertRecipe(helper, "assembler/manipulator_mk_i", 4, ClayiumRecipeEnergy.e(1.0D, 4), 20,
                List.of(stack("az91_d_alloy_ingot", 16), componentStack("precision_circuit", 1)),
                List.of(componentStack("manipulator_mk_i", 1)));
        assertRecipe(helper, "assembler/manipulator_mk_ii", 4, ClayiumRecipeEnergy.e(1.0D, 8), 20,
                List.of(componentStack("manipulator_mk_i", 16), componentStack("clay_core", 6)),
                List.of(componentStack("manipulator_mk_ii", 1)));
        assertRecipe(helper, "assembler/manipulator_mk_iii", 10, ClayiumRecipeEnergy.e(1.0D, 12), 20,
                List.of(componentStack("manipulator_mk_ii", 64), componentStack("clay_anima", 6)),
                List.of(componentStack("manipulator_mk_iii", 1)));
        assertRecipe(helper, "reactor/clay_core", 7, ClayiumRecipeEnergy.e(10.0D, 7), 8_000_000,
                List.of(componentStack("integrated_circuit", 6), stack("excited_clay_dust", 1)),
                List.of(componentStack("clay_core", 1)));
        assertRecipe(helper, "assembly_machine/assembler_tier_6", 4,
                ClayiumRecipeEnergy.e(1.0D, 6), 40,
                List.of(new ItemStack(AssemblyMachineRegistration.MACHINE_HULLS.get(6).get()),
                        stack("dense_clay_gear", 4)),
                List.of(new ItemStack(definition(AssemblySeries.ASSEMBLER, 6).block().get())));
        assertRecipe(helper, "assembly_machine/assembler_tier_10", 6,
                ClayiumRecipeEnergy.e(1.0D, 10), 40,
                List.of(new ItemStack(AssemblyMachineRegistration.MACHINE_HULLS.get(10).get()),
                        new ItemStack(definition(AssemblySeries.ASSEMBLER, 6).block().get())),
                List.of(new ItemStack(definition(AssemblySeries.ASSEMBLER, 10).block().get())));
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void centrifugeMultiOutputStopsSavesAndConsumesExactlyOnce(GameTestHelper helper) {
        ClayMachineBlockEntity machine = machine(helper, AssemblySeries.CENTRIFUGE, 4);
        machine.inventory().setStackInSlot(0, stack("advanced_industrial_clay_dust", 2));
        machine.inventory().setStackInSlot(machine.outputSlot(2), new ItemStack(Blocks.COBBLESTONE));
        machine.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, machine);
        require(machine.activeRecipe() == null
                        && machine.inventory().getStackInSlot(0).getCount() == 2,
                "A blocked centrifuge reserved input before all four outputs had space");

        machine.inventory().setStackInSlot(machine.outputSlot(2), ItemStack.EMPTY);
        tick(helper, machine);
        require(machine.activeRecipe() != null
                        && machine.inventory().getStackInSlot(machine.reservedSlot(0)).getCount() == 2,
                "The centrifuge did not reserve its input after outputs became available");
        TierManager.ProcessingParameters expected = TierManager.calculate(
                TierManager.general(4), 12, 10_000L, 1.0D);
        require(machine.requiredEnergy() == expected.totalEnergy().internalValue(),
                "The GUI CE value differs from the server processing calculation");

        tick(helper, machine);
        CompoundTag saved = machine.saveWithFullMetadata(helper.getLevel().registryAccess());
        ClayMachineBlockEntity loaded = new ClayMachineBlockEntity(machine.getBlockPos(), machine.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        runUntilIdle(helper, loaded, (int)expected.requiredProgress() + 2);

        List<ItemStack> expectedOutputs = List.of(
                stack("energized_clay_dust", 64),
                stack("clay_dust", 64),
                stack("dense_clay_dust", 64),
                stack("industrial_clay_dust", 12));
        for (int output = 0; output < expectedOutputs.size(); output++) {
            ItemStack actual = loaded.inventory().getStackInSlot(loaded.outputSlot(output));
            ItemStack expectedOutput = expectedOutputs.get(output);
            require(ItemStack.isSameItemSameComponents(actual, expectedOutput)
                            && actual.getCount() == expectedOutput.getCount(),
                    "Centrifuge output " + output + " was not preserved through saving");
        }
        require(loaded.inventory().getStackInSlot(0).isEmpty()
                        && loaded.inventory().getStackInSlot(loaded.reservedSlot(0)).isEmpty(),
                "The saved centrifuge duplicated or retained its consumed input");
        require(loaded.energyStorage().stored().internalValue()
                        == STARTING_ENERGY - expected.totalEnergy().internalValue(),
                "The centrifuge consumed a different CE total from the GUI value");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void centrifugeMergesBeforeUsingEmptyOutputSlots(GameTestHelper helper) {
        ClayMachineBlockEntity machine = machine(helper, AssemblySeries.CENTRIFUGE, 4);
        machine.inventory().setStackInSlot(0, stack("industrial_clay_dust", 2));
        machine.inventory().setStackInSlot(machine.outputSlot(1), stack("energized_clay_dust", 1));
        machine.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, machine);
        require(machine.activeRecipe() != null, "The centrifuge rejected outputs that fit after merging");
        runUntilIdle(helper, machine, (int)machine.requiredProgress() + 2);
        List<ItemStack> outputs = List.of(
                stack("clay_dust", 8), stack("energized_clay_dust", 13),
                stack("dense_clay_dust", 8), stack("industrial_clay_dust", 1));
        for (int slot = 0; slot < outputs.size(); slot++) {
            ItemStack actual = machine.inventory().getStackInSlot(machine.outputSlot(slot));
            require(ItemStack.isSameItemSameComponents(actual, outputs.get(slot))
                            && actual.getCount() == outputs.get(slot).getCount(),
                    "The centrifuge lost an output after merging into a later slot");
        }
        require(machine.inventory().getStackInSlot(machine.reservedSlot(0)).isEmpty(),
                "The centrifuge did not consume the reserved input exactly once");
        helper.succeed();
    }

    private static void assertProcess(
            GameTestHelper helper,
            AssemblySeries series,
            int tier,
            List<ItemStack> inputs,
            List<ItemStack> outputs,
            long baseEnergy,
            int baseTicks) {
        ClayMachineBlockEntity machine = machine(helper, series, tier);
        for (int input = 0; input < inputs.size(); input++) {
            machine.inventory().setStackInSlot(input, inputs.get(input).copy());
        }
        machine.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, machine);
        TierManager.ProcessingParameters expected = TierManager.calculate(
                TierManager.general(tier), baseTicks, baseEnergy, 1.0D);
        require(machine.activeRecipe() != null, series + " tier " + tier + " did not start");
        require(machine.requiredProgress() == expected.requiredProgress()
                        && machine.requiredEnergy() == expected.totalEnergy().internalValue(),
                series + " tier " + tier + " used different GUI and server parameters");
        runUntilIdle(helper, machine, (int)expected.requiredProgress() + 2);
        for (int output = 0; output < outputs.size(); output++) {
            ItemStack actual = machine.inventory().getStackInSlot(machine.outputSlot(output));
            ItemStack expectedOutput = outputs.get(output);
            require(ItemStack.isSameItemSameComponents(actual, expectedOutput)
                            && actual.getCount() == expectedOutput.getCount(),
                    series + " tier " + tier + " produced the wrong output");
        }
        require(machine.energyStorage().stored().internalValue()
                        == STARTING_ENERGY - expected.totalEnergy().internalValue(),
                series + " tier " + tier + " consumed the wrong CE total");
    }

    private static void assertRecipe(
            GameTestHelper helper,
            String id,
            int tier,
            long energy,
            int ticks,
            List<ItemStack> inputs,
            List<ItemStack> outputs) {
        ClayiumMachineRecipe recipe = (ClayiumMachineRecipe)helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id(id)).orElseThrow().value();
        require(recipe.requiredTier() == tier && recipe.clayEnergy() == energy
                        && recipe.processingTicks() == ticks,
                id + " does not preserve the hotfix2 tier, CE and tick values");
        require(recipe.itemInputs().size() == inputs.size(), id + " has the wrong input count");
        for (int index = 0; index < inputs.size(); index++) {
            ItemStack expected = inputs.get(index);
            var actual = recipe.itemInputs().get(index);
            require(actual.count() == expected.getCount() && actual.ingredient().test(expected),
                    id + " has the wrong input balance at " + index);
        }
        require(recipe.itemOutputs().size() == outputs.size(), id + " has the wrong output count");
        for (int index = 0; index < outputs.size(); index++) {
            require(ItemStack.isSameItemSameComponents(recipe.itemOutputs().get(index), outputs.get(index))
                            && recipe.itemOutputs().get(index).getCount() == outputs.get(index).getCount(),
                    id + " has the wrong output balance");
        }
    }

    private static void assertTiers(AssemblySeries series, int... expected) {
        require(Arrays.equals(series.tiers(), expected), series + " has the wrong original tier list");
        OriginalRegistryCatalog.MachineFamily family = OriginalRegistryCatalog.MACHINE_FAMILIES.stream()
                .filter(candidate -> candidate.namedIdStem().equals(series.recipeMethod().id()))
                .findFirst().orElseThrow();
        require(Arrays.equals(family.tiers(), expected), series + " differs from the original catalog");
        for (int tier : expected) {
            require(AssemblyMachineRegistration.MACHINES.containsKey(
                            OriginalRegistryCatalog.machineId(family, tier)),
                    "Missing " + series + " tier " + tier);
        }
    }

    private static AssemblyMachineRegistration.MachineDefinition definition(
            AssemblySeries series, int tier) {
        return AssemblyMachineRegistration.MACHINES.values().stream()
                .filter(definition -> definition.series() == series && definition.tier() == tier)
                .findFirst().orElseThrow();
    }

    private static ClayMachineBlockEntity machine(
            GameTestHelper helper, AssemblySeries series, int tier) {
        BlockState state = definition(series, tier).block().get().defaultBlockState();
        ClayMachineBlockEntity machine = new ClayMachineBlockEntity(helper.absolutePos(BlockPos.ZERO), state);
        machine.setLevel(helper.getLevel());
        return machine;
    }

    private static ItemStack stack(String id, int count) {
        return new ItemStack(material(id), count);
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static ItemStack componentStack(String id, int count) {
        return new ItemStack(MaterialRegistration.COMPONENTS.get(id).get(), count);
    }

    private static void tick(GameTestHelper helper, ClayMachineBlockEntity machine) {
        ClayMachineBlockEntity.serverTick(
                helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
    }

    private static void runUntilIdle(
            GameTestHelper helper, ClayMachineBlockEntity machine, int maximumTicks) {
        for (int tick = 0; tick < maximumTicks && machine.activeRecipe() != null; tick++) {
            tick(helper, machine);
        }
        require(machine.activeRecipe() == null, "Machine did not complete within " + maximumTicks + " ticks");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private AssemblyMachineGameTests() {
    }
}
