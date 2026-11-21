package net.claustra01.clayium.machine.iface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record SynchronizedTarget(ResourceKey<Level> dimension, BlockPos pos) {
    public static final Codec<SynchronizedTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(SynchronizedTarget::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(SynchronizedTarget::pos))
            .apply(instance, SynchronizedTarget::new));

    public static SynchronizedTarget at(Level level, BlockPos pos) {
        return new SynchronizedTarget(level.dimension(), pos.immutable());
    }
}
