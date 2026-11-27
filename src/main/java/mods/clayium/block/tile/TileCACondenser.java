package mods.clayium.block.tile;

import net.minecraft.item.ItemStack;

public class TileCACondenser extends TileCAMachines {
    public TileCACondenser() {
        super();
    }

    public void proceedCraft() {
        if (this.containerItemStacks[2] == null) {
            this.machineConsumingEnergy = (long)((float)this.recipe.getEnergy(this.containerItemStacks[0], this.baseTier) * this.multConsumingEnergy);
        }

        if (this.consumeClayEnergy(this.machineConsumingEnergy)) {
            if (this.containerItemStacks[2] == null) {
                this.machineTimeToCraft = (long)((float)this.recipe.getTime(this.containerItemStacks[0], this.baseTier) * this.multCraftTime);
                this.containerItemStacks[2] = this.containerItemStacks[0]
                    .func_77979_a(this.recipe.getConsumedStackSize(this.containerItemStacks[0], this.baseTier));
                if (this.containerItemStacks[0].field_77994_a <= 0) {
                    this.containerItemStacks[0] = null;
                }
            }

            this.machineCraftTime++;
            this.isDoingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                ItemStack itemstack = this.recipe.getResult(this.containerItemStacks[2], this.baseTier).func_77946_l();
                itemstack.field_77994_a = (int)((double)itemstack.field_77994_a * (Math.log(this.getResonance()) + 1.0));
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                if (this.containerItemStacks[1] == null) {
                    this.containerItemStacks[1] = itemstack.func_77946_l();
                } else if (this.containerItemStacks[1].func_77973_b() == itemstack.func_77973_b()) {
                    this.containerItemStacks[1].field_77994_a = this.containerItemStacks[1].field_77994_a + itemstack.field_77994_a;
                }

                if (this.containerItemStacks[1].field_77994_a >= this.containerItemStacks[1].func_77976_d()) {
                    this.containerItemStacks[1].field_77994_a = this.containerItemStacks[1].func_77976_d();
                }

                if ((
                        this.containerItemStacks[2].field_77994_a = this.containerItemStacks[2].field_77994_a
                            - this.recipe.getConsumedStackSize(this.containerItemStacks[2], this.baseTier)
                    )
                    <= 0) {
                    this.containerItemStacks[2] = null;
                }

                if (this.externalControlState > 0) {
                    this.externalControlState--;
                    if (this.externalControlState == 0) {
                        this.externalControlState = -1;
                    }
                }
            }
        }
    }
}
