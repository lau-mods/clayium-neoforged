package mods.clayium.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

public interface IItemExRenderer {
    @SideOnly(Side.CLIENT)
    void preRenderItem(ItemRenderType var1, ItemStack var2, Object... var3);

    @SideOnly(Side.CLIENT)
    void postRenderItem(ItemRenderType var1, ItemStack var2, Object... var3);

    @SideOnly(Side.CLIENT)
    void preRenderItemPass(ItemRenderType var1, ItemStack var2, int var3, Object... var4);

    @SideOnly(Side.CLIENT)
    void postRenderItemPass(ItemRenderType var1, ItemStack var2, int var3, Object... var4);
}
