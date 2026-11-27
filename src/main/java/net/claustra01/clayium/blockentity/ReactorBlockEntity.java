package net.claustra01.clayium.blockentity;

import javax.annotation.Nullable;
import net.claustra01.clayium.block.MachineHullBlock;
import net.claustra01.clayium.block.UpgradeBlock;
import net.claustra01.clayium.block.UpgradeBlock.UpgradeKind;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.IClayLaserMachine;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Reactor (T7 multiblock, 3x3x3 volume around front controller):
 * requires 26 blocks at tier > 6 (tier >= 7) with a {@link LaserInterfaceBlockEntity} at {@code (0, +1, +1)},
 * and adds {@link ClayLaser#getEnergy()} per tick when irradiated.
 */
public class ReactorBlockEntity extends BlastFurnaceBlockEntity implements IClayLaserMachine {
    @Nullable
    public ClayLaser irradiatedLaser;

    public ReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.REACTOR.get(), pos, state);
        ensureTierInitialized();
    }

    @Override
    protected void initParamsByTier(int tier) {
        this.recipeCategoryId = "reactor";
    }

    @Override
    public boolean checkStructureConstructed() {
        int sum = 0;
        int num = 0;
        boolean valid = true;
        BlockPos corePos = getRelativePos(0, 0, 0);

        for (int yy = -1; yy <= 1; yy++) {
            for (int xx = -1; xx <= 1; xx++) {
                for (int zz = 0; zz <= 2; zz++) {
                    if (xx == 0 && yy == 0 && zz == 0) {
                        continue;
                    }
                    num++;
                    int atier = getStructureBlockTier(xx, yy, zz);
                    if (atier <= 6) {
                        valid = false;
                    }
                    if (atier >= 0) {
                        sum += (int) Math.pow(2.0, 16 - atier);
                    } else {
                        sum += (int) Math.pow(2.0, 17);
                    }
                    BlockEntity be = getRelativeBlockEntity(xx, yy, zz);
                    if (be instanceof ClayInterfaceBlockEntity || be instanceof RedstoneInterfaceBlockEntity) {
                        BlockPos ifacePos = getRelativePos(xx, yy, zz);
                        ((ISynchronizedInterface) be).setCoreBlockOffset(
                                corePos.getX() - ifacePos.getX(),
                                corePos.getY() - ifacePos.getY(),
                                corePos.getZ() - ifacePos.getZ()
                        );
                    }

                    if (yy == 1 && xx == 0 && zz == 1) {
                        if (be instanceof LaserInterfaceBlockEntity laserIface) {
                            BlockPos ifacePos = getRelativePos(xx, yy, zz);
                            laserIface.setCoreBlockOffset(
                                    corePos.getX() - ifacePos.getX(),
                                    corePos.getY() - ifacePos.getY(),
                                    corePos.getZ() - ifacePos.getZ()
                            );
                        } else {
                            valid = false;
                        }
                    } else if (be instanceof LaserInterfaceBlockEntity) {
                        valid = false;
                    }
                }
            }
        }

        if (num > 0 && sum > 0) {
            this.recipeTier = Math.max((int) (16.0 - Math.floor(Math.log((double) (sum / num)) / Math.log(2.0) + 0.5)), 0);
        } else {
            this.recipeTier = 0;
        }
        return valid;
    }

    @Override
    protected int getStructureBlockTier(int xx, int yy, int zz) {
        Block block = getRelativeBlock(xx, yy, zz);
        if (isStandardMachineHull(block)) {
            return ((MachineHullBlock) block).getTier();
        }
        if (block instanceof UpgradeBlock upgrade && upgrade.getKind() == UpgradeKind.OVERCLOCKER) {
            return upgrade.getTier();
        }
        BlockEntity be = getRelativeBlockEntity(xx, yy, zz);
        if (be instanceof ClayInterfaceBlockEntity iface) {
            return iface.getTier();
        }
        if (be instanceof RedstoneInterfaceBlockEntity) {
            return getRelativeBlockTier(xx, yy, zz);
        }
        if (be instanceof LaserInterfaceBlockEntity laserIface) {
            return laserIface.getTier();
        }
        return -1;
    }

    @Override
    protected void onCraftingStep() {
        if (this.irradiatedLaser != null) {
            this.machineCraftTime += (long) this.irradiatedLaser.getEnergy();
            this.irradiatedLaser = null;
        }
    }

    @Override
    public boolean irradiateClayLaser(ClayLaser laser, Direction incomingSide) {
        this.irradiatedLaser = laser;
        return true;
    }
}
