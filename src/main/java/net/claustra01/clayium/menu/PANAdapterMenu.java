package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.PANAdapterBlockEntity;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.network.IClayButtonMenu;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for PAN Adapter (T10..T13): 3x3 pattern memory slots, 3x3 result preview slots across 1..8 pages,
 * plus 9 bottom sub-item slots.
 */
public class PANAdapterMenu extends ClayiumBaseMenu implements IClayButtonMenu {
    public static final int BUTTON_PREVIOUS_PAGE = NormalInventoryMenu.BUTTON_PREVIOUS_PAGE;
    public static final int BUTTON_NEXT_PAGE = NormalInventoryMenu.BUTTON_NEXT_PAGE;

    @Nullable
    private final PANAdapterBlockEntity adapter;
    private final Container pagedContainer;
    private int currentPage = 0;
    private int maxPage = 1;

    public PANAdapterMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public PANAdapterMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.PAN_ADAPTER.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.adapter = be instanceof PANAdapterBlockEntity pa ? pa : null;
        this.maxPage = this.adapter != null ? Math.max(1, this.adapter.getPageNum()) : 1;
        if (this.adapter != null && !playerInv.player.level().isClientSide) {
            this.adapter.onSlotChange();
        }
        this.pagedContainer = this.adapter != null ? new AdapterPageContainer() : new SimpleContainer(27);
        initializeLayout();

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return currentPage;
            }

            @Override
            public void set(int value) {
                setPage(value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return maxPage;
            }

            @Override
            public void set(int value) {
                maxPage = Math.max(1, value);
            }
        });
    }

    @Override
    protected void configureLayout() {
        this.machineGuiSizeY = 96;
    }

    @Override
    protected void addMachineSlots() {
        int patternOffsetX = (this.machineGuiSizeX - 54) / 2 - 30 + 1;
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                addSlot(new ClayMemorySlot(
                        this.pagedContainer,
                        i + j * 3,
                        i * 18 + patternOffsetX,
                        j * 18 + 18,
                        SlotTextureKind.SMALL) {
                    @Override
                    public void handlePhantomClick(ItemStack carried) {
                        if (carried.isEmpty()) {
                            set(ItemStack.EMPTY);
                        } else {
                            ItemStack copy = carried.copy();
                            if (ClayiumFilterMatcher.isFilter(copy) && ClayiumFilterMatcher.isCopyMode(copy)) {
                                ClayiumFilterMatcher.withCopyFlag(copy, false);
                            }
                            set(copy);
                        }
                    }
                });
            }
        }

        int previewOffsetX = (this.machineGuiSizeX - 54) / 2 + 30 + 1;
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                addSlot(new ClayResultSlot(
                        this.pagedContainer,
                        9 + i + j * 3,
                        i * 18 + previewOffsetX,
                        j * 18 + 18,
                        SlotTextureKind.SMALL) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }
                });
            }
        }

        int subOffsetX = (this.machineGuiSizeX - 162) / 2 + 1;
        for (int i = 0; i < 9; i++) {
            addSlot(new ClaySlot(
                    this.pagedContainer,
                    18 + i,
                    i * 18 + subOffsetX,
                    74,
                    SlotTextureKind.SMALL));
        }
    }

    @Override
    protected boolean canTransferToMachineInventory(ItemStack stack) {
        return false;
    }

    @Override
    protected boolean transferStackFromMachineInventory(ItemStack stack, int slotIndex) {
        if (slotIndex < 18) {
            return false;
        }
        return super.transferStackFromMachineInventory(stack, slotIndex);
    }

    public boolean isMultipage() {
        return this.maxPage > 1;
    }

    public int getPresentPageNum() {
        return this.currentPage + 1;
    }

    public int getMaxPageNum() {
        return this.maxPage;
    }

    public void setPage(int page) {
        if (this.maxPage <= 1) {
            this.currentPage = 0;
            return;
        }
        this.currentPage = Math.floorMod(page, this.maxPage);
    }

    @Override
    public void handleGuiButton(ServerPlayer player, int buttonId) {
        if (buttonId == BUTTON_PREVIOUS_PAGE) {
            setPage(this.currentPage - 1);
            broadcastChanges();
        } else if (buttonId == BUTTON_NEXT_PAGE) {
            setPage(this.currentPage + 1);
            broadcastChanges();
        }
    }

    private final class AdapterPageContainer implements Container {
        private int resolveBackingSlot(int localSlot) {
            if (localSlot >= 0 && localSlot < 18) {
                return currentPage * PANAdapterBlockEntity.SLOTS_PER_PAGE + localSlot;
            }
            if (localSlot >= 18 && localSlot < 27) {
                return PANAdapterBlockEntity.SUB_SLOT_START + (localSlot - 18);
            }
            return -1;
        }

        @Override
        public int getContainerSize() {
            return 27;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < 27; i++) {
                if (!getItem(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            int backing = resolveBackingSlot(slot);
            return backing >= 0 && adapter != null ? adapter.getStack(backing) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (slot < 18) {
                return ItemStack.EMPTY;
            }
            int backing = resolveBackingSlot(slot);
            if (backing < 0 || adapter == null || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack cur = adapter.getStack(backing);
            if (cur.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack split = cur.split(amount);
            if (cur.isEmpty()) {
                adapter.setStack(backing, ItemStack.EMPTY);
            } else {
                adapter.onSlotChange();
            }
            return split;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            if (slot < 18) {
                return ItemStack.EMPTY;
            }
            int backing = resolveBackingSlot(slot);
            if (backing < 0 || adapter == null) {
                return ItemStack.EMPTY;
            }
            ItemStack cur = adapter.getStack(backing);
            adapter.setStack(backing, ItemStack.EMPTY);
            return cur;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            int backing = resolveBackingSlot(slot);
            if (backing >= 0 && adapter != null) {
                adapter.setStack(backing, stack);
            }
        }

        @Override
        public void setChanged() {
            if (adapter != null) {
                adapter.onSlotChange();
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return PANAdapterMenu.this.stillValid(player);
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < 27; i++) {
                setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
