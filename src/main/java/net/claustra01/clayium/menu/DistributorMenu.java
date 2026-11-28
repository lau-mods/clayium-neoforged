package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.DistributorBlockEntity;
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
 * Menu for Clay Distributor (T7..T9): colonies arranged in grid sections matching {@code ContainerClayDistributor}.
 */
public class DistributorMenu extends ClayiumBaseMenu {
    @Nullable
    private final DistributorBlockEntity distributor;
    private final Container container;

    public DistributorMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public DistributorMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.DISTRIBUTOR.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.distributor = be instanceof DistributorBlockEntity d ? d : null;
        this.container = this.distributor != null ? wrapContainer(this.distributor) : new SimpleContainer(48);
        initializeLayout();
    }

    @Override
    protected void configureLayout() {
        if (this.distributor != null) {
            this.machineGuiSizeY = this.distributor.inventoryY * this.distributor.inventoryColonyY * 18
                    + (this.distributor.inventoryColonyY - 1) * 2
                    + 18;
        }
    }

    @Override
    protected void addMachineSlots() {
        if (this.distributor == null) {
            return;
        }
        int totalW = this.distributor.inventoryX * this.distributor.inventoryColonyX * 18
                + (this.distributor.inventoryColonyX - 1) * 2;
        int baseX = (this.machineGuiSizeX - totalW) / 2 + 1;
        for (int jj = 0; jj < this.distributor.inventoryColonyY; jj++) {
            for (int ii = 0; ii < this.distributor.inventoryColonyX; ii++) {
                for (int j = 0; j < this.distributor.inventoryY; j++) {
                    for (int i = 0; i < this.distributor.inventoryX; i++) {
                        int slotIndex = i + this.distributor.inventoryX
                                * (j + this.distributor.inventoryY * (ii + this.distributor.inventoryColonyX * jj));
                        int x = i * 18 + ii * (this.distributor.inventoryX * 18 + 2) + baseX;
                        int y = j * 18 + jj * (this.distributor.inventoryY * 18 + 2) + 18;
                        addSlot(new ClaySlot(this.container, slotIndex, x, y, SlotTextureKind.SMALL));
                    }
                }
            }
        }
    }

    @Override
    protected boolean canTransferToMachineInventory(ItemStack stack) {
        return this.distributor != null && !this.distributor.isCrowded();
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        if (this.distributor == null) {
            return false;
        }
        int colonySize = this.distributor.inventoryX * this.distributor.inventoryY;
        int colonyCount = this.distributor.inventoryColonyX * this.distributor.inventoryColonyY;
        int colony = Math.max(0, Math.min(colonyCount - 1, this.distributor.autoExtractColony));
        int start = colonySize * colony;
        int end = Math.min(this.machineSlotCount, start + colonySize);
        return start < end && moveItemStackTo(stack, start, end, false);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.distributor != null && this.distributor.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }

    @Nullable
    public DistributorBlockEntity getDistributor() {
        return this.distributor;
    }
}
