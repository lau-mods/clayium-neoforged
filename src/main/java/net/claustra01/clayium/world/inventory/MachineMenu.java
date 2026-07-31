/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.MachineBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Shared menu for one-input/one-output Clayium machines. */
public final class MachineMenu extends AbstractContainerMenu {
    private static final int DEVICE_SLOTS = 2;
    private final Container container;
    private final ContainerData data;

    public MachineMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(DEVICE_SLOTS), new SimpleContainerData(6));
    }

    public MachineMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ClayiumRegistries.MACHINE_MENU.get(), containerId);
        this.container = container;
        this.data = data;
        checkContainerSize(container, DEVICE_SLOTS);
        checkContainerDataCount(data, 6);
        container.startOpen(inventory.player);
        addSlot(new Slot(container, MachineBlockEntity.INPUT_SLOT, 44, 35));
        addSlot(new Slot(container, MachineBlockEntity.OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return id == 0
                && stillValid(player)
                && container instanceof MachineBlockEntity machine
                && machine.addManualEnergy();
    }

    public int progress() {
        return data.get(0);
    }

    public int totalProgress() {
        return data.get(1);
    }

    public int energy() {
        return data.get(2);
    }

    public int capacity() {
        return data.get(3);
    }

    public MachineBlockEntity.StopReason stopReason() {
        return MachineBlockEntity.StopReason.byOrdinal(data.get(4));
    }

    public int tier() {
        return data.get(5);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < DEVICE_SLOTS) {
            if (!moveItemStackTo(stack, DEVICE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
