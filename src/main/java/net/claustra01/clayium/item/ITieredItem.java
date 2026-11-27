package net.claustra01.clayium.item;

import net.minecraft.world.item.ItemStack;

/**
 * Marks an item that exposes an intrinsic progression tier (-1 for untiered).
 */
public interface ITieredItem {
    int getTier(ItemStack stack);
}
