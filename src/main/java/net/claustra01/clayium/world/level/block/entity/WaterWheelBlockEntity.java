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
    public static final int GENERATION_RATE_DENOMINATOR = 40;
    private static final int WATER_SCAN_INTERVAL = 20;
    private static final int GENERATION_PROGRESS_REQUIRED = 20_000;
    private static final int GENERATION_CHANCE_DENOMINATOR = 40;
    private int tickCounter;
    private int generationChanceProgress;
    private int generationProgress;
    private int surroundingFlowingWater;

    public WaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.WATER_WHEEL_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WaterWheelBlockEntity wheel) {
        wheel.tickCounter++;
        if (wheel.tickCounter >= WATER_SCAN_INTERVAL) {
            wheel.tickCounter = 0;
            wheel.surroundingFlowingWater = wheel.countFlowingWater();
            boolean active = wheel.surroundingFlowingWater > 0;
            if (state.hasProperty(WaterWheelBlock.ACTIVE)
                    && state.getValue(WaterWheelBlock.ACTIVE) != active) {
                level.setBlock(pos, state.setValue(WaterWheelBlock.ACTIVE, active), 3);
            }
            wheel.setChanged();
        }
        if (wheel.surroundingFlowingWater <= 0) {
            return;
        }
        wheel.generationChanceProgress += wheel.surroundingFlowingWater;
        if (wheel.generationChanceProgress < GENERATION_CHANCE_DENOMINATOR) {
            return;
        }
        wheel.generationChanceProgress -= GENERATION_CHANCE_DENOMINATOR;
        int tier = wheel.waterWheelTier();
        int progressPerEvent = 1_000 * (int) Math.pow(tier, 6);
        wheel.generationProgress += progressPerEvent;
        if (wheel.generationProgress < GENERATION_PROGRESS_REQUIRED) {
            wheel.setChanged();
            return;
        }
        wheel.generationProgress = Math.min(
                GENERATION_PROGRESS_REQUIRED - 1,
                wheel.generationProgress - GENERATION_PROGRESS_REQUIRED);
        long energyPerGeneration = (long) Math.pow(tier, 8);
        long supplyStopThreshold = 5 * energyPerGeneration;
        for (Direction direction : Direction.values()) {
            BlockEntity adjacent = level.getBlockEntity(pos.relative(direction));
            if (adjacent instanceof MachineBlockEntity machine
                    && (machine.machineTierIndex() == 2 || machine.machineTierIndex() == 3)
                    && machine.clayEnergyStored() < supplyStopThreshold) {
                machine.receiveClayEnergy(energyPerGeneration, false);
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

    public long generationNumeratorPerSecond() {
        int tier = waterWheelTier();
        long energyPerGeneration = (long) Math.pow(tier, 8);
        int eventsRequired = tier == 1 ? 20 : 1;
        return (long) surroundingFlowingWater * 20 * energyPerGeneration / eventsRequired;
    }

    private int waterWheelTier() {
        return getBlockState().getBlock() instanceof WaterWheelBlock wheel
                ? wheel.tier().progressionIndex()
                : 1;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tickCounter = Math.max(0, Math.min(WATER_SCAN_INTERVAL - 1, tag.getInt("TickCounter")));
        generationChanceProgress = Math.max(
                0, Math.min(GENERATION_CHANCE_DENOMINATOR - 1, tag.getInt("GenerationChanceProgress")));
        generationProgress =
                Math.max(0, Math.min(GENERATION_PROGRESS_REQUIRED - 1, tag.getInt("GenerationProgress")));
        surroundingFlowingWater = Math.max(0, Math.min(27, tag.getInt("SurroundingWater")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TickCounter", tickCounter);
        tag.putInt("GenerationChanceProgress", generationChanceProgress);
        tag.putInt("GenerationProgress", generationProgress);
        tag.putInt("SurroundingWater", surroundingFlowingWater);
    }
}
