package net.claustra01.clayium.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import java.util.Objects;

public record BufferConfiguration(
        List<Integer> faces,
        BufferBlockEntity.RedstoneCondition redstoneCondition,
        int transferLimit,
        ItemStack filter) {
    public static final Codec<BufferConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.listOf(6, 6).fieldOf("faces").forGetter(BufferConfiguration::faces),
            BufferBlockEntity.RedstoneCondition.CODEC
                    .fieldOf("redstone_condition").forGetter(BufferConfiguration::redstoneCondition),
            Codec.intRange(0, 6_400).fieldOf("transfer_limit").forGetter(BufferConfiguration::transferLimit),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("filter", ItemStack.EMPTY).forGetter(BufferConfiguration::filter))
            .apply(instance, BufferConfiguration::new));

    public BufferConfiguration {
        faces = List.copyOf(faces);
        filter = filter.copyWithCount(filter.isEmpty() ? 0 : 1);
    }
    @Override
    public boolean equals(Object other) {
        return other instanceof BufferConfiguration configuration && faces.equals(configuration.faces)
                && redstoneCondition == configuration.redstoneCondition && transferLimit == configuration.transferLimit
                && ItemStack.matches(filter, configuration.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faces, redstoneCondition, transferLimit, ItemStack.hashItemAndComponents(filter));
    }
}
