package mods.clayium.block.tile;

import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilTransfer;
import net.minecraft.item.ItemStack;

public class TileClayFabricator extends TileSolarClayFabricator {
    public float exponentOfNumber;

    public TileClayFabricator() {
        super();
    }

    public void initParams() {
        super.initParams();
        this.exponentOfNumber = 0.8F;
        this.insertRoutes = new int[]{-1, 0, -1, -1, -1, -1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.slotsDrop = new int[]{0, 1};
    }

    public void initParamsByTier(int tier) {
        this.initCraftTime = 0.01F;
        if (tier >= 8) {
            this.acceptableTier = 11;
            this.baseCraftTime = 5.0F;
            this.exponentOfNumber = 0.85F;
            this.initCraftTime = (float)(
                Math.pow(10.0, (double)this.acceptableTier)
                    * 64.0
                    / (Math.pow((double)this.baseCraftTime, (double)this.acceptableTier) * Math.pow(64.0, (double)this.exponentOfNumber))
                    / (double)(ClayiumCore.multiplyProgressionRateF(4.5E7F) / 20.0F)
            );
        }

        if (tier >= 9) {
            this.acceptableTier = 13;
            this.baseCraftTime = 2.0F;
            this.exponentOfNumber = 0.3F;
            this.initCraftTime = (float)(
                Math.pow(10.0, (double)this.acceptableTier)
                    * 64.0
                    / (Math.pow((double)this.baseCraftTime, (double)this.acceptableTier) * Math.pow(64.0, (double)this.exponentOfNumber))
                    / (double)(ClayiumCore.multiplyProgressionRateF(1.0E9F) / 20.0F)
            );
        }

        if (tier >= 13) {
            this.acceptableTier = 13;
            this.baseCraftTime = 1.3F;
            this.exponentOfNumber = 0.06F;
            this.initCraftTime = (float)(
                Math.pow(10.0, (double)this.acceptableTier)
                    * 64.0
                    / (Math.pow((double)this.baseCraftTime, (double)this.acceptableTier) * Math.pow(64.0, (double)this.exponentOfNumber))
                    / (double)(ClayiumCore.multiplyProgressionRateF(1.0E12F) / 20.0F)
            );
        }
    }

    protected boolean canCraft(int tier, int size) {
        return tier >= 0 && tier <= this.acceptableTier
            ? UtilTransfer.canProduceItemStack(getCompressedClay(tier, size), this.containerItemStacks, 1, 2, this.func_70297_j_()) >= 1
            : false;
    }

    public boolean canProceedCraft() {
        return this.containerItemStacks[2] == null
            ? (
                this.containerItemStacks[0] == null
                    ? false
                    : this.canCraft(getTierOfCompressedClay(this.containerItemStacks[0], false), this.containerItemStacks[0].field_77994_a)
            )
            : this.canCraft(getTierOfCompressedClay(this.containerItemStacks[2], false), this.containerItemStacks[2].field_77994_a);
    }

    public void proceedCraft() {
        if (this.containerItemStacks[2] == null) {
            this.machineTimeToCraft = (long)(
                Math.pow((double)this.baseCraftTime, (double)getTierOfCompressedClay(this.containerItemStacks[0], false))
                    * Math.pow((double)this.containerItemStacks[0].field_77994_a, (double)this.exponentOfNumber)
                    * (double)this.multCraftTime
            );
            this.containerItemStacks[2] = this.containerItemStacks[0].func_77946_l();
        }

        this.machineCraftTime++;
        this.isDoingWork = true;
        this.clayEnergy = (long)(
            Math.pow(10.0, (double)getTierOfCompressedClay(this.containerItemStacks[2], false))
                * (double)this.containerItemStacks[2].field_77994_a
                * (double)this.machineCraftTime
                / (double)this.machineTimeToCraft
        );
        if (this.machineCraftTime >= this.machineTimeToCraft) {
            this.clayEnergy = 0L;
            ItemStack result = getCompressedClay(getTierOfCompressedClay(this.containerItemStacks[2], false));
            result.field_77994_a = this.containerItemStacks[2].field_77994_a;
            this.containerItemStacks[2] = null;
            UtilTransfer.produceItemStack(result, this.containerItemStacks, 1, 2, this.func_70297_j_());
            this.machineCraftTime = 0L;
            if (this.externalControlState > 0) {
                this.externalControlState--;
                if (this.externalControlState == 0) {
                    this.externalControlState = -1;
                }
            }
        }
    }

    public boolean func_94041_b(int slot, ItemStack itemstack) {
        return slot == 0 ? getTierOfCompressedClay(itemstack, false) >= 0 && getTierOfCompressedClay(itemstack, false) <= this.acceptableTier : true;
    }
}
