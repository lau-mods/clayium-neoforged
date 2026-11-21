package net.claustra01.clayium;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClayiumConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec.BooleanValue INVERT_CLAY_LASER_RS_CONDITION;
    public static final ModConfigSpec.BooleanValue CA_REACTOR_GLITTERING;
    public static final ModConfigSpec.IntValue CLAY_STEEL_PICKAXE_RANGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        INVERT_CLAY_LASER_RS_CONDITION = builder
                .comment("Invert the redstone condition used by Clay Energy Lasers")
                .define("InvertClayLaserRSCondition", false);
        CA_REACTOR_GLITTERING = builder
                .comment("Render the full-bright shimmer on formed CA reactor cores")
                .define("CAReactorGlittering", true);
        SPEC = builder.build();
        builder = new ModConfigSpec.Builder();
        CLAY_STEEL_PICKAXE_RANGE = builder
                .comment("Maximum range mode and remembered-shape radius for Clay Steel tools")
                .defineInRange("ClaySteelPickaxeRange", 2, 0, 64);
        SERVER_SPEC = builder.build();
    }

    private ClayiumConfig() {
    }
}
