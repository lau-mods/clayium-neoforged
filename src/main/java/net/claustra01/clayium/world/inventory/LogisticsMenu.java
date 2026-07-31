/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.logistics.LogisticsKind;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.LogisticsBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Original per-device Phase 5 container layouts. */
public final class LogisticsMenu extends AbstractContainerMenu {
    private final Container container;
    private final LogisticsKind kind;
    private final int tier;
    private final int machineHeight;
    private final int machineMenuSlots;
    private final int insertionMenuSlot;
    private final int filterMenuStart;
    private final int filterMenuSlots;
    private int storedCountLow;
    private int storedCountHigh;

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

        LogisticsBlockEntity logistics = container instanceof LogisticsBlockEntity value ? value : null;
        kind = logistics == null ? LogisticsKind.BUFFER : logistics.kindValue();
        tier = logistics == null ? 4 : logistics.tierValue();
        machineHeight = calculateMachineHeight();

        int firstInsertion = -1;
        int filtersStart = -1;
        int filters = 0;
        switch (kind) {
            case BUFFER -> addBufferSlots();
            case MULTITRACK_BUFFER -> {
                addMultitrackSlots();
                filtersStart = kind.slots(tier);
                filters = kind.tracks(tier);
            }
            case DISTRIBUTOR -> addDistributorSlots();
            case STORAGE_CONTAINER -> {
                firstInsertion = addStorageSlots();
                filtersStart = 2;
                filters = 1;
            }
            case VOID_CONTAINER -> {
                firstInsertion = addVoidSlots();
                filtersStart = 1;
                filters = 1;
            }
        }
        machineMenuSlots = slots.size();
        insertionMenuSlot = firstInsertion;
        filterMenuStart = filtersStart;
        filterMenuSlots = filters;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        inventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        machineHeight + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, machineHeight + 70));
        }

        if (logistics != null) {
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return (int) (logistics.storedCount() & 0xffffL);
                }

                @Override
                public void set(int value) {
                    storedCountLow = value & 0xffff;
                }
            });
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return (int) ((logistics.storedCount() >>> 16) & 0xffffL);
                }

                @Override
                public void set(int value) {
                    storedCountHigh = value & 0xffff;
                }
            });
        }
    }

    private void addBufferSlots() {
        int columns = kind.columns(tier);
        int inventoryX = (176 - columns * 18) / 2 + 1;
        for (int slot = 0; slot < kind.slots(tier); slot++) {
            addSlot(new Slot(container, slot, inventoryX + slot % columns * 18, 18 + slot / columns * 18));
        }
    }

    private void addMultitrackSlots() {
        int tracks = kind.tracks(tier);
        int trackLength = kind.columns(tier);
        int offsetX = (176 - (trackLength * 18 + 22)) / 2 + 1;
        for (int track = 0; track < tracks; track++) {
            for (int column = 0; column < trackLength; column++) {
                addSlot(new Slot(
                        container,
                        track * trackLength + column,
                        offsetX + column * 18,
                        18 + track * 18));
            }
        }
        for (int track = 0; track < tracks; track++) {
            addSlot(new Slot(
                    container,
                    LogisticsBlockEntity.INVENTORY_SLOTS + track,
                    offsetX + trackLength * 18 + 4,
                    18 + track * 18));
        }
    }

    private void addDistributorSlots() {
        int colonyX = tier == 7 ? 2 : tier == 8 ? 3 : 4;
        int colonyY = tier == 7 ? 2 : tier == 8 ? 2 : 3;
        int width = 2 * colonyX * 18 + (colonyX - 1) * 2;
        int offsetX = (176 - width) / 2 + 1;
        int slot = 0;
        for (int colonyRow = 0; colonyRow < colonyY; colonyRow++) {
            for (int colonyColumn = 0; colonyColumn < colonyX; colonyColumn++) {
                for (int row = 0; row < 2; row++) {
                    for (int column = 0; column < 2; column++) {
                        addSlot(new Slot(
                                container,
                                slot++,
                                offsetX + colonyColumn * 38 + column * 18,
                                18 + colonyRow * 38 + row * 18));
                    }
                }
            }
        }
    }

    private int addStorageSlots() {
        addSlot(new Slot(container, LogisticsBlockEntity.STORAGE_INPUT_SLOT, 44, 35));
        addSlot(new Slot(container, LogisticsBlockEntity.STORAGE_CONTENT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(container, LogisticsBlockEntity.CONTAINER_FILTER_SLOT, 142, 18));
        return 0;
    }

    private int addVoidSlots() {
        addSlot(new Slot(container, LogisticsBlockEntity.STORAGE_CONTENT_SLOT, 80, 35));
        addSlot(new Slot(container, LogisticsBlockEntity.CONTAINER_FILTER_SLOT, 142, 18));
        return 0;
    }

    private int calculateMachineHeight() {
        return switch (kind) {
            case BUFFER -> kind.rows(tier) * 18 + 18;
            case MULTITRACK_BUFFER -> kind.tracks(tier) * 18 + 18;
            case DISTRIBUTOR -> {
                int colonyY = tier == 7 || tier == 8 ? 2 : 3;
                yield colonyY * 38 + 16;
            }
            case STORAGE_CONTAINER, VOID_CONTAINER -> 72;
        };
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
        if (kind == LogisticsKind.STORAGE_CONTAINER && index == 1) {
            ItemStack available = container.getItem(LogisticsBlockEntity.STORAGE_CONTENT_SLOT).copy();
            if (available.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack moving = available.copy();
            if (!moveItemStackTo(moving, machineMenuSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            int moved = available.getCount() - moving.getCount();
            if (moved <= 0) {
                return ItemStack.EMPTY;
            }
            container.removeItem(LogisticsBlockEntity.STORAGE_CONTENT_SLOT, moved);
            return available.copyWithCount(moved);
        }
        Slot slot = slots.get(index);
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < machineMenuSlots) {
            if (!moveItemStackTo(stack, machineMenuSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ClayiumRegistries.SMART_FILTER.get()) && filterMenuSlots > 0) {
            if (!moveItemStackTo(stack, filterMenuStart, filterMenuStart + filterMenuSlots, false)) {
                return ItemStack.EMPTY;
            }
        } else if (insertionMenuSlot >= 0) {
            if (!moveItemStackTo(stack, insertionMenuSlot, insertionMenuSlot + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            int inventoryEnd = filterMenuSlots == 0 ? machineMenuSlots : filterMenuStart;
            if (!moveItemStackTo(stack, 0, inventoryEnd, false)) {
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

    public LogisticsKind kind() {
        return kind;
    }

    public int tier() {
        return tier;
    }

    public int machineHeight() {
        return machineHeight;
    }

    public int rows() {
        return kind.rows(tier);
    }

    public int columns() {
        return kind.columns(tier);
    }

    public int tracks() {
        return kind.tracks(tier);
    }

    public long storedCount() {
        return Integer.toUnsignedLong(storedCountLow | storedCountHigh << 16);
    }

    public long storageCapacity() {
        return LogisticsBlockEntity.STORAGE_CAPACITY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
