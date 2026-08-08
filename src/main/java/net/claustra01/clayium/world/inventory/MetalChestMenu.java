/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.storage.MetalStorageCatalog;
import net.claustra01.clayium.world.level.block.entity.MetalChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MetalChestMenu extends AbstractContainerMenu {
    private final Container container;
    private final BlockPos pos;
    private final int columns;
    private final int rows;
    private final int pages;
    private final int chestSlots;
    private final DataSlot page = DataSlot.standalone();

    public MetalChestMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, resolve(inventory, buffer.readBlockPos()));
    }

    public MetalChestMenu(int id, Inventory inventory, MetalChestBlockEntity chest) {
        this(id, inventory, (Container) chest);
    }

    private MetalChestMenu(int id, Inventory inventory, Container container) {
        super(ClayiumRegistries.METAL_CHEST_MENU.get(), id);
        this.container = container;
        MetalChestBlockEntity chest = container instanceof MetalChestBlockEntity value ? value : null;
        MetalStorageCatalog.Chest definition = chest == null
                ? MetalStorageCatalog.CHESTS.getFirst() : chest.definition();
        pos = chest == null ? BlockPos.ZERO : chest.getBlockPos().immutable();
        columns = definition.columns();
        rows = definition.rows();
        pages = definition.pages();
        chestSlots = columns * rows;
        container.startOpen(inventory.player);
        addDataSlot(page);

        int width = imageWidth();
        for (int slot = 0; slot < chestSlots; slot++) {
            addSlot(new PageSlot(container, slot, (width - columns * 18) / 2 + slot % columns * 18,
                    18 + slot / columns * 18));
        }
        int playerX = (width - 162) / 2;
        int playerY = machineHeight() + 12;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, playerX + column * 18, playerY + row * 18));
        for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column, playerX + column * 18, playerY + 58));
    }

    private static Container resolve(Inventory inventory, BlockPos pos) {
        return inventory.player.level().getBlockEntity(pos) instanceof MetalChestBlockEntity chest
                ? chest : new SimpleContainer(MetalChestBlockEntity.MAX_SLOTS);
    }

    public int columns() { return columns; }
    public int rows() { return rows; }
    public int pages() { return pages; }
    public int page() { return page.get(); }
    public int imageWidth() { return Math.max(176, columns * 18 + 14); }
    public int machineHeight() { return rows * 18 + (pages > 1 ? 40 : 28); }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (id != 0 && id != 1) return false;
        int next = Math.floorMod(page.get() + (id == 0 ? -1 : 1), pages);
        page.set(next);
        broadcastChanges();
        return true;
    }

    @Override public boolean stillValid(Player player) {
        return pos == BlockPos.ZERO || player.distanceToSqr(pos.getCenter()) <= 64.0D;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < chestSlots) {
            if (!moveItemStackTo(stack, chestSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, chestSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    private final class PageSlot extends Slot {
        private final int pageOffset;
        private PageSlot(Container container, int pageOffset, int x, int y) {
            super(container, pageOffset, x, y);
            this.pageOffset = pageOffset;
        }
        private int actualIndex() { return page.get() * chestSlots + pageOffset; }
        @Override public ItemStack getItem() { return container.getItem(actualIndex()); }
        @Override public boolean hasItem() { return !getItem().isEmpty(); }
        @Override public void set(ItemStack stack) { container.setItem(actualIndex(), stack); setChanged(); }
        @Override public ItemStack remove(int amount) { return container.removeItem(actualIndex(), amount); }
        @Override public boolean mayPlace(ItemStack stack) { return container.canPlaceItem(actualIndex(), stack); }
        @Override public void setChanged() { container.setChanged(); }
    }
}
