/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.tier;

import net.claustra01.clayium.Clayium;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Stable named Clayium progression tiers.
 *
 * <p>The legacy index is retained only for comparison and migration tooling;
 * it is not a persistence format. The first fourteen entries mirror the
 * original Clayium 1.7.10 tier prefix sequence. PAN content is deliberately
 * not assigned a new numeric tier; its dedicated progression rules will be
 * defined in the later PAN phase.</p>
 */
public enum ClayTier {
    RAW("raw", 0, "Raw"),
    CLAY("clay", 1, "Clay"),
    DENSE_CLAY("dense_clay", 2, "Dense Clay"),
    SIMPLE("simple", 3, "Simple"),
    BASIC("basic", 4, "Basic"),
    ADVANCED("advanced", 5, "Advanced"),
    PRECISION("precision", 6, "Precision"),
    CLAY_STEEL("clay_steel", 7, "Clay Steel"),
    CLAYIUM("clayium", 8, "Clayium"),
    ULTIMATE("ultimate", 9, "Ultimate"),
    ANTIMATTER("antimatter", 10, "Antimatter"),
    PURE_ANTIMATTER("pure_antimatter", 11, "Pure Antimatter"),
    OEC("oec", 12, "OEC"),
    OPA("opa", 13, "OPA");

    public static final Codec<ClayTier> CODEC = Codec.STRING.comapFlatMap(ClayTier::decode, ClayTier::id);
    public static final StreamCodec<io.netty.buffer.ByteBuf, ClayTier> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(ClayTier::byIdOrRaw, ClayTier::id);
    private static final Map<String, ClayTier> BY_ID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(ClayTier::id, Function.identity()));

    private final String id;
    private final int progressionIndex;
    private final String displayName;

    ClayTier(String id, int progressionIndex, String displayName) {
        this.id = id;
        this.progressionIndex = progressionIndex;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public int progressionIndex() {
        return progressionIndex;
    }

    public String displayName() {
        return displayName;
    }

    public ResourceLocation resourceLocation() {
        return Clayium.id("tier/" + id);
    }

    public String translationKey() {
        return Clayium.MODID + ".tier." + id;
    }

    public boolean isAtLeast(ClayTier requiredTier) {
        return progressionIndex >= requiredTier.progressionIndex;
    }

    public static Optional<ClayTier> tryById(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static ClayTier byIdOrRaw(String id) {
        return BY_ID.getOrDefault(id, RAW);
    }

    public static ClayTier byLegacyIndex(int index) {
        return Arrays.stream(values())
                .filter(tier -> tier.progressionIndex == index)
                .findFirst()
                .orElse(RAW);
    }

    private static DataResult<ClayTier> decode(String id) {
        ClayTier tier = BY_ID.get(id);
        return tier == null
                ? DataResult.error(() -> "Unknown Clayium tier: " + id)
                : DataResult.success(tier);
    }
}
