/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.claustra01.clayium.world.level.block.entity.AbstractConfigurableMachineBlockEntity;

/** Shared player-inventory plumbing for dedicated machine menus. */
public abstract class AbstractDedicatedMachineMenu extends AbstractContainerMenu {
    protected final Container container;
    protected final ContainerData data;
    private int machineSlots;
    private final int machineHeight;

    protected AbstractDedicatedMachineMenu(MenuType<?> type, int id, Inventory inventory,
                                           Container container, ContainerData data, int machineHeight) {
        super(type, id);
        this.container = container;
        this.data = data;
        this.machineHeight = machineHeight;
        container.startOpen(inventory.player);
    }

    protected final void finishLayout(Inventory inventory) {
        machineSlots = slots.size();
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, machineHeight + 12 + row * 18));
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, machineHeight + 70));
        }
        addDataSlots(data);
    }

    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int target = 0; target < machineSlots && !moved; target++) {
                if (slots.get(target).mayPlace(stack)) moved = moveItemStackTo(stack, target, target + 1, false);
            }
            if (!moved) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    public final int machineHeight() { return machineHeight; }
    public final int progress() { return data.get(0); }
    public final int totalProgress() { return data.get(1); }
    public final long energy() {
        return Integer.toUnsignedLong(data.get(2)) | Integer.toUnsignedLong(data.get(3)) << 32;
    }

    protected static class RestrictedSlot extends Slot {
        protected RestrictedSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return container.canPlaceItem(getContainerSlot(), stack); }
    }
    protected static final class EnergySlot extends RestrictedSlot {
        protected EnergySlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public int getMaxStackSize() {
            return container instanceof AbstractConfigurableMachineBlockEntity machine
                    ? machine.inventorySlotLimit(getContainerSlot()) : 1;
        }
        @Override public int getMaxStackSize(ItemStack stack) {
            return Math.min(stack.getMaxStackSize(), getMaxStackSize());
        }
    }
    protected static class OutputSlot extends Slot {
        protected OutputSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
    }
    protected static class GhostSlot extends Slot {
        protected GhostSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
