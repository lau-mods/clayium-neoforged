package net.claustra01.clayium.pan;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Conductor lookup helpers for PAN Cables and PAN Core BFS graph traversal.
 */
public final class UtilPAN {
    private UtilPAN() {}

    public static boolean isPANConductor(@Nullable BlockGetter level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        if (level instanceof Level world) {
            if (world.isOutsideBuildHeight(pos) || !world.hasChunkAt(pos)) {
                return false;
            }
        } else if (pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        return isPANConductor(level.getBlockState(pos).getBlock());
    }

    public static boolean isPANConductor(@Nullable Block block) {
        return block instanceof IPANConductor;
    }
}
