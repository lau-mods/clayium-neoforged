package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import mods.clayium.core.ClayiumCore;
import mods.clayium.item.CMaterials;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ClayTreeLeaf extends BlockLeaves implements ITieredBlock {
    public static final String[][] iconNames = new String[][]{
        {"clayium:leaves_claytree", "leaves_big_oak"}, {"clayium:leaves_claytree_opaque", "leaves_big_oak_opaque"}
    };
    public static final String[] nameSuffix = new String[]{"claytree", "big_oak"};

    public ClayTreeLeaf() {
        super();
        this.func_149647_a(ClayiumCore.creativeTabClayium);
    }

    protected void func_150124_c(World world, int x, int y, int z, int meta, int chance) {
        if ((meta & 3) == 0 && world.field_73012_v.nextInt(chance) == 0) {
            this.func_149642_a(world, x, y, z, CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DUST));
        }
    }

    @SideOnly(Side.CLIENT)
    public int func_149635_D() {
        return 16777215;
    }

    @SideOnly(Side.CLIENT)
    public int func_149741_i(int meta) {
        return 16777215;
    }

    @SideOnly(Side.CLIENT)
    public int func_149720_d(IBlockAccess p_149720_1_, int p_149720_2_, int p_149720_3_, int p_149720_4_) {
        return 16777215;
    }

    public int func_149643_k(World world, int x, int y, int z) {
        return world.func_72805_g(x, y, z) & 3;
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149691_a(int p_149691_1_, int p_149691_2_) {
        this.func_150122_b(Minecraft.func_71410_x().field_71474_y.field_74347_j);
        return (p_149691_2_ & 3) == 1 ? this.field_150129_M[this.field_150127_b][1] : this.field_150129_M[this.field_150127_b][0];
    }

    @SideOnly(Side.CLIENT)
    public void func_149666_a(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister register) {
        for (int i = 0; i < iconNames.length; i++) {
            this.field_150129_M[i] = new IIcon[iconNames[i].length];

            for (int j = 0; j < iconNames[i].length; j++) {
                this.field_150129_M[i][j] = register.func_94245_a(iconNames[i][j]);
            }
        }
    }

    public Item func_149650_a(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        return CMaterials.get(CMaterials.CLAY, CMaterials.DUST).func_77973_b();
    }

    public int func_149692_a(int p_149692_1_) {
        return CMaterials.get(CMaterials.CLAY, CMaterials.DUST).func_77960_j();
    }

    public String[] func_150125_e() {
        return nameSuffix;
    }

    public int getTier(ItemStack itemstack) {
        return 7;
    }

    public int getTier(IBlockAccess world, int x, int y, int z) {
        return 7;
    }
}
