package net.claustra01.clayium.logistics.multitrack;

import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class MultitrackBufferMenu extends AbstractContainerMenu {
    private final MultitrackBufferBlockEntity buffer;
    private final BlockPos bufferPos;
    private final ContainerData data;
    private int selectedTrack;

    public MultitrackBufferMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, bufferAt(inventory, pos), new SimpleContainerData(14));
    }

    public MultitrackBufferMenu(int id, Inventory playerInventory, MultitrackBufferBlockEntity buffer) {
        this(id, playerInventory, buffer, null);
    }

    private MultitrackBufferMenu(int id, Inventory playerInventory, MultitrackBufferBlockEntity buffer,
                                 ContainerData clientData) {
        super(MultitrackRegistration.MULTITRACK_BUFFER_MENU.get(), id);
        this.buffer = buffer;
        this.bufferPos = buffer == null ? BlockPos.ZERO : buffer.getBlockPos();
        this.data = clientData != null ? clientData : new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> buffer.tier();
                    case 1 -> selectedTrack;
                    case 2, 3, 4, 5, 6, 7 -> buffer.track(selectedTrack)
                            .faceMode(Direction.values()[index - 2]).ordinal();
                    case 8, 9, 10, 11, 12, 13 -> buffer.faceTrack(Direction.values()[index - 8]);
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return 14; }
        };
        if (buffer != null) {
            for (int track = 0; track < buffer.trackCount(); track++) {
                int slotTrack = track;
                for (int slot = 0; slot < MultitrackBufferBlockEntity.SLOTS_PER_TRACK; slot++) {
                    int x = 62 + slot % 3 * 18;
                    int y = 61 + slot / 3 * 18;
                    addSlot(new SlotItemHandler(buffer.track(track).inventory(), slot, x, y) {
                        @Override public boolean isActive() { return selectedTrack() == slotTrack; }
                    });
                }
                addSlot(new SlotItemHandler(buffer.track(track).filter(), 0, 126, 79) {
                    @Override public boolean isActive() { return selectedTrack() == slotTrack; }
                });
            }
        }
        addPlayerInventory(playerInventory);
        addDataSlots(this.data);
    }

    private static MultitrackBufferBlockEntity bufferAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof MultitrackBufferBlockEntity buffer ? buffer : null;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 35 + column * 18, 141 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 35 + column * 18, 199));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        int machineSlots = trackCount() *
                (MultitrackBufferBlockEntity.SLOTS_PER_TRACK + 1);
        if (index < machineSlots) {
            if (!moveItemStackTo(moving, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int trackStart = selectedTrack() * (MultitrackBufferBlockEntity.SLOTS_PER_TRACK + 1);
            int target = moving.is(LogisticsRegistration.ITEM_FILTER.get())
                    ? trackStart + MultitrackBufferBlockEntity.SLOTS_PER_TRACK : trackStart;
            int end = moving.is(LogisticsRegistration.ITEM_FILTER.get())
                    ? target + 1 : target + MultitrackBufferBlockEntity.SLOTS_PER_TRACK;
            if (!moveItemStackTo(moving, target, end, false)) {
                return ItemStack.EMPTY;
            }
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

    public MultitrackBufferBlockEntity buffer() { return buffer; }
    public BlockPos bufferPos() { return bufferPos; }
    public int tier() { return data.get(0); }
    public int trackCount() { return Math.min(6, tier() - 2); }
    public int selectedTrack() { return data.get(1); }
    public void setSelectedTrack(int track) {
        selectedTrack = track;
        broadcastChanges();
    }
    public BufferBlockEntity.FaceMode faceMode(Direction direction) {
        return BufferBlockEntity.FaceMode.values()[data.get(2 + direction.get3DDataValue())];
    }
    public int faceTrack(Direction direction) { return data.get(8 + direction.get3DDataValue()); }
}
