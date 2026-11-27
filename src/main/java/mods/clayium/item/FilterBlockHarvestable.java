package mods.clayium.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class FilterBlockHarvestable implements ISpecialFilter {
    public FilterBlockHarvestable() {
        super();
    }

    public boolean filterMatch(NBTTagCompound filterTag, ItemStack itemstack, boolean lastResult) {
        return false;
    }

    public boolean filterMatch(NBTTagCompound filterTag, World world, int x, int y, int z, boolean lastResult) {
        if (lastResult) {
            return true;
        } else if (world == null) {
            return false;
        } else {
            Block block = world.func_147439_a(x, y, z);
            return block instanceof IGrowable ? !((IGrowable)block).func_149851_a(world, x, y, z, world.field_72995_K) : false;
        }
    }

    public void addTooltip(NBTTagCompound filterTag, List list, int indent) {
    }

    public void openGui(ItemStack itemstack, World world, EntityPlayer player) {
    }
}
