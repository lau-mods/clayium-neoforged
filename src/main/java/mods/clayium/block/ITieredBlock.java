package mods.clayium.block;

import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public interface ITieredBlock {
    int getTier(ItemStack var1);

    int getTier(IBlockAccess var1, int var2, int var3, int var4);
}
