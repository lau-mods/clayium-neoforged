package mods.clayium.item;

import net.minecraft.item.ItemStack;

public class ItemCraftingTools extends ItemTiered {
    private ItemStack brokenItem;

    public ItemCraftingTools(ItemStack itemstack) {
        super();
        this.brokenItem = itemstack.func_77946_l();
        this.func_77625_d(1);
        this.setNoRepair();
    }

    public ItemStack getContainerItem(ItemStack itemstack) {
        return itemstack.func_77960_j() >= this.func_77612_l() ? this.brokenItem.func_77946_l() : new ItemStack(this, 1, itemstack.func_77960_j() + 1);
    }

    public boolean hasContainerItem(ItemStack itemstack) {
        return this.getContainerItem(itemstack) != null;
    }

    public boolean func_77630_h(ItemStack itemstack) {
        return itemstack.func_77960_j() >= this.func_77612_l();
    }
}
