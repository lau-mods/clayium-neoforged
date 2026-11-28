package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.BufferBlockEntity;
import net.claustra01.clayium.blockentity.CACollectorBlockEntity;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.blockentity.MetalChestBlockEntity;
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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Normal/Multi-page inventory menu covering Clay Buffer, CA Collector, and Metal Chests (up to 832 slots across pages).
 */
public class NormalInventoryMenu extends ClayiumBaseMenu implements IClayButtonMenu {
    public static final int BUTTON_PREVIOUS_PAGE = 6;
    public static final int BUTTON_NEXT_PAGE = 7;

    @Nullable
    private final ClayContainerBlockEntity containerEntity;
    private final Container backingContainer;
    private final int cols;
    private final int rows;
    private final int pages;
    private int currentPage = 0;
    private final int[] energyShorts = new int[4];

    public NormalInventoryMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public NormalInventoryMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.NORMAL_INVENTORY.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.containerEntity = be instanceof ClayContainerBlockEntity cc ? cc : null;
        this.backingContainer = this.containerEntity != null ? wrapContainer(this.containerEntity) : new SimpleContainer(27);

        if (this.containerEntity instanceof MetalChestBlockEntity chest) {
            this.cols = chest.getCols();
            this.rows = chest.getRows();
            this.pages = chest.getPages();
        } else if (this.containerEntity instanceof BufferBlockEntity buffer) {
            this.cols = buffer.inventoryX;
            this.rows = buffer.inventoryY;
            this.pages = 1;
        } else if (this.containerEntity instanceof CACollectorBlockEntity) {
            this.cols = 3;
            this.rows = 3;
            this.pages = 1;
        } else {
            int size = this.backingContainer.getContainerSize();
            this.cols = Math.min(9, Math.max(1, size));
            this.rows = Math.max(1, (size + this.cols - 1) / this.cols);
            this.pages = 1;
        }

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
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    if (containerEntity == null) {
                        return energyShorts[idx];
                    }
                    int[] buf = new int[4];
                    putLongIntoDataSlots(containerEntity.getClayEnergy(), buf);
                    return buf[idx];
                }

                @Override
                public void set(int value) {
                    energyShorts[idx] = value;
                }
            });
        }

        if (this.containerEntity instanceof MetalChestBlockEntity chest && !playerInv.player.isSpectator()) {
            chest.startOpen(playerInv.player);
        }
    }

    @Override
    protected void configureLayout() {
        this.machineGuiSizeY = this.rows * 18 + 18;
        if (this.rows > 6) {
            this.machineGuiSizeY -= 20;
        }
        if (this.cols > 9) {
            this.machineGuiSizeX += (this.cols - 9) * 18;
        }
        if (!drawPlayerInventoryName()) {
            this.machineGuiSizeX += 8;
        }
    }

    @Override
    protected void addMachineSlots() {
        int offsetY = this.rows > 6 ? 6 : 18;
        int slotsPerPage = this.cols * this.rows;
        for (int page = 0; page < this.pages; page++) {
            for (int r = 0; r < this.rows; r++) {
                for (int c = 0; c < this.cols; c++) {
                    int idx = page * slotsPerPage + c + r * this.cols;
                    if (idx < this.backingContainer.getContainerSize()) {
                        int x = c * 18 + (this.machineGuiSizeX - 18 * this.cols) / 2 + 1;
                        int y = r * 18 + offsetY;
                        addSlot(new PagedSlot(this.backingContainer, idx, x, y, page));
                    }
                }
            }
        }
    }

    public boolean isMultipage() {
        return this.pages > 1;
    }

    public int getPresentPageNum() {
        return this.currentPage + 1;
    }

    public int getMaxPageNum() {
        return this.pages;
    }

    public void setPage(int page) {
        if (this.pages <= 1) {
            this.currentPage = 0;
            return;
        }
        int p = page % this.pages;
        if (p < 0) {
            p += this.pages;
        }
        this.currentPage = p;
    }

    @Override
    public void handleGuiButton(ServerPlayer player, int buttonId) {
        if (!isMultipage() || !stillValid(player)) {
            return;
        }
        if (buttonId == BUTTON_PREVIOUS_PAGE) {
            setPage(this.currentPage - 1);
            broadcastChanges();
        } else if (buttonId == BUTTON_NEXT_PAGE) {
            setPage(this.currentPage + 1);
            broadcastChanges();
        }
    }

    @Override
    public boolean drawInventoryName() {
        return this.rows <= 6;
    }

    @Override
    public boolean drawPlayerInventoryName() {
        return this.rows <= 6;
    }

    @Nullable
    public ClayContainerBlockEntity getContainerEntity() {
        return this.containerEntity;
    }

    public boolean showsClayEnergy() {
        return this.containerEntity != null && this.containerEntity.acceptEnergyClay();
    }

    public long getClayEnergy() {
        if (this.containerEntity != null && !this.level.isClientSide) {
            return this.containerEntity.getClayEnergy();
        }
        return readLongFrom4Shorts(energyShorts[0], energyShorts[1], energyShorts[2], energyShorts[3]);
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        int slotsPerPage = this.cols * this.rows;
        int start = Math.min(this.currentPage * slotsPerPage, this.machineSlotCount);
        int end = Math.min(start + slotsPerPage, this.machineSlotCount);
        return start < end && moveItemStackTo(stack, start, end, false);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.containerEntity != null && this.containerEntity.isRemoved()) {
            return false;
        }
        if (this.interactionPos != null && this.level.getBlockEntity(this.interactionPos) == null) {
            return false;
        }
        return super.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.containerEntity instanceof MetalChestBlockEntity chest && !player.isSpectator()) {
            chest.stopOpen(player);
        }
    }

    private class PagedSlot extends Slot {
        private final int page;

        PagedSlot(Container container, int slot, int x, int y, int page) {
            super(container, slot, x, y);
            this.page = page;
        }

        @Override
        public boolean isActive() {
            return this.page == NormalInventoryMenu.this.currentPage;
        }
    }
}
