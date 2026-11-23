package net.claustra01.clayium.workbench;

import java.util.Optional;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ClayCraftingBoardMenu extends AbstractContainerMenu {
    private static final int RESULT_SLOT = 0;
    private static final int FIRST_CRAFT_SLOT = 1;
    private static final int CRAFT_SLOT_END = 10;

    private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final ClayCraftingBoardBlockEntity board;
    private final Player player;
    private final int adjacentSlotCount;
    private final int playerSlotStart;
    @Nullable private final BlockPos adjacentPos;
    @Nullable private final BlockEntity adjacentBlockEntity;
    @Nullable private final BlockState adjacentState;
    private final IItemHandler adjacent;

    public ClayCraftingBoardMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, blockEntity(inventory, buffer), buffer.readVarInt());
    }

    public ClayCraftingBoardMenu(
            int containerId, Inventory inventory, ClayCraftingBoardBlockEntity board) {
        this(containerId, inventory, board,
                board.adjacentInventory() == null ? 0 : Math.min(board.adjacentInventory().getSlots(), 27));
    }

    private ClayCraftingBoardMenu(int containerId, Inventory inventory,
            ClayCraftingBoardBlockEntity board, int slotCount) {
        super(WorkbenchRegistration.CLAY_CRAFTING_BOARD_MENU.get(), containerId);
        this.board = board;
        this.player = inventory.player;
        addSlot(new ResultSlot(player, craftSlots, resultSlots, 0, 124, 35) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                ItemStack[] pattern = new ItemStack[9];
                for (int slot = 0; slot < pattern.length; slot++) {
                    pattern[slot] = craftSlots.getItem(slot).copyWithCount(1);
                }
                super.onTake(player, stack);
                if (!player.level().isClientSide) {
                    refillFromAdjacent(pattern);
                }
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(craftSlots, column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }

        adjacentSlotCount = slotCount;
        BlockPos foundPos = null;
        BlockEntity foundEntity = null;
        BlockState foundState = null;
        IItemHandler foundHandler = null;
        if (!player.level().isClientSide) {
            for (Direction direction : Direction.values()) {
                BlockPos pos = board.getBlockPos().relative(direction);
                IItemHandler handler = player.level().getCapability(
                        Capabilities.ItemHandler.BLOCK, pos, direction.getOpposite());
                if (handler != null && handler.getSlots() > 0) {
                    foundPos = pos;
                    foundEntity = player.level().getBlockEntity(pos);
                    foundState = player.level().getBlockState(pos);
                    foundHandler = handler;
                    break;
                }
            }
        }
        adjacentPos = foundPos;
        adjacentBlockEntity = foundEntity;
        adjacentState = foundState;
        adjacent = foundHandler == null ? new ItemStackHandler(slotCount) : foundHandler;
        for (int slot = 0; slot < adjacentSlotCount; slot++) {
            addSlot(new SlotItemHandler(adjacent, slot, 8 + slot % 9 * 18, 73 + slot / 9 * 18));
        }
        playerSlotStart = CRAFT_SLOT_END + adjacentSlotCount;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, machineHeight() + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, machineHeight() + 70));
        }
    }

    private static ClayCraftingBoardBlockEntity blockEntity(
            Inventory inventory, RegistryFriendlyByteBuf buffer) {
        if (inventory.player.level().getBlockEntity(buffer.readBlockPos())
                instanceof ClayCraftingBoardBlockEntity board) {
            return board;
        }
        throw new IllegalStateException("Clay crafting board menu opened without its block entity");
    }

    @Override
    public void slotsChanged(Container inventory) {
        updateResult(this, player.level(), player, craftSlots, resultSlots, null);
    }

    private static void updateResult(
            AbstractContainerMenu menu,
            Level level,
            Player player,
            CraftingContainer craftSlots,
            ResultContainer resultSlots,
            @Nullable RecipeHolder<CraftingRecipe> preferredRecipe) {
        if (level.isClientSide) {
            return;
        }
        CraftingInput input = craftSlots.asCraftInput();
        ItemStack result = ItemStack.EMPTY;
        Optional<RecipeHolder<CraftingRecipe>> recipe = level.getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level, preferredRecipe);
        if (recipe.isPresent() && (!(player instanceof ServerPlayer serverPlayer)
                || resultSlots.setRecipeUsed(level, serverPlayer, recipe.get()))) {
            ItemStack assembled = recipe.get().value().assemble(input, level.registryAccess());
            if (assembled.isItemEnabled(level.enabledFeatures())) {
                result = assembled;
            }
        }
        resultSlots.setItem(0, result);
        menu.setRemoteSlot(RESULT_SLOT, result);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                    menu.containerId, menu.incrementStateId(), RESULT_SLOT, result));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        clearContainer(player, craftSlots);
    }

    private void refillFromAdjacent(ItemStack[] pattern) {
        if (adjacentPos == null || player.level().getBlockEntity(adjacentPos) != adjacentBlockEntity) {
            return;
        }
        for (int gridSlot = 0; gridSlot < pattern.length; gridSlot++) {
            ItemStack expected = pattern[gridSlot];
            if (expected.isEmpty() || !craftSlots.getItem(gridSlot).isEmpty()) {
                continue;
            }
            for (int chestSlot = 0; chestSlot < adjacentSlotCount; chestSlot++) {
                ItemStack available = adjacent.getStackInSlot(chestSlot);
                if (ItemStack.isSameItemSameComponents(expected, available)) {
                    ItemStack extracted = adjacent.extractItem(chestSlot, 1, false);
                    if (!extracted.isEmpty()) {
                        craftSlots.setItem(gridSlot, extracted);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return board.getLevel() != null
                && board.getLevel().getBlockEntity(board.getBlockPos()) == board
                && (adjacentPos == null || board.getLevel().getBlockEntity(adjacentPos) == adjacentBlockEntity)
                && (adjacentPos == null || board.getLevel().getBlockState(adjacentPos) == adjacentState)
                && player.distanceToSqr(
                        board.getBlockPos().getX() + 0.5,
                        board.getBlockPos().getY() + 0.5,
                        board.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    public int machineHeight() {
        int rows = (adjacentSlotCount + 8) / 9;
        return rows == 0 ? 72 : 74 + rows * 18;
    }

    public int adjacentSlotCount() {
        return adjacentSlotCount;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerSlotEnd = playerSlotStart + 36;
        if (index == RESULT_SLOT) {
            if (!moveItemStackTo(stack, playerSlotStart, playerSlotEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index >= playerSlotStart && index < playerSlotEnd) {
            if (!moveItemStackTo(stack, FIRST_CRAFT_SLOT, CRAFT_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, playerSlotStart, playerSlotEnd, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

}
