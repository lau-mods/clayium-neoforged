/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Immutable area mode and optional custom mining shape for Clay Steel tools. */
public record ClaySteelToolSettings(int mode, List<BlockPos> customShape) {
    public static final ClaySteelToolSettings DEFAULT = new ClaySteelToolSettings(0, List.of());
    public static final Codec<ClaySteelToolSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 2).fieldOf("mode").forGetter(ClaySteelToolSettings::mode),
            BlockPos.CODEC.listOf(0, 125).optionalFieldOf("custom_shape", List.of())
                    .forGetter(ClaySteelToolSettings::customShape)
    ).apply(instance, ClaySteelToolSettings::new));
    public static final StreamCodec<io.netty.buffer.ByteBuf, ClaySteelToolSettings> STREAM_CODEC =
            ByteBufCodecs.fromCodec(CODEC);

    public ClaySteelToolSettings {
        customShape = List.copyOf(customShape);
        if (customShape.size() > 125 || customShape.stream().anyMatch(pos ->
                Math.abs(pos.getX()) > 2 || Math.abs(pos.getY()) > 2 || Math.abs(pos.getZ()) > 2)) {
            throw new IllegalArgumentException("Clay Steel custom tool shape exceeds 5x5x5");
        }
    }

    public ClaySteelToolSettings withMode(int nextMode) {
        return new ClaySteelToolSettings(nextMode, customShape);
    }
}
