package mods.clayium.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventoryInItemStack extends NBTInventoryWrapper {
    protected ItemStack itemstack;
    protected String tagname;

    public InventoryInItemStack(ItemStack itemstack, String tagname, int sizeInventory) {
        super(
            itemstack != null && itemstack.field_77990_d != null && itemstack.field_77990_d.func_74764_b(tagname)
                ? itemstack.field_77990_d.func_150295_c(tagname, 10)
                : new NBTTagList(),
            sizeInventory
        );
        this.tagname = tagname;
        this.itemstack = itemstack;
        this.initTagCompound();
    }

    public void setItemStack(ItemStack itemstack) {
        this.itemstack = itemstack;
        NBTTagList tagList = itemstack != null && itemstack.field_77990_d != null && itemstack.field_77990_d.func_74764_b(this.tagname)
            ? itemstack.field_77990_d.func_150295_c(this.tagname, 10)
            : new NBTTagList();
        this.setTagList(tagList);
        this.initTagCompound();
    }

    public void initTagCompound() {
        if (this.itemstack != null && this.itemstack.field_77990_d == null) {
            this.itemstack.func_77982_d(new NBTTagCompound());
        }

        if (this.itemstack != null && this.itemstack.field_77990_d != null && !this.itemstack.field_77990_d.func_74764_b(this.tagname)) {
            this.itemstack.field_77990_d.func_74782_a(this.tagname, this.tagList);
        }

        if (this.itemstack != null) {
            this.setInventoryName(this.itemstack.func_82833_r());
        }
    }

    public void func_70296_d() {
        super.func_70296_d();
        this.itemstack.field_77990_d.func_74782_a(this.tagname, this.tagList);
    }
}
