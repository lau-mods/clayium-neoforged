package mods.clayium.item.filter;

import java.util.List;
import mods.clayium.core.ClayiumCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class ItemFilterString extends ItemFilterTemp {
    public ItemFilterString() {
        super();
    }

    public boolean filterMatch(NBTTagCompound filterTag, ItemStack itemstack) {
        return filterTag == null ? false : this.filterStringMatch(filterTag.func_74779_i("FilterString"), itemstack);
    }

    public boolean filterMatch(NBTTagCompound filterTag, World world, int x, int y, int z) {
        if (filterTag == null) {
            return false;
        } else {
            String filterString = filterTag.func_74779_i("FilterString");
            return this.shouldApplySpecialPatternForBlock(filterString, world, x, y, z)
                ? this.filterStringMatch(filterString, world, x, y, z)
                : super.filterMatch(filterTag, world, x, z, z);
        }
    }

    public abstract boolean filterStringMatch(String var1, ItemStack var2);

    public boolean shouldApplySpecialPatternForBlock(String filterString, World world, int x, int y, int z) {
        return false;
    }

    public boolean filterStringMatch(String filterString, World world, int x, int y, int z) {
        return false;
    }

    public void openGui(ItemStack itemstack, World world, EntityPlayer player) {
        player.openGui(ClayiumCore.INSTANCE, 21, world, (int)player.field_70165_t, (int)player.field_70163_u, (int)player.field_70161_v);
    }

    public void addTooltip(NBTTagCompound filterTag, List list, int indent) {
        if (list.size() < 100) {
            if (filterTag != null) {
                String in = "";

                for (int i = 0; i < indent; i++) {
                    in = in + " ";
                }

                String filter = filterTag.func_74779_i("FilterString");
                if (filter != null && !filter.equals("")) {
                    list.add(in + filter);
                }
            }
        }
    }

    public int getFilterSize(NBTTagCompound filterTag) {
        return 1;
    }
}
