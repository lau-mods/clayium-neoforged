package net.claustra01.clayium;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClayiumConfig {
    public static final int MIN_TIER = 0;
    public static final int MAX_TIER = 13;

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;
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
        INVERT_CLAY_LASER_RS_CONDITION = builder
                .comment("Invert the redstone condition used by Clay Energy Lasers")
                .define("InvertClayLaserRSCondition", false);
        builder.pop();
        COMMON_SPEC = builder.build();

        builder = new ModConfigSpec.Builder();
        builder.push("render");
        CA_REACTOR_GLITTERING = builder
                .comment("Render the full-bright shimmer on formed CA reactor cores")
                .define("CAReactorGlittering", true);
        LASER_QUALITY = builder
                .comment("Number of crossed planes used to render a Clay Energy Laser beam")
                .defineInRange("LaserQuality", 8, 1, 32);
        builder.pop();
        CLIENT_SPEC = builder.build();

        builder = new ModConfigSpec.Builder();
        builder.push("misc");
        CLAY_STEEL_PICKAXE_RANGE = builder
                .comment("Maximum range mode and remembered-shape radius for Clay Steel tools")
                .defineInRange("ClaySteelPickaxeRange", 2, 0, 64);
        builder.pop();
        builder.push("tier_balance");
        GENERAL_MACHINE = new MachineBalance(builder, "MachineBase_Crafting");
        GENERAL_MACHINE.defineTier(builder, 5, 0.25D, 5.0D);
        GENERAL_MACHINE.defineTier(builder, 6, 0.0625D, 25.0D);
        GENERAL_MACHINE.defineTier(builder, 10, 0.01D, 250.0D);
        SMELTER_MACHINE = new MachineBalance(builder, "MachineSmelter_Crafting");
        SMELTER_MACHINE.defineTier(builder, 4, 2.0D, 1.0D);
        SMELTER_MACHINE.defineTier(builder, 5, 0.5D, 14.0D);
        SMELTER_MACHINE.defineTier(builder, 6, 0.125D, 200.0D);
        SMELTER_MACHINE.defineTier(builder, 7, 0.03D, 2_800.0D);
        SMELTER_MACHINE.defineTier(builder, 8, 0.01D, 40_000.0D);
        SMELTER_MACHINE.defineTier(builder, 9, 0.0025D, 560_000.0D);
        CA_CONDENSER_MACHINE = new MachineBalance(builder, "MachineCACondenser_Crafting");
        CA_CONDENSER_MACHINE.defineTier(builder, 10, 0.1D, 10.0D);
        CA_CONDENSER_MACHINE.defineTier(builder, 11, 0.01D, 100.0D);

        int[] machineIntervals = {20, 20, 20, 20, 20, 2, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] machineAmounts = {8, 8, 8, 8, 8, 16, 64, 64, 64, 64, 64, 64, 64, 64};
        MACHINE_TRANSPORT = new TransportBalance(
                builder, "MachineBase_Transport", machineIntervals, machineAmounts);
        int[] bufferIntervals = {8, 8, 8, 8, 8, 4, 2, 1, 1, 1, 1, 1, 1, 1};
        int[] bufferAmounts = {1, 1, 1, 1, 1, 4, 16, 64, 128, 192, 256, 512, 1_024, 6_400};
        BUFFER_TRANSPORT = new TransportBalance(
                builder, "MachineBuffer_Transport", bufferIntervals, bufferAmounts);
        builder.pop();
        SERVER_SPEC = builder.build();
    }

    public static double progressionRate() {
        return PROGRESSION_RATE.get();
    }

    public static boolean hardcoreAluminium() {
        return HARDCORE_ALUMINIUM.get();
    }

    public static boolean hardcoreOsmium() {
        return HARDCORE_OSMIUM.get();
    }

    public static boolean enableInjectorRecipeOfInterface() {
        return ENABLE_INJECTOR_RECIPE_OF_INTERFACE.get();
    }

    public static int multiplyProgressionCount(int value) {
        return Math.max(1, (int)(value * progressionRate()));
    }

    public static long divideProgressionTicks(long value) {
        return Math.max(1L, (long)(value / progressionRate()));
    }

    public static int laserQuality() {
        return LASER_QUALITY.get();
    }

    public static final class MachineBalance {
        private final ModConfigSpec.DoubleValue defaultCraftTime;
        private final ModConfigSpec.DoubleValue defaultEnergy;
        private final ModConfigSpec.DoubleValue[] craftTime = new ModConfigSpec.DoubleValue[MAX_TIER + 1];
        private final ModConfigSpec.DoubleValue[] energy = new ModConfigSpec.DoubleValue[MAX_TIER + 1];

        private final String prefix;

        private MachineBalance(ModConfigSpec.Builder builder, String prefix) {
            this.prefix = prefix;
            defaultCraftTime = builder.defineInRange(prefix + "_multCraftTime_def", 1.0D, 0.0D, 1.0E9D);
            defaultEnergy = builder.defineInRange(prefix + "_multConsumingEnergy_def", 1.0D, 0.0D, 1.0E9D);
        }

        private void defineTier(ModConfigSpec.Builder builder, int tier, double time, double consumingEnergy) {
            checkTier(tier);
            craftTime[tier] = builder.defineInRange(
                    tierKey(prefix + "_multCraftTime", tier), time, 0.0D, 1.0E9D);
            energy[tier] = builder.defineInRange(
                    tierKey(prefix + "_multConsumingEnergy", tier), consumingEnergy, 0.0D, 1.0E9D);
        }

        public double craftTime(int tier) {
            checkTier(tier);
            return (craftTime[tier] == null ? defaultCraftTime : craftTime[tier]).get();
        }

        public double energy(int tier) {
            checkTier(tier);
            return (energy[tier] == null ? defaultEnergy : energy[tier]).get();
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
        private final ModConfigSpec.IntValue[] insertInterval = new ModConfigSpec.IntValue[MAX_TIER + 1];
        private final ModConfigSpec.IntValue[] extractInterval = new ModConfigSpec.IntValue[MAX_TIER + 1];
        private final ModConfigSpec.IntValue[] maximumInsert = new ModConfigSpec.IntValue[MAX_TIER + 1];
        private final ModConfigSpec.IntValue[] maximumExtract = new ModConfigSpec.IntValue[MAX_TIER + 1];

        private TransportBalance(ModConfigSpec.Builder builder, String prefix, int[] intervals, int[] amounts) {
            if (intervals.length != MAX_TIER + 1 || amounts.length != MAX_TIER + 1) {
                throw new IllegalArgumentException("Transport balance must define tiers 0 through 13");
            }
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

        public int insertInterval(int tier) {
            checkTier(tier);
            return insertInterval[tier].get();
        }

        public int extractInterval(int tier) {
            checkTier(tier);
            return extractInterval[tier].get();
        }

        public int maximumInsert(int tier) {
            checkTier(tier);
            return maximumInsert[tier].get();
        }

        public int maximumExtract(int tier) {
            checkTier(tier);
            return maximumExtract[tier].get();
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

    public static void checkTier(int tier) {
        if (tier < MIN_TIER || tier > MAX_TIER) {
            throw new IllegalArgumentException("Tier must be from 0 through 13: " + tier);
        }
    }

    private ClayiumConfig() {
    }
}
