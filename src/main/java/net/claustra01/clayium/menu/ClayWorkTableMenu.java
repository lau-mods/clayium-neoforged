package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayWorkTableBlockEntity;
import net.claustra01.clayium.network.IClayButtonMenu;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for Clay Work Table (Tier 0).
 */
public class ClayWorkTableMenu extends AbstractContainerMenu implements IClayButtonMenu {
    @Nullable
    private final ClayWorkTableBlockEntity blockEntity;
    private final BlockPos pos;
    private final Level level;
    private final ContainerData data;

    public ClayWorkTableMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos());
    }

    public ClayWorkTableMenu(int containerId, Inventory playerInv, BlockPos pos) {
        super(ClayiumMenus.CLAY_WORK_TABLE.get(), containerId);
        this.pos = pos;
        this.level = playerInv.player.level();
        BlockEntity be = this.level.getBlockEntity(pos);
        this.blockEntity = be instanceof ClayWorkTableBlockEntity cwt ? cwt : null;

        Container container = this.blockEntity != null ? wrapWorkTable(this.blockEntity) : new net.minecraft.world.SimpleContainer(5);
        addSlot(new Slot(container, 0, 17, 30));
        addSlot(new Slot(container, 1, 80, 17));
        addSlot(new ClayResultSlot(container, 2, 143, 30, SlotTextureKind.SMALL));
        addSlot(new ClayResultSlot(container, 3, 143, 55, SlotTextureKind.SMALL));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        if (this.blockEntity != null) {
            this.data = new ContainerData() {
                @Override
                public int get(int index) {
                    return switch (index) {
                        case 0 -> blockEntity.cookTime;
                        case 1 -> blockEntity.timeToCook;
                        case 2 -> blockEntity.cookingMethod;
                        default -> 0;
                    };
                }

                @Override
                public void set(int index, int value) {
                    switch (index) {
                        case 0 -> blockEntity.cookTime = value;
                        case 1 -> blockEntity.timeToCook = value;
                        case 2 -> blockEntity.cookingMethod = value;
                    }
                }

                @Override
                public int getCount() {
                    return 3;
                }
            };
        } else {
            this.data = new SimpleContainerData(3);
        }
        addDataSlots(this.data);
    }

    public int getCookTime() {
        return this.data.get(0);
    }

    public int getTimeToCook() {
        return this.data.get(1);
    }

    public int getCookingMethod() {
        return this.data.get(2);
    }

    public int getCookProgressScaled(int width) {
        int total = getTimeToCook();
        int cur = getCookTime();
        return total > 0 ? Math.min(width, cur * width / total) : 0;
    }

    public boolean canPushButton(int buttonId) {
        return this.blockEntity != null && this.blockEntity.canPushButton(buttonId) != 0;
    }

    @Override
    public void handleGuiButton(ServerPlayer player, int buttonId) {
        if (this.blockEntity != null && buttonId >= 1 && buttonId <= 6 && stillValid(player)) {
            this.blockEntity.pushButton(buttonId);
            broadcastChanges();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (!this.level.getBlockState(this.pos).is(ClayiumBlocks.byPath("clay_work_table").get())) {
            return false;
        }
        return player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 2 || index == 3) {
            if (!moveItemStackTo(stack, 4, 40, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (index >= 4) {
            if (ClayWorkTableBlockEntity.isItemTool(stack)) {
                if (!moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 1, false)) {
                if (index < 31) {
                    if (!moveItemStackTo(stack, 31, 40, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, 4, 31, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (!moveItemStackTo(stack, 4, 40, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    private static Container wrapWorkTable(ClayWorkTableBlockEntity be) {
        return new Container() {
            @Override
            public int getContainerSize() {
                return 5;
            }

            @Override
            public boolean isEmpty() {
                for (int i = 0; i < 4; i++) {
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
                ItemStack s = be.getStack(slot);
                if (s.isEmpty() || amount <= 0) {
                    return ItemStack.EMPTY;
                }
                int taken = Math.min(amount, s.getCount());
                ItemStack res = s.copyWithCount(taken);
                s.shrink(taken);
                if (s.isEmpty()) {
                    be.setStack(slot, ItemStack.EMPTY);
                } else {
                    setChanged();
                }
                return res;
            }

            @Override
            public ItemStack removeItemNoUpdate(int slot) {
                ItemStack s = be.getStack(slot);
                be.setStack(slot, ItemStack.EMPTY);
                return s;
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
                be.setChanged();
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }

            @Override
            public void clearContent() {
                for (int i = 0; i < 5; i++) {
                    be.setStack(i, ItemStack.EMPTY);
                }
            }
        };
    }
}
