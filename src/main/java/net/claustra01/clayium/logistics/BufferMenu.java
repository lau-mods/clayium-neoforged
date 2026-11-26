package net.claustra01.clayium.logistics;

import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Original ContainerNormalInventory over TileClayBuffer. */
public final class BufferMenu extends AbstractContainerMenu {
    private final BlockPos bufferPos;
    private final BufferBlockEntity buffer;
    private final NormalInventoryLayout layout;
    private final int storageSlots;

    public BufferMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, bufferAt(playerInventory, pos));
    }

    public BufferMenu(int containerId, Inventory playerInventory, BufferBlockEntity buffer) {
        super(LogisticsRegistration.BUFFER_MENU.get(), containerId);
        this.buffer = buffer;
        this.bufferPos = buffer == null ? BlockPos.ZERO : buffer.getBlockPos();
        this.layout = buffer == null ? new NormalInventoryLayout(1, 1, 1)
                : new NormalInventoryLayout(buffer.inventoryColumns(), buffer.inventoryRows(), 1);
        this.storageSlots = buffer == null ? 0 : buffer.inventory().getSlots();
        for (int row = 0; row < layout.rows() && buffer != null; row++) {
            for (int column = 0; column < layout.columns(); column++) {
                addSlot(new SlotItemHandler(buffer.inventory(), column + row * layout.columns(),
                        layout.slotX(column), layout.slotY(row)));
            }
        }
        int playerX = layout.playerX();
        int playerY = layout.machineHeight();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        playerX + 8 + column * 18, playerY + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, playerX + 8 + column * 18, playerY + 70));
        }
    }

    private static BufferBlockEntity bufferAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof BufferBlockEntity buffer ? buffer : null;
    }

    public NormalInventoryLayout layout() {
        return layout;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (index < storageSlots) {
            if (!moveItemStackTo(moving, storageSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(moving, 0, storageSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return buffer != null
                && !buffer.isRemoved()
                && player.level().getBlockEntity(bufferPos) == buffer
                && player.distanceToSqr(bufferPos.getX() + 0.5D, bufferPos.getY() + 0.5D, bufferPos.getZ() + 0.5D) <= 64.0D;
    }

    public BlockPos bufferPos() {
        return bufferPos;
    }

    public BufferBlockEntity buffer() {
        return buffer;
    }
}
