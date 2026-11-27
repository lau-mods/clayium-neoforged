package net.claustra01.clayium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;

/**
 * Marks a Clayium block that exposes an intrinsic progression tier (-1 for untiered).
 */
public interface ITieredBlock {
    int getTier();

    default int getTier(ItemStack stack) {
        return getTier();
    }

    default int getTier(BlockGetter level, BlockPos pos) {
        return getTier();
    }
}
