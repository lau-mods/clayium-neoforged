package net.claustra01.clayium.storage;

import net.claustra01.clayium.registry.StorageRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class MetalChestMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final MetalChestBlockEntity chest;
    private final MetalChestType type;
    private int page;

    public MetalChestMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, chestAt(playerInventory, pos));
    }

    public MetalChestMenu(int containerId, Inventory playerInventory, MetalChestBlockEntity chest) {
        super(StorageRegistration.METAL_CHEST_MENU.get(), containerId);
        this.chest = chest;
        pos = chest.getBlockPos();
        type = chest.type();
        chest.startOpen(playerInventory.player);
        int storageX = (imageWidth() - type.columns() * 18) / 2 + 1;
        int storageY = 18;
        for (int index = 0; index < type.slots(); index++) {
            int slotPage = index / type.pageSize();
            int slotIndex = index % type.pageSize();
            addSlot(new SlotItemHandler(chest.inventory(), index,
                    storageX + slotIndex % type.columns() * 18, storageY + slotIndex / type.columns() * 18) {
                @Override public boolean isActive() { return page == slotPage; }
            });
        }
        int playerX = (imageWidth() - 176) / 2 + 8;
        int playerY = imageHeight() - 82;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        playerX + column * 18, playerY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, playerX + column * 18, playerY + 58));
        }
    }

    private static MetalChestBlockEntity chestAt(Inventory inventory, BlockPos pos) {
        return (MetalChestBlockEntity)inventory.player.level().getBlockEntity(pos);
    }

    public int imageWidth() {
        return 176 + Math.max(0, type.columns() - 9) * 18 + (type.rows() > 6 ? 8 : 0);
    }

    public int imageHeight() {
        return type.rows() * 18 + 112;
    }

    public MetalChestType type() {
        return type;
    }

    public int page() {
        return page;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= type.pages()) return false;
        page = id;
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.isActive()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        int storageSlots = type.slots();
        int pageStart = page * type.pageSize();
        int pageEnd = pageStart + type.pageSize();
        if (index < storageSlots ? !moveItemStackTo(moving, storageSlots, slots.size(), true)
                : !moveItemStackTo(moving, pageStart, pageEnd, false)) return ItemStack.EMPTY;
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return !chest.isRemoved() && player.level().getBlockEntity(pos) == chest
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        chest.stopOpen(player);
    }
}
