/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.PanAdapterBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Original-style PAN Adapter pattern editor with tier-dependent pattern pages. */
public final class PanAdapterMenu extends AbstractContainerMenu {
    private final Container container;
    private final PanAdapterBlockEntity adapter;
    private final int pages;
    private final SimpleContainer patternView = new SimpleContainer(9);
    private final SimpleContainer previews = new SimpleContainer(9);
    private int page;

    public PanAdapterMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, clientContainer(inventory, buffer));
    }

    private PanAdapterMenu(int id, Inventory inventory, Container container) {
        this(id, inventory, container,
                container instanceof PanAdapterBlockEntity value ? value : null,
                container instanceof PanAdapterBlockEntity value ? value.pages() : 1);
    }

    public PanAdapterMenu(int id, Inventory inventory, PanAdapterBlockEntity adapter) {
        this(id, inventory, adapter, adapter, adapter.pages());
    }

    private PanAdapterMenu(int id, Inventory inventory, Container container,
                           PanAdapterBlockEntity adapter, int pages) {
        super(ClayiumRegistries.PAN_ADAPTER_MENU.get(), id);
        this.container = container;
        this.adapter = adapter;
        this.pages = Math.max(1, pages);
        container.startOpen(inventory.player);
        for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++) {
            int visibleSlot = column + row * 3;
            addSlot(new DisplaySlot(patternView, visibleSlot, 32 + column * 18, 18 + row * 18));
        }
        for (int slot = 0; slot < 9; slot++) {
            int x = 92 + (slot % 3) * 18;
            int y = 18 + (slot / 3) * 18;
            addSlot(new DisplaySlot(previews, slot, x, y));
        }
        for (int slot = 0; slot < 9; slot++) {
            addSlot(new Slot(container, PanAdapterBlockEntity.AUXILIARY_START + slot, 8 + slot * 18, 74));
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 108 + row * 18));
        for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column, 8 + column * 18, 166));
        addDataSlot(new DataSlot() {
            @Override public int get() { return page; }
            @Override public void set(int value) { page = Math.max(0, Math.min(PanAdapterMenu.this.pages - 1, value)); }
        });
        refreshViews();
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (!stillValid(player)) return false;
        if (id == 0) page = Math.floorMod(page - 1, pages);
        else if (id == 1) page = (page + 1) % pages;
        else return false;
        refreshViews();
        broadcastChanges();
        return true;
    }

    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < 9) {
            ItemStack carried = getCarried();
            ItemStack pattern = carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1);
            patternView.setItem(slotId, pattern);
            if (adapter != null && adapter.getLevel() != null && !adapter.getLevel().isClientSide) {
                adapter.setPattern(page, slotId, pattern);
                refreshViews();
            }
            broadcastChanges();
            return;
        }
        if (slotId >= 9 && slotId < 18) return;
        super.clicked(slotId, button, clickType, player);
    }

    public int page() { return page; }
    public int pages() { return pages; }
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }

    private void refreshViews() {
        if (adapter == null || adapter.getLevel() == null || adapter.getLevel().isClientSide) return;
        for (int slot = 0; slot < 9; slot++) {
            patternView.setItem(slot, adapter.pattern(page, slot).copy());
            previews.setItem(slot, adapter.preview(page, slot));
        }
    }

    private static final class DisplaySlot extends Slot {
        private DisplaySlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }

    private static Container clientContainer(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        var pos = buffer.readBlockPos();
        return inventory.player.level().getBlockEntity(pos) instanceof PanAdapterBlockEntity adapter
                ? adapter : new SimpleContainer(PanAdapterBlockEntity.SLOTS);
    }
}
