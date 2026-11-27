package net.claustra01.clayium.energy;

import net.minecraft.world.item.ItemStack;

/**
 * Capability-like contract for items that carry consumable Clay Energy (e.g. compressed clay blocks).
 * 100,000 internal units equal 1 CE (1 unit = 10 uCE).
 */
public interface IClayEnergy {
    long getClayEnergy(ItemStack stack);
}
