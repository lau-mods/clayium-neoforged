package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.CompressedClayHelper;
import net.claustra01.clayium.util.ProgressionRate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Solar Clay Fabricator (MK1 T5, MK2 T6, Lithium T7): compresses clay blocks using overhead sky light
 * without requiring Clay Energy.
 */
public class SolarClayFabricatorBlockEntity extends AbstractClayMachineBlockEntity {
    public int acceptableTier;
    public float baseCraftTime;

    public SolarClayFabricatorBlockEntity(BlockPos pos, BlockState state) {
        this(ClayiumBlockEntities.SOLAR_CLAY_FABRICATOR.get(), pos, state);
        ensureTierInitialized();
    }

    protected SolarClayFabricatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 3);
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
        this.insertRoutes = new int[]{-1, -1, -1, 0, -1, -1};
        this.extractRoutes = new int[]{-1, -1, 0, -1, -1, -1};
        this.autoInsert = true;
        this.autoExtract = true;
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsExtract.add(new int[]{1});
        this.slotsDrop = new int[]{0, 1, 2};
        this.acceptableTier = 4;
        this.clayEnergySlot = -1;
        this.maxAutoExtract = new int[]{64};
        this.maxAutoInsert = new int[]{64};
        this.autoExtractInterval = 4;
        this.autoInsertInterval = 4;
        this.initCraftTime = 0.3F;
        this.baseCraftTime = 4.0F;
    }

    @Override
    protected void initParamsByTier(int tier) {
        this.acceptableTier = 4;
        this.baseCraftTime = 4.0F;
        this.initCraftTime = computeSolarInitCraftTime(this.acceptableTier, this.baseCraftTime, 5000.0F);
        if (tier >= 6) {
            this.acceptableTier = 6;
            this.baseCraftTime = 3.0F;
            this.initCraftTime = computeSolarInitCraftTime(this.acceptableTier, this.baseCraftTime, 50000.0F);
        }
        if (tier >= 7) {
            this.acceptableTier = 9;
            this.baseCraftTime = 2.0F;
            this.initCraftTime = computeSolarInitCraftTime(this.acceptableTier, this.baseCraftTime, 3000000.0F);
        }
        this.multCraftTime = this.initCraftTime;
    }

    public static float computeSolarInitCraftTime(int acceptableTier, float baseCraftTime, float cePerSecTarget) {
        return (float) (
                Math.pow(10.0, acceptableTier + 1)
                        * (double) (baseCraftTime - 1.0F)
                        / ((double) baseCraftTime * (Math.pow(baseCraftTime, acceptableTier) - 1.0))
                        / (double) (ProgressionRate.multiplyF(cePerSecTarget) / 20.0F)
        );
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == 0) {
            int clayTier = CompressedClayHelper.getTierOfCompressedClay(stack, true);
            return clayTier >= 0 && clayTier <= this.acceptableTier;
        }
        return true;
    }

    protected boolean canCraft(int tier) {
        if (tier < 0 || tier > this.acceptableTier) {
            return false;
        }
        ItemStack result = CompressedClayHelper.getCompressedClay(tier + 1);
        return !result.isEmpty() && ClayiumTransfer.canProduceItemStack(result, this.items, 1, 2, getInventoryStackLimit(1)) >= 1;
    }

    public boolean hasClearSky() {
        if (this.level == null) {
            return false;
        }
        int maxY = this.level.getMaxBuildHeight();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(this.worldPosition.getX(), 0, this.worldPosition.getZ());
        for (int y = maxY - 1; y > this.worldPosition.getY(); y--) {
            cursor.setY(y);
            BlockState above = this.level.getBlockState(cursor);
            if (above.getLightBlock(this.level, cursor) > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canProceedCraft() {
        if (!hasClearSky()) {
            return false;
        }
        ItemStack buf = getStack(2);
        return !buf.isEmpty()
                ? canCraft(CompressedClayHelper.getTierOfCompressedClay(buf, true))
                : canCraft(CompressedClayHelper.getTierOfCompressedClay(getStack(0), true));
    }

    @Override
    public void proceedCraft() {
        if (getStack(2).isEmpty()) {
            ItemStack in = getStack(0);
            int clayTier = CompressedClayHelper.getTierOfCompressedClay(in, true);
            this.machineTimeToCraft = (long) (Math.pow(this.baseCraftTime, clayTier) * (double) this.multCraftTime);
            ItemStack moved = in.split(1);
            if (in.isEmpty()) {
                setStack(0, ItemStack.EMPTY);
            }
            setStack(2, moved);
        }

        this.machineCraftTime++;
        this.doingWork = true;
        int activeClayTier = CompressedClayHelper.getTierOfCompressedClay(getStack(2), true);
        if (this.machineTimeToCraft > 0L) {
            this.clayEnergyStorage.setStored((long) (
                    Math.pow(10.0, activeClayTier + 1)
                            * (double) this.machineCraftTime
                            / (double) this.machineTimeToCraft
            ));
        }

        if (this.machineCraftTime >= this.machineTimeToCraft) {
            this.clayEnergyStorage.setStored(0L);
            ItemStack result = CompressedClayHelper.getCompressedClay(activeClayTier + 1);
            setStack(2, ItemStack.EMPTY);
            ClayiumTransfer.produceItemStack(result, this.items, 1, 2, getInventoryStackLimit(1));
            this.machineCraftTime = 0L;
            if (this.externalControlState > 0) {
                this.externalControlState--;
                if (this.externalControlState == 0) {
                    this.externalControlState = -1;
                }
            }
            markForSync();
        }
    }
}
