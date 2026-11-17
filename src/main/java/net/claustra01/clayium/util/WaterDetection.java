package net.claustra01.clayium.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;

public final class WaterDetection {
    public static boolean hasWater(BlockGetter level, BlockPos pos) {
        return isWater(level.getFluidState(pos));
    }

    public static boolean isWaterSource(BlockGetter level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        return isWater(fluidState) && fluidState.isSource();
    }

    public static boolean isFlowingWater(BlockGetter level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        return isWater(fluidState) && !fluidState.isSource();
    }

    public static int countFlowingWater(BlockGetter level, BlockPos center) {
        int flowingWater = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (isFlowingWater(level, pos)) {
                flowingWater++;
            }
        }
        return flowingWater;
    }

    private static boolean isWater(FluidState fluidState) {
        return fluidState.is(FluidTags.WATER);
    }

    private WaterDetection() {
    }
}
