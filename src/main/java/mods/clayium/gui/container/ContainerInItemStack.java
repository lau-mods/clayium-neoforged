package mods.clayium.gui.container;

import mods.clayium.item.InventoryInItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerInItemStack extends ContainerTemp {
    protected int inventoryX = 5;
    protected int inventoryY = 2;
    protected EntityPlayer player;
    protected int holderPos;
    protected String tagName;

    public ContainerInItemStack(EntityPlayer player, int inventoryX, int inventoryY, String tagName) {
        super(player.field_71071_by, null, null, inventoryX, inventoryY, tagName);
        this.player = player;
    }

    public void setMachineInventorySlots(InventoryPlayer player) {
        this.inventoryX = (Integer)this.additionalParams[0];
        this.inventoryY = (Integer)this.additionalParams[1];

        for (int j = 0; j < this.inventoryY; j++) {
            for (int i = 0; i < this.inventoryX; i++) {
                this.addMachineSlotToContainer(
                    new Slot(this.tile, i + j * this.inventoryX, i * 18 + (this.machineGuiSizeX - 18 * this.inventoryX) / 2, j * 18 + 18)
                );
            }
        }
    }

    public void addPlayerInventorySlots(InventoryPlayer player) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.func_75146_a(new Slot(player, j + i * 9 + 9, this.playerSlotOffsetX + 8 + j * 18, this.playerSlotOffsetY + 12 + i * 18));
            }
        }

        for (int var4 = 0; var4 < 9; var4++) {
            if (player.field_70461_c == var4) {
                this.func_75146_a(new Slot(player, var4, this.playerSlotOffsetX + 8 + var4 * 18, this.playerSlotOffsetY + 70) {
                    public boolean func_82869_a(EntityPlayer p_82869_1_) {
                        return false;
                    }
                });
                this.holderPos = this.field_75151_b.size() - 1;
            } else {
                this.func_75146_a(new Slot(player, var4, this.playerSlotOffsetX + 8 + var4 * 18, this.playerSlotOffsetY + 70));
            }
        }
    }

    protected void initParameters(InventoryPlayer player) {
        this.inventoryX = (Integer)this.additionalParams[0];
        this.inventoryY = (Integer)this.additionalParams[1];
        this.tagName = (String)this.additionalParams[2];
        this.machineGuiSizeY = this.inventoryY * 18 + 18;
        this.tile = new InventoryInItemStack(player.func_70448_g(), this.tagName, this.inventoryX * this.inventoryY);
        super.initParameters(player);
    }

    public void func_75142_b() {
        super.func_75142_b();
        ((InventoryInItemStack)this.tile).setItemStack(this.func_75139_a(this.holderPos).func_75211_c());
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
}
