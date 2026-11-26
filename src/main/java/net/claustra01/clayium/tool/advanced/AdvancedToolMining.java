package net.claustra01.clayium.tool.advanced;

import net.claustra01.clayium.world.operation.BlockOperationService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class AdvancedToolMining {
    private static final ThreadLocal<Boolean> BREAKING_AREA = ThreadLocal.withInitial(() -> false);

    public static void breakAdditionalBlocks(
            ServerLevel level, ServerPlayer player, ItemStack tool, BlockPos origin, Direction face) {
        if (BREAKING_AREA.get()) {
            return;
        }
        BREAKING_AREA.set(true);
        try {
            for (BlockPos target : AdvancedToolBehavior.targets(tool, origin, face, player.getDirection())) {
                if (target.equals(origin)) {
                    continue;
                }
                if (tool.isEmpty()) {
                    break;
                }
                var state = level.getBlockState(target);
                if (!tool.isCorrectToolForDrops(state) || tool.getDestroySpeed(state) <= 6.0F) {
                    continue;
                }
                BlockOperationService.breakBlock(
                        level,
                        player.blockPosition(),
                        target,
                        player.getUUID(),
                        tool,
                        ignored -> { },
                        ignored -> true,
                        drops -> drops.forEach(drop -> Block.popResource(level, target, drop)));
            }
        } finally {
            BREAKING_AREA.remove();
        }
    }

    public static Direction hitFace(ServerPlayer player, BlockPos position) {
        HitResult hit = player.pick(player.blockInteractionRange() + 1.0D, 0.0F, false);
        if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(position)) {
            return blockHit.getDirection();
        }
        var delta = player.getEyePosition().subtract(position.getCenter());
        return Direction.getNearest((float) delta.x, (float) delta.y, (float) delta.z);
    }

    private AdvancedToolMining() {
    }
}
