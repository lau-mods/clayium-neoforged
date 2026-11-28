package net.claustra01.clayium.menu;

import net.claustra01.clayium.component.ClayiumDataComponentTypes.FilterConfigData;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.item.FilterItem;
import net.claustra01.clayium.item.GadgetItem;
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
 * Menu for Whitelist, Blacklist, and Fuzzy item filters (5x2 phantom sample slots backed by DataComponents).
 */
public class ItemFilterWhitelistMenu extends ClayiumBaseMenu {
    private static final int COLS = 5;
    private static final int ROWS = 2;
    private static final int SLOT_COUNT = COLS * ROWS;

    private final int heldSlotIndex;
    private final NonNullList<ItemStack> filterSlots = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final Container filterContainer;

    public ItemFilterWhitelistMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readVarInt());
    }

    public ItemFilterWhitelistMenu(int containerId, Inventory playerInv, int heldSlotIndex) {
        super(ClayiumMenus.ITEM_FILTER_WHITELIST.get(), containerId, playerInv, null, null, null);
        this.heldSlotIndex = heldSlotIndex;
        loadFromHeldFilter();
        this.filterContainer = new WhitelistContainer();
        initializeLayout();
    }

    public static int resolveHeldSlotIndex(Inventory inv, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? inv.selected : -1;
    }

    private ItemStack getHeldFilterStack() {
        if (this.heldSlotIndex < 0) {
            return this.playerInventory.player.getOffhandItem();
        }
        return this.playerInventory.getItem(this.heldSlotIndex);
    }

    private void loadFromHeldFilter() {
        ItemStack held = getHeldFilterStack();
        FilterConfigData cfg = ClayiumFilterMatcher.getConfig(held);
        cfg.slots().copyInto(this.filterSlots);
    }

    private static boolean isEditableFilterHolder(ItemStack stack) {
        return stack.getItem() instanceof FilterItem
                || (stack.getItem() instanceof GadgetItem gadget && gadget.getFamily() == GadgetItem.GadgetFamily.AUTO_EAT);
    }

    private void saveToHeldFilter() {
        ItemStack held = getHeldFilterStack();
        if (!isEditableFilterHolder(held)) {
            return;
        }
        FilterConfigData cur = ClayiumFilterMatcher.getConfig(held);
        FilterConfigData next = new FilterConfigData(
                cur.pattern(),
                cur.regex(),
                ItemContainerContents.fromItems(this.filterSlots),
                cur.copy()
        );
        held.set(ClayiumDataComponents.FILTER_CONFIG.get(), next);
        if (!this.level.isClientSide && ClayiumFilterMatcher.getFilterSize(held) > ClayiumFilterMatcher.MAX_FILTER_SIZE) {
            for (int i = SLOT_COUNT - 1; i >= 0 && ClayiumFilterMatcher.getFilterSize(held) > ClayiumFilterMatcher.MAX_FILTER_SIZE; i--) {
                if (!this.filterSlots.get(i).isEmpty()) {
                    this.filterSlots.set(i, ItemStack.EMPTY);
                    held.set(ClayiumDataComponents.FILTER_CONFIG.get(), new FilterConfigData(
                            cur.pattern(),
                            cur.regex(),
                            ItemContainerContents.fromItems(this.filterSlots),
                            cur.copy()
                    ));
                }
            }
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
                addSlot(new ClayMemorySlot(
                        this.filterContainer,
                        i + j * COLS,
                        i * 18 + baseX,
                        j * 18 + 18,
                        SlotTextureKind.SMALL));
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
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return isEditableFilterHolder(getHeldFilterStack());
    }

    private class WhitelistContainer implements Container {
        @Override
        public int getContainerSize() {
            return SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            return filterSlots.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? filterSlots.get(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= 0 && slot < SLOT_COUNT) {
                filterSlots.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
                saveToHeldFilter();
            }
        }

        @Override
        public void setChanged() {
            saveToHeldFilter();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            filterSlots.clear();
            saveToHeldFilter();
        }
    }
}
