package mods.clayium.block.tile;

import mods.clayium.item.filter.ItemFilterTemp;
import mods.clayium.util.UtilItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class TileVacuumContainer extends TileStorageContainer {
    public TileVacuumContainer() {
        super();
    }

    public int getItemUseMode(ItemStack itemStack, EntityPlayer player) {
        int m = super.getItemUseMode(itemStack, player);
        return m == 99 ? -1 : m;
    }

    public ItemStack func_70301_a(int slot) {
        return slot >= this.extractSlotNum + this.insertSlotNum + 1 ? super.func_70301_a(slot) : null;
    }

    public ItemStack func_70298_a(int slot, int size) {
        return slot >= this.extractSlotNum + this.insertSlotNum + 1 ? super.func_70298_a(slot, size) : null;
    }

    public void func_70299_a(int slot, ItemStack itemstack) {
        if (slot >= this.extractSlotNum + this.insertSlotNum + 1) {
            super.func_70299_a(slot, itemstack);
        }
    }

    public boolean checkFilterSlot(ItemStack itemstack, ItemStack filter) {
        return UtilItemStack.areTypeEqual(itemstack, filter) || ItemFilterTemp.match(filter, itemstack);
    }
}
