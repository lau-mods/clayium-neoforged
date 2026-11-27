package mods.clayium.block.tile;

import mods.clayium.block.CBlocks;
import mods.clayium.util.UtilItemStack;
import mods.clayium.util.UtilTransfer;
import mods.clayium.util.crafting.SimpleMachinesRecipes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class TileAutoClayCondenser extends TileClayMachines {
    public TileAutoClayCondenser() {
        super();
    }

    public void initParams() {
        this.containerItemStacks = new ItemStack[22];
        this.clayEnergySlot = -1;
        this.listSlotsInsert.add(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
        this.listSlotsExtract.add(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
        this.insertRoutes = new int[]{-1, 0, -1, -1, -1, -1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
        this.autoInsert = true;
        this.autoExtract = true;
        this.recipeId = "";
    }

    public void initParamsByTier(int tier) {
        this.setDefaultTransportationParamsByTier(tier, TileClayContainerTiered.ParamMode.MACHINE);
        this.maxAutoInsertDefault = this.maxAutoExtractDefault = 64;
    }

    public void setRecipe(String recipeId_) {
    }

    public void refreshRecipe() {
    }

    public SimpleMachinesRecipes getRecipe() {
        return null;
    }

    public void sortInventory() {
        int[] num = this.getQuantityOfClay();

        for (int i = 0; i < 20; i++) {
            this.containerItemStacks[i] = null;
        }

        int i = 0;
        int j = 0;

        while (j < 17 && i < 20) {
            if (num[j] == 0) {
                j++;
            } else {
                int maxSize = Math.min(64, this.func_70297_j_());
                int size = Math.min(maxSize, num[j]);
                num[j] -= size;
                this.containerItemStacks[i] = getCompressedClay(j, size);
                i++;
            }
        }

        this.setSyncFlag();
    }

    public int[] getQuantityOfClay() {
        int[] num = new int[17];

        for (int i = 0; i < 17; i++) {
            num[i] = UtilTransfer.countItemStack(getCompressedClay(i), this.containerItemStacks, 0, 20);
        }

        return num;
    }

    protected boolean canCraft(int tier) {
        return UtilTransfer.canProduceItemStack(getCompressedClay(tier), this.containerItemStacks, 0, 20, this.func_70297_j_()) >= 1;
    }

    public void func_145845_h() {
        super.func_145845_h();
        if (this.baseTier >= 7) {
            while (this.externalControlState >= 0 && this.canProceedCraft()) {
                this.proceedCraft();
            }
        }

        this.clayEnergy = 0L;
        int[] num = this.getQuantityOfClay();

        for (int i = 0; i < num.length; i++) {
            this.clayEnergy = this.clayEnergy + (long)Math.pow(10.0, (double)i) * (long)num[i];
        }
    }

    public boolean canProceedCraft() {
        return this.containerItemStacks[20] != null ? this.canCraft(getTierOfCompressedClay(this.containerItemStacks[20])) : this.getConsumedClay() != -1;
    }

    public int getConsumedClay() {
        int[] num = this.getQuantityOfClay();
        int max = this.containerItemStacks[21] != null && getTierOfCompressedClay(this.containerItemStacks[21]) != -1
            ? getTierOfCompressedClay(this.containerItemStacks[21])
            : 13;

        for (int j = max - 1; j >= 0; j--) {
            if (num[j] >= 9 && this.canCraft(j)) {
                return j;
            }
        }

        return -1;
    }

    public void proceedCraft() {
        if (this.containerItemStacks[20] == null) {
            this.machineConsumingEnergy = (long)(0.0F * this.multConsumingEnergy);
        }

        if (this.consumeClayEnergy(this.machineConsumingEnergy)) {
            if (this.containerItemStacks[20] == null) {
                this.machineTimeToCraft = (long)(1.0F * this.multCraftTime);
                int tier = this.getConsumedClay();
                UtilTransfer.consumeItemStack(getCompressedClay(tier, 9), this.containerItemStacks, 0, 20);
                this.containerItemStacks[20] = getCompressedClay(tier, 9);
            }

            this.machineCraftTime++;
            this.isDoingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                ItemStack result = getCompressedClay(getTierOfCompressedClay(this.containerItemStacks[20]) + 1);
                this.containerItemStacks[20] = null;
                UtilTransfer.produceItemStack(result, this.containerItemStacks, 0, 20, this.func_70297_j_());
                this.sortInventory();
                this.machineCraftTime = 0L;
                if (this.externalControlState > 0) {
                    this.externalControlState--;
                    if (this.externalControlState == 0) {
                        this.externalControlState = -1;
                    }
                }
            }
        }
    }

    public static ItemStack getCompressedClay(int tier, int size) {
        return tier == 0 ? new ItemStack(Blocks.field_150435_aG, size) : new ItemStack(CBlocks.blockCompressedClay, size, tier - 1);
    }

    public static ItemStack getCompressedClay(int tier) {
        return getCompressedClay(tier, 1);
    }

    public static int getTierOfCompressedClay(ItemStack itemstack) {
        if (itemstack == null) {
            return -1;
        } else {
            for (int i = 0; i < 17; i++) {
                if (UtilItemStack.areItemDamageEqual(getCompressedClay(i), itemstack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    public boolean func_102008_b(int slot, ItemStack itemstack, int side) {
        return super.func_102008_b(slot, itemstack, side) && getTierOfCompressedClay(itemstack) >= getTierOfCompressedClay(this.containerItemStacks[21]);
    }

    public boolean func_94041_b(int i, ItemStack itemstack) {
        return i == 20 ? false : getTierOfCompressedClay(itemstack) != -1;
    }
}
