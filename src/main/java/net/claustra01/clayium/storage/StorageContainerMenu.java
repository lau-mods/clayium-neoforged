package net.claustra01.clayium.storage;

import net.claustra01.clayium.registry.StorageRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class StorageContainerMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final StorageContainerBlockEntity storage;
    private final ContainerData data;

    public StorageContainerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, storageAt(playerInventory, pos), new SimpleContainerData(9));
    }

    public StorageContainerMenu(
            int containerId, Inventory playerInventory, StorageContainerBlockEntity storage, ContainerData data) {
        super(StorageRegistration.STORAGE_CONTAINER_MENU.get(), containerId);
        this.storage = storage;
        this.pos = storage.getBlockPos();
        this.data = data;
        addSlot(new ContainerInputSlot(storage.itemHandler(), 44, 35, playerInventory.player.level().isClientSide));
        addSlot(new ContainerOutputSlot(storage.itemHandler(), 116, 35, playerInventory.player.level().isClientSide));
        addSlot(new MemorySlot(storage.memoryInventory(), 142, 18));
        addPlayerInventory(playerInventory, 8, 84);
        addDataSlots(data);
    }

    private static StorageContainerBlockEntity storageAt(Inventory inventory, BlockPos pos) {
        return (StorageContainerBlockEntity)inventory.player.level().getBlockEntity(pos);
    }

    private void addPlayerInventory(Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, x + column * 18, y + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        ItemStack original = slot.getItem().copy();
        if (index == 0) {
            return ItemStack.EMPTY;
        } else if (index == 1) {
            ItemStack moving = storage.itemHandler().extractItem(0, original.getCount(), true);
            if (!moveItemStackTo(moving, 3, slots.size(), true)) return ItemStack.EMPTY;
            storage.itemHandler().extractItem(0, original.getCount() - moving.getCount(), false);
        } else if (index == 2) {
            return ItemStack.EMPTY;
        } else {
            ItemStack source = slot.getItem();
            ItemStack remainder = storage.itemHandler().insertItem(0, source, false);
            if (remainder.getCount() == source.getCount()) return ItemStack.EMPTY;
            slot.set(remainder);
        }
        return original;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == 0 && clickType == ClickType.PICKUP && !getCarried().isEmpty()) {
            ItemStack remainder = storage.itemHandler().insertItem(0, getCarried(), false);
            setCarried(remainder);
            broadcastChanges();
            return;
        }
        if (slotId == 2 && clickType == ClickType.PICKUP) {
            ItemStack carried = getCarried();
            storage.memoryInventory().setStackInSlot(0,
                    carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    public long amount() {
        return readLong(0);
    }

    public long capacity() {
        return readLong(4);
    }

    public boolean expanded() {
        return data.get(8) != 0;
    }

    private long readLong(int start) {
        long value = 0;
        for (int part = 0; part < 4; part++) value |= (data.get(start + part) & 0xFFFFL) << (part * 16);
        return value;
    }

    @Override
    public boolean stillValid(Player player) {
        return !storage.isRemoved() && player.level().getBlockEntity(pos) == storage
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    private static final class MemorySlot extends SlotItemHandler {
        private MemorySlot(net.neoforged.neoforge.items.IItemHandler handler, int x, int y) {
            super(handler, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
