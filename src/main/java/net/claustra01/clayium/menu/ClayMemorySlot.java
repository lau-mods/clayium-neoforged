package net.claustra01.clayium.menu;

import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Phantom/ghost memory slot storing a template filter/pattern item copy without consuming player items.
 */
public class ClayMemorySlot extends ClaySlot {
    public ClayMemorySlot(Container container, int slot, int x, int y, SlotTextureKind textureKind) {
        super(container, slot, x, y, textureKind);
    }

    public boolean isPhantomSlot() {
        return true;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    public void handlePhantomClick(ItemStack carried) {
        if (carried.isEmpty()) {
            set(ItemStack.EMPTY);
        } else {
            ItemStack copy = carried.copyWithCount(1);
            if (ClayiumFilterMatcher.isFilter(copy) && ClayiumFilterMatcher.isCopyMode(copy)) {
                ClayiumFilterMatcher.withCopyFlag(copy, false);
            }
            set(copy);
        }
    }
}
