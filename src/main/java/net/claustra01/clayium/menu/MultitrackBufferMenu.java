package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.blockentity.MultitrackBufferBlockEntity;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for Multitrack Buffer (T4..T13): colored track slots paired with per-track phantom filter slots.
 */
public class MultitrackBufferMenu extends ClayiumBaseMenu {
    @Nullable
    private final MultitrackBufferBlockEntity buffer;
    private final Container container;
    private int storageSlotCount = 0;

    public MultitrackBufferMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public MultitrackBufferMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.MULTITRACK_BUFFER.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.buffer = be instanceof MultitrackBufferBlockEntity mb ? mb : null;
        this.container = this.buffer != null ? wrapContainer(this.buffer) : new SimpleContainer(60);
        initializeLayout();
    }

    @Override
    protected void configureLayout() {
        if (this.buffer == null) {
            return;
        }
        int y = this.buffer.tracks.length;
        this.machineGuiSizeY = y * 18 + 18;
        if (y > 6) {
            this.machineGuiSizeY -= 20;
        }
        int maxTrackWidth = 0;
        for (int[] track : this.buffer.tracks) {
            maxTrackWidth = Math.max(maxTrackWidth, track.length);
        }
        this.machineGuiSizeX = Math.max(this.machineGuiSizeX, maxTrackWidth * 18 + 4 + 18 + 8);
    }

    @Override
    protected void addMachineSlots() {
        if (this.buffer == null) {
            return;
        }
        int offsetY = 18;
        int maxTrackWidth = 0;
        for (int[] track : this.buffer.tracks) {
            maxTrackWidth = Math.max(maxTrackWidth, track.length);
        }
        int totalWidth = maxTrackWidth * 18 + 4 + 18;
        int offsetX = (this.machineGuiSizeX - totalWidth) / 2 + 1;
        int[] trackCols = new int[this.buffer.tracks.length];

        for (int slotIdx = 0; slotIdx < this.buffer.slot2track.length; slotIdx++) {
            int track = this.buffer.slot2track[slotIdx];
            if (track >= 0 && track < trackCols.length) {
                addSlot(new ClaySlot(
                        this.container,
                        slotIdx,
                        trackCols[track] * 18 + offsetX,
                        track * 18 + offsetY,
                        SlotTextureKind.multitrack(track)
                ).setRestricted());
                trackCols[track]++;
                this.storageSlotCount++;
            }
        }

        for (int t = 0; t < this.buffer.tracks.length; t++) {
            int filterSlot = MultitrackBufferBlockEntity.FILTER_SLOT_START + t;
            if (filterSlot < this.container.getContainerSize()) {
                addSlot(new ClayMemorySlot(
                        this.container,
                        filterSlot,
                        maxTrackWidth * 18 + 4 + offsetX,
                        t * 18 + offsetY,
                        SlotTextureKind.multitrackFilter(t)
                ));
            }
        }
    }

    @Override
    public boolean drawInventoryName() {
        return this.buffer == null || this.buffer.tracks.length <= 6;
    }

    @Override
    public boolean drawPlayerInventoryName() {
        return drawInventoryName();
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        return this.storageSlotCount > 0 && moveItemStackTo(stack, 0, this.storageSlotCount, false);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.buffer != null && this.buffer.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }

    @Nullable
    public ClayContainerBlockEntity getContainerEntity() {
        return this.buffer;
    }
}
