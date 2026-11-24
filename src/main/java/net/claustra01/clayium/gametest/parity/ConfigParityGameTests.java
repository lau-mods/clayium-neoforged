package net.claustra01.clayium.gametest.parity;

import java.lang.reflect.Field;
import java.util.Locale;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.TierManager;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConfigParityGameTests {
    private static final double EPSILON = 0.000_000_001D;

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalDefaultsAndRemovedOptionsAreExact(GameTestHelper helper) {
        requireClose(ClayiumConfig.PROGRESSION_RATE.getDefault(), 1.0D, "ProgressionRate");
        require(!ClayiumConfig.HARDCORE_ALUMINIUM.getDefault(), "HardcoreAluminium default is not false");
        require(!ClayiumConfig.HARDCORE_OSMIUM.getDefault(), "HardcoreOsmium default is not false");
        require(!ClayiumConfig.ENABLE_INJECTOR_RECIPE_OF_INTERFACE.getDefault(),
                "EnableInjectorRecipeOfInterface default is not false");
        require(ClayiumConfig.CLAY_STEEL_PICKAXE_RANGE.getDefault() == 2,
                "ClaySteelPickaxeRange default is not 2");
        require(!ClayiumConfig.INVERT_CLAY_LASER_RS_CONDITION.getDefault(),
                "InvertClayLaserRSCondition default is not false");
        require(ClayiumConfig.CA_REACTOR_GLITTERING.getDefault(),
                "CAReactorGlittering default is not true");
        require(ClayiumConfig.LASER_QUALITY.getDefault() == 8, "LaserQuality default is not 8");

        String declaredFields = java.util.Arrays.stream(ClayiumConfig.class.getDeclaredFields())
                .map(Field::getName)
                .map(name -> name.toUpperCase(Locale.ROOT))
                .reduce("", (left, right) -> left + '\n' + right);
        for (String removed : new String[]{
                "UTILITY_MODE", "FLUID_CAPSULE", "FLUID_ID", "ALTERNATIVE_TILE_ENTITY",
                "ENTITY_ID", "PACKET_SENDING_RATE"}) {
            require(!declaredFields.contains(removed), "Removed original setting was restored: " + removed);
        }

        assertOriginalTierDefaults();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void tierAndRenderOverridesAffectRuntimeLookups(GameTestHelper helper) {
        var craftTime = ClayiumConfig.GENERAL_MACHINE.craftTimeValue(5);
        var energy = ClayiumConfig.GENERAL_MACHINE.energyValue(5);
        var insertInterval = ClayiumConfig.BUFFER_TRANSPORT.insertIntervalValue(8);
        var extractInterval = ClayiumConfig.BUFFER_TRANSPORT.extractIntervalValue(8);
        var maximumInsert = ClayiumConfig.BUFFER_TRANSPORT.maximumInsertValue(8);
        var maximumExtract = ClayiumConfig.BUFFER_TRANSPORT.maximumExtractValue(8);
        double savedCraftTime = craftTime.get();
        double savedEnergy = energy.get();
        int savedInsertInterval = insertInterval.get();
        int savedExtractInterval = extractInterval.get();
        int savedMaximumInsert = maximumInsert.get();
        int savedMaximumExtract = maximumExtract.get();
        int savedLaserQuality = ClayiumConfig.LASER_QUALITY.get();
        try {
            craftTime.set(0.5D);
            energy.set(7.0D);
            insertInterval.set(3);
            extractInterval.set(5);
            maximumInsert.set(17);
            maximumExtract.set(29);
            ClayiumConfig.LASER_QUALITY.set(12);

            TierManager.MachineParameters machine = TierManager.general(5);
            requireClose(machine.craftTimeMultiplier(), 0.5D, "configured tier 5 craft time");
            requireClose(machine.energyMultiplier(), 7.0D, "configured tier 5 energy");
            TierManager.ProcessingParameters processing = TierManager.calculate(machine, 100L, 100L, 1.0D);
            require(processing.requiredProgress() == 50L,
                    "Configured craft-time multiplier did not affect processing");
            require(processing.energyPerProgress().internalValue() == 700L,
                    "Configured energy multiplier did not affect processing");

            TierManager.TransportParameters transport = TierManager.bufferTransport(8);
            require(transport.insertIntervalTicks() == 3 && transport.extractIntervalTicks() == 5,
                    "Configured insert/extract intervals were not read independently");
            require(transport.maximumInsertItems() == 17 && transport.maximumExtractItems() == 29,
                    "Configured insert/extract limits were not read independently");
            require(ClayiumConfig.laserQuality() == 12, "LaserQuality was not read by the renderer lookup");
        } finally {
            craftTime.set(savedCraftTime);
            energy.set(savedEnergy);
            insertInterval.set(savedInsertInterval);
            extractInterval.set(savedExtractInterval);
            maximumInsert.set(savedMaximumInsert);
            maximumExtract.set(savedMaximumExtract);
            ClayiumConfig.LASER_QUALITY.set(savedLaserQuality);
        }
        helper.succeed();
    }

    private static void assertOriginalTierDefaults() {
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

        int[] bufferIntervals = {8, 8, 8, 8, 8, 4, 2, 1, 1, 1, 1, 1, 1, 1};
        int[] bufferAmounts = {1, 1, 1, 1, 1, 4, 16, 64, 128, 192, 256, 512, 1_024, 6_400};
        for (int tier = 0; tier <= 13; tier++) {
            TierManager.TransportParameters machine = TierManager.machineTransport(tier);
            int machineInterval = tier <= 4 ? 20 : tier == 5 ? 2 : 1;
            int machineAmount = tier <= 4 ? 8 : tier == 5 ? 16 : 64;
            require(machine.insertIntervalTicks() == machineInterval
                            && machine.extractIntervalTicks() == machineInterval,
                    "Wrong standard transport interval at tier " + tier);
            require(machine.maximumInsertItems() == machineAmount
                            && machine.maximumExtractItems() == machineAmount,
                    "Wrong standard transport amount at tier " + tier);

            TierManager.TransportParameters buffer = TierManager.bufferTransport(tier);
            require(buffer.insertIntervalTicks() == bufferIntervals[tier]
                            && buffer.extractIntervalTicks() == bufferIntervals[tier],
                    "Wrong buffer interval at tier " + tier);
            require(buffer.maximumInsertItems() == bufferAmounts[tier]
                            && buffer.maximumExtractItems() == bufferAmounts[tier],
                    "Wrong buffer amount at tier " + tier);
        }
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

    private ConfigParityGameTests() {
    }
}
