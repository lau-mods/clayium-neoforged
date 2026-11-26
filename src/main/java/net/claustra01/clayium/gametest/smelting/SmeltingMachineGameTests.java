package net.claustra01.clayium.gametest.smelting;

import java.util.Arrays;
import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.series.SmeltingSeries;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SmeltingMachineGameTests {
    private static final long STARTING_ENERGY = 10_000_000_000L;

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allElevenSmeltingMachinesUseOriginalTiersAndRejectFe(GameTestHelper helper) {
        assertTiers(SmeltingSeries.SMELTER, 4, 5, 6, 7, 8, 9);
        assertTiers(SmeltingSeries.ALLOY_SMELTER, 6);
        assertTiers(SmeltingSeries.ELECTROLYSIS_REACTOR, 6, 7, 8, 9);

        long registered = MachineRegistration.MACHINES.values().stream()
                .filter(definition -> definition.series() instanceof SmeltingSeries)
                .count();
        require(registered == 11, "The smelting milestone did not register exactly eleven machines");
        require(SmeltingSeries.ALLOY_SMELTER.inputSlots() == 2
                        && SmeltingSeries.SMELTER.inputSlots() == 1
                        && SmeltingSeries.ELECTROLYSIS_REACTOR.inputSlots() == 1,
                "A smelting series exposes the wrong number of recipe inputs");

        BlockPos position = new BlockPos(1, 1, 1);
        for (SmeltingSeries series : SmeltingSeries.values()) {
            for (int tier : series.tiers()) {
                helper.setBlock(position, definition(series, tier).block().get());
                for (Direction direction : Direction.values()) {
                    require(helper.getLevel().getCapability(
                                    Capabilities.EnergyStorage.BLOCK,
                                    helper.absolutePos(position), direction) == null,
                            series + " tier " + tier + " exposed FE on " + direction);
                }
                require(helper.getLevel().getCapability(
                                Capabilities.EnergyStorage.BLOCK,
                                helper.absolutePos(position), null) == null,
                        series + " tier " + tier + " exposed unsided FE");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void smeltingAlloyAndElectrolysisPreserveOriginalBalances(GameTestHelper helper) {
        assertProcess(
                helper,
                SmeltingSeries.SMELTER,
                4,
                List.of(new ItemStack(Items.RAW_IRON)),
                Items.IRON_INGOT,
                1,
                200,
                4L,
                TierManager.smelter(4));
        ItemStack smeltedCopper = assertProcess(
                helper,
                SmeltingSeries.SMELTER,
                4,
                List.of(new ItemStack(Items.RAW_COPPER)),
                Items.COPPER_INGOT,
                1,
                200,
                4L,
                TierManager.smelter(4));
        assertProcess(
                helper,
                SmeltingSeries.SMELTER,
                4,
                List.of(stack("silicon_dust", 1)),
                material("silicon_ingot"),
                1,
                200,
                4L,
                TierManager.smelter(4));
        assertProcess(
                helper,
                SmeltingSeries.SMELTER,
                5,
                List.of(stack("aluminium_dust", 1)),
                material("aluminium_ingot"),
                1,
                200,
                ClayiumRecipeEnergy.e(0.5D, 5),
                TierManager.smelter(5));
        assertProcess(
                helper,
                SmeltingSeries.ALLOY_SMELTER,
                6,
                List.of(new ItemStack(smeltedCopper.getItem(), 3), stack("tin_ingot", 1)),
                material("bronze_ingot"),
                4,
                40,
                ClayiumRecipeEnergy.e(1.0D, 4),
                TierManager.general(6));
        assertProcess(
                helper,
                SmeltingSeries.ELECTROLYSIS_REACTOR,
                6,
                List.of(stack("impure_aluminium_dust", 1)),
                material("aluminium_dust"),
                1,
                100,
                ClayiumRecipeEnergy.e(1.0D, 6),
                TierManager.general(6));

        ClayMachineBlockEntity lowerTier = machine(helper, SmeltingSeries.ELECTROLYSIS_REACTOR, 6);
        lowerTier.inventory().setStackInSlot(0, stack("impure_manganese_dust", 1));
        lowerTier.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, lowerTier);
        require(lowerTier.activeRecipe() == null
                        && lowerTier.inventory().getStackInSlot(0).is(material("impure_manganese_dust").asItem()),
                "A tier 6 electrolysis reactor started a tier 7 recipe");
        helper.succeed();
    }

    private static ItemStack assertProcess(
            GameTestHelper helper,
            SmeltingSeries series,
            int tier,
            List<ItemStack> inputs,
            ItemLike output,
            int outputCount,
            int baseTicks,
            long baseEnergy,
            TierManager.MachineParameters parameters) {
        ClayMachineBlockEntity machine = machine(helper, series, tier);
        for (int input = 0; input < inputs.size(); input++) {
            machine.inventory().setStackInSlot(input, inputs.get(input).copy());
        }
        machine.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, machine);

        TierManager.ProcessingParameters expected = TierManager.calculate(
                parameters, baseTicks, baseEnergy, 1.0D);
        require(machine.activeRecipe() != null, series + " tier " + tier + " did not start its representative recipe");
        require(machine.requiredProgress() == expected.requiredProgress(),
                series + " tier " + tier + " used the wrong processing time");
        for (int input = 0; input < inputs.size(); input++) {
            require(machine.inventory().getStackInSlot(input).isEmpty(),
                    series + " did not reserve input slot " + input);
            require(machine.inventory().getStackInSlot(machine.reservedSlot(input)).getCount() == inputs.get(input).getCount(),
                    series + " reserved the wrong amount from input slot " + input);
        }

        runUntilIdle(helper, machine, (int)expected.requiredProgress() + 2);
        ItemStack result = machine.inventory().getStackInSlot(machine.outputSlot());
        require(result.is(output.asItem()) && result.getCount() == outputCount,
                series + " tier " + tier + " produced the wrong output balance");
        for (int input = 0; input < inputs.size(); input++) {
            require(machine.inventory().getStackInSlot(input).isEmpty()
                            && machine.inventory().getStackInSlot(machine.reservedSlot(input)).isEmpty(),
                    series + " did not consume its reserved input " + input + " exactly once");
        }
        long consumed = expected.requiredProgress() * expected.energyPerProgress().internalValue();
        require(machine.energyStorage().stored().internalValue() == STARTING_ENERGY - consumed,
                series + " tier " + tier + " consumed the wrong CE amount");
        return result.copy();
    }

    private static void assertTiers(SmeltingSeries series, int... expected) {
        require(Arrays.equals(series.tiers(), expected), series + " has the wrong original tier list");
        for (int tier : expected) {
            require(MachineRegistration.MACHINES.containsKey(series.recipeMethod().id() + "_tier_" + tier),
                    "Missing " + series + " tier " + tier);
        }
    }

    private static MachineRegistration.MachineDefinition definition(SmeltingSeries series, int tier) {
        MachineRegistration.MachineDefinition definition = MachineRegistration.MACHINES.get(
                series.recipeMethod().id() + "_tier_" + tier);
        require(definition != null, "Missing machine definition for " + series + " tier " + tier);
        return definition;
    }

    private static ClayMachineBlockEntity machine(GameTestHelper helper, SmeltingSeries series, int tier) {
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

    private static void tick(GameTestHelper helper, ClayMachineBlockEntity machine) {
        ClayMachineBlockEntity.serverTick(
                helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
    }

    private static void runUntilIdle(GameTestHelper helper, ClayMachineBlockEntity machine, int maximumTicks) {
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

    private SmeltingMachineGameTests() {
    }
}
