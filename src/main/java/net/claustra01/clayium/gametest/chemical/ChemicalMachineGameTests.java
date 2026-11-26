package net.claustra01.clayium.gametest.chemical;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.chemical.ChemicalMetalSeparation;
import net.claustra01.clayium.machine.series.ChemicalSeries;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ChemicalMachineGameTests {
    private static final long STARTING_ENERGY = 10_000_000_000L;

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void metalSeparatorPreservesWeightsTimeEnergyAndFullOutput(GameTestHelper helper) {
        require(ChemicalMetalSeparation.TOTAL_WEIGHT == 488, "Separator weights do not total 488");
        ClayiumMachineRecipe separation = recipe(helper, "chemical_metal_separator/industrial_clay");
        require(separation.randomOutputs().size() == ChemicalMetalSeparation.PRODUCTS.size()
                        && separation.randomOutputs().stream().mapToInt(ClayiumMachineRecipe.RandomOutput::weight).sum() == 488
                        && separation.processingTicks() == 40 && separation.clayEnergy() == 5_000L,
                "The loaded separator recipe has the wrong original balance");
        for (int index = 0; index < separation.randomOutputs().size(); index++) {
            var expected = ChemicalMetalSeparation.PRODUCTS.get(index);
            var actual = separation.randomOutputs().get(index);
            require(actual.stack().is(material(expected.itemId()).asItem())
                            && actual.weight() == expected.weight() && actual.chance() == 1.0F,
                    "The loaded separator recipe differs from the original weight table");
        }

        ClayMachineBlockEntity blocked = machine(helper, ChemicalSeries.CHEMICAL_METAL_SEPARATOR, 6);
        require(blocked.outputSlots() == 16,
                "The metal separator does not expose the original sixteen output slots");
        blocked.inventory().setStackInSlot(0, stack("industrial_clay_dust", 1));
        for (int output = 0; output < blocked.outputSlots(); output++) {
            blocked.inventory().setStackInSlot(blocked.outputSlot(output), new ItemStack(Items.STONE, 64));
        }
        blocked.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        RandomSource blockedRandom = RandomSource.create(814L);
        RandomSource untouchedRandom = RandomSource.create(814L);
        for (int tick = 0; tick < 80; tick++) {
            blocked.tickServer(blockedRandom);
        }
        require(blocked.inventory().getStackInSlot(0).getCount() == 1,
                "A full separator reserved or consumed industrial clay dust");
        require(blocked.energyStorage().stored().internalValue() == STARTING_ENERGY,
                "A full separator consumed CE");
        require(blockedRandom.nextInt(488) == untouchedRandom.nextInt(488),
                "A full separator consumed a random roll");

        ClayMachineBlockEntity machine = machine(helper, ChemicalSeries.CHEMICAL_METAL_SEPARATOR, 6);
        machine.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        RandomSource actualRandom = RandomSource.create(814L);
        RandomSource expectedRandom = RandomSource.create(814L);
        Map<String, Integer> observed = new LinkedHashMap<>();
        Map<String, Integer> expected = new LinkedHashMap<>();
        for (int cycle = 0; cycle < 488; cycle++) {
            machine.inventory().setStackInSlot(0, stack("industrial_clay_dust", 1));
            machine.tickServer(actualRandom);
            require(machine.activeRecipe() != null && machine.requiredProgress() == 40L
                            && machine.inventory().getStackInSlot(0).isEmpty(),
                    "The separator did not reserve one dust for 40 ticks");
            for (int tick = 0; tick < 40; tick++) {
                machine.tickServer(actualRandom);
            }
            int roll = expectedRandom.nextInt(488);
            for (var output : separation.randomOutputs()) {
                if (roll < output.weight()) {
                    expected.merge(BuiltInRegistries.ITEM.getKey(output.stack().getItem()).getPath(), 1, Integer::sum);
                    break;
                }
                roll -= output.weight();
            }
            ItemStack result = machine.inventory().getStackInSlot(machine.outputSlot());
            require(machine.activeRecipe() == null && result.getCount() == 1,
                    "The separator did not finish with one product");
            observed.merge(BuiltInRegistries.ITEM.getKey(result.getItem()).getPath(), 1, Integer::sum);
            require(machine.energyStorage().stored().internalValue() == STARTING_ENERGY - 200_000L * (cycle + 1),
                    "The separator did not consume 2 CE per cycle");
            machine.inventory().setStackInSlot(machine.outputSlot(), ItemStack.EMPTY);
        }
        require(observed.equals(expected), "Machine outputs differ from the fixed random sequence");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void chemicalAndMatterMachinesUseOriginalTiersRecipesAndDirectFluids(GameTestHelper helper) {
        require(Arrays.equals(ChemicalSeries.CHEMICAL_REACTOR.tiers(), new int[] {4, 5, 8}),
                "Chemical reactors have the wrong original tiers");
        require(Arrays.equals(ChemicalSeries.MATTER_TRANSFORMER.tiers(), new int[] {7, 8, 9, 10, 11, 12}),
                "Matter transformers have the wrong original tiers");
        long seriesMachines = MachineRegistration.MACHINES.values().stream()
                .filter(definition -> definition.series() == ChemicalSeries.CHEMICAL_REACTOR
                        || definition.series() == ChemicalSeries.MATTER_TRANSFORMER)
                .count();
        require(seriesMachines == 9L, "Chemical and matter series did not register exactly nine tier variants");
        for (int tier : ChemicalSeries.CHEMICAL_REACTOR.tiers()) {
            require(MachineRegistration.MACHINES.containsKey("chemical_reactor_tier_" + tier),
                    "Missing chemical reactor tier " + tier);
            ClayiumMachineRecipe tierRecipe = recipe(helper, tier == 4
                    ? "chemical_reactor/salt_and_calcareous_clay"
                    : tier == 5 ? "chemical_reactor/dense_clay_separation"
                    : "chemical_reactor/industrial_clay_and_salt");
            require(tierRecipe.requiredTier() == tier, "Chemical recipe has the wrong Tier");
            runRepresentative(helper, ChemicalSeries.CHEMICAL_REACTOR, tier, tierRecipe);
            if (tier > 4) {
                rejects(helper, ChemicalSeries.CHEMICAL_REACTOR, tier == 5 ? 4 : 5, tierRecipe);
            }
        }
        String[] transformerRecipes = {
                "lithium_to_sodium", "potassium_to_rubidium", "radium_to_actinium",
                "neptunium_to_plutonium", "plutonium_to_americium", "americium_to_curium"
        };
        int transformerIndex = 0;
        for (int tier : ChemicalSeries.MATTER_TRANSFORMER.tiers()) {
            require(MachineRegistration.MACHINES.containsKey("transformer_tier_" + tier),
                    "Missing matter transformer tier " + tier);
            ClayiumMachineRecipe tierRecipe = recipe(helper,
                    "matter_transformer/" + transformerRecipes[transformerIndex++]);
            require(tierRecipe.requiredTier() == tier, "Transformer recipe has the wrong Tier");
            runRepresentative(helper, ChemicalSeries.MATTER_TRANSFORMER, tier, tierRecipe);
            if (tier > 7) {
                rejects(helper, ChemicalSeries.MATTER_TRANSFORMER, tier - 1, tierRecipe);
            }
        }

        ClayiumMachineRecipe dense = recipe(helper, "chemical_reactor/dense_clay_separation");
        require(dense.requiredTier() == 5 && dense.processingTicks() == 30
                        && dense.clayEnergy() == ClayiumRecipeEnergy.e(1.0D, 5)
                        && dense.itemInputs().getFirst().count() == 1 && dense.itemOutputs().size() == 2,
                "Dense clay separation does not preserve its input, two products, time, and CE");
        ClayiumMachineRecipe tierEight = recipe(helper, "chemical_reactor/industrial_clay_and_salt");
        require(tierEight.processingTicks() == 1
                        && tierEight.clayEnergy() == ClayiumRecipeEnergy.e(10.0D, 8)
                        && tierEight.itemInputs().size() == 2 && tierEight.itemOutputs().size() == 2,
                "Tier 8 chemical reaction does not preserve its original balance");
        ClayiumMachineRecipe transform = recipe(helper, "matter_transformer/lithium_to_sodium");
        require(transform.requiredTier() == 7 && transform.processingTicks() == 200
                        && transform.clayEnergy() == ClayiumRecipeEnergy.e(10.0D, 7)
                        && transform.itemInputs().getFirst().count() == 1
                        && transform.itemOutputs().getFirst().getCount() == 1,
                "Lithium transmutation does not preserve its original balance");

        ClayMachineBlockEntity transformer = machine(helper, ChemicalSeries.MATTER_TRANSFORMER, 7);
        transformer.inventory().setStackInSlot(0, stack("lithium_ingot", 1));
        transformer.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        runProcess(helper, transformer, transform, TierManager.general(7));
        require(transformer.inventory().getStackInSlot(transformer.outputSlot()).is(material("sodium_ingot").asItem()),
                "The tier 7 transformer did not produce sodium");

        ClayiumMachineRecipe fluidRecipe = recipe(helper, "gametest/chemical_fluid_input");
        require(fluidRecipe.fluidInputs().size() == 1
                        && fluidRecipe.fluidInputs().getFirst().is(Fluids.WATER)
                        && fluidRecipe.fluidInputs().getFirst().getAmount() == 250,
                "The chemical fluid recipe is not a direct 250 mB FluidStack input");
        fluidRecipe.itemInputs().forEach(input -> Arrays.stream(input.ingredient().getItems()).forEach(stack ->
                require(!BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("capsule"),
                        "A chemical recipe uses a removed capsule item")));
        ClayMachineBlockEntity reactor = machine(helper, ChemicalSeries.CHEMICAL_REACTOR, 4);
        reactor.inventory().setStackInSlot(0, stack("clay_dust", 1));
        reactor.fluidInput().fill(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE);
        reactor.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        runProcess(helper, reactor, fluidRecipe, TierManager.general(4));
        require(reactor.fluidInput().isEmpty(), "The reactor did not consume its direct FluidStack input");
        require(reactor.inventory().getStackInSlot(reactor.outputSlot()).is(material("quartz_dust").asItem()),
                "The direct-fluid chemical reaction produced the wrong item");
        helper.succeed();
    }

    private static void runProcess(
            GameTestHelper helper,
            ClayMachineBlockEntity machine,
            ClayiumMachineRecipe recipe,
            TierManager.MachineParameters parameters) {
        tick(helper, machine);
        TierManager.ProcessingParameters expected = TierManager.calculate(
                parameters, recipe.processingTicks(), recipe.clayEnergy(), 1.0D);
        require(machine.activeRecipe() != null && machine.requiredProgress() == expected.requiredProgress(),
                "A representative chemical machine did not start with the original recipe parameters");
        long before = machine.energyStorage().stored().internalValue();
        for (long tick = 0; tick < expected.requiredProgress(); tick++) {
            tick(helper, machine);
        }
        require(machine.activeRecipe() == null, "A representative chemical machine did not finish");
        require(machine.energyStorage().stored().internalValue()
                        == before - expected.requiredProgress() * expected.energyPerProgress().internalValue(),
                "A representative chemical machine consumed the wrong CE amount");
        for (int index = 0; index < recipe.itemOutputs().size(); index++) {
            ItemStack result = machine.inventory().getStackInSlot(machine.outputSlot(index));
            require(ItemStack.isSameItemSameComponents(result, recipe.itemOutputs().get(index))
                            && result.getCount() == recipe.itemOutputs().get(index).getCount(),
                    "A representative chemical machine lost its output or byproduct");
        }
    }

    private static ClayiumMachineRecipe recipe(GameTestHelper helper, String id) {
        return helper.getLevel().getRecipeManager().byKey(ClayiumMod.id(id))
                .filter(holder -> holder.value() instanceof ClayiumMachineRecipe)
                .map(holder -> (ClayiumMachineRecipe)holder.value())
                .orElseThrow(() -> new GameTestAssertException("Missing machine recipe " + id));
    }

    private static void runRepresentative(
            GameTestHelper helper,
            ChemicalSeries series,
            int tier,
            ClayiumMachineRecipe recipe) {
        ClayMachineBlockEntity machine = machine(helper, series, tier);
        fillInputs(machine, recipe);
        machine.receiveClayEnergy(ClayEnergy.MAX);
        runProcess(helper, machine, recipe, series.parameters(tier));
    }

    private static void rejects(GameTestHelper helper, ChemicalSeries series, int tier,
                                ClayiumMachineRecipe recipe) {
        ClayMachineBlockEntity machine = machine(helper, series, tier);
        fillInputs(machine, recipe);
        machine.receiveClayEnergy(ClayEnergy.MAX);
        tick(helper, machine);
        require(machine.activeRecipe() == null, "A lower Tier machine started a higher Tier recipe");
    }

    private static void fillInputs(ClayMachineBlockEntity machine, ClayiumMachineRecipe recipe) {
        for (int input = 0; input < recipe.itemInputs().size(); input++) {
            ItemStack ingredient = recipe.itemInputs().get(input).ingredient().getItems()[0].copy();
            ingredient.setCount(recipe.itemInputs().get(input).count());
            machine.inventory().setStackInSlot(input, ingredient);
        }
    }

    private static ClayMachineBlockEntity machine(GameTestHelper helper, ChemicalSeries series, int tier) {
        MachineRegistration.MachineDefinition definition = MachineRegistration.MACHINES.get(series.registrationId(tier));
        require(definition != null, "Missing machine " + series.registrationId(tier));
        BlockState state = definition.block().get().defaultBlockState();
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

    private static void tick(GameTestHelper helper, ClayMachineBlockEntity machine) {
        ClayMachineBlockEntity.serverTick(
                helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private ChemicalMachineGameTests() {
    }
}
