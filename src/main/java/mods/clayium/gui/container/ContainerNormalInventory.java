package mods.clayium.gui.container;

import invtweaks.api.container.ChestContainer;
import mods.clayium.block.tile.INormalInventory;
import mods.clayium.block.tile.InventoryMultiPage;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

@ChestContainer
public class ContainerNormalInventory extends ContainerTemp {
    public static int buttonIdPrevious = 6;
    public static int buttonIdNext = 7;

    public ContainerNormalInventory(InventoryPlayer player, INormalInventory tile, Block block) {
        super(player, new InventoryMultiPage(tile), block);
    }

    public boolean isMultipage() {
        return ((InventoryMultiPage)this.tile).isMultiPage();
    }

    public int getPresentPageNum() {
        return ((InventoryMultiPage)this.tile).getPresentPage() + 1;
    }

    public int getMaxPageNum() {
        return ((InventoryMultiPage)this.tile).pageNum;
    }

    public void setMachineInventorySlots(InventoryPlayer player) {
        INormalInventory tile = (INormalInventory)this.tile;
        int offsetY = 18;
        if (tile.getInventoryY() > 6) {
            offsetY -= 12;
        }

        for (int j = 0; j < tile.getInventoryY(); j++) {
            for (int i = 0; i < tile.getInventoryX(); i++) {
                this.addMachineSlotToContainer(
                    new Slot(
                        tile,
                        tile.getInventoryStart() + i + j * tile.getInventoryX(),
                        i * 18 + (this.machineGuiSizeX - 18 * tile.getInventoryX()) / 2 + 1,
                        j * 18 + offsetY
                    )
                );
            }
        }
    }

    protected void initParameters(InventoryPlayer player) {
        INormalInventory tile = (INormalInventory)this.tile;
        this.machineGuiSizeY = tile.getInventoryY() * 18 + 18;
        if (tile.getInventoryY() > 6) {
            this.machineGuiSizeY -= 20;
        }

        this.machineGuiSizeX = this.machineGuiSizeX + (tile.getInventoryX() > 9 ? (tile.getInventoryX() - 9) * 18 : 0);
        if (this.isLargeCallback()) {
            this.machineGuiSizeX += 8;
        }

        super.initParameters(player);
    }

    public boolean drawInventoryName() {
        return ((INormalInventory)this.tile).getInventoryY() <= 6;
    }

    public boolean drawPlayerInventoryName() {
        return ((INormalInventory)this.tile).getInventoryY() <= 6;
    }

    public boolean canTransferToMachineInventory(ItemStack itemstack1) {
        return true;
    }

    public boolean transferStackToMachineInventory(ItemStack itemstack1) {
        return this.func_75135_a(itemstack1, 0, this.playerSlotIndex, false);
    }

    public boolean transferStackFromMachineInventory(ItemStack itemstack1, int slot) {
        return this.transferStackToPlayerInventory(itemstack1, true);
    }

    public boolean pushClientButton(EntityPlayer player, int action) {
        return action != buttonIdPrevious && action != buttonIdNext ? true : this.func_75140_a(player, action);
    }

    @ChestContainer.RowSizeCallback
    public int rowSizeCallback() {
        return ((INormalInventory)this.tile).getInventoryX();
    }

    @ChestContainer.IsLargeCallback
    public boolean isLargeCallback() {
        return !this.drawPlayerInventoryName();
    }
}
