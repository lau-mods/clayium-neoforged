/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.data.FilterSettings;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

/** Item-backed editor used by whitelist, blacklist, fuzzy, and string filters. */
public final class ItemFilterMenu extends AbstractContainerMenu {
    private static final int FILTER_SLOTS = FilterSettings.ENTRY_SLOTS;
    private final Inventory playerInventory;
    private final int selectedSlot;
    private final Container ghostInventory = new SimpleContainer(FILTER_SLOTS);
    private final ClayFilterItem.Kind kind;

    public ItemFilterMenu(int id, Inventory inventory) {
        super(ClayiumRegistries.ITEM_FILTER_MENU.get(), id);
        playerInventory = inventory;
        selectedSlot = inventory.selected;
        ItemStack filter = filterStack();
        kind = filter.getItem() instanceof ClayFilterItem item
                ? item.kind()
                : ClayFilterItem.Kind.WHITELIST;
        FilterSettings settings =
                filter.getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
        for (int slot = 0; slot < FILTER_SLOTS; slot++) {
            ghostInventory.setItem(slot, settings.entry(slot));
        }
        if (isListEditor()) {
            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 5; column++) {
                    addSlot(new GhostSlot(
                            ghostInventory,
                            column + row * 5,
                            44 + column * 18,
                            18 + row * 18));
                }
            }
        }
        int playerY = isListEditor() ? 66 : 48;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, playerY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            final int hotbarSlot = column;
            addSlot(new Slot(inventory, column, 8 + column * 18, playerY + 58) {
                @Override
                public boolean mayPickup(Player player) {
                    return hotbarSlot != selectedSlot;
                }
            });
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (isListEditor() && slotId >= 0 && slotId < FILTER_SLOTS) {
            ItemStack carried = getCarried();
            ItemStack stored = ghostInventory.getItem(slotId);
            if (ClayFilterItem.isFilter(carried)
                    && ((ClayFilterItem) carried.getItem()).isCopy(carried)
                    && ClayFilterItem.isFilter(stored)) {
                setCarried(ClayFilterItem.asCopied(stored));
            } else {
                setGhost(slotId, carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1), player);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void setGhost(int slot, ItemStack value, Player player) {
        ItemStack filter = filterStack();
        if (!(filter.getItem() instanceof ClayFilterItem)) {
            return;
        }
        FilterSettings current =
                filter.getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
        FilterSettings changed = current.withEntry(slot, value);
        ItemStack checking = filter.copy();
        checking.set(ClayiumDataComponents.FILTER_SETTINGS.get(), changed);
        if (ClayFilterItem.nestedSize(checking, java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()))
                >= FilterSettings.MAX_NESTED_FILTER_SIZE) {
            checking.set(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
            changed = FilterSettings.DEFAULT;
            player.displayClientMessage(Component.translatable("message.clayium_neoforged.filter_broken"), false);
        }
        filter.set(ClayiumDataComponents.FILTER_SETTINGS.get(), changed);
        ghostInventory.setItem(slot, changed.entry(slot));
        playerInventory.setChanged();
        broadcastChanges();
    }

    public void setPattern(String pattern) {
        if (!isStringEditor() || !(filterStack().getItem() instanceof ClayFilterItem)) {
            return;
        }
        ItemStack filter = filterStack();
        FilterSettings current =
                filter.getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
        filter.set(ClayiumDataComponents.FILTER_SETTINGS.get(), current.withPattern(pattern));
        playerInventory.setChanged();
    }

    public String pattern() {
        return filterStack()
                .getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT)
                .pattern();
    }

    public boolean isListEditor() {
        return kind == ClayFilterItem.Kind.WHITELIST
                || kind == ClayFilterItem.Kind.BLACKLIST
                || kind == ClayFilterItem.Kind.FUZZY;
    }

    public boolean isStringEditor() {
        return switch (kind) {
            case ITEM_TAG, ITEM_NAME, TRANSLATION_KEY, UNIQUE_ID, MOD_ID, ITEM_DAMAGE, BLOCK_STATE -> true;
            default -> false;
        };
    }

    @Override
    public boolean stillValid(Player player) {
        return selectedSlot == playerInventory.selected && filterStack().getItem() instanceof ClayFilterItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private ItemStack filterStack() {
        return playerInventory.getItem(selectedSlot);
    }

    private static final class GhostSlot extends Slot {
        private GhostSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
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
