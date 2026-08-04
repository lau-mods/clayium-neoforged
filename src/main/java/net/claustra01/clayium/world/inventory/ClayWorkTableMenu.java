/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.ClayWorkTableBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Dedicated menu for the Clay Work Table's persistent input and output slots. */
public final class ClayWorkTableMenu extends AbstractContainerMenu {
    private static final int DEVICE_SLOT_COUNT = 4;
    private static final int PLAYER_INVENTORY_START = DEVICE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    private final Container container;
    private final ContainerData data;

    public ClayWorkTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(DEVICE_SLOT_COUNT), new SimpleContainerData(4));
    }

    public ClayWorkTableMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data) {
        super(ClayiumRegistries.CLAY_WORK_TABLE_MENU.get(), containerId);
        this.container = container;
        this.data = data;
        checkContainerSize(container, DEVICE_SLOT_COUNT);
        checkContainerDataCount(data, 4);
        container.startOpen(playerInventory.player);

        addSlot(new Slot(container, ClayWorkTableBlockEntity.INPUT_SLOT, 17, 30));
        addSlot(new Slot(container, ClayWorkTableBlockEntity.TOOL_SLOT, 80, 17) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addSlot(new Slot(container, ClayWorkTableBlockEntity.OUTPUT_SLOT, 143, 30) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(container, ClayWorkTableBlockEntity.REMAINDER_OUTPUT_SLOT, 143, 55) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return id >= 1
                && id <= 6
                && stillValid(player)
                && container instanceof ClayWorkTableBlockEntity workTable
                && workTable.pushOperation(id);
    }

    public int progress() {
        return data.get(0);
    }

    public int totalProgress() {
        return data.get(1);
    }

    public int activeOperation() {
        return data.get(2);
    }

    public boolean canUseOperation(int buttonId) {
        return buttonId >= 1
                && buttonId <= 6
                && (data.get(3) & 1 << (buttonId - 1)) != 0;
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
        if (index < DEVICE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean isTool = stack.is(ClayiumRegistries.CLAY_ROLLING_PIN.get())
                    || stack.is(ClayiumRegistries.CLAY_SLICER.get())
                    || stack.is(ClayiumRegistries.CLAY_SPATULA.get());
            int target = isTool ? ClayWorkTableBlockEntity.TOOL_SLOT : ClayWorkTableBlockEntity.INPUT_SLOT;
            if (!moveItemStackTo(stack, target, target + 1, false)) {
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
