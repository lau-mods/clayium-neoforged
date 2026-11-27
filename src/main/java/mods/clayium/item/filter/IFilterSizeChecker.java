package mods.clayium.item.filter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IFilterSizeChecker {
    void checkFilterSize(ItemStack var1, EntityPlayer var2, World var3);
}
