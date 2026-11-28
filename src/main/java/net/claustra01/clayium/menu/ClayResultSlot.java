package net.claustra01.clayium.menu;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Output-only machine slot that disallows manual item placement and uses {@link SlotTextureKind#LARGE} by default.
 */
public class ClayResultSlot extends ClaySlot {
    public ClayResultSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y, SlotTextureKind.LARGE);
    }

    public ClayResultSlot(Container container, int slot, int x, int y, SlotTextureKind textureKind) {
        super(container, slot, x, y, textureKind);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
