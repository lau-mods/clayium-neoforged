/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Minimal immutable state sent to clients for displaying Clay Energy. */
public record ClayEnergySnapshot(long energy, long capacity) {
    private static final Codec<Long> NON_NEGATIVE_LONG = Codec.LONG.comapFlatMap(
            value -> value < 0 ? com.mojang.serialization.DataResult.error(() -> "Value must not be negative") : com.mojang.serialization.DataResult.success(value),
            value -> value);
    public static final Codec<ClayEnergySnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NON_NEGATIVE_LONG.fieldOf("energy").forGetter(ClayEnergySnapshot::energy),
            NON_NEGATIVE_LONG.fieldOf("capacity").forGetter(ClayEnergySnapshot::capacity)
    ).apply(instance, ClayEnergySnapshot::new));
    public static final StreamCodec<io.netty.buffer.ByteBuf, ClayEnergySnapshot> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG, ClayEnergySnapshot::energy,
                    ByteBufCodecs.VAR_LONG, ClayEnergySnapshot::capacity,
                    ClayEnergySnapshot::new);

    public ClayEnergySnapshot {
        if (energy < 0 || capacity < 0 || energy > capacity) {
            throw new IllegalArgumentException("Invalid Clay Energy snapshot: " + energy + "/" + capacity);
        }
    }
}
