package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.CompressedClayHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Auto Clay Condenser (MK1 T5, MK2 T7): automatically compresses 9 compressed clay items of tier {@code t}
 * into 1 of tier {@code t+1} up to tier 13 (or the sample cap in slot 21). MK2 performs all possible steps per tick.
 */
public class AutoClayCondenserBlockEntity extends AbstractClayMachineBlockEntity {
    public AutoClayCondenserBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.AUTO_CLAY_CONDENSER.get(), pos, state, 22);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(22, ItemStack.EMPTY);
        this.clayEnergySlot = -1;
        this.listSlotsInsert.add(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
        this.listSlotsExtract.add(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
        this.insertRoutes = new int[]{-1, 0, -1, -1, -1, -1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.MACHINE);
        this.maxAutoInsertDefault = 64;
        this.maxAutoExtractDefault = 64;
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= 20) {
            return slot == 21;
        }
        return CompressedClayHelper.getTierOfCompressedClay(stack, true) >= 0;
    }

    public int[] getQuantityOfClay() {
        int[] num = new int[17];
        for (int i = 0; i < num.length; i++) {
            ItemStack sample = CompressedClayHelper.getCompressedClay(i);
            if (sample.isEmpty()) {
                continue;
            }
            int count = 0;
            for (int slot = 0; slot < 20; slot++) {
                ItemStack s = getStack(slot);
                if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, sample)) {
                    count += s.getCount();
                }
            }
            num[i] = count;
        }
        return num;
    }

    public void sortInventory() {
        int[] num = getQuantityOfClay();
        for (int i = 0; i < 20; i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        int i = 0;
        int j = 0;
        while (j < 17 && i < 20) {
            if (num[j] == 0) {
                j++;
            } else {
                int maxSize = Math.min(64, getInventoryStackLimit(i));
                int size = Math.min(maxSize, num[j]);
                num[j] -= size;
                this.items.set(i, CompressedClayHelper.getCompressedClay(j, size));
                i++;
            }
        }
        markForSync();
    }

    protected boolean canCraftClayTier(int tier) {
        ItemStack target = CompressedClayHelper.getCompressedClay(tier + 1);
        return !target.isEmpty() && ClayiumTransfer.canProduceItemStack(target, this.items, 0, 20, getInventoryStackLimit(0)) >= 1;
    }

    public int getConsumedClayTier() {
        int[] num = getQuantityOfClay();
        int sampleTier = CompressedClayHelper.getTierOfCompressedClay(getStack(21), true);
        int max = sampleTier != -1 ? sampleTier : 13;
        for (int j = Math.min(max - 1, 12); j >= 0; j--) {
            if (num[j] >= 9 && canCraftClayTier(j)) {
                return j;
            }
        }
        return -1;
    }

    @Override
    public boolean canProceedCraft() {
        ItemStack buf = getStack(20);
        if (!buf.isEmpty()) {
            int t = CompressedClayHelper.getTierOfCompressedClay(buf, true);
            return t >= 0 && canCraftClayTier(t);
        }
        return getConsumedClayTier() != -1;
    }

    @Override
    public void proceedCraft() {
        if (getStack(20).isEmpty()) {
            this.machineConsumingEnergy = 0L;
        }
        if (consumeClayEnergy(this.machineConsumingEnergy)) {
            if (getStack(20).isEmpty()) {
                this.machineTimeToCraft = (long) (1.0F * this.multCraftTime);
                int tier = getConsumedClayTier();
                if (tier < 0) {
                    return;
                }
                consumeFromInventory(CompressedClayHelper.getCompressedClay(tier), 9);
                setStack(20, CompressedClayHelper.getCompressedClay(tier, 9));
            }

            this.machineCraftTime++;
            this.doingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                int tier = CompressedClayHelper.getTierOfCompressedClay(getStack(20), true);
                ItemStack result = CompressedClayHelper.getCompressedClay(tier + 1);
                setStack(20, ItemStack.EMPTY);
                ClayiumTransfer.produceItemStack(result, this.items, 0, 20, getInventoryStackLimit(0));
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

    private void consumeFromInventory(ItemStack template, int needed) {
        int rem = needed;
        for (int i = 0; i < 20 && rem > 0; i++) {
            ItemStack s = getStack(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, template)) {
                int take = Math.min(rem, s.getCount());
                s.shrink(take);
                rem -= take;
                if (s.isEmpty()) {
                    this.items.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level != null && !this.level.isClientSide) {
            if (this.baseTier >= 7) {
                while (this.externalControlState >= 0 && canProceedCraft()) {
                    proceedCraft();
                }
            }
            long displayEnergy = 0L;
            int[] num = getQuantityOfClay();
            for (int i = 0; i < num.length; i++) {
                displayEnergy += (long) Math.pow(10.0, (double) i) * (long) num[i];
            }
            this.clayEnergyStorage.setStored(displayEnergy);
        }
    }
}
