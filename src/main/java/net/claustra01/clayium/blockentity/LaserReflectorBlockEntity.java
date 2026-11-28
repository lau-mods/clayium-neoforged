package net.claustra01.clayium.blockentity;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.ClayLaserManager;
import net.claustra01.clayium.laser.IClayLaserMachine;
import net.claustra01.clayium.laser.IClayLaserManager;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Laser Reflector (Tier 7): receives Clay Lasers from any side and re-emits the merged beam toward {@code FACING}
 * with 1-tick delay to avoid same-tick infinite reflection loops.
 */
public class LaserReflectorBlockEntity extends ClayContainerBlockEntity implements IClayLaserMachine, IClayLaserManager {
    private record TimedLaser(ClayLaser laser, long gameTime) {}

    private final ClayLaserManager laserManager = new ClayLaserManager();
    private final List<TimedLaser> incomingLasers = new ArrayList<>();

    public LaserReflectorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.LASER_REFLECTOR.get(), pos, state, 0);
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.autoInsert = false;
        this.autoExtract = false;
        this.clayEnergySlot = -1;
        this.slotsDrop = new int[0];
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
    public int getItemUseMode(ItemStack stack, Player player) {
        int mode = super.getItemUseMode(stack, player);
        if (mode == 2) {
            return 20;
        }
        return mode == 20 ? 20 : -1;
    }

    @Override
    public boolean irradiateClayLaser(ClayLaser laser, Direction incomingSide) {
        if (laser == null || this.level == null) {
            return false;
        }
        this.incomingLasers.add(new TimedLaser(laser, this.level.getGameTime()));
        return true;
    }

    @Override
    public void serverTick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        long now = this.level.getGameTime();
        List<ClayLaser> ready = new ArrayList<>();
        List<TimedLaser> nextTick = new ArrayList<>();
        for (TimedLaser tl : this.incomingLasers) {
            if (tl.gameTime() != now) {
                ready.add(tl.laser());
            } else {
                nextTick.add(tl);
            }
        }
        this.incomingLasers.clear();
        this.incomingLasers.addAll(nextTick);

        ClayLaser merged = null;
        if (!ready.isEmpty()) {
            merged = ClayLaser.mergeClayLasers(ready.toArray(ClayLaser[]::new));
            merged.age++;
        }
        boolean changed = this.laserManager.update(this.level, this.worldPosition, getFrontDirection(), merged, merged != null);
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ClayLaserManager", Tag.TAG_COMPOUND)) {
            this.laserManager.load(tag.getCompound("ClayLaserManager"));
        }
    }
}
