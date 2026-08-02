/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClayiumConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_REGISTRY_SUMMARY = BUILDER
            .comment("Log a short message when the Clayium common registry foundation is initialized.")
            .define("logRegistrySummary", false);

    public static final ModConfigSpec.IntValue CE_SYNC_INTERVAL_TICKS = BUILDER
            .comment("Minimum interval between Clay Energy display synchronization updates.")
            .defineInRange("ceSyncIntervalTicks", 5, 1, 20);

    public static final ModConfigSpec.DoubleValue CE_FE_CONVERSION_MULTIPLIER = BUILDER
            .comment("Multiplier applied to both CE consumption and FE production of CE-FE converters.")
            .defineInRange("ceFeConversionMultiplier", 1.0D, 0.01D, 1000.0D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClayiumConfig() {
    }
}
