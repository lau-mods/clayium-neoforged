package net.claustra01.clayium.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/** Face modes copied by the direction memory card. */
public record BufferConfiguration(List<Integer> faces) {
    public static final Codec<BufferConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.listOf(6, 6).fieldOf("faces").forGetter(BufferConfiguration::faces))
            .apply(instance, BufferConfiguration::new));

    public BufferConfiguration {
        faces = List.copyOf(faces);
    }
}
