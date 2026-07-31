/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.machine.MachineLayout;
import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.entity.MachineBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Shared menu with the original simple, assembler, and centrifuge slot layouts. */
public final class MachineMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private final MachineLayout layout;
    private final ClayTier tier;
    private final int deviceSlotCount;

    public MachineMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, MachineLayout.SIMPLE, ClayTier.CLAY);
    }

    public static MachineMenu assembler(int containerId, Inventory inventory) {
        return new MachineMenu(containerId, inventory, MachineLayout.ASSEMBLER, ClayTier.SIMPLE);
    }

    public static MachineMenu centrifugeTier3(int containerId, Inventory inventory) {
        return new MachineMenu(containerId, inventory, MachineLayout.CENTRIFUGE, ClayTier.SIMPLE);
    }

    public static MachineMenu centrifugeTier4(int containerId, Inventory inventory) {
        return new MachineMenu(containerId, inventory, MachineLayout.CENTRIFUGE, ClayTier.BASIC);
    }

    public static MachineMenu centrifugeTier5(int containerId, Inventory inventory) {
        return new MachineMenu(containerId, inventory, MachineLayout.CENTRIFUGE, ClayTier.ADVANCED);
    }

    public static MachineMenu centrifugeTier6(int containerId, Inventory inventory) {
        return new MachineMenu(containerId, inventory, MachineLayout.CENTRIFUGE, ClayTier.PRECISION);
    }

    private MachineMenu(
            int containerId,
            Inventory inventory,
            MachineLayout layout,
            ClayTier tier) {
        this(
                containerId,
                inventory,
                new SimpleContainer(MachineLayout.STORAGE_SLOT_COUNT),
                new SimpleContainerData(8),
                layout,
                tier);
    }

    public MachineMenu(
            int containerId,
            Inventory inventory,
            Container container,
            ContainerData data,
            MachineLayout layout,
            ClayTier tier) {
        super(menuType(layout, tier), containerId);
        this.container = container;
        this.data = data;
        this.layout = layout;
        this.tier = tier;
        checkContainerSize(container, MachineLayout.STORAGE_SLOT_COUNT);
        checkContainerDataCount(data, 8);
        container.startOpen(inventory.player);
        this.deviceSlotCount = addMachineSlots();
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

    private int addMachineSlots() {
        int processingSlots = switch (layout) {
            case SIMPLE -> {
                addSlot(new Slot(container, 0, 44, 35));
                addOutputSlot(1, 116, 35);
                yield 2;
            }
            case ASSEMBLER -> {
                addSlot(new Slot(container, 0, 32, 35));
                addSlot(new Slot(container, 1, 50, 35));
                addOutputSlot(2, 116, 35);
                yield 3;
            }
            case CENTRIFUGE -> {
                addSlot(new Slot(container, 0, 44, 35));
                int[] outputs = layout.outputSlots(tier);
                for (int index = 0; index < outputs.length; index++) {
                    addOutputSlot(outputs[index], 116, 35 + 18 * index - 9 * (outputs.length - 1));
                }
                yield outputs.length + 1;
            }
        };
        addSlot(new Slot(container, MachineLayout.ENERGY_SLOT, 146, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return EnergeticClayFuel.isFuel(stack);
            }
        });
        return processingSlots + 1;
    }

    private void addOutputSlot(int inventorySlot, int x, int y) {
        addSlot(new Slot(container, inventorySlot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
    }

    private static MenuType<MachineMenu> menuType(MachineLayout layout, ClayTier tier) {
        if (layout == MachineLayout.ASSEMBLER) {
            return ClayiumRegistries.ASSEMBLER_MACHINE_MENU.get();
        }
        if (layout == MachineLayout.CENTRIFUGE) {
            return switch (Math.max(1, Math.min(4, tier.progressionIndex() - 2))) {
                case 1 -> ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_1.get();
                case 2 -> ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_2.get();
                case 3 -> ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_3.get();
                default -> ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_4.get();
            };
        }
        return ClayiumRegistries.MACHINE_MENU.get();
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

    public long energy() {
        return Integer.toUnsignedLong(data.get(2)) | (Integer.toUnsignedLong(data.get(3)) << 32);
    }

    public long energyPerTick() {
        return Integer.toUnsignedLong(data.get(6)) | (Integer.toUnsignedLong(data.get(7)) << 32);
    }

    public MachineBlockEntity.StopReason stopReason() {
        return MachineBlockEntity.StopReason.byOrdinal(data.get(4));
    }

    public int tier() {
        return data.get(5);
    }

    public MachineLayout layout() {
        return layout;
    }

    public ClayTier machineTier() {
        return tier;
    }

    public int inputSlotCount() {
        return layout.inputSlots().length;
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
        if (index < deviceSlotCount) {
            if (!moveItemStackTo(stack, deviceSlotCount, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int energyMenuSlot = deviceSlotCount - 1;
            if (EnergeticClayFuel.isFuel(stack)) {
                if (!moveItemStackTo(stack, energyMenuSlot, energyMenuSlot + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, inputSlotCount(), false)) {
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
