package mods.clayium.item.filter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import mods.clayium.item.ItemTiered;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilItemStack;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public abstract class ItemFilterTemp extends ItemTiered implements IItemFilter {
    public static final int maxFilterSize = 100;
    public static volatile IIcon iiconCopy = null;

    ItemFilterTemp() {
        super();
        this.func_77625_d(1);
    }

    public ItemStack func_77659_a(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_) {
        if (this.isCopy(p_77659_1_) && p_77659_3_.func_70093_af()) {
            if (!p_77659_2_.field_72995_K) {
                p_77659_3_.func_145747_a(new ChatComponentText("Cleared the copy flag."));
            }

            return this.clearCopyFlag(p_77659_1_);
        } else {
            this.openGui(p_77659_1_, p_77659_2_, p_77659_3_);
            return super.func_77659_a(p_77659_1_, p_77659_2_, p_77659_3_);
        }
    }

    @SideOnly(Side.CLIENT)
    public void func_77624_a(ItemStack itemstack, EntityPlayer player, List list, boolean flag) {
        super.func_77624_a(itemstack, player, list, flag);
        this.addTooltip(itemstack.field_77990_d, list, 0);
    }

    public void addFilterInformation(ItemStack itemstack, EntityPlayer player, List list, boolean flag) {
        this.addTooltip(itemstack.field_77990_d, list, 0);
    }

    public String func_77653_i(ItemStack itemstack) {
        String name = this.func_77657_g(itemstack);
        if (this.isCopy(itemstack)) {
            name = name + ".copy";
        }

        return StatCollector.func_74838_a("" + name + ".name").trim();
    }

    public boolean filterMatch(NBTTagCompound filterTag, ItemStack itemstack) {
        return false;
    }

    public boolean filterMatch(NBTTagCompound filterTag, World world, int x, int y, int z) {
        return this.filterMatch(filterTag, UtilBuilder.getItemBlock(world, x, y, z));
    }

    public void addTooltip(NBTTagCompound filterTag, List list, int indent) {
    }

    public int getFilterSize(NBTTagCompound filterTag) {
        return 1;
    }

    public void openGui(ItemStack itemstack, World world, EntityPlayer player) {
    }

    public void checkFilterSize(ItemStack filter, EntityPlayer player, World world) {
        if (filter != null && filter.func_77973_b() instanceof IItemWithFilterSize) {
            IItemWithFilterSize filteritem = (IItemWithFilterSize)filter.func_77973_b();
            if (filteritem.getFilterSize(filter.func_77978_p()) >= 100) {
                filter.func_77982_d(new NBTTagCompound());
                if (!world.field_72995_K) {
                    player.func_145747_a(new ChatComponentText("The filter has broken! The filter size is too large!"));
                }
            }
        }
    }

    public boolean isCopy(ItemStack filter) {
        return filter.func_77960_j() == 1;
    }

    public ItemStack setCopyFlag(ItemStack filter) {
        filter.func_77964_b(1);
        return filter;
    }

    public ItemStack clearCopyFlag(ItemStack filter) {
        filter.func_77964_b(0);
        return filter;
    }

    public static boolean match(ItemStack filter, ItemStack itemstack) {
        return isFilter(filter) ? ((IItemFilter)filter.func_77973_b()).filterMatch(filter.func_77978_p(), itemstack) : false;
    }

    public static boolean match(ItemStack filter, World world, int x, int y, int z) {
        return isFilter(filter) ? ((IItemFilter)filter.func_77973_b()).filterMatch(filter.func_77978_p(), world, x, y, z) : false;
    }

    public static boolean matchBetweenItemstacks(ItemStack filter, ItemStack itemstack, boolean fuzzy) {
        if (filter == null) {
            return itemstack == null;
        } else if (fuzzy) {
            if (itemstack == null) {
                return false;
            } else {
                return UtilItemStack.areItemDamageEqualOrDamageable(filter, itemstack) ? true : UtilItemStack.haveSameOD(filter, itemstack);
            }
        } else {
            return itemstack != null && UtilItemStack.areTypeEqual(filter, itemstack);
        }
    }

    public static boolean isFilter(ItemStack filter) {
        return filter != null && filter.func_77973_b() instanceof IItemFilter;
    }

    @SideOnly(Side.CLIENT)
    public synchronized void func_94581_a(IIconRegister iicon) {
        super.func_94581_a(iicon);
        if (iiconCopy == null) {
            iiconCopy = iicon.func_94245_a("clayium:filtercopy");
        }
    }

    public int getRenderPasses(int meta) {
        return meta == 1 ? 2 : 1;
    }

    public boolean func_77623_v() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_77618_c(int meta, int pass) {
        return pass == 0 ? super.func_77617_a(meta) : iiconCopy;
    }
}
