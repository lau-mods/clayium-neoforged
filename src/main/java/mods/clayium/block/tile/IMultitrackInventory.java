package mods.clayium.block.tile;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public interface IMultitrackInventory extends ISidedInventory {
    int[] getAccessibleSlotsFromSide(int var1, int var2);

    boolean canInsertItem(int var1, ItemStack var2, int var3, int var4);

    boolean canExtractItem(int var1, ItemStack var2, int var3, int var4);
}
