package net.claustra01.clayium.tool.advanced;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;

public record AdvancedToolPattern(List<BlockPos> offsets) {
    public static final AdvancedToolPattern EMPTY = new AdvancedToolPattern(List.of());
    public static final Codec<AdvancedToolPattern> CODEC = BlockPos.CODEC.listOf()
            .xmap(AdvancedToolPattern::new, AdvancedToolPattern::offsets);

    public AdvancedToolPattern {
        offsets = List.copyOf(offsets);
    }
}
