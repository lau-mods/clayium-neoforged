package net.claustra01.clayium.blockentity;

import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.ClayLaserManager;
import net.claustra01.clayium.laser.IClayLaserManager;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Energy Laser (T7 Blue, T8 Green, T9 Red, T10 Antimatter): consumes CE per tick when not halted by redstone
 * and emits a directional {@link ClayLaser} via {@link ClayLaserManager}.
 */
public class ClayEnergyLaserBlockEntity extends TieredClayContainerBlockEntity implements IClayLaserManager {
    private final ClayLaserManager laserManager = new ClayLaserManager();
    public long consumingEnergy;
    @Nullable
    public ClayLaser laser;
    public boolean powered = false;

    public ClayEnergyLaserBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CLAY_ENERGY_LASER.get(), pos, state, 1);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
        this.clayEnergySlot = 0;
        this.listSlotsInsert.add(new int[]{0});
        this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, 0, ROUTE_DISABLED, ROUTE_DISABLED};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.slotsDrop = new int[]{0};
        this.autoInsert = false;
        this.autoExtract = true;
        this.maxAutoExtractDefault = 1;
        this.autoExtractInterval = 8;
    }

    @Override
    protected void initParamsByTier(int tier) {
        switch (tier) {
            case 7 -> {
                this.consumingEnergy = 40_000L;
                this.laser = new ClayLaser(0, 1, 0, 0);
            }
            case 8 -> {
                this.consumingEnergy = 400_000L;
                this.laser = new ClayLaser(0, 0, 1, 0);
            }
            case 9 -> {
                this.consumingEnergy = 4_000_000L;
                this.laser = new ClayLaser(0, 0, 0, 1);
            }
            case 10 -> {
                this.consumingEnergy = 40_000_000L;
                this.laser = new ClayLaser(0, 3, 3, 3);
            }
            default -> {
                this.consumingEnergy = 40_000L;
                this.laser = new ClayLaser(0, 1, 0, 0);
            }
        }
    }

    public void updateRedstoneState() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        boolean hasSignal = this.level.hasNeighborSignal(this.worldPosition);
        this.powered = ClayiumConfig.INVERT_CLAY_LASER_RS_CONDITION.get() ? !hasSignal : hasSignal;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        updateRedstoneState();
        boolean emit = !this.powered && this.laser != null && consumeClayEnergy(this.consumingEnergy);
        boolean changed = this.laserManager.update(this.level, this.worldPosition, getFrontDirection(), this.laser, emit);
        if (changed) {
            markForInstantSync();
        }
    }

    public ClayLaserManager getLaserManager() {
        return this.laserManager;
    }

    @Override
    public boolean isIrradiating() {
        return this.laserManager.isIrradiating();
    }

    @Override
    public int getLaserLength() {
        return this.laserManager.getLaserLength();
    }

    @Override
    public Direction getLaserDirection() {
        return this.laserManager.getDirection();
    }

    @Nullable
    @Override
    public ClayLaser getIrradiatingClayLaser() {
        return this.laserManager.getClayLaser();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag managerTag = new CompoundTag();
        this.laserManager.save(managerTag);
        tag.put("ClayLaserManager", managerTag);
        tag.putBoolean("Powered", this.powered);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ClayLaserManager", Tag.TAG_COMPOUND)) {
            this.laserManager.load(tag.getCompound("ClayLaserManager"));
        }
        this.powered = tag.getBoolean("Powered");
    }
}
