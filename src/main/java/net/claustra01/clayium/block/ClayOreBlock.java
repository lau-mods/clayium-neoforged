package net.claustra01.clayium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Clay Ore / Dense Clay Ore / Large Dense Clay Ore.
 */
public class ClayOreBlock extends TieredBlock {
    private final int variantIndex;

    public ClayOreBlock(Properties properties, int variantIndex, String textureName) {
        super(properties, -1, textureName);
        this.variantIndex = variantIndex;
    }

    public int getVariantIndex() {
        return this.variantIndex;
    }

    @Override
    public int getExpDrop(
            BlockState state,
            LevelAccessor level,
            BlockPos pos,
            @Nullable BlockEntity blockEntity,
            @Nullable Entity breaker,
            ItemStack tool) {
        return this.variantIndex == 0 ? UniformInt.of(0, 1).sample(level.getRandom()) : 0;
    }
}
