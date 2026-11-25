package net.claustra01.clayium.gametest.energy;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EnergyGameTests {
    private static final double EPSILON = 0.000_000_001D;

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void energeticClayFuelValuesAndConsumptionAreExact(GameTestHelper helper) {
        long[] expectedInternal = {
                100_000L, 1_000_000L, 10_000_000L, 100_000_000L, 1_000_000_000L,
                10_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L
        };
        for (CompressionTier compressionTier : CompressionTier.values()) {
            if (compressionTier.clayEnergy() == 0L) {
                continue;
            }
            ItemStack fuel = new ItemStack(MaterialRegistration.COMPRESSION_BLOCKS
                    .get(compressionTier.id()).get().asItem());
            ClayEnergyStorage storage = new ClayEnergyStorage();
            require(storage.produceClayEnergy(fuel), "Energetic clay was not accepted: " + compressionTier.id());
            require(fuel.isEmpty(), "Fuel conversion did not consume one item: " + compressionTier.id());
            require(storage.stored().internalValue()
                            == expectedInternal[compressionTier.originalMetadata() - 4],
                    "Fuel value differs from the original compression tier: " + compressionTier.id());
        }

        ItemStack energeticClay = new ItemStack(MaterialRegistration.COMPRESSION_BLOCKS
                .get(CompressionTier.ENERGETIC_CLAY.id()).get().asItem());
        ItemStack octupleClay = new ItemStack(MaterialRegistration.COMPRESSION_BLOCKS
                .get(CompressionTier.OCTUPLE_COMPRESSED_ENERGETIC_CLAY.id()).get().asItem());
        ClayEnergyStorage storage = new ClayEnergyStorage();
        require(storage.consume(ClayEnergy.ofInternal(40_000L), energeticClay),
                "An empty machine did not convert fuel when energy was required");
        require(energeticClay.isEmpty(), "The energetic clay was not consumed");
        require(storage.stored().internalValue() == 60_000L, "The unconsumed fuel energy was not retained");
        require(storage.consume(ClayEnergy.ofInternal(100_000L), octupleClay),
                "The second fuel did not convert when the store became insufficient");
        require(octupleClay.isEmpty(), "The octuple compressed fuel was not consumed");
        require(energeticClay.isEmpty(), "The consumed energetic clay reappeared");
        ClayEnergyStorage loaded = new ClayEnergyStorage();
        loaded.deserializeNBT(helper.getLevel().registryAccess(),
                storage.serializeNBT(helper.getLevel().registryAccess()));
        require(loaded.stored().internalValue() == 9_999_999_960_000L,
                "Fuel energy and consumption did not survive serialization");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void clayEnergyBoundariesFormattingAndNbtRoundTripAreSafe(GameTestHelper helper) {
        require(ClayEnergy.ofInternal(Long.MAX_VALUE - 4L)
                        .plus(ClayEnergy.ofInternal(10L)).internalValue() == Long.MAX_VALUE,
                "Clay Energy addition did not saturate at the long boundary");
        require(ClayEnergy.ofInternal(4L)
                        .minus(ClayEnergy.ofInternal(10L)).internalValue() == 0L,
                "Clay Energy subtraction did not saturate at zero");
        require(ClayEnergy.ofCe(Long.MAX_VALUE).internalValue() == Long.MAX_VALUE,
                "Whole CE conversion overflowed instead of saturating");
        require(ClayEnergy.ofInternal(100L).format().equals("0.001 CE"),
                "Fractional CE formatting is not exact");
        require(ClayEnergy.ofInternal(100_000L).format().equals("1 CE"),
                "Whole CE formatting is not exact");

        ClayEnergyStorage saved = new ClayEnergyStorage(ClayEnergy.ofInternal(Long.MAX_VALUE - 42L));
        CompoundTag tag = saved.serializeNBT(helper.getLevel().registryAccess());
        ClayEnergyStorage loaded = new ClayEnergyStorage();
        loaded.deserializeNBT(helper.getLevel().registryAccess(), tag);
        require(loaded.stored().equals(saved.stored()), "Saved Clay Energy did not survive an NBT reload");

        ItemStack fuelAtCapacity = new ItemStack(MaterialRegistration.COMPRESSION_BLOCKS
                .get(CompressionTier.OCTUPLE_COMPRESSED_ENERGETIC_CLAY.id()).get().asItem());
        ClayEnergyStorage full = new ClayEnergyStorage(ClayEnergy.MAX);
        require(!full.produceClayEnergy(fuelAtCapacity) && fuelAtCapacity.getCount() == 1,
                "A full long-range store destroyed fuel that it could not accept");

        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void ordinaryClayEnergyStorageDoesNotExposeFe(GameTestHelper helper) {
        require(!ClayEnergyStorage.EXPOSES_FE_CAPABILITY,
                "Ordinary Clayium machines must not expose an FE capability");
        require(!IEnergyStorage.class.isAssignableFrom(ClayEnergyStorage.class),
                "Clay Energy storage unexpectedly implements the FE storage contract");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalTierAndTransportTablesAreExact(GameTestHelper helper) {
        assertMachine(TierManager.general(5), 0.25D, 5.0D, "generic tier 5");
        assertMachine(TierManager.general(6), 0.0625D, 25.0D, "generic tier 6");
        assertMachine(TierManager.general(10), 0.01D, 250.0D, "generic tier 10");
        assertMachine(TierManager.general(9), 1.0D, 1.0D, "generic default tier");

        double[] smelterTimes = {2.0D, 0.5D, 0.125D, 0.03D, 0.01D, 0.0025D};
        double[] smelterEnergy = {1.0D, 14.0D, 200.0D, 2_800.0D, 40_000.0D, 560_000.0D};
        for (int tier = 4; tier <= 9; tier++) {
            assertMachine(TierManager.smelter(tier), smelterTimes[tier - 4], smelterEnergy[tier - 4],
                    "smelter tier " + tier);
        }
        assertMachine(TierManager.caCondenser(10), 0.1D, 10.0D, "CA condenser tier 10");
        assertMachine(TierManager.caCondenser(11), 0.01D, 100.0D, "CA condenser tier 11");
        requireClose(TierManager.overclockerFactor(1), 1.5D, "overclocker rank 1 factor");
        requireClose(TierManager.overclockerFactor(2), 2.3D, "overclocker rank 2 factor");
        requireClose(TierManager.overclockerFactor(3), 3.5D, "overclocker rank 3 factor");
        requireClose(TierManager.overclockerFactor(4), 5.0D, "overclocker rank 4 factor");

        for (int tier = 0; tier <= 13; tier++) {
            TierManager.TransportParameters machine = TierManager.machineTransport(tier);
            int interval = tier <= 4 ? 20 : tier == 5 ? 2 : 1;
            int amount = tier <= 4 ? 8 : tier == 5 ? 16 : 64;
            require(machine.insertIntervalTicks() == interval && machine.extractIntervalTicks() == interval,
                    "Wrong standard transport interval at tier " + tier);
            require(machine.maximumInsertItems() == amount && machine.maximumExtractItems() == amount,
                    "Wrong standard transport amount at tier " + tier);
        }
        int[] bufferIntervals = {8, 8, 8, 8, 8, 4, 2, 1, 1, 1, 1, 1, 1, 1};
        int[] bufferAmounts = {1, 1, 1, 1, 1, 4, 16, 64, 128, 192, 256, 512, 1_024, 6_400};
        for (int tier = 0; tier <= 13; tier++) {
            TierManager.TransportParameters buffer = TierManager.bufferTransport(tier);
            require(buffer.insertIntervalTicks() == bufferIntervals[tier]
                            && buffer.extractIntervalTicks() == bufferIntervals[tier],
                    "Wrong buffer interval at tier " + tier);
            require(buffer.maximumInsertItems() == bufferAmounts[tier]
                            && buffer.maximumExtractItems() == bufferAmounts[tier],
                    "Wrong buffer amount at tier " + tier);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void tierCalculationsPreserveRoundingAndPreventFreeWork(GameTestHelper helper) {
        require(TierManager.recipeEnergy(1.0D, 4) == 100L, "CRecipes.e(1, 4) differs from the original");
        require(TierManager.recipeEnergy(1.0D, 3) == 10L, "CRecipes.e(1, 3) differs from the original");
        require(TierManager.recipeEnergy(1.0D, 2) == 1L, "CRecipes.e(1, 2) differs from the original");
        require(TierManager.recipeEnergy(0.5D, 2) == 1L,
                "A positive sub-unit recipe became free after floor rounding");
        require(TierManager.recipeEnergy(0.0D, 2) == 0L,
                "A zero-energy recipe was charged energy");

        TierManager.ProcessingParameters tierFive = TierManager.calculate(
                TierManager.general(5), 100L, 100L, 1.0D);
        require(tierFive.requiredProgress() == 25L, "Tier 5 processing time was not quartered");
        require(tierFive.energyPerProgress().internalValue() == 500L,
                "Tier 5 energy was not multiplied by five");
        require(tierFive.totalEnergy().internalValue() == 12_500L,
                "Tier 5 total processing energy is incorrect");
        requireClose(tierFive.progressPerTick(), 1.0D, "Tier 5 base progress");

        TierManager.ProcessingParameters tierSix = TierManager.calculate(
                TierManager.general(6), 100L, 100L, 1.0D);
        require(tierSix.requiredProgress() == 6L, "Tier 6 processing time did not use floor rounding");
        require(tierSix.energyPerProgress().internalValue() == 2_500L,
                "Tier 6 energy was not multiplied by twenty-five");
        require(tierSix.totalEnergy().internalValue() == 15_000L,
                "The same recipe did not receive its Tier 6 energy calculation");

        TierManager.ProcessingParameters mildOverclock = TierManager.calculate(
                TierManager.general(5), 100L, 100L, TierManager.totalOverclockFactor(1));
        require(mildOverclock.requiredProgress() == 25L, "Rank 1 overclock progress target differs");
        requireClose(mildOverclock.progressPerTick(), 1.5D, "rank 1 overclock progress per tick");
        require(mildOverclock.energyPerProgress().internalValue() == 500L,
                "Rank 1 overclock energy per progress differs");
        require(mildOverclock.totalEnergy().internalValue() == 12_500L,
                "Rank 1 overclock total energy differs");
        requireClose(mildOverclock.timeMultiplier(), 0.25D, "rank 1 time multiplier");

        TierManager.ProcessingParameters overclocked = TierManager.calculate(
                TierManager.general(5), 100L, 100L, TierManager.totalOverclockFactor(4, 3));
        requireClose(overclocked.timeMultiplier(), 0.14285714285714285D, "overclocked time multiplier");
        requireClose(overclocked.energyMultiplier(), 11.575161985907584D,
                "overclocked energy multiplier");
        requireClose(overclocked.progressPerTick(), 10.0D, "overclocked progress");
        require(overclocked.requiredProgress() == 14L,
                "Overclocked progress target did not use original floor rounding");
        require(overclocked.energyPerProgress().internalValue() == 1_157L,
                "Overclocked energy per progress was rounded incorrectly");
        require(overclocked.totalEnergy().internalValue() == 16_198L,
                "Overclocked total energy was rounded incorrectly");
        require(overclocked.totalEnergy().internalValue() >= tierFive.totalEnergy().internalValue(),
                "Overclocking reduced the total CE cost");

        TierManager.ProcessingParameters guarded = TierManager.calculate(
                TierManager.general(0), 1L, TierManager.recipeEnergy(0.5D, 2), 1.0D);
        require(guarded.requiredProgress() == 1L, "A positive recipe was rounded to zero progress");
        require(guarded.energyPerProgress().internalValue() == 1L,
                "A positive recipe was allowed to process without energy");
        TierManager.ProcessingParameters free = TierManager.calculate(
                TierManager.general(0), 1L, TierManager.recipeEnergy(0.0D, 2), 1.0D);
        require(free.energyPerProgress().internalValue() == 0L
                        && free.totalEnergy().internalValue() == 0L,
                "A zero-energy recipe was charged energy");

        TierManager.ProcessingParameters boundary = TierManager.calculate(
                TierManager.smelter(9), Long.MAX_VALUE, Long.MAX_VALUE, 1.0D);
        require(boundary.totalEnergy().internalValue() == Long.MAX_VALUE,
                "Tier calculation overflowed the long energy boundary");
        helper.succeed();
    }

    private static void assertMachine(
            TierManager.MachineParameters actual, double expectedTime, double expectedEnergy, String description) {
        requireClose(actual.craftTimeMultiplier(), expectedTime, description + " time multiplier");
        requireClose(actual.energyMultiplier(), expectedEnergy, description + " energy multiplier");
    }

    private static void requireClose(double actual, double expected, String description) {
        require(Math.abs(actual - expected) <= EPSILON * Math.max(1.0D, Math.abs(expected)),
                "Wrong " + description + ": " + actual + " instead of " + expected);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private EnergyGameTests() {
    }
}
