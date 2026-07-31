/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Portable six-face I/O configuration used by the Clay Configurator memory mode. */
public record IoMemory(
        List<Integer> insertionRoutes,
        List<Integer> extractionRoutes,
        boolean pipe,
        String facing) {
    public static final IoMemory DEFAULT = new IoMemory(List.of(), List.of(), false, "");
    public static final Codec<IoMemory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.listOf().fieldOf("insertion_routes").forGetter(IoMemory::insertionRoutes),
                    Codec.INT.listOf().fieldOf("extraction_routes").forGetter(IoMemory::extractionRoutes),
                    Codec.BOOL.optionalFieldOf("pipe", false).forGetter(IoMemory::pipe),
                    Codec.STRING.optionalFieldOf("facing", "").forGetter(IoMemory::facing))
            .apply(instance, IoMemory::new));
    public static final StreamCodec<io.netty.buffer.ByteBuf, IoMemory> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
            IoMemory::insertionRoutes,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
            IoMemory::extractionRoutes,
            ByteBufCodecs.BOOL,
            IoMemory::pipe,
            ByteBufCodecs.STRING_UTF8,
            IoMemory::facing,
            IoMemory::new);

    public IoMemory {
        insertionRoutes = List.copyOf(insertionRoutes);
        extractionRoutes = List.copyOf(extractionRoutes);
    }

    public static IoMemory of(int[] insertionRoutes, int[] extractionRoutes, boolean pipe, String facing) {
        return new IoMemory(
                java.util.Arrays.stream(insertionRoutes).boxed().toList(),
                java.util.Arrays.stream(extractionRoutes).boxed().toList(),
                pipe,
                facing);
    }

    public int[] insertionRoutesOrDefault(int[] defaults) {
        return routesOrDefault(insertionRoutes, defaults);
    }

    public int[] extractionRoutesOrDefault(int[] defaults) {
        return routesOrDefault(extractionRoutes, defaults);
    }

    private static int[] routesOrDefault(List<Integer> routes, int[] defaults) {
        return routes.size() == 6 ? routes.stream().mapToInt(Integer::intValue).toArray() : defaults.clone();
    }
}
