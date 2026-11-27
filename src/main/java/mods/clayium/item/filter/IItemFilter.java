package mods.clayium.item.filter;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface IItemFilter extends IFilterSizeChecker, IItemWithFilterSize {
    void addFilterInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4);

    void addTooltip(NBTTagCompound var1, List var2, int var3);

    boolean filterMatch(NBTTagCompound var1, ItemStack var2);

    boolean filterMatch(NBTTagCompound var1, World var2, int var3, int var4, int var5);

    boolean isCopy(ItemStack var1);

    ItemStack setCopyFlag(ItemStack var1);

    ItemStack clearCopyFlag(ItemStack var1);
}
