package mods.clayium.gui.container;

import mods.clayium.gui.SlotMemory;
import mods.clayium.item.InventoryInItemStack;
import mods.clayium.item.filter.IFilterSizeChecker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerItemFilterWhiteList extends ContainerTemp {
    static int inventoryX = 5;
    static int inventoryY = 2;
    protected EntityPlayer player;
    protected int filterPos;

    public ContainerItemFilterWhiteList(EntityPlayer player) {
        super(player.field_71071_by, null, null);
        this.player = player;
    }

    public void setMachineInventorySlots(InventoryPlayer player) {
        for (int j = 0; j < inventoryY; j++) {
            for (int i = 0; i < inventoryX; i++) {
                this.addMachineSlotToContainer(
                    new SlotMemory(this.tile, i + j * inventoryX, i * 18 + (this.machineGuiSizeX - 18 * inventoryX) / 2, j * 18 + 18)
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
                this.filterPos = this.field_75151_b.size() - 1;
            } else {
                this.func_75146_a(new Slot(player, var4, this.playerSlotOffsetX + 8 + var4 * 18, this.playerSlotOffsetY + 70));
            }
        }
    }

    protected void initParameters(InventoryPlayer player) {
        this.machineGuiSizeY = inventoryY * 18 + 18;
        this.tile = new InventoryInItemStack(player.func_70448_g(), "Items", inventoryX * inventoryY);
        super.initParameters(player);
    }

    public void func_75142_b() {
        super.func_75142_b();
        ((InventoryInItemStack)this.tile).setItemStack(this.func_75139_a(this.filterPos).func_75211_c());
        ItemStack item = this.func_75139_a(this.filterPos).func_75211_c();
        if (item != null && item.func_77973_b() instanceof IFilterSizeChecker) {
            ((IFilterSizeChecker)item.func_77973_b()).checkFilterSize(item, this.player, this.player.func_130014_f_());
        }
    }

    public boolean canTransferToMachineInventory(ItemStack itemstack1) {
        return false;
    }

    public boolean transferStackToMachineInventory(ItemStack itemstack1) {
        return false;
    }

    public boolean transferStackFromMachineInventory(ItemStack itemstack1, int slot) {
        return false;
    }
}
