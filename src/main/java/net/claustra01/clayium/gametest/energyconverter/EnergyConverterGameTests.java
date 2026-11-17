package net.claustra01.clayium.gametest.energyconverter;

import java.util.HashMap;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.fe.EnergyConverterBlockEntity;
import net.claustra01.clayium.energy.fe.EnergyConverterSpecification;
import net.claustra01.clayium.machine.series.ProcessingSeries;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EnergyConverterGameTests {
    private static final Map<BlockPos, TestReceiver> TEST_RECEIVERS = new HashMap<>();

    public static void registerTestCapability(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, blockEntity, side) -> TEST_RECEIVERS.get(pos),
                Blocks.IRON_BLOCK);
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void convertersOnlyTurnClayEnergyIntoExternalFe(GameTestHelper helper) {
        assertOneWayConversion(helper, 4, new BlockPos(2, 2, 2));
        assertOneWayConversion(helper, 13, new BlockPos(5, 2, 2));

        BlockPos machinePosition = new BlockPos(8, 2, 2);
        helper.setBlock(machinePosition, MachineRegistration.MACHINES.values().stream()
                .filter(definition -> definition.series() == ProcessingSeries.GRINDER && definition.tier() == 4)
                .findFirst().orElseThrow().block().get());
        BlockPos absoluteMachinePosition = helper.absolutePos(machinePosition);
        for (Direction direction : Direction.values()) {
            require(helper.getLevel().getCapability(
                            Capabilities.EnergyStorage.BLOCK, absoluteMachinePosition, direction) == null,
                    "An ordinary Clayium machine accepted FE on " + direction);
        }
        require(helper.getLevel().getCapability(
                        Capabilities.EnergyStorage.BLOCK, absoluteMachinePosition, null) == null,
                "An ordinary Clayium machine exposed unsided FE");
        TEST_RECEIVERS.clear();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allConverterTiersPreserveRatesRecipesRedstoneAndStoredFe(GameTestHelper helper) {
        long[] clayEnergy = {
                100L, 1_000L, 10_000L, 100_000L, 1_000_000L,
                10_000_000L, 100_000_000L, 1_000_000_000L, 10_000_000_000L, 100_000_000_000L
        };
        int[] fe = {10, 30, 90, 270, 810, 2_430, 7_290, 21_870, 65_610, 196_830};
        int[] capacity = {
                10_000, 30_000, 90_000, 270_000, 810_000,
                2_430_000, 7_290_000, 21_870_000, 65_610_000, 196_830_000
        };
        BlockPos position = new BlockPos(2, 2, 2);
        for (int tier = 4; tier <= 13; tier++) {
            EnergyConverterSpecification specification = EnergyConverterSpecification.VALUES.get(tier - 4);
            require(specification.tier() == tier
                            && specification.clayEnergyPerTick() == clayEnergy[tier - 4]
                            && specification.generatedFePerTick() == fe[tier - 4]
                            && specification.outputLimitPerTick() == fe[tier - 4]
                            && specification.capacity() == capacity[tier - 4],
                    "Tier " + tier + " differs from the GR-05 table");
            require(specification.clayEnergyCost(2.0D).internalValue() == clayEnergy[tier - 4] * 2L
                            && specification.generatedFe(2.0D) == fe[tier - 4] * 2
                            && specification.outputLimit(2.0D) == fe[tier - 4] * 2,
                    "Tier " + tier + " did not preserve the converter-specific linear overclock exponent");

            EnergyConverterBlockEntity converter = placedConverter(helper, tier, position);
            converter.clayEnergyStorage().add(ClayEnergy.ofInternal(specification.clayEnergyPerTick()));
            EnergyConverterBlockEntity.serverTick(
                    helper.getLevel(), converter.getBlockPos(), converter.getBlockState(), converter);
            require(converter.storedFe() == specification.generatedFePerTick(),
                    "Tier " + tier + " generated the wrong FE amount");
            require(converter.clayEnergyStorage().stored().internalValue() == 0L,
                    "Tier " + tier + " consumed the wrong CE amount");
            IEnergyStorage capability = helper.getLevel().getCapability(
                    Capabilities.EnergyStorage.BLOCK, converter.getBlockPos(), Direction.NORTH);
            require(capability != null
                            && capability.getMaxEnergyStored() == specification.capacity()
                            && !capability.canReceive()
                            && capability.canExtract(),
                    "Tier " + tier + " exposes the wrong FE contract");

            loadStoredFe(helper, converter, specification.capacity());
            converter.clayEnergyStorage().add(ClayEnergy.ofInternal(specification.clayEnergyPerTick()));
            EnergyConverterBlockEntity.serverTick(
                    helper.getLevel(), converter.getBlockPos(), converter.getBlockState(), converter);
            require(converter.storedFe() == specification.capacity()
                            && converter.clayEnergyStorage().stored().internalValue()
                                    == specification.clayEnergyPerTick(),
                    "Tier " + tier + " consumed CE with a full FE buffer");

            var holder = helper.getLevel().getRecipeManager()
                    .byKey(ClayiumMod.id("energy_converter/" + specification.id())).orElseThrow();
            require(holder.value() instanceof ClayiumMachineRecipe recipe
                            && recipe.processingMethod() == ClayiumRecipeMethod.ASSEMBLER
                            && recipe.getResultItem(helper.getLevel().registryAccess()).is(
                                    EnergyConverterRegistration.CONVERTERS.get(tier).item().get()),
                    "Tier " + tier + " has no enabled assembler recipe");
        }

        EnergyConverterBlockEntity redstoneStopped = placedConverter(helper, 4, position);
        long cost = redstoneStopped.specification().clayEnergyPerTick();
        redstoneStopped.clayEnergyStorage().add(ClayEnergy.ofInternal(cost));
        helper.setBlock(position.relative(Direction.WEST), Blocks.REDSTONE_BLOCK);
        EnergyConverterBlockEntity.serverTick(
                helper.getLevel(), redstoneStopped.getBlockPos(), redstoneStopped.getBlockState(), redstoneStopped);
        require(redstoneStopped.storedFe() == 0
                        && redstoneStopped.clayEnergyStorage().stored().internalValue() == cost,
                "Redstone did not stop CE conversion");
        helper.setBlock(position.relative(Direction.WEST), Blocks.AIR);
        EnergyConverterBlockEntity.serverTick(
                helper.getLevel(), redstoneStopped.getBlockPos(), redstoneStopped.getBlockState(), redstoneStopped);
        require(redstoneStopped.storedFe() == 10, "Conversion did not restart after redstone was removed");

        CompoundTag saved = redstoneStopped.saveWithFullMetadata(helper.getLevel().registryAccess());
        EnergyConverterBlockEntity loaded = new EnergyConverterBlockEntity(
                redstoneStopped.getBlockPos(), redstoneStopped.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.storedFe() == 10, "Stored FE did not survive a save and reload");
        IEnergyStorage loadedCapability = loaded.energyStorage(null);
        require(loadedCapability.extractEnergy(Integer.MAX_VALUE, false) == 10
                        && loadedCapability.getEnergyStored() == 0,
                "FE extraction was not safe for an int-range request");
        helper.succeed();
    }

    private static void assertOneWayConversion(GameTestHelper helper, int tier, BlockPos relativePosition) {
        EnergyConverterBlockEntity converter = placedConverter(helper, tier, relativePosition);
        BlockPos receiverPosition = helper.absolutePos(relativePosition.relative(Direction.EAST));
        helper.getLevel().setBlockAndUpdate(receiverPosition, Blocks.IRON_BLOCK.defaultBlockState());
        TestReceiver receiver = new TestReceiver(Integer.MAX_VALUE);
        TEST_RECEIVERS.put(receiverPosition, receiver);
        helper.getLevel().invalidateCapabilities(receiverPosition);

        EnergyConverterSpecification specification = converter.specification();
        IEnergyStorage exposed = helper.getLevel().getCapability(
                Capabilities.EnergyStorage.BLOCK, converter.getBlockPos(), Direction.WEST);
        require(exposed != null && exposed.receiveEnergy(Integer.MAX_VALUE, false) == 0,
                "Tier " + tier + " accepted external FE");
        require(converter.clayEnergyStorage().stored().internalValue() == 0L,
                "Tier " + tier + " converted external FE into CE");

        CompressionTier fuelTier = tier == 4
                ? CompressionTier.ENERGETIC_CLAY : CompressionTier.OCTUPLE_COMPRESSED_ENERGETIC_CLAY;
        ItemStack fuel = new ItemStack(MaterialRegistration.COMPRESSION_BLOCKS.get(fuelTier.id()).get());
        converter.fuelHandler(null).insertItem(0, fuel, false);
        loadStoredFe(helper, converter, specification.capacity() - specification.generatedFePerTick());

        EnergyConverterBlockEntity.serverTick(
                helper.getLevel(), converter.getBlockPos(), converter.getBlockState(), converter);
        require(receiver.stored == specification.outputLimitPerTick(),
                "Tier " + tier + " did not respect its per-tick push limit");
        require(exposed.extractEnergy(Integer.MAX_VALUE, false) == 0,
                "Tier " + tier + " exceeded its shared push and extraction limit");
        long expectedRemainingCe = ClayEnergy.ofCe(fuelTier.clayEnergy()).internalValue()
                - specification.clayEnergyPerTick();
        require(converter.clayEnergyStorage().stored().internalValue() == expectedRemainingCe,
                "Tier " + tier + " did not consume the specified CE");
        TEST_RECEIVERS.remove(receiverPosition);
    }

    private static void loadStoredFe(
            GameTestHelper helper, EnergyConverterBlockEntity converter, int amount) {
        CompoundTag saved = converter.saveWithFullMetadata(helper.getLevel().registryAccess());
        saved.putInt("fe_energy", amount);
        converter.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(converter.storedFe() == amount, "FE buffer did not load the requested amount");
    }

    private static EnergyConverterBlockEntity placedConverter(
            GameTestHelper helper, int tier, BlockPos relativePosition) {
        helper.setBlock(relativePosition, EnergyConverterRegistration.CONVERTERS.get(tier).block().get());
        return helper.getBlockEntity(relativePosition);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static final class TestReceiver implements IEnergyStorage {
        private final int capacity;
        private int stored;

        private TestReceiver(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int accepted = Math.min(Math.max(0, maxReceive), capacity - stored);
            if (!simulate) {
                stored += accepted;
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return stored;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    private EnergyConverterGameTests() {
    }
}
