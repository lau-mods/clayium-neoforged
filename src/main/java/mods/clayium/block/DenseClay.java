package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import mods.clayium.core.ClayiumCore;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class DenseClay extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon TopIcon;
    @SideOnly(Side.CLIENT)
    private IIcon FrontIcon;
    @SideOnly(Side.CLIENT)
    private IIcon SideIcon;

    public DenseClay() {
        super(Material.field_151571_B);
        this.func_149647_a(ClayiumCore.creativeTabClayium);
        this.func_149663_c("blockDenseClay");
        this.func_149658_d("clayium:denseclay");
        this.func_149711_c(1.0F);
        this.func_149752_b(1.0F);
        this.func_149672_a(Block.field_149767_g);
        this.setHarvestLevel("shovel", 0);
    }

    public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float posX, float posY, float posZ) {
        int metadata = world.func_72805_g(x, y, z);
        metadata = (metadata - 2 + 1) % 4 + 2;
        world.func_72921_c(x, y, z, metadata, 2);
        return true;
    }

    public void func_149699_a(World world, int x, int y, int z, EntityPlayer player) {
    }

    public void func_149695_a(World world, int x, int y, int z, Block neighborBlock) {
    }

    public int quantityDropped(int meta, int fortune, Random random) {
        return this.func_149679_a(fortune, random);
    }

    public Item func_149650_a(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        return Item.func_150898_a(this);
    }

    public int func_149745_a(Random random) {
        return 36;
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149691_a(int side, int meta) {
        if (side == 1) {
            return this.TopIcon;
        } else {
            return side == meta ? this.FrontIcon : this.SideIcon;
        }
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        this.TopIcon = par1IconRegister.func_94245_a("clayium:machinehull-0");
        this.FrontIcon = par1IconRegister.func_94245_a("clayium:machinehull-1");
        this.SideIcon = par1IconRegister.func_94245_a("clayium:denseclay");
    }

    public void func_149689_a(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemstack) {
        int direction = MathHelper.func_76128_c((double)(entity.field_70177_z * 4.0F / 360.0F) + 0.5) & 3;
        if (direction == 0) {
            world.func_72921_c(x, y, z, 2, 2);
        }

        if (direction == 1) {
            world.func_72921_c(x, y, z, 5, 2);
        }

        if (direction == 2) {
            world.func_72921_c(x, y, z, 3, 2);
        }

        if (direction == 3) {
            world.func_72921_c(x, y, z, 4, 2);
        }

        if (itemstack.func_82837_s()) {
        }
    }
}
