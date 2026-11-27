package mods.clayium.gui.container;

import mods.clayium.item.filter.IFilterSizeChecker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ContainerItemFilterString extends ContainerTemp {
    protected EntityPlayer player;
    protected int filterPos;

    public ContainerItemFilterString(EntityPlayer player) {
        super(player.field_71071_by, null, null);
        this.player = player;
    }

    public String getFilterString() {
        NBTTagCompound tag = this.player.func_71045_bC() != null ? this.player.func_71045_bC().func_77978_p() : null;
        return tag == null ? "" : tag.func_74779_i("FilterString");
    }

    public void setFilterString(String string) {
        if (this.player.func_71045_bC() != null) {
            NBTTagCompound tag = this.player.func_71045_bC().func_77942_o() ? this.player.func_71045_bC().func_77978_p() : new NBTTagCompound();
            tag.func_74778_a("FilterString", string);
            this.player.func_71045_bC().func_77982_d(tag);
            ((Slot)this.field_75151_b.get(this.filterPos)).func_75218_e();
            this.func_75142_b();
        }
    }

    public String getTextFieldString(EntityPlayer player, int id) {
        return this.getFilterString();
    }

    public void setTextFieldString(EntityPlayer player, String string, int id) {
        this.setFilterString(string);
    }

    public void func_75142_b() {
        super.func_75142_b();
        ItemStack item = this.func_75139_a(this.filterPos).func_75211_c();
        if (item != null && item.func_77973_b() instanceof IFilterSizeChecker) {
            ((IFilterSizeChecker)item.func_77973_b()).checkFilterSize(item, this.player, this.player.func_130014_f_());
        }
    }

    public void setMachineInventorySlots(InventoryPlayer player) {
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
        this.machineGuiSizeY = 36;
        super.initParameters(player);
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

    public String getInventoryName() {
        return this.func_75139_a(this.filterPos).func_75211_c().func_82833_r();
    }
}
