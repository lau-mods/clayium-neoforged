package net.claustra01.clayium.menu;

import net.claustra01.clayium.component.ClayiumDataComponentTypes.FilterConfigData;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.item.FilterItem;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for String-pattern item filters (Tag, Display Name, Translation Key, Registry ID, Mod ID, Damage, BlockState).
 */
public class ItemFilterStringMenu extends ClayiumBaseMenu {
    private final int heldSlotIndex;

    public ItemFilterStringMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readVarInt());
    }

    public ItemFilterStringMenu(int containerId, Inventory playerInv, int heldSlotIndex) {
        super(ClayiumMenus.ITEM_FILTER_STRING.get(), containerId, playerInv, null, null, null);
        this.heldSlotIndex = heldSlotIndex;
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

    public String getFilterString() {
        return ClayiumFilterMatcher.getConfig(getHeldFilterStack()).pattern();
    }

    public void applyFilterText(String pattern) {
        ItemStack held = getHeldFilterStack();
        if (!(held.getItem() instanceof FilterItem)) {
            return;
        }
        String sanitized = pattern == null ? "" : pattern;
        if (sanitized.length() > 128) {
            sanitized = sanitized.substring(0, 128);
        }
        FilterConfigData cur = ClayiumFilterMatcher.getConfig(held);
        held.set(
                ClayiumDataComponents.FILTER_CONFIG.get(),
                new FilterConfigData(sanitized, cur.regex(), cur.slots(), cur.copy())
        );
        broadcastChanges();
    }

    @Override
    protected void configureLayout() {
        this.machineGuiSizeY = 36;
    }

    @Override
    protected void addMachineSlots() {}

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
        return getHeldFilterStack().getItem() instanceof FilterItem;
    }
}
