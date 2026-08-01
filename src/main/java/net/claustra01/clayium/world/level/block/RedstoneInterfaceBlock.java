/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.level.block;

import net.claustra01.clayium.logistics.LogisticsKind;
import net.claustra01.clayium.world.level.block.entity.LogisticsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/** Tiered redstone interface backed by the shared configurable item device. */
public final class RedstoneInterfaceBlock extends LogisticsBlock {
    public RedstoneInterfaceBlock(Properties properties, int tier) {
        super(properties, LogisticsKind.REDSTONE_INTERFACE, tier);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof LogisticsBlockEntity device
                ? device.redstoneSignal()
                : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }
}
