/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Crafting menu whose 3x3 input belongs to the block entity and is not discarded on close. */
public final class ClayCraftingTableMenu extends AbstractContainerMenu {
    private static final int RESULT_SLOT = 0;
    private static final int GRID_START = 1;
    private static final int GRID_END = 10;
    private final CraftingContainer craftSlots;
    private final ResultContainer resultSlots = new ResultContainer();
    private final Player player;
    @Nullable private final Container adjacentChest;
    private final int playerStart;
    private final int playerEnd;

    public ClayCraftingTableMenu(int containerId, Inventory inventory, boolean hasAdjacentChest) {
        super(ClayiumRegistries.CLAY_CRAFTING_TABLE_MENU.get(), containerId);
        this.player = inventory.player;
        this.craftSlots = new TransientCraftingContainer(this, 3, 3);
        this.adjacentChest = hasAdjacentChest ? new SimpleContainer(27) : null;
        addSlots(inventory);
        this.playerStart = hasAdjacentChest ? 37 : 10;
        this.playerEnd = playerStart + 36;
    }

    public ClayCraftingTableMenu(int containerId, Inventory inventory, CraftingContainer craftSlots,
                                 @Nullable Container adjacentChest) {
        super(ClayiumRegistries.CLAY_CRAFTING_TABLE_MENU.get(), containerId);
        this.player = inventory.player;
        this.craftSlots = new PersistentCraftingContainer(this, craftSlots);
        this.adjacentChest = adjacentChest;
        addSlots(inventory);
        this.playerStart = adjacentChest == null ? 10 : 37;
        this.playerEnd = playerStart + 36;
        slotsChanged(this.craftSlots);
    }

    private void addSlots(Inventory inventory) {
        addSlot(new ResultSlot(player, craftSlots, resultSlots, 0, 124, 35));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(craftSlots, column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }
        if (adjacentChest != null) {
            adjacentChest.startOpen(player);
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 9; column++) {
                    addSlot(new Slot(adjacentChest, column + row * 9, 8 + column * 18, 73 + row * 18));
                }
            }
        }
        int playerY = machineHeight();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, playerY + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, playerY + 70));
        }
    }

    private static void updateResult(
            AbstractContainerMenu menu, Level level, Player player, CraftingContainer grid,
            ResultContainer result, @Nullable RecipeHolder<CraftingRecipe> previous) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack output = ItemStack.EMPTY;
        Optional<RecipeHolder<CraftingRecipe>> match = level.getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, grid.asCraftInput(), level, previous);
        if (match.isPresent() && result.setRecipeUsed(level, serverPlayer, match.get())) {
            output = match.get().value().assemble(grid.asCraftInput(), level.registryAccess());
            if (!output.isItemEnabled(level.enabledFeatures())) {
                output = ItemStack.EMPTY;
            }
        }
        result.setItem(0, output);
        menu.setRemoteSlot(0, output);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                menu.containerId, menu.incrementStateId(), RESULT_SLOT, output));
    }

    @Override
    public void slotsChanged(Container container) {
        updateResult(this, player.level(), player, craftSlots, resultSlots, null);
    }

    @Override
    public boolean stillValid(Player player) {
        return craftSlots.stillValid(player);
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
        ItemStack copy = stack.copy();
        if (index == RESULT_SLOT) {
            stack.getItem().onCraftedBy(stack, player.level(), player);
            if (!moveItemStackTo(stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (index >= playerStart) {
            boolean moved = adjacentChest != null && moveItemStackTo(stack, GRID_END, playerStart, false);
            if (!moved && !moveItemStackTo(stack, GRID_START, GRID_END, false)) {
                int inventoryEnd = playerStart + 27;
                if (index < inventoryEnd) {
                    if (!moveItemStackTo(stack, inventoryEnd, playerEnd, false)) return ItemStack.EMPTY;
                } else if (!moveItemStackTo(stack, playerStart, inventoryEnd, false)) return ItemStack.EMPTY;
            }
        } else if (index >= GRID_END && index < playerStart) {
            if (!moveItemStackTo(stack, GRID_START, GRID_END, false)
                    && !moveItemStackTo(stack, playerStart, playerEnd, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, playerStart, playerEnd, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        if (index == RESULT_SLOT) player.drop(stack, false);
        return copy;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    public boolean hasAdjacentChest() { return adjacentChest != null; }

    public int machineHeight() { return hasAdjacentChest() ? 128 : 72; }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (adjacentChest != null) adjacentChest.stopOpen(player);
    }

    /**
     * Keeps the block entity's persistent grid while restoring the change callback supplied by
     * vanilla's transient crafting grid. Without this adapter, slot edits never recalculate the
     * result slot.
     */
    private static final class PersistentCraftingContainer implements CraftingContainer {
        private final AbstractContainerMenu menu;
        private final CraftingContainer delegate;

        private PersistentCraftingContainer(AbstractContainerMenu menu, CraftingContainer delegate) {
            this.menu = menu;
            this.delegate = delegate;
        }

        @Override
        public int getContainerSize() {
            return delegate.getContainerSize();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return delegate.getItem(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack removed = delegate.removeItem(slot, amount);
            if (!removed.isEmpty()) {
                menu.slotsChanged(this);
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return delegate.removeItemNoUpdate(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            delegate.setItem(slot, stack);
            menu.slotsChanged(this);
        }

        @Override
        public int getMaxStackSize() {
            return delegate.getMaxStackSize();
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return delegate.getMaxStackSize(stack);
        }

        @Override
        public void setChanged() {
            delegate.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return delegate.stillValid(player);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return delegate.canPlaceItem(slot, stack);
        }

        @Override
        public int getWidth() {
            return delegate.getWidth();
        }

        @Override
        public int getHeight() {
            return delegate.getHeight();
        }

        @Override
        public List<ItemStack> getItems() {
            return delegate.getItems();
        }

        @Override
        public void fillStackedContents(StackedContents contents) {
            delegate.fillStackedContents(contents);
        }

        @Override
        public void clearContent() {
            delegate.clearContent();
            menu.slotsChanged(this);
        }
    }
}
