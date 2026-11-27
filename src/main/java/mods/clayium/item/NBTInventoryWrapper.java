package mods.clayium.item;

import mods.clayium.util.UtilItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

public class NBTInventoryWrapper implements IInventory {
    protected NBTTagList tagList;
    protected ItemStack[] containerItemStacks;
    protected String inventoryName;

    NBTInventoryWrapper(NBTTagList tagList, int sizeInventory) {
        super();
        this.containerItemStacks = new ItemStack[sizeInventory];
        this.setTagList(tagList);
    }

    public void setTagList(NBTTagList tagList) {
        this.tagList = tagList;
        this.containerItemStacks = new ItemStack[this.containerItemStacks.length];
        this.refreshContainer();
    }

    public void refreshContainer() {
        ItemStack[] res = UtilItemStack.tagList2Items(this.tagList);

        for (int i = 0; i < res.length && i < this.containerItemStacks.length; i++) {
            this.containerItemStacks[i] = res[i];
        }
    }

    public void updateList() {
        this.tagList = UtilItemStack.items2TagList(this.containerItemStacks);
    }

    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }

    public int func_70302_i_() {
        return this.containerItemStacks.length;
    }

    public ItemStack func_70301_a(int slot) {
        this.refreshContainer();
        return this.containerItemStacks != null && this.containerItemStacks.length > slot && slot >= 0 ? this.containerItemStacks[slot] : null;
    }

    public ItemStack func_70298_a(int par1, int par2) {
        if (this.containerItemStacks[par1] != null) {
            if (this.containerItemStacks[par1].field_77994_a <= par2) {
                ItemStack itemstack = this.containerItemStacks[par1];
                this.containerItemStacks[par1] = null;
                this.updateList();
                return itemstack;
            } else {
                ItemStack itemstack = this.containerItemStacks[par1].func_77979_a(par2);
                if (this.containerItemStacks[par1].field_77994_a == 0) {
                    this.containerItemStacks[par1] = null;
                }

                this.updateList();
                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack func_70304_b(int p_70304_1_) {
        return null;
    }

    public void func_70299_a(int slot, ItemStack itemstack) {
        this.containerItemStacks[slot] = itemstack;
        if (itemstack != null && itemstack.field_77994_a > this.func_70297_j_()) {
            itemstack.field_77994_a = this.func_70297_j_();
        }

        this.updateList();
    }

    public String func_145825_b() {
        return this.inventoryName;
    }

    public boolean func_145818_k_() {
        return true;
    }

    public int func_70297_j_() {
        return 64;
    }

    public void func_70296_d() {
        this.updateList();
    }

    public boolean func_70300_a(EntityPlayer p_70300_1_) {
        return true;
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public boolean func_94041_b(int p_94041_1_, ItemStack p_94041_2_) {
        return true;
    }
}
