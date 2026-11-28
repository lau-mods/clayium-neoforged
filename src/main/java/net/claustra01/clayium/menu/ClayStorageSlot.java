package net.claustra01.clayium.menu;

import net.claustra01.clayium.blockentity.StorageContainerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Virtual insert sink slot backed by {@link StorageContainerBlockEntity#insertIntoStorage}.
 */
public class ClayStorageSlot extends ClaySlot {
    private final StorageContainerBlockEntity storage;

    public ClayStorageSlot(Container container, StorageContainerBlockEntity storage, int slot, int x, int y) {
        super(container, slot, x, y, SlotTextureKind.LARGE);
        this.storage = storage;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.storage.acceptsItem(stack)
                && (this.storage.isVacuumContainer() || this.storage.getStoredCount() < this.storage.getMaxStorageSize());
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(64, stack.getMaxStackSize());
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int increment) {
        if (stack.isEmpty() || !mayPlace(stack)) {
            return stack;
        }
        int attempt = Math.min(stack.getCount(), increment);
        if (attempt <= 0) {
            return stack;
        }
        int moved = this.storage.insertIntoStorage(stack.copyWithCount(attempt), false);
        if (moved <= 0) {
            return stack;
        }
        ItemStack leftover = stack.copy();
        leftover.shrink(moved);
        return leftover.isEmpty() ? ItemStack.EMPTY : leftover;
    }
}
