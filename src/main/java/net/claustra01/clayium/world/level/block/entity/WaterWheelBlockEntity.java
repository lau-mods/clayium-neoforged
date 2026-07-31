/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.level.block.entity;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.WaterWheelBlock;
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
    private static final long ENERGY_PER_GENERATION = 1;
    private static final long SUPPLY_STOP_THRESHOLD = 5;
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
        boolean active = wheel.surroundingFlowingWater > 0;
        if (state.hasProperty(WaterWheelBlock.ACTIVE)
                && state.getValue(WaterWheelBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(WaterWheelBlock.ACTIVE, active), 3);
        }
        if (wheel.surroundingFlowingWater <= 0) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockEntity adjacent = level.getBlockEntity(pos.relative(direction));
            if (adjacent instanceof MachineBlockEntity machine
                    && machine.clayEnergyStored() < SUPPLY_STOP_THRESHOLD) {
                machine.receiveClayEnergy(ENERGY_PER_GENERATION, false);
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
        return surroundingFlowingWater > 0 ? ENERGY_PER_GENERATION : 0;
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
