package net.claustra01.clayium;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClayiumConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue INVERT_CLAY_LASER_RS_CONDITION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        INVERT_CLAY_LASER_RS_CONDITION = builder
                .comment("Invert the redstone condition used by Clay Energy Lasers")
                .define("InvertClayLaserRSCondition", false);
        SPEC = builder.build();
    }

    private ClayiumConfig() {
    }
}
