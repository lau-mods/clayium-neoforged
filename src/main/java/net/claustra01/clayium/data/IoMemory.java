/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.data;

import com.mojang.serialization.Codec;
import java.util.List;
import net.claustra01.clayium.logistics.SideMode;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Portable six-face I/O configuration used by the Clay Configurator memory mode. */
public record IoMemory(List<String> sideModes) {
    public static final IoMemory DEFAULT = new IoMemory(List.of());
    public static final Codec<IoMemory> CODEC =
            Codec.STRING.listOf().xmap(IoMemory::new, IoMemory::sideModes);
    public static final StreamCodec<io.netty.buffer.ByteBuf, IoMemory> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(IoMemory::new, IoMemory::sideModes);

    public IoMemory {
        sideModes = List.copyOf(sideModes);
    }

    public static IoMemory of(SideMode[] modes) {
        return new IoMemory(java.util.Arrays.stream(modes).map(SideMode::name).toList());
    }

    public SideMode[] modesOrDefault(SideMode[] defaults) {
        if (sideModes.size() != 6) {
            return defaults.clone();
        }
        SideMode[] result = new SideMode[6];
        for (int index = 0; index < result.length; index++) {
            try {
                result[index] = SideMode.valueOf(sideModes.get(index));
            } catch (IllegalArgumentException exception) {
                return defaults.clone();
            }
        }
        return result;
    }
}
