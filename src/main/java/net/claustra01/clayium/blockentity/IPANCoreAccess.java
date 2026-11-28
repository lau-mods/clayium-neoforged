package net.claustra01.clayium.blockentity;

import java.util.Set;
import net.minecraft.world.item.ItemStack;

/**
 * PAN Core interface exposed to PAN Duplicators for antimatter duplication lookup.
 * Implemented by PAN Core.
 */
public interface IPANCoreAccess {
    record PANConversionEntry(ItemStack stack, double ceCost) {}

    Set<PANConversionEntry> getConversionEntries();
}
