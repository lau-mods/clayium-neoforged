package net.claustra01.clayium;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClayiumConfig {
    public static final int MIN_TIER = 0;
    public static final int MAX_TIER = 13;

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec.BooleanValue INVERT_CLAY_LASER_RS_CONDITION;
    public static final ModConfigSpec.BooleanValue CA_REACTOR_GLITTERING;
    public static final ModConfigSpec.IntValue LASER_QUALITY;
    public static final ModConfigSpec.DoubleValue PROGRESSION_RATE;
    public static final ModConfigSpec.BooleanValue HARDCORE_ALUMINIUM;
    public static final ModConfigSpec.BooleanValue HARDCORE_OSMIUM;
    public static final ModConfigSpec.BooleanValue ENABLE_INJECTOR_RECIPE_OF_INTERFACE;
    public static final ModConfigSpec.IntValue CLAY_STEEL_PICKAXE_RANGE;
    public static final MachineBalance GENERAL_MACHINE;
    public static final MachineBalance SMELTER_MACHINE;
    public static final MachineBalance CA_CONDENSER_MACHINE;
    public static final TransportBalance MACHINE_TRANSPORT;
    public static final TransportBalance BUFFER_TRANSPORT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("misc");
        INVERT_CLAY_LASER_RS_CONDITION = builder
                .comment("Invert the redstone condition used by Clay Energy Lasers")
                .define("InvertClayLaserRSCondition", false);
        builder.pop();
        builder.push("render");
        CA_REACTOR_GLITTERING = builder
                .comment("Render the full-bright shimmer on formed CA reactor cores")
                .define("CAReactorGlittering", true);
        LASER_QUALITY = builder
                .comment("Number of crossed planes used to render a Clay Energy Laser beam")
                .defineInRange("LaserQuality", 8, 1, 32);
        builder.pop();
        SPEC = builder.build();

        builder = new ModConfigSpec.Builder();
        builder.push("mode");
        HARDCORE_ALUMINIUM = builder
                .comment("Enable the original impure-aluminium progression and recipes")
                .define("HardcoreAluminium", false);
        HARDCORE_OSMIUM = builder
                .comment("Enable the original impure-osmium progression and recipes")
                .define("HardcoreOsmium", false);
        PROGRESSION_RATE = builder
                .comment("Scale original progression outputs and processing times at their defined call sites")
                .defineInRange("ProgressionRate", 1.0D, 0.001D, 9_999.0D);
        ENABLE_INJECTOR_RECIPE_OF_INTERFACE = builder
                .comment("Enable CA Injector upgrade recipes for interface blocks")
                .define("EnableInjectorRecipeOfInterface", false);
        builder.pop();
        builder.push("misc");
        CLAY_STEEL_PICKAXE_RANGE = builder
                .comment("Maximum range mode and remembered-shape radius for Clay Steel tools")
                .defineInRange("ClaySteelPickaxeRange", 2, 0, 64);
        builder.pop();
        builder.push("tier_balance");
        GENERAL_MACHINE = MachineBalance.define(builder, "MachineBase_Crafting",
                overrides(new double[][]{{5, 0.25D}, {6, 0.0625D}, {10, 0.01D}}),
                overrides(new double[][]{{5, 5.0D}, {6, 25.0D}, {10, 250.0D}}));
        SMELTER_MACHINE = MachineBalance.define(builder, "MachineSmelter_Crafting",
                overrides(new double[][]{
                        {4, 2.0D}, {5, 0.5D}, {6, 0.125D}, {7, 0.03D}, {8, 0.01D}, {9, 0.0025D}}),
                overrides(new double[][]{
                        {4, 1.0D}, {5, 14.0D}, {6, 200.0D}, {7, 2_800.0D},
                        {8, 40_000.0D}, {9, 560_000.0D}}));
        CA_CONDENSER_MACHINE = MachineBalance.define(builder, "MachineCACondenser_Crafting",
                overrides(new double[][]{{10, 0.1D}, {11, 0.01D}}),
                overrides(new double[][]{{10, 10.0D}, {11, 100.0D}}));

        int[] machineIntervals = {20, 20, 20, 20, 20, 2, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] machineAmounts = {8, 8, 8, 8, 8, 16, 64, 64, 64, 64, 64, 64, 64, 64};
        MACHINE_TRANSPORT = TransportBalance.define(
                builder, "MachineBase_Transport", machineIntervals, machineAmounts);
        int[] bufferIntervals = {8, 8, 8, 8, 8, 4, 2, 1, 1, 1, 1, 1, 1, 1};
        int[] bufferAmounts = {1, 1, 1, 1, 1, 4, 16, 64, 128, 192, 256, 512, 1_024, 6_400};
        BUFFER_TRANSPORT = TransportBalance.define(
                builder, "MachineBuffer_Transport", bufferIntervals, bufferAmounts);
        builder.pop();
        SERVER_SPEC = builder.build();
    }

    public static double progressionRate() {
        return value(PROGRESSION_RATE);
    }

    public static boolean hardcoreAluminium() {
        return value(HARDCORE_ALUMINIUM);
    }

    public static boolean hardcoreOsmium() {
        return value(HARDCORE_OSMIUM);
    }

    public static boolean enableInjectorRecipeOfInterface() {
        return value(ENABLE_INJECTOR_RECIPE_OF_INTERFACE);
    }

    public static int multiplyProgressionCount(int value) {
        return Math.max(1, (int)(value * progressionRate()));
    }

    public static long divideProgressionTicks(long value) {
        return Math.max(1L, (long)(value / progressionRate()));
    }

    public static int laserQuality() {
        return value(LASER_QUALITY);
    }

    private static double[] overrides(double[][] entries) {
        double[] values = new double[MAX_TIER + 1];
        java.util.Arrays.fill(values, Double.NaN);
        for (double[] entry : entries) {
            values[(int)entry[0]] = entry[1];
        }
        return values;
    }

    private static <T> T value(ModConfigSpec.ConfigValue<T> configValue) {
        try {
            return configValue.get();
        } catch (IllegalStateException ignored) {
            return configValue.getDefault();
        }
    }

    public static final class MachineBalance {
        private final ModConfigSpec.DoubleValue defaultCraftTime;
        private final ModConfigSpec.DoubleValue defaultEnergy;
        private final ModConfigSpec.DoubleValue[] craftTime = new ModConfigSpec.DoubleValue[MAX_TIER + 1];
        private final ModConfigSpec.DoubleValue[] energy = new ModConfigSpec.DoubleValue[MAX_TIER + 1];

        private MachineBalance(
                ModConfigSpec.Builder builder, String prefix, double[] craftOverrides, double[] energyOverrides) {
            defaultCraftTime = builder.defineInRange(prefix + "_multCraftTime_def", 1.0D, 0.0D, 1.0E9D);
            defaultEnergy = builder.defineInRange(prefix + "_multConsumingEnergy_def", 1.0D, 0.0D, 1.0E9D);
            for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
                if (!Double.isNaN(craftOverrides[tier])) {
                    craftTime[tier] = builder.defineInRange(
                            tierKey(prefix + "_multCraftTime", tier), craftOverrides[tier], 0.0D, 1.0E9D);
                }
                if (!Double.isNaN(energyOverrides[tier])) {
                    energy[tier] = builder.defineInRange(
                            tierKey(prefix + "_multConsumingEnergy", tier), energyOverrides[tier], 0.0D, 1.0E9D);
                }
            }
        }

        private static MachineBalance define(
                ModConfigSpec.Builder builder, String prefix, double[] craftOverrides, double[] energyOverrides) {
            return new MachineBalance(builder, prefix, craftOverrides, energyOverrides);
        }

        public double craftTime(int tier) {
            checkTier(tier);
            return value(craftTime[tier] == null ? defaultCraftTime : craftTime[tier]);
        }

        public double energy(int tier) {
            checkTier(tier);
            return value(energy[tier] == null ? defaultEnergy : energy[tier]);
        }

        public ModConfigSpec.DoubleValue craftTimeValue(int tier) {
            checkTier(tier);
            return craftTime[tier] == null ? defaultCraftTime : craftTime[tier];
        }

        public ModConfigSpec.DoubleValue energyValue(int tier) {
            checkTier(tier);
            return energy[tier] == null ? defaultEnergy : energy[tier];
        }
    }

    public static final class TransportBalance {
        private final ModConfigSpec.IntValue defaultInsertInterval;
        private final ModConfigSpec.IntValue defaultExtractInterval;
        private final ModConfigSpec.IntValue defaultMaximumInsert;
        private final ModConfigSpec.IntValue defaultMaximumExtract;
        private final ModConfigSpec.IntValue[] insertInterval = new ModConfigSpec.IntValue[MAX_TIER + 1];
        private final ModConfigSpec.IntValue[] extractInterval = new ModConfigSpec.IntValue[MAX_TIER + 1];
        private final ModConfigSpec.IntValue[] maximumInsert = new ModConfigSpec.IntValue[MAX_TIER + 1];
        private final ModConfigSpec.IntValue[] maximumExtract = new ModConfigSpec.IntValue[MAX_TIER + 1];

        private TransportBalance(ModConfigSpec.Builder builder, String prefix, int[] intervals, int[] amounts) {
            defaultInsertInterval = builder.defineInRange(prefix + "_autoInsertInterval_def", 20, 1, 999);
            defaultExtractInterval = builder.defineInRange(prefix + "_autoExtractInterval_def", 20, 1, 999);
            defaultMaximumInsert = builder.defineInRange(prefix + "_maxAutoInsertDefault_def", 8, 1, 9_999);
            defaultMaximumExtract = builder.defineInRange(prefix + "_maxAutoExtractDefault_def", 8, 1, 9_999);
            for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
                insertInterval[tier] = builder.defineInRange(
                        tierKey(prefix + "_autoInsertInterval", tier), intervals[tier], 1, 999);
                extractInterval[tier] = builder.defineInRange(
                        tierKey(prefix + "_autoExtractInterval", tier), intervals[tier], 1, 999);
                maximumInsert[tier] = builder.defineInRange(
                        tierKey(prefix + "_maxAutoInsertDefault", tier), amounts[tier], 1, 9_999);
                maximumExtract[tier] = builder.defineInRange(
                        tierKey(prefix + "_maxAutoExtractDefault", tier), amounts[tier], 1, 9_999);
            }
        }

        private static TransportBalance define(
                ModConfigSpec.Builder builder, String prefix, int[] intervals, int[] amounts) {
            if (intervals.length != MAX_TIER + 1 || amounts.length != MAX_TIER + 1) {
                throw new IllegalArgumentException("Transport balance must define tiers 0 through 13");
            }
            return new TransportBalance(builder, prefix, intervals, amounts);
        }

        public int insertInterval(int tier) {
            checkTier(tier);
            return value(insertInterval[tier] == null ? defaultInsertInterval : insertInterval[tier]);
        }

        public int extractInterval(int tier) {
            checkTier(tier);
            return value(extractInterval[tier] == null ? defaultExtractInterval : extractInterval[tier]);
        }

        public int maximumInsert(int tier) {
            checkTier(tier);
            return value(maximumInsert[tier] == null ? defaultMaximumInsert : maximumInsert[tier]);
        }

        public int maximumExtract(int tier) {
            checkTier(tier);
            return value(maximumExtract[tier] == null ? defaultMaximumExtract : maximumExtract[tier]);
        }

        public ModConfigSpec.IntValue insertIntervalValue(int tier) {
            checkTier(tier);
            return insertInterval[tier];
        }

        public ModConfigSpec.IntValue extractIntervalValue(int tier) {
            checkTier(tier);
            return extractInterval[tier];
        }

        public ModConfigSpec.IntValue maximumInsertValue(int tier) {
            checkTier(tier);
            return maximumInsert[tier];
        }

        public ModConfigSpec.IntValue maximumExtractValue(int tier) {
            checkTier(tier);
            return maximumExtract[tier];
        }
    }

    private static String tierKey(String prefix, int tier) {
        return prefix + '_' + (tier < 10 ? "0" : "") + tier;
    }

    private static void checkTier(int tier) {
        if (tier < MIN_TIER || tier > MAX_TIER) {
            throw new IllegalArgumentException("Tier must be from 0 through 13: " + tier);
        }
    }

    private ClayiumConfig() {
    }
}
