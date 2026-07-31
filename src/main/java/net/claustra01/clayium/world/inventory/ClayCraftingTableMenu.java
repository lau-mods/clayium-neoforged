/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import java.util.Optional;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
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
    private static final int PLAYER_START = 10;
    private static final int PLAYER_END = 46;

    private final CraftingContainer craftSlots;
    private final ResultContainer resultSlots = new ResultContainer();
    private final Player player;

    public ClayCraftingTableMenu(int containerId, Inventory inventory) {
        super(ClayiumRegistries.CLAY_CRAFTING_TABLE_MENU.get(), containerId);
        this.player = inventory.player;
        this.craftSlots = new TransientCraftingContainer(this, 3, 3);
        addSlots(inventory);
    }

    public ClayCraftingTableMenu(int containerId, Inventory inventory, CraftingContainer craftSlots) {
        super(ClayiumRegistries.CLAY_CRAFTING_TABLE_MENU.get(), containerId);
        this.player = inventory.player;
        this.craftSlots = craftSlots;
        addSlots(inventory);
        slotsChanged(craftSlots);
    }

    private void addSlots(Inventory inventory) {
        addSlot(new ResultSlot(player, craftSlots, resultSlots, 0, 124, 35));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(craftSlots, column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
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
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (index >= PLAYER_START) {
            if (!moveItemStackTo(stack, GRID_START, GRID_END, false)) {
                int inventoryEnd = PLAYER_START + 27;
                if (index < inventoryEnd) {
                    if (!moveItemStackTo(stack, inventoryEnd, PLAYER_END, false)) return ItemStack.EMPTY;
                } else if (!moveItemStackTo(stack, PLAYER_START, inventoryEnd, false)) return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, false)) {
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
}
