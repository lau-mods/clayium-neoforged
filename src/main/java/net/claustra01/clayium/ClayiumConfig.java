package net.claustra01.clayium;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClayiumConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue HARDCORE_ALUMINIUM;
    public static final ModConfigSpec.BooleanValue HARDCORE_OSMIUM;
    public static final ModConfigSpec.DoubleValue PROGRESSION_RATE;
    public static final ModConfigSpec.BooleanValue ENABLE_INJECTOR_RECIPE_OF_INTERFACE;

    public static final ModConfigSpec.IntValue CLAY_STEEL_PICKAXE_RANGE;
    public static final ModConfigSpec.IntValue PACKET_SENDING_RATE;
    public static final ModConfigSpec.BooleanValue ENABLE_INSTANT_SYNC;
    public static final ModConfigSpec.BooleanValue INVERT_CLAY_LASER_RS_CONDITION;
    public static final ModConfigSpec.BooleanValue ENABLE_FLUID_CAPSULE;
    public static final ModConfigSpec.IntValue FLUID_CAPSULE_CREATIVE_TAB_MODE;
    public static final ModConfigSpec.IntValue FLUID_CAPSULE_MAX_STORAGE;
    public static final ModConfigSpec.BooleanValue ENABLE_RF_GENERATOR;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> RF_GENERATOR_DEFINITIONS;

    public static final ModConfigSpec.IntValue RENDERING_RATE;
    public static final ModConfigSpec.BooleanValue CA_REACTOR_GLITTERING;
    public static final ModConfigSpec.IntValue LASER_QUALITY;

    public static final ModConfigSpec.IntValue CLAY_ORE_VEIN_NUMBER;
    public static final ModConfigSpec.IntValue CLAY_ORE_VEIN_SIZE;
    public static final ModConfigSpec.IntValue CLAY_ORE_VEIN_MIN_Y;
    public static final ModConfigSpec.IntValue CLAY_ORE_VEIN_MAX_Y;
    public static final ModConfigSpec.BooleanValue GENERATE_DENSE_CLAY_ORE_VEIN;
    public static final ModConfigSpec.IntValue DENSE_CLAY_ORE_VEIN_SIZE;
    public static final ModConfigSpec.IntValue LARGE_DENSE_CLAY_ORE_VEIN_NUMBER;
    public static final ModConfigSpec.IntValue LARGE_DENSE_CLAY_ORE_VEIN_SIZE;
    public static final ModConfigSpec.IntValue LARGE_DENSE_CLAY_ORE_VEIN_MIN_Y;
    public static final ModConfigSpec.IntValue LARGE_DENSE_CLAY_ORE_VEIN_MAX_Y;

    static {
        BUILDER.push("mode");
        HARDCORE_ALUMINIUM = BUILDER.define("HardcoreAluminium", false);
        HARDCORE_OSMIUM = BUILDER.define("HardcoreOsmium", false);
        PROGRESSION_RATE = BUILDER.defineInRange("ProgressionRate", 1.0, 0.001, 9999.0);
        ENABLE_INJECTOR_RECIPE_OF_INTERFACE = BUILDER
                .comment("This recipe makes it much easier to construct multi-block machines.")
                .define("EnableInjectorRecipeOfInterface", false);
        BUILDER.pop();

        BUILDER.push("misc");
        CLAY_STEEL_PICKAXE_RANGE = BUILDER.defineInRange("ClaySteelPickaxeRange", 2, 0, 64);
        PACKET_SENDING_RATE = BUILDER.defineInRange("PacketSendingRate", 20, 1, 9999);
        ENABLE_INSTANT_SYNC = BUILDER.define("EnableInstantSync", true);
        INVERT_CLAY_LASER_RS_CONDITION = BUILDER.define("InvertClayLaserRSCondition", false);
        ENABLE_FLUID_CAPSULE = BUILDER.define("EnableFluidCapsule", true);
        FLUID_CAPSULE_CREATIVE_TAB_MODE = BUILDER
                .comment("0: Disable, 1: 1000mB only, 2: Display All")
                .defineInRange("FluidCapsuleCreativeTabMode", 1, 0, 2);
        FLUID_CAPSULE_MAX_STORAGE = BUILDER.defineInRange("FluidCapsuleMaxStorage", 1000, 1, 1000000);
        ENABLE_RF_GENERATOR = BUILDER
                .comment("This setting enables assembler recipes for RF/FE Converters.")
                .define("EnableRFGenerator", false);
        RF_GENERATOR_DEFINITIONS = BUILDER
                .comment("BlockName;IconName:Tier:CEConsumptionPerTick:FEProductionPerTick:FEOutputPerTick:FEStorageSize:OverclockExponent")
                .defineListAllowEmpty(
                        "RFGenerator",
                        List.of(
                                "BasicRFGenerator;rfgenerator:4:0.001:10:10:10000:1",
                                "AdvancedRFGenerator;rfgenerator:5:0.01:30:30:30000:1",
                                "PrecisionRFGenerator;rfgenerator:6:0.1:90:90:90000:1",
                                "ClaySteelRFGenerator;rfgenerator:7:1:270:270:270000:1",
                                "ClayiumRFGenerator;rfgenerator:8:10:810:810:810000:1",
                                "UltimateRFGenerator;rfgenerator:9:100:2430:2430:2430000:1",
                                "AntimatterRFGenerator;rfgenerator:10:1000:7290:7290:7290000:1",
                                "PureAntimatterRFGenerator;rfgenerator:11:10000:21870:21870:21870000:1",
                                "OECRFGenerator;rfgenerator:12:100000:65610:65610:65610000:1",
                                "OPARFGenerator;rfgenerator:13:1000000:196830:196830:196830000:1"
                        ),
                        () -> "",
                        obj -> obj instanceof String s && !s.isBlank()
                );
        BUILDER.pop();

        BUILDER.push("render");
        RENDERING_RATE = BUILDER.defineInRange("RenderingRate", 200, 1, 9999);
        CA_REACTOR_GLITTERING = BUILDER.define("CAReactorGlittering", true);
        LASER_QUALITY = BUILDER.defineInRange("LaserQuality", 8, 1, 32);
        BUILDER.pop();

        BUILDER.push("worldgen");
        CLAY_ORE_VEIN_NUMBER = BUILDER.comment("Number of Clay Ore Veins per Chunk").defineInRange("ClayOreVeinNumber", 8, 0, 99);
        CLAY_ORE_VEIN_SIZE = BUILDER.defineInRange("ClayOreVeinSize", 24, 0, 50);
        CLAY_ORE_VEIN_MIN_Y = BUILDER.defineInRange("ClayOreVeinMinY", 24, -64, 320);
        CLAY_ORE_VEIN_MAX_Y = BUILDER.defineInRange("ClayOreVeinMaxY", 88, -64, 320);
        GENERATE_DENSE_CLAY_ORE_VEIN = BUILDER.define("GenerateDenseClayOreVein", true);
        DENSE_CLAY_ORE_VEIN_SIZE = BUILDER.defineInRange("DenseClayOreVeinSize", 10, 0, 50);
        LARGE_DENSE_CLAY_ORE_VEIN_NUMBER = BUILDER.comment("Number of Large Dense Clay Ore Veins per Chunk").defineInRange("LargeDenseClayOreVeinNumber", 2, 0, 99);
        LARGE_DENSE_CLAY_ORE_VEIN_SIZE = BUILDER.defineInRange("LargeDenseClayOreVeinSize", 6, 0, 50);
        LARGE_DENSE_CLAY_ORE_VEIN_MIN_Y = BUILDER.defineInRange("LargeDenseClayOreVeinMinY", 10, -64, 320);
        LARGE_DENSE_CLAY_ORE_VEIN_MAX_Y = BUILDER.defineInRange("LargeDenseClayOreVeinMaxY", 16, -64, 320);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClayiumConfig() {}
}
