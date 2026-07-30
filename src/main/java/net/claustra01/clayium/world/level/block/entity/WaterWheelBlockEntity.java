/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.level.block.entity;

import net.claustra01.clayium.energy.ClayEnergyReceiver;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Deterministic bounded water scan and adjacent CE emission. */
public final class WaterWheelBlockEntity extends BlockEntity {
    private static final int GENERATION_INTERVAL = 20;
    private int tickCounter;
    private int surroundingFlowingWater;

    public WaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.WATER_WHEEL_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WaterWheelBlockEntity wheel) {
        wheel.tickCounter++;
        if (wheel.tickCounter < GENERATION_INTERVAL) {
            return;
        }
        wheel.tickCounter = 0;
        wheel.surroundingFlowingWater = wheel.countFlowingWater();
        if (wheel.surroundingFlowingWater <= 0) {
            return;
        }
        long available = wheel.surroundingFlowingWater;
        for (Direction direction : Direction.values()) {
            if (available <= 0) {
                break;
            }
            BlockEntity adjacent = level.getBlockEntity(pos.relative(direction));
            if (adjacent instanceof ClayEnergyReceiver receiver) {
                available -= receiver.receiveClayEnergy(available, false);
            }
        }
        wheel.setChanged();
    }

    private int countFlowingWater() {
        if (level == null) {
            return 0;
        }
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                worldPosition.offset(-1, -1, -1),
                worldPosition.offset(1, 1, 1))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof LiquidBlock
                    && state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)
                    && !state.getFluidState().isSource()) {
                count++;
            }
        }
        return count;
    }

    public int surroundingFlowingWater() {
        return surroundingFlowingWater;
    }

    public long generatedClayEnergyPerCycle() {
        return surroundingFlowingWater;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tickCounter = Math.max(0, Math.min(GENERATION_INTERVAL - 1, tag.getInt("TickCounter")));
        surroundingFlowingWater = Math.max(0, Math.min(27, tag.getInt("SurroundingWater")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TickCounter", tickCounter);
        tag.putInt("SurroundingWater", surroundingFlowingWater);
    }
}
