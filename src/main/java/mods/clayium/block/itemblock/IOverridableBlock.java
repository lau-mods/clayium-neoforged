package mods.clayium.block.itemblock;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface IOverridableBlock {
    void overrideTo(
        ItemStack var1,
        EntityPlayer var2,
        World var3,
        int var4,
        int var5,
        int var6,
        int var7,
        float var8,
        float var9,
        float var10,
        Block var11,
        int var12,
        Class var13,
        NBTTagCompound var14
    );

    boolean canOverride(ItemStack var1, EntityPlayer var2, World var3, int var4, int var5, int var6, int var7, float var8, float var9, float var10);

    void onOverridden(ItemStack var1, EntityPlayer var2, World var3, int var4, int var5, int var6, int var7, float var8, float var9, float var10);
}
