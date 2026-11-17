package net.claustra01.clayium.machine.waterwheel;

import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.registry.WaterWheelRegistration;
import net.claustra01.clayium.util.WaterDetection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WaterWheelBlockEntity extends BlockEntity {
    public static final long PROGRESS_LIMIT = 20_000L;

    private long progress;
    private int efficiency;
    private int cachedFlowingWater;
    private long nextWaterScan;

    public WaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(WaterWheelRegistration.WATER_WHEEL_BLOCK_ENTITY.get(), pos, state);
        efficiency = initialEfficiency(tier());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WaterWheelBlockEntity waterWheel) {
        waterWheel.runGenerationTick(level.random);
    }

    public void runGenerationTick(RandomSource random) {
        if (level == null) {
            return;
        }
        int flowingWater = flowingWaterCount();
        if (random.nextInt(40) >= flowingWater) {
            return;
        }
        progress += efficiency;
        updateActiveState();
        if (progress < PROGRESS_LIMIT) {
            setChanged();
            return;
        }
        progress -= PROGRESS_LIMIT;
        if (random.nextInt(5) == 0 && efficiency > 0) {
            efficiency--;
        }
        emitEnergy();
        updateActiveState();
        setChanged();
    }

    public int flowingWaterCount() {
        if (level == null) {
            return 0;
        }
        long gameTime = level.getGameTime();
        if (gameTime >= nextWaterScan) {
            cachedFlowingWater = WaterDetection.countFlowingWater(level, worldPosition);
            nextWaterScan = gameTime + 20;
        }
        return cachedFlowingWater;
    }

    public void invalidateWaterCache() {
        nextWaterScan = Long.MIN_VALUE;
    }

    private void emitEnergy() {
        long amount = tierPower(8);
        long limit = amount * 5L;
        for (Direction direction : Direction.values()) {
            if (!(level.getBlockEntity(worldPosition.relative(direction)) instanceof ClayMachineBlockEntity machine)
                    || machine.tier() < 2 || machine.tier() > 3) {
                continue;
            }
            long stored = machine.energyStorage().stored().internalValue();
            if (stored < limit) {
                machine.receiveClayEnergy(ClayEnergy.ofInternal(Math.min(amount, limit - stored)));
            }
        }
    }

    private void updateActiveState() {
        BlockState state = getBlockState();
        boolean active = progress * 10L / PROGRESS_LIMIT / 2L != 0L;
        if (state.getValue(WaterWheelBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(WaterWheelBlock.ACTIVE, active), 3);
        }
    }

    public int tier() {
        return ((WaterWheelBlock)getBlockState().getBlock()).tier();
    }

    public long progress() {
        return progress;
    }

    public int efficiency() {
        return efficiency;
    }

    public long generationAmount() {
        return tierPower(8);
    }

    public long generationLimit() {
        return generationAmount() * 5L;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("progress", progress);
        tag.putInt("efficiency", efficiency);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getLong("progress");
        efficiency = tag.getInt("efficiency");
    }

    private int initialEfficiency(int tier) {
        return 1_000 * (int)Math.pow(Math.max(tier, 1), 3);
    }

    private long tierPower(int exponent) {
        return (long)Math.pow(Math.max(tier(), 1), exponent);
    }

}
