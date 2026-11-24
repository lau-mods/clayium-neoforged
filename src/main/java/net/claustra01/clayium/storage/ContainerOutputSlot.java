package net.claustra01.clayium.storage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Output-only view of the original Storage Container extraction slot. */
public final class ContainerOutputSlot extends SlotItemHandler {
    private final boolean clientSide;
    private ItemStack clientStack = ItemStack.EMPTY;

    public ContainerOutputSlot(IItemHandler handler, int x, int y, boolean clientSide) {
        super(handler, 0, x, y);
        this.clientSide = clientSide;
    }

    @Override
    public ItemStack getItem() {
        return clientSide ? clientStack : super.getItem();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int increment) {
        return stack;
    }

    @Override
    public void set(ItemStack stack) {
        if (clientSide) {
            clientStack = stack.copy();
        }
    }

    @Override
    public ItemStack remove(int amount) {
        if (clientSide) return clientStack.split(amount);
        return getItemHandler().extractItem(0, amount, false);
    }

    @Override
    public boolean mayPickup(Player player) {
        return clientSide ? !clientStack.isEmpty()
                : !getItemHandler().extractItem(0, 1, true).isEmpty();
    }

    @Override
    public boolean allowModification(Player player) {
        return mayPickup(player);
    }
}
