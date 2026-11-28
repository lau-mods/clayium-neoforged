package net.claustra01.clayium.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Custom slot with explicit background {@link SlotTextureKind} and optional placement restriction.
 */
public class ClaySlot extends Slot {
    private final SlotTextureKind textureKind;
    private boolean restricted = false;

    public ClaySlot(Container container, int slot, int x, int y, SlotTextureKind textureKind) {
        super(container, slot, x, y);
        this.textureKind = textureKind;
    }

    public ClaySlot setRestricted() {
        this.restricted = true;
        return this;
    }

    public SlotTextureKind getTextureKind() {
        return this.textureKind;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (this.restricted) {
            return this.container.canPlaceItem(getSlotIndex(), stack);
        }
        return super.mayPlace(stack);
    }
}
