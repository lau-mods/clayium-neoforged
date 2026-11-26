package net.claustra01.clayium.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class MultiblockStructureNotifier {
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof Level level) {
            componentChanged(level, event.getPos());
        }
    }

    public static void componentChanged(Level level, BlockPos changedPos) {
        if (level.isClientSide) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    cursor.setWithOffset(changedPos, x, y, z);
                    if (level.getBlockState(cursor).getBlock() instanceof MultiblockControllerBlock
                            && level.getBlockEntity(cursor) instanceof MultiblockControllerBlockEntity controller) {
                        controller.markStructureDirty();
                    }
                }
            }
        }
    }

    private MultiblockStructureNotifier() {
    }
}
