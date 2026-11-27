package mods.clayium.gui.container;

import mods.clayium.block.tile.TileAreaActivator;
import mods.clayium.block.tile.TileAreaMiner;
import mods.clayium.gui.RectangleTexture;
import mods.clayium.gui.SlotMemory;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAreaActivator extends ContainerTemp {
    public ContainerAreaActivator(InventoryPlayer player, TileAreaActivator tile, Block block) {
        super(player, tile, block);
    }

    public void setMachineInventorySlots(InventoryPlayer player) {
        TileAreaActivator tile = (TileAreaActivator)this.tile;

        for (int j = 0; j < tile.inventoryY; j++) {
            for (int i = 0; i < tile.inventoryX; i++) {
                this.addMachineSlotToContainer(new Slot(tile, i + j * tile.inventoryX, i * 18 + (this.machineGuiSizeX - 18 * tile.inventoryX) / 2, j * 18 + 18));
            }
        }

        this.addMachineSlotToContainer(
            new SlotMemory(
                tile,
                tile.filterHarvestSlot,
                tile.inventoryX * 18 + 4 + (this.machineGuiSizeX - 18 * tile.inventoryX) / 2,
                18,
                RectangleTexture.SmallSlotFilterTexture
            )
        );
    }

    protected void initParameters(InventoryPlayer player) {
        TileAreaActivator tile = (TileAreaActivator)this.tile;
        this.machineGuiSizeY = tile.inventoryY * 18 + 18 + 18;
        super.initParameters(player);
    }

    public boolean canTransferToMachineInventory(ItemStack itemstack1) {
        return true;
    }

    public boolean transferStackToMachineInventory(ItemStack itemstack1) {
        TileAreaMiner tile = (TileAreaMiner)this.tile;
        return this.func_75135_a(itemstack1, 0, tile.inventoryY * tile.inventoryX, false);
    }

    public boolean transferStackFromMachineInventory(ItemStack itemstack1, int slot) {
        return this.transferStackToPlayerInventory(itemstack1, true);
    }
}
