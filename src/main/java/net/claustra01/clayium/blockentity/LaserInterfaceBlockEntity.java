package net.claustra01.clayium.blockentity;

import javax.annotation.Nullable;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.IClayLaserMachine;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Laser Interface (T7..T13): synchronizes with a core {@link IClayLaserMachine} (e.g. {@link ReactorBlockEntity})
 * and forwards irradiated Clay Lasers to the core.
 */
public class LaserInterfaceBlockEntity extends TieredClayContainerBlockEntity
        implements ISynchronizedInterface, IClayLaserMachine {
    protected int coreOffsetX = 0;
    protected int coreOffsetY = 0;
    protected int coreOffsetZ = 0;
    protected boolean synced = false;
    public boolean acceptCoordChanger = false;
    @Nullable
    protected ResourceKey<Level> coreDimension = null;

    public LaserInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.LASER_INTERFACE.get(), pos, state, 1);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
        this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.slotsDrop = new int[]{0};
        this.autoInsert = false;
        this.autoExtract = false;
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    @Override
    public boolean acceptInterfaceSync() {
        return false;
    }

    @Override
    public boolean setCoreBlockOffset(int dx, int dy, int dz) {
        markForSync();
        this.coreOffsetX = dx;
        this.coreOffsetY = dy;
        this.coreOffsetZ = dz;
        return syncCoreBlock();
    }

    @Override
    public boolean setCoreBlockDimension(ResourceKey<Level> dimension) {
        if (this.level != null && this.level.dimension().equals(dimension)) {
            this.coreDimension = null;
            return true;
        }
        this.coreDimension = dimension;
        return getCoreLevel() != null;
    }

    @Override
    public BlockPos getCoreBlockOffset() {
        return new BlockPos(this.coreOffsetX, this.coreOffsetY, this.coreOffsetZ);
    }

    @Override
    public boolean isSynced() {
        return this.synced;
    }

    @Override
    public boolean acceptsCoordChanger() {
        return this.acceptCoordChanger;
    }

    @Nullable
    protected Level getCoreLevel() {
        if (this.level == null) {
            return null;
        }
        if (this.coreDimension == null || this.level.dimension().equals(this.coreDimension)) {
            return this.level;
        }
        if (this.level.getServer() != null) {
            return this.level.getServer().getLevel(this.coreDimension);
        }
        return null;
    }

    @Nullable
    public IClayLaserMachine getCoreLaserMachine() {
        Level coreLevel = getCoreLevel();
        if (coreLevel == null
                || (this.coreOffsetX == 0 && this.coreOffsetY == 0 && this.coreOffsetZ == 0 && this.coreDimension == null)) {
            return null;
        }
        BlockPos corePos = this.worldPosition.offset(this.coreOffsetX, this.coreOffsetY, this.coreOffsetZ);
        BlockEntity be = coreLevel.getBlockEntity(corePos);
        return be instanceof IClayLaserMachine machine && be != this ? machine : null;
    }

    public boolean syncCoreBlock() {
        if (getCoreLaserMachine() == null) {
            this.coreOffsetX = 0;
            this.coreOffsetY = 0;
            this.coreOffsetZ = 0;
            this.synced = false;
            return false;
        }
        this.synced = true;
        return true;
    }

    @Override
    public void serverTick() {
        syncCoreBlock();
        super.serverTick();
    }

    @Override
    public boolean irradiateClayLaser(ClayLaser laser, Direction incomingSide) {
        IClayLaserMachine core = getCoreLaserMachine();
        if (core != null) {
            return core.irradiateClayLaser(laser, incomingSide);
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        syncCoreBlock();
        super.saveAdditional(tag, registries);
        tag.putInt("CoreBlockX", this.coreOffsetX);
        tag.putInt("CoreBlockY", this.coreOffsetY);
        tag.putInt("CoreBlockZ", this.coreOffsetZ);
        tag.putBoolean("AcceptCoordChanger", this.acceptCoordChanger);
        tag.putBoolean("InterDimensional", this.coreDimension != null);
        if (this.coreDimension != null) {
            tag.putString("CoreBlockDimension", this.coreDimension.location().toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.coreOffsetX = tag.getInt("CoreBlockX");
        this.coreOffsetY = tag.getInt("CoreBlockY");
        this.coreOffsetZ = tag.getInt("CoreBlockZ");
        this.acceptCoordChanger = tag.getBoolean("AcceptCoordChanger");
        if (tag.contains("CoreBlockDimension")) {
            ResourceLocation dimId = ResourceLocation.tryParse(tag.getString("CoreBlockDimension"));
            if (dimId != null) {
                this.coreDimension = ResourceKey.create(Registries.DIMENSION, dimId);
            }
        } else {
            this.coreDimension = null;
        }
        syncCoreBlock();
    }
}
