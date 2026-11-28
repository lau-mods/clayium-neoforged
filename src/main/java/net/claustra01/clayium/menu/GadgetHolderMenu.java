package net.claustra01.clayium.menu;

import net.claustra01.clayium.item.GadgetHolderItem;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Menu for Gadget Holder (5x2 inventory stored inside {@link ClayiumDataComponents#GADGET_INVENTORY}).
 */
public class GadgetHolderMenu extends ClayiumBaseMenu {
    private static final int COLS = 5;
    private static final int ROWS = 2;
    private static final int SLOT_COUNT = COLS * ROWS;

    private final int heldSlotIndex;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final Container holderContainer;

    public GadgetHolderMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readVarInt());
    }

    public GadgetHolderMenu(int containerId, Inventory playerInv, int heldSlotIndex) {
        super(ClayiumMenus.GADGET_HOLDER.get(), containerId, playerInv, null, null, null);
        this.heldSlotIndex = heldSlotIndex;
        loadFromHeldStack();
        this.holderContainer = new HolderContainer();
        initializeLayout();
    }

    public static int resolveHeldSlotIndex(Inventory inv, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? inv.selected : -1;
    }

    private ItemStack getHeldHolderStack() {
        if (this.heldSlotIndex < 0) {
            return this.playerInventory.player.getOffhandItem();
        }
        return this.playerInventory.getItem(this.heldSlotIndex);
    }

    private void loadFromHeldStack() {
        ItemStack held = getHeldHolderStack();
        ItemContainerContents contents = held.get(ClayiumDataComponents.GADGET_INVENTORY.get());
        if (contents != null) {
            contents.copyInto(this.items);
        }
    }

    private void saveToHeldStack() {
        ItemStack held = getHeldHolderStack();
        if (held.getItem() instanceof GadgetHolderItem) {
            held.set(ClayiumDataComponents.GADGET_INVENTORY.get(), ItemContainerContents.fromItems(this.items));
        }
    }

    @Override
    protected void configureLayout() {
        this.machineGuiSizeY = ROWS * 18 + 18;
    }

    @Override
    protected void addMachineSlots() {
        int baseX = (this.machineGuiSizeX - 18 * COLS) / 2;
        for (int j = 0; j < ROWS; j++) {
            for (int i = 0; i < COLS; i++) {
                addSlot(new ClaySlot(
                        this.holderContainer,
                        i + j * COLS,
                        i * 18 + baseX,
                        j * 18 + 18,
                        SlotTextureKind.SMALL) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !(stack.getItem() instanceof GadgetHolderItem);
                    }
                });
            }
        }
    }

    @Override
    protected void addPlayerInventorySlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(
                        this.playerInventory,
                        col + row * 9 + 9,
                        this.playerSlotOffsetX + 8 + col * 18,
                        this.playerSlotOffsetY + 12 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            final int slotCol = col;
            addSlot(new Slot(
                    this.playerInventory,
                    col,
                    this.playerSlotOffsetX + 8 + col * 18,
                    this.playerSlotOffsetY + 70) {
                @Override
                public boolean mayPickup(Player player) {
                    return slotCol != heldSlotIndex;
                }
            });
        }
    }

    @Override
    protected boolean canTransferToMachineInventory(ItemStack stack) {
        return !(stack.getItem() instanceof GadgetHolderItem);
    }

    @Override
    public boolean stillValid(Player player) {
        return getHeldHolderStack().getItem() instanceof GadgetHolderItem;
    }

    private class HolderContainer implements Container {
        @Override
        public int getContainerSize() {
            return SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            return items.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.get(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack s = getItem(slot);
            if (s.isEmpty() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            int take = Math.min(amount, s.getCount());
            ItemStack out = s.copyWithCount(take);
            s.shrink(take);
            if (s.isEmpty()) {
                items.set(slot, ItemStack.EMPTY);
            }
            saveToHeldStack();
            return out;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack s = getItem(slot);
            items.set(slot, ItemStack.EMPTY);
            saveToHeldStack();
            return s;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= 0 && slot < SLOT_COUNT) {
                items.set(slot, stack);
                saveToHeldStack();
            }
        }

        @Override
        public void setChanged() {
            saveToHeldStack();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            items.clear();
            saveToHeldStack();
        }
    }
}
