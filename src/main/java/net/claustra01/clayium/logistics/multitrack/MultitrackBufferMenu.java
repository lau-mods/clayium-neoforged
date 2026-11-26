package net.claustra01.clayium.logistics.multitrack;

import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Original ContainerMultitrackBuffer: every track on its own row in the track colour, the
 * track's SlotMemory filter four pixels right of the row.
 */
public final class MultitrackBufferMenu extends AbstractContainerMenu {
    private final MultitrackBufferBlockEntity buffer;
    private final BlockPos bufferPos;
    private final int tracks;
    private final int slotsPerTrack;
    private final int machineSlots;

    public MultitrackBufferMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, bufferAt(inventory, pos));
    }

    public MultitrackBufferMenu(int id, Inventory playerInventory, MultitrackBufferBlockEntity buffer) {
        super(MultitrackRegistration.MULTITRACK_BUFFER_MENU.get(), id);
        this.buffer = buffer;
        this.bufferPos = buffer == null ? BlockPos.ZERO : buffer.getBlockPos();
        this.tracks = buffer == null ? 0 : buffer.trackCount();
        this.slotsPerTrack = buffer == null ? 0 : buffer.slotsPerTrack();
        int offsetX = trackOffsetX();
        for (int track = 0; track < tracks; track++) {
            for (int slot = 0; slot < slotsPerTrack; slot++) {
                addSlot(new SlotItemHandler(buffer.track(track).inventory(), slot,
                        slot * 18 + offsetX, track * 18 + 18));
            }
        }
        for (int track = 0; track < tracks; track++) {
            addSlot(new MemorySlot(buffer.track(track), slotsPerTrack * 18 + 4 + offsetX, track * 18 + 18));
        }
        machineSlots = tracks * (slotsPerTrack + 1);
        int playerX = (machineWidth() - 176) / 2;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        playerX + 8 + column * 18, machineHeight() + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, playerX + 8 + column * 18, machineHeight() + 70));
        }
    }

    private static MultitrackBufferBlockEntity bufferAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof MultitrackBufferBlockEntity buffer ? buffer : null;
    }

    public int trackCount() {
        return tracks;
    }

    public int slotsPerTrack() {
        return slotsPerTrack;
    }

    public int machineWidth() {
        return Math.max(176, slotsPerTrack * 18 + 4 + 18 + 8);
    }

    public int machineHeight() {
        return tracks * 18 + 18 - (tracks > 6 ? 20 : 0);
    }

    public int trackOffsetX() {
        return (machineWidth() - (slotsPerTrack * 18 + 4 + 18)) / 2 + 1;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= tracks * slotsPerTrack && slotId < machineSlots) {
            if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) {
                ItemStack carried = getCarried();
                ((MemorySlot)slots.get(slotId)).remember(carried);
                broadcastChanges();
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        int trackSlots = tracks * slotsPerTrack;
        if (index < machineSlots) {
            if (!moveItemStackTo(moving, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(moving, 0, trackSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return buffer != null && !buffer.isRemoved() && player.level().getBlockEntity(bufferPos) == buffer
                && player.distanceToSqr(bufferPos.getX() + 0.5, bufferPos.getY() + 0.5,
                bufferPos.getZ() + 0.5) <= 64.0;
    }

    public MultitrackBufferBlockEntity buffer() {
        return buffer;
    }

    public BlockPos bufferPos() {
        return bufferPos;
    }

    private static final class MemorySlot extends SlotItemHandler {
        private final MultitrackBufferBlockEntity.Track track;

        private MemorySlot(MultitrackBufferBlockEntity.Track track, int x, int y) {
            super(track.filter(), 0, x, y);
            this.track = track;
        }

        private void remember(ItemStack carried) {
            track.filter().setStackInSlot(0, carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
