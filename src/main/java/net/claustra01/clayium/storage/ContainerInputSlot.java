package net.claustra01.clayium.storage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class ContainerInputSlot extends SlotItemHandler {
    private final boolean clientSide;
    private ItemStack clientStack = ItemStack.EMPTY;

    public ContainerInputSlot(IItemHandler handler, int x, int y, boolean clientSide) {
        super(handler, 0, x, y);
        this.clientSide = clientSide;
    }

    @Override
    public ItemStack getItem() {
        return clientSide ? clientStack : super.getItem();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (clientSide) return !stack.isEmpty()
                && (clientStack.isEmpty() || ItemStack.isSameItemSameComponents(clientStack, stack));
        return super.mayPlace(stack);
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int increment) {
        if (!mayPlace(stack)) return stack;
        int offered = Math.min(increment, stack.getCount());
        ItemStack remainder = getItemHandler().insertItem(0, stack.copyWithCount(offered), false);
        stack.shrink(offered - remainder.getCount());
        return stack;
    }

    @Override
    public void set(ItemStack stack) {
        if (clientSide) {
            clientStack = stack.copy();
        } else if (!stack.isEmpty()) {
            getItemHandler().insertItem(0, stack, false);
        }
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
