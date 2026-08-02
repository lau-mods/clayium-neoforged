/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.laser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;

/** Bounded, deterministic laser traversal. */
public final class ClayLaserPath {
    public static final int MAX_LENGTH = 32;
    private ClayLaserPath() {}

    public static Result irradiate(ServerLevel level, BlockPos origin, Direction direction, ClayLaser laser) {
        for (int distance = 1; distance < MAX_LENGTH; distance++) {
            BlockPos target = origin.relative(direction, distance);
            if (!level.hasChunkAt(target)) return new Result(distance, false);
            var state = level.getBlockState(target);
            if (state.isAir() || level.getFluidState(target).is(FluidTags.WATER)) continue;
            if (level.getBlockEntity(target) instanceof ClayLaserReceiver receiver) {
                receiver.receiveClayLaser(laser, direction.getOpposite());
            }
            return new Result(distance, true);
        }
        return new Result(MAX_LENGTH, false);
    }

    public record Result(int length, boolean hasTarget) {}
}
