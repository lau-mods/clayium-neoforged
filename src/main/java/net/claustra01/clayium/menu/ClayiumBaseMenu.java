package net.claustra01.clayium.menu;

import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Base {@link AbstractContainerMenu} for Clayium screens preserving exact 1.7.10 {@code ContainerTemp} layout metrics,
 * phantom {@link ClayMemorySlot} interactions, and ClayInterface remote interaction validity.
 */
public abstract class ClayiumBaseMenu extends AbstractContainerMenu {
    public int machineGuiSizeX = 176;
    public int machineGuiSizeY = 72;
    public int playerSlotOffsetX = 0;
    public int playerSlotOffsetY = 72;

    protected final Inventory playerInventory;
    protected final Level level;
    @Nullable
    protected final BlockPos interactionPos;
    @Nullable
    protected final Block expectedBlock;
    @Nullable
    private final BooleanSupplier customValidityCheck;

    protected int machineSlotCount = 0;

    protected ClayiumBaseMenu(
            MenuType<?> menuType,
            int containerId,
            Inventory playerInventory,
            @Nullable BlockPos interactionPos,
            @Nullable Block expectedBlock,
            @Nullable BooleanSupplier customValidityCheck) {
        super(menuType, containerId);
        this.playerInventory = playerInventory;
        this.level = playerInventory.player.level();
        this.interactionPos = interactionPos;
        this.expectedBlock = expectedBlock;
        this.customValidityCheck = customValidityCheck;
    }

    protected final void initializeLayout() {
        configureLayout();
        this.playerSlotOffsetX = (this.machineGuiSizeX - 176) / 2;
        this.playerSlotOffsetY = this.machineGuiSizeY;
        int startMachineSlots = this.slots.size();
        addMachineSlots();
        this.machineSlotCount = this.slots.size() - startMachineSlots;
        addPlayerInventorySlots();
    }

    protected void configureLayout() {}

    protected abstract void addMachineSlots();

    protected void addPlayerInventorySlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(
                        this.playerInventory,
                        col + row * 9 + 9,
                        this.playerSlotOffsetX + 8 + col * 18,
                        this.playerSlotOffsetY + 12 + row * 18
                ));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(
                    this.playerInventory,
                    col,
                    this.playerSlotOffsetX + 8 + col * 18,
                    this.playerSlotOffsetY + 70
            ));
        }
    }

    public int getTotalHeight() {
        return this.machineGuiSizeY + 94;
    }

    public int getMachineSlotCount() {
        return this.machineSlotCount;
    }

    public boolean drawInventoryName() {
        return true;
    }

    public boolean drawPlayerInventoryName() {
        return drawInventoryName();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.customValidityCheck != null && !this.customValidityCheck.getAsBoolean()) {
            return false;
        }
        if (this.interactionPos != null) {
            if (this.expectedBlock != null && !this.level.getBlockState(this.interactionPos).is(this.expectedBlock)) {
                return false;
            }
            return player.distanceToSqr(
                    this.interactionPos.getX() + 0.5,
                    this.interactionPos.getY() + 0.5,
                    this.interactionPos.getZ() + 0.5
            ) <= 64.0;
        }
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size() && this.slots.get(slotId) instanceof ClayMemorySlot memorySlot) {
            if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE || clickType == ClickType.CLONE) {
                memorySlot.handlePhantomClick(getCarried());
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    protected boolean canTransferToMachineInventory(ItemStack stack) {
        return true;
    }

    protected boolean transferStackToMachineInventory(ItemStack stack) {
        if (this.machineSlotCount <= 0) {
            return false;
        }
        return moveItemStackTo(stack, 0, this.machineSlotCount, false);
    }

    protected boolean transferStackFromMachineInventory(ItemStack stack, int slotIndex) {
        return moveItemStackToPlayerInventory(stack, slotIndex >= 0);
    }

    protected boolean moveItemStackToPlayerInventory(ItemStack stack, boolean reverseDirection) {
        int playerStart = this.machineSlotCount;
        int playerEnd = this.slots.size();
        if (playerStart >= playerEnd) {
            return false;
        }
        return moveItemStackTo(stack, playerStart, playerEnd, reverseDirection);
    }

    protected int getQuickMoveSlotLimit(int slotIndex) {
        return Integer.MAX_VALUE;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = this.slots.get(index);
        if (slot instanceof ClayMemorySlot) {
            slot.set(ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stackInSlot = slot.getItem();
        ItemStack originalCopy = stackInSlot.copy();

        if (index < this.machineSlotCount) {
            if (!transferStackFromMachineInventory(stackInSlot, index)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!canTransferToMachineInventory(stackInSlot) || !transferStackToMachineInventory(stackInSlot)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stackInSlot.getCount() == originalCopy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stackInSlot);
        return originalCopy;
    }

    protected List<Slot> allSlotsView() {
        return this.slots;
    }

    /**
     * Wraps a {@link net.claustra01.clayium.blockentity.ClayContainerBlockEntity} as a standard {@link Container}.
     */
    public static Container wrapContainer(net.claustra01.clayium.blockentity.ClayContainerBlockEntity be) {
        return new Container() {
            @Override
            public int getContainerSize() {
                return be.getInventorySize();
            }

            @Override
            public boolean isEmpty() {
                for (int i = 0; i < getContainerSize(); i++) {
                    if (!be.getStack(i).isEmpty()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public ItemStack getItem(int slot) {
                return be.getStack(slot);
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack stack = be.getStack(slot);
                if (stack.isEmpty() || amount <= 0) {
                    return ItemStack.EMPTY;
                }
                int taken = Math.min(amount, stack.getCount());
                ItemStack out = stack.copyWithCount(taken);
                stack.shrink(taken);
                if (stack.isEmpty()) {
                    be.setStack(slot, ItemStack.EMPTY);
                } else {
                    setChanged();
                }
                return out;
            }

            @Override
            public ItemStack removeItemNoUpdate(int slot) {
                ItemStack stack = be.getStack(slot);
                be.setStack(slot, ItemStack.EMPTY);
                return stack;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                be.setStack(slot, stack);
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }

            @Override
            public void setChanged() {
                be.markForSync();
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }

            @Override
            public boolean canPlaceItem(int index, ItemStack stack) {
                return be.isItemValidForSlot(index, stack);
            }

            @Override
            public void clearContent() {
                for (int i = 0; i < getContainerSize(); i++) {
                    be.setStack(i, ItemStack.EMPTY);
                }
            }
        };
    }

    public static void putLongIntoDataSlots(long value, int[] highLow4) {
        highLow4[0] = (int) ((value >>> 48) & 0xFFFFL);
        highLow4[1] = (int) ((value >>> 32) & 0xFFFFL);
        highLow4[2] = (int) ((value >>> 16) & 0xFFFFL);
        highLow4[3] = (int) (value & 0xFFFFL);
    }

    public static long readLongFrom4Shorts(int s0, int s1, int s2, int s3) {
        return (((long) (s0 & 0xFFFF)) << 48)
                | (((long) (s1 & 0xFFFF)) << 32)
                | (((long) (s2 & 0xFFFF)) << 16)
                | ((long) (s3 & 0xFFFF));
    }
}
