/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.machine;

import net.claustra01.clayium.tier.ClayTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Immutable common definition shared by machine blocks and recipe processors. */
public record MachineDefinition(
        ResourceLocation id,
        ClayTier minimumTier,
        int processingTimeTicks,
        long clayEnergyPerTick
) {
    private static final Codec<Long> NON_NEGATIVE_LONG = Codec.LONG.comapFlatMap(
            value -> value < 0 ? com.mojang.serialization.DataResult.error(() -> "Value must not be negative") : com.mojang.serialization.DataResult.success(value),
            value -> value);
    public static final Codec<MachineDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(MachineDefinition::id),
            ClayTier.CODEC.fieldOf("minimum_tier").forGetter(MachineDefinition::minimumTier),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("processing_time_ticks").forGetter(MachineDefinition::processingTimeTicks),
            NON_NEGATIVE_LONG.fieldOf("clay_energy_per_tick").forGetter(MachineDefinition::clayEnergyPerTick)
    ).apply(instance, MachineDefinition::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MachineDefinition> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, MachineDefinition::id,
            ClayTier.STREAM_CODEC, MachineDefinition::minimumTier,
            ByteBufCodecs.VAR_INT, MachineDefinition::processingTimeTicks,
            ByteBufCodecs.VAR_LONG, MachineDefinition::clayEnergyPerTick,
            MachineDefinition::new);

    public MachineDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(minimumTier, "minimumTier");
        if (processingTimeTicks < 1) {
            throw new IllegalArgumentException("processingTimeTicks must be positive");
        }
        if (clayEnergyPerTick < 0) {
            throw new IllegalArgumentException("clayEnergyPerTick must not be negative");
        }
    }

    public boolean supports(ClayTier availableTier) {
        return availableTier != null && availableTier.isAtLeast(minimumTier);
    }
}
