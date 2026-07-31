/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.LogisticsBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;

/** Six-row inventory menu shared by Phase 5 logistics devices. */
public final class LogisticsMenu extends AbstractContainerMenu {
    private final Container container;
    private final int deviceSlots;
    private final int rows;
    private final int filterSlots;

    public LogisticsMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(
                id,
                inventory,
                inventory.player.level().getBlockEntity(data.readBlockPos()) instanceof LogisticsBlockEntity logistics
                        ? logistics
                        : new SimpleContainer(LogisticsBlockEntity.MAX_SLOTS));
    }

    public LogisticsMenu(int id, Inventory inventory, Container container) {
        super(ClayiumRegistries.LOGISTICS_MENU.get(), id);
        this.container = container;
        checkContainerSize(container, LogisticsBlockEntity.MAX_SLOTS);
        container.startOpen(inventory.player);
        int inventorySlots = container instanceof LogisticsBlockEntity logistics
                ? logistics.activeSlots()
                : LogisticsBlockEntity.INVENTORY_SLOTS;
        this.filterSlots = container instanceof LogisticsBlockEntity logistics
                ? logistics.filterSlots()
                : 0;
        this.deviceSlots = inventorySlots + filterSlots;
        this.rows = Math.max(1, (inventorySlots + 8) / 9);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9;
                if (slot < inventorySlots) {
                    addSlot(new Slot(container, slot, 8 + column * 18, 18 + row * 18));
                }
            }
        }
        for (int filter = 0; filter < filterSlots; filter++) {
            addSlot(new Slot(
                    container,
                    LogisticsBlockEntity.INVENTORY_SLOTS + filter,
                    35 + filter * 18,
                    24 + rows * 18));
        }
        int playerY = 32 + rows * 18 + (filterSlots > 0 ? 24 : 0);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, playerY + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, playerY + 70));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < deviceSlots) {
            if (!moveItemStackTo(stack, deviceSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int filterStart = deviceSlots - filterSlots;
            if (stack.is(ClayiumRegistries.SMART_FILTER.get()) && filterSlots > 0) {
                if (!moveItemStackTo(stack, filterStart, deviceSlots, false)
                        && !moveItemStackTo(stack, 0, filterStart, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, filterStart, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    public int rows() {
        return rows;
    }

    public int filterSlots() {
        return filterSlots;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
