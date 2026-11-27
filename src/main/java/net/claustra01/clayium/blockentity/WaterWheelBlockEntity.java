package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.util.UtilTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Water Wheel (Tier 1 Clay, Tier 2 Dense Clay): extracts rotational energy from flowing water in a 3x3x3 region
 * and emits Clay Energy pulses directly into adjacent Tier 2/3 {@link AbstractClayMachineBlockEntity}s.
 */
public class WaterWheelBlockEntity extends TieredClayContainerBlockEntity {
    public static final int PROGRESS_MAX = 20000;
    private static final RandomSource FALLBACK_RANDOM = RandomSource.create();

    public int progress = 0;
    public int progressEfficiency = 1000;

    public WaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.WATER_WHEEL.get(), pos, state, 1);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsExtract.add(new int[]{0});
        this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.slotsDrop = new int[]{0};
        this.autoInsert = false;
        this.autoExtract = false;
    }

    @Override
    protected void initParamsByTier(int tier) {
        this.progressEfficiency = (int) (1000.0 * Math.pow(Math.max(tier, 1), 3.0));
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    public int getProgressIcon() {
        return this.progress * 10 / PROGRESS_MAX / 2 == 0 ? 0 : 1;
    }

    public double getProgressRatio() {
        return (double) this.progress / (double) PROGRESS_MAX;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        tickWaterWheel(this.level.random);
    }

    public void tickWaterWheel(RandomSource random) {
        if (this.level == null) {
            return;
        }
        RandomSource rng = random != null ? random : FALLBACK_RANDOM;
        int waterCount = countSurroundingFlowingWater();
        if (waterCount > 0 && rng.nextInt(40) < waterCount) {
            int factor = Math.max(getTier(), 1);
            this.progress += (int) ((double) this.progressEfficiency * Math.pow(factor, 3.0));
            markForSync();
            if (this.progress >= PROGRESS_MAX) {
                this.progress -= PROGRESS_MAX;
                if (this.progressEfficiency > 0 && rng.nextInt(5) == 0) {
                    this.progressEfficiency--;
                }
                emitEnergy();
            }
        }
        syncWheelActiveState();
    }

    private void syncWheelActiveState() {
        if (this.level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(ClayContainerBlock.ACTIVE)) {
            boolean shouldBeActive = getProgressIcon() != 0;
            if (state.getValue(ClayContainerBlock.ACTIVE) != shouldBeActive) {
                this.level.setBlock(this.worldPosition, state.setValue(ClayContainerBlock.ACTIVE, shouldBeActive), Block.UPDATE_CLIENTS);
            }
        }
    }

    public void emitEnergy() {
        if (this.level == null) {
            return;
        }
        int factor = Math.max(getTier(), 1);
        double energyPerPulse = Math.pow(factor, 8.0);
        double maxStoredForWheel = 5.0 * energyPerPulse;
        for (Direction dir : Direction.values()) {
            BlockEntity be = this.level.getBlockEntity(this.worldPosition.relative(dir));
            if (be instanceof AbstractClayMachineBlockEntity machine
                    && UtilTier.acceptWaterWheel(machine.getTier())
                    && (double) machine.getClayEnergyStorage().getStored() < maxStoredForWheel) {
                machine.getClayEnergyStorage().addEnergy((long) energyPerPulse);
                machine.markForSync();
            }
        }
    }

    public int countSurroundingFlowingWater() {
        if (this.level == null) {
            return 0;
        }
        int count = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    cursor.set(this.worldPosition.getX() + dx, this.worldPosition.getY() + dy, this.worldPosition.getZ() + dz);
                    FluidState fluid = this.level.getFluidState(cursor);
                    if (fluid.is(FluidTags.WATER) && !fluid.isSource()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", this.progress);
        tag.putInt("ProgressEfficiency", this.progressEfficiency);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getInt("Progress");
        if (tag.contains("ProgressEfficiency")) {
            this.progressEfficiency = tag.getInt("ProgressEfficiency");
        }
    }
}
