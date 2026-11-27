package mods.clayium.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class CreativeTab extends CreativeTabs {
    Item tabIconItem;

    public CreativeTab(String label) {
        this(label, Items.field_151119_aD);
    }

    public CreativeTab(String label, Item tabIconItem) {
        super(label);
        this.tabIconItem = tabIconItem;
    }

    @SideOnly(Side.CLIENT)
    public Item func_78016_d() {
        return this.tabIconItem;
    }

    public void setTabIconItem(Item tabIconItem) {
        this.tabIconItem = tabIconItem;
    }
}
