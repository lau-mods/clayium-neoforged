/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.data;

import net.claustra01.clayium.tier.ClayTier;
import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.network.codec.StreamCodec;

/** Immutable ItemStack data for a named machine tier. */
public record MachineTierData(ClayTier tier) {
    public static final MachineTierData DEFAULT = new MachineTierData(ClayTier.RAW);
    public static final Codec<MachineTierData> CODEC = ClayTier.CODEC.xmap(MachineTierData::new, MachineTierData::tier);
    public static final StreamCodec<io.netty.buffer.ByteBuf, MachineTierData> STREAM_CODEC =
            ClayTier.STREAM_CODEC.map(MachineTierData::new, MachineTierData::tier);

    public MachineTierData {
        Objects.requireNonNull(tier, "tier");
    }
}
