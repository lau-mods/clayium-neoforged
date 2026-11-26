package net.claustra01.clayium.storage;

import net.claustra01.clayium.registry.StorageRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class VoidContainerMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final VoidContainerBlockEntity container;

    public VoidContainerMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, containerAt(inventory, pos));
    }

    public VoidContainerMenu(int containerId, Inventory inventory, VoidContainerBlockEntity container) {
        super(StorageRegistration.VOID_CONTAINER_MENU.get(), containerId);
        this.container = container;
        pos = container.getBlockPos();
        addSlot(new ContainerInputSlot(container.itemHandler(), 80, 35, inventory.player.level().isClientSide));
        addSlot(new MemorySlot(container.memoryInventory(), 142, 18));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    private static VoidContainerBlockEntity containerAt(Inventory inventory, BlockPos pos) {
        return (VoidContainerBlockEntity)inventory.player.level().getBlockEntity(pos);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == 1) {
            if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) {
                ItemStack carried = getCarried();
                container.memoryInventory().setStackInSlot(0,
                        carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
                broadcastChanges();
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index <= 1 || index >= slots.size() || !slots.get(index).hasItem()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        ItemStack remainder = container.itemHandler().insertItem(0, moving, false);
        slot.set(remainder);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return !container.isRemoved() && player.level().getBlockEntity(pos) == container
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    private static final class MemorySlot extends SlotItemHandler {
        private MemorySlot(IItemHandler handler, int x, int y) {
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
