package net.claustra01.clayium.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayCraftingTableBlockEntity;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/**
 * Menu for Clay Crafting Table (Tier 0): persistent 3x3 grid plus optional adjacent chest inventory.
 * Matches original {@code ContainerClayCraftingTable} & {@code SlotCCrafting} behavior: consumes matching
 * ingredients from the attached chest first before decrementing the 3x3 crafting grid.
 */
public class ClayCraftingTableMenu extends ClayiumBaseMenu {
    private static final int GRID_X = 30;
    private static final int GRID_Y = 17;
    private static final int CHEST_X = 8;
    private static final int CHEST_Y = 73;
    private static final int RESULT_X = 124;
    private static final int RESULT_Y = 35;

    @Nullable
    private final ClayCraftingTableBlockEntity tableEntity;
    private final Container gridContainer;
    @Nullable
    private final Container chestContainer;
    private final int chestRows;
    private final int chestCols;
    private final ResultContainer resultContainer = new ResultContainer();
    private final GridCraftingView craftMatrix;
    private int resultSlotIndex;

    public ClayCraftingTableMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos(), findAdjacentChestPos(playerInv, buf));
    }

    private static @Nullable BlockPos findAdjacentChestPos(Inventory playerInv, FriendlyByteBuf buf) {
        boolean hasChest = buf.readBoolean();
        return hasChest ? buf.readBlockPos() : null;
    }

    public static void writeInitData(FriendlyByteBuf buf, BlockPos targetPos, BlockPos interactionPos, @Nullable BlockPos chestPos) {
        buf.writeBlockPos(targetPos);
        buf.writeBlockPos(interactionPos);
        buf.writeBoolean(chestPos != null);
        if (chestPos != null) {
            buf.writeBlockPos(chestPos);
        }
    }

    @Nullable
    public static BlockPos findAdjacentChest(net.minecraft.world.level.Level level, BlockPos origin) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = origin.relative(dir);
            if (level.getBlockEntity(neighbor) instanceof ChestBlockEntity) {
                return neighbor;
            }
        }
        return null;
    }

    public ClayCraftingTableMenu(
            int containerId,
            Inventory playerInv,
            BlockPos targetPos,
            BlockPos interactionPos,
            @Nullable BlockPos chestPos) {
        super(ClayiumMenus.CLAY_CRAFTING_TABLE.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.tableEntity = be instanceof ClayCraftingTableBlockEntity cct ? cct : null;
        this.gridContainer = this.tableEntity != null ? wrapContainer(this.tableEntity) : new SimpleContainer(9);

        Container attachedChest = null;
        if (chestPos != null && playerInv.player.level().getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            attachedChest = chest;
        }
        this.chestContainer = attachedChest;
        if (attachedChest != null) {
            this.chestCols = 9;
            this.chestRows = Math.max(1, Math.min(3, attachedChest.getContainerSize() / 9));
        } else {
            this.chestCols = 0;
            this.chestRows = 0;
        }
        this.craftMatrix = new GridCraftingView(this.gridContainer);

        initializeLayout();
        updateCraftingResult();
    }

    @Override
    protected void configureLayout() {
        int maxBottom = GRID_Y + 3 * 18;
        if (this.chestContainer != null) {
            maxBottom = Math.max(maxBottom, CHEST_Y + this.chestRows * 18);
        }
        this.machineGuiSizeY = maxBottom + 1;
    }

    @Override
    protected void addMachineSlots() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new ClaySlot(this.gridContainer, x + y * 3, GRID_X + x * 18, GRID_Y + y * 18, SlotTextureKind.SMALL) {
                    @Override
                    public void setChanged() {
                        super.setChanged();
                        updateCraftingResult();
                    }
                });
            }
        }
        if (this.chestContainer != null) {
            for (int y = 0; y < this.chestRows; y++) {
                for (int x = 0; x < this.chestCols; x++) {
                    addSlot(new ClaySlot(
                            this.chestContainer,
                            x + y * this.chestCols,
                            CHEST_X + x * 18,
                            CHEST_Y + y * 18,
                            SlotTextureKind.SMALL));
                }
            }
        }
        this.resultSlotIndex = this.slots.size();
        addSlot(new CraftingResultSlot(this.playerInventory.player, this.resultContainer, RESULT_X, RESULT_Y));
    }

    public boolean hasAdjacentChest() {
        return this.chestContainer != null;
    }

    private void updateCraftingResult() {
        if (this.level == null) {
            return;
        }
        CraftingInput input = this.craftMatrix.asCraftInput();
        ItemStack result = ItemStack.EMPTY;
        Optional<RecipeHolder<CraftingRecipe>> match = this.level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, this.level);
        if (match.isPresent()) {
            CraftingRecipe recipe = match.get().value();
            if (this.level.isClientSide || !(this.playerInventory.player instanceof ServerPlayer sp)
                    || this.resultContainer.setRecipeUsed(this.level, sp, match.get())) {
                ItemStack assembled = recipe.assemble(input, this.level.registryAccess());
                if (assembled.isItemEnabled(this.level.enabledFeatures())) {
                    result = assembled;
                }
            }
        }
        this.resultContainer.setItem(0, result);
        if (!this.level.isClientSide && this.playerInventory.player instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, incrementStateId(), this.resultSlotIndex, result));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        updateCraftingResult();
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        if (this.chestContainer != null && moveItemStackTo(stack, 9, this.resultSlotIndex, false)) {
            return true;
        }
        return moveItemStackTo(stack, 0, 9, false);
    }

    @Override
    protected boolean transferStackFromMachineInventory(ItemStack stack, int slotIndex) {
        if (slotIndex == this.resultSlotIndex) {
            if (this.chestContainer != null && moveItemStackTo(stack, 9, this.resultSlotIndex, true)) {
                return true;
            }
            return moveItemStackToPlayerInventory(stack, true);
        }
        if (slotIndex >= 9 && slotIndex < this.resultSlotIndex) {
            if (moveItemStackTo(stack, 0, 9, false)) {
                return true;
            }
        } else if (slotIndex < 9 && this.chestContainer != null && moveItemStackTo(stack, 9, this.resultSlotIndex, false)) {
            return true;
        }
        return moveItemStackToPlayerInventory(stack, false);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.tableEntity != null && this.tableEntity.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }

    private class CraftingResultSlot extends Slot {
        private final Player player;

        CraftingResultSlot(Player player, Container container, int x, int y) {
            super(container, 0, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            stack.onCraftedBy(player.level(), player, stack.getCount());
            var remainingItems = ClayCraftingTableMenu.this.level.getRecipeManager()
                    .getRemainingItemsFor(RecipeType.CRAFTING, ClayCraftingTableMenu.this.craftMatrix.asCraftInput(), ClayCraftingTableMenu.this.level);

            for (int i = 0; i < 9; i++) {
                ItemStack gridStack = ClayCraftingTableMenu.this.gridContainer.getItem(i);
                ItemStack rem = i < remainingItems.size() ? remainingItems.get(i) : ItemStack.EMPTY;
                if (!gridStack.isEmpty()) {
                    boolean tookFromChest = false;
                    if (ClayCraftingTableMenu.this.chestContainer != null) {
                        for (int k = 0; k < ClayCraftingTableMenu.this.chestContainer.getContainerSize(); k++) {
                            ItemStack chestStack = ClayCraftingTableMenu.this.chestContainer.getItem(k);
                            if (!chestStack.isEmpty() && ItemStack.isSameItemSameComponents(chestStack, gridStack)) {
                                ClayCraftingTableMenu.this.chestContainer.removeItem(k, 1);
                                tookFromChest = true;
                                break;
                            }
                        }
                    }
                    if (!tookFromChest) {
                        ClayCraftingTableMenu.this.gridContainer.removeItem(i, 1);
                    }
                }
                if (!rem.isEmpty()) {
                    if (!player.getInventory().add(rem)) {
                        if (ClayCraftingTableMenu.this.gridContainer.getItem(i).isEmpty()) {
                            ClayCraftingTableMenu.this.gridContainer.setItem(i, rem);
                        } else {
                            player.drop(rem, false);
                        }
                    }
                }
            }
            if (ClayCraftingTableMenu.this.tableEntity != null) {
                ClayCraftingTableMenu.this.tableEntity.markForSync();
            }
            updateCraftingResult();
        }
    }

    private static class GridCraftingView implements CraftingContainer {
        private final Container backing;

        GridCraftingView(Container backing) {
            this.backing = backing;
        }

        @Override
        public int getWidth() {
            return 3;
        }

        @Override
        public int getHeight() {
            return 3;
        }

        @Override
        public List<ItemStack> getItems() {
            List<ItemStack> out = new ArrayList<>(9);
            for (int i = 0; i < 9; i++) {
                out.add(this.backing.getItem(i));
            }
            return out;
        }

        @Override
        public int getContainerSize() {
            return 9;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < 9; i++) {
                if (!this.backing.getItem(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot >= 0 && slot < 9 ? this.backing.getItem(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return this.backing.removeItem(slot, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return this.backing.removeItemNoUpdate(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            this.backing.setItem(slot, stack);
        }

        @Override
        public void setChanged() {
            this.backing.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < 9; i++) {
                this.backing.setItem(i, ItemStack.EMPTY);
            }
        }

        @Override
        public void fillStackedContents(StackedContents contents) {
            for (int i = 0; i < 9; i++) {
                contents.accountSimpleStack(this.backing.getItem(i));
            }
        }
    }
}
