package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.CompressedClayHelper;
import net.claustra01.clayium.util.ProgressionRate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Fabricator (MK1 T8, MK2 T9, MK3 T13): duplicates compressed clay stacks placed in slot 0.
 */
public class ClayFabricatorBlockEntity extends SolarClayFabricatorBlockEntity {
    public float exponentOfNumber;

    public ClayFabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CLAY_FABRICATOR.get(), pos, state);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.exponentOfNumber = 0.8F;
        this.insertRoutes = new int[]{-1, 0, -1, -1, -1, -1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.slotsDrop = new int[]{0, 1};
    }

    @Override
    protected void initParamsByTier(int tier) {
        this.initCraftTime = 0.01F;
        if (tier >= 8) {
            this.acceptableTier = 11;
            this.baseCraftTime = 5.0F;
            this.exponentOfNumber = 0.85F;
            this.initCraftTime = computeFabricatorInitCraftTime(this.acceptableTier, this.baseCraftTime, this.exponentOfNumber, 4.5E7F);
        }
        if (tier >= 9) {
            this.acceptableTier = 13;
            this.baseCraftTime = 2.0F;
            this.exponentOfNumber = 0.3F;
            this.initCraftTime = computeFabricatorInitCraftTime(this.acceptableTier, this.baseCraftTime, this.exponentOfNumber, 1.0E9F);
        }
        if (tier >= 13) {
            this.acceptableTier = 13;
            this.baseCraftTime = 1.3F;
            this.exponentOfNumber = 0.06F;
            this.initCraftTime = computeFabricatorInitCraftTime(this.acceptableTier, this.baseCraftTime, this.exponentOfNumber, 1.0E12F);
        }
        this.multCraftTime = this.initCraftTime;
    }

    public static float computeFabricatorInitCraftTime(int acceptableTier, float baseCraftTime, float exponent, float cePerSecTarget) {
        return (float) (
                Math.pow(10.0, acceptableTier) * 64.0
                        / (Math.pow(baseCraftTime, acceptableTier) * Math.pow(64.0, exponent))
                        / (double) (ProgressionRate.multiplyF(cePerSecTarget) / 20.0F)
        );
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == 0) {
            int clayTier = CompressedClayHelper.getTierOfCompressedClay(stack, false);
            return clayTier >= 0 && clayTier <= this.acceptableTier;
        }
        return true;
    }

    protected boolean canCraft(int tier, int size) {
        if (tier < 0 || tier > this.acceptableTier) {
            return false;
        }
        ItemStack result = CompressedClayHelper.getCompressedClay(tier, size);
        return !result.isEmpty() && ClayiumTransfer.canProduceItemStack(result, this.items, 1, 2, getInventoryStackLimit(1)) >= 1;
    }

    @Override
    public boolean canProceedCraft() {
        ItemStack buf = getStack(2);
        if (buf.isEmpty()) {
            ItemStack in = getStack(0);
            return !in.isEmpty() && canCraft(CompressedClayHelper.getTierOfCompressedClay(in, false), in.getCount());
        }
        return canCraft(CompressedClayHelper.getTierOfCompressedClay(buf, false), buf.getCount());
    }

    @Override
    public void proceedCraft() {
        if (getStack(2).isEmpty()) {
            ItemStack in = getStack(0);
            int clayTier = CompressedClayHelper.getTierOfCompressedClay(in, false);
            this.machineTimeToCraft = (long) (
                    Math.pow(this.baseCraftTime, clayTier)
                            * Math.pow(in.getCount(), this.exponentOfNumber)
                            * (double) this.multCraftTime
            );
            setStack(2, in.copy());
        }

        this.machineCraftTime++;
        this.doingWork = true;
        ItemStack buf = getStack(2);
        int clayTier = CompressedClayHelper.getTierOfCompressedClay(buf, false);
        if (this.machineTimeToCraft > 0L) {
            this.clayEnergyStorage.setStored((long) (
                    Math.pow(10.0, clayTier)
                            * (double) buf.getCount()
                            * (double) this.machineCraftTime
                            / (double) this.machineTimeToCraft
            ));
        }

        if (this.machineCraftTime >= this.machineTimeToCraft) {
            this.clayEnergyStorage.setStored(0L);
            ItemStack result = CompressedClayHelper.getCompressedClay(clayTier, buf.getCount());
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
