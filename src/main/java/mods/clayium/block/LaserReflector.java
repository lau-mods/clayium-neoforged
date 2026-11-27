package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileLaserReflector;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilDirection;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class LaserReflector extends ClayContainer implements ITieredBlock {
    public LaserReflector() {
        super(Material.field_151573_f, TileLaserReflector.class, 2);
        this.func_149672_a(Block.field_149778_k);
        this.preventFirstPass();
    }

    public int func_149645_b() {
        return ClayiumCore.laserReflectorRenderId;
    }

    public boolean func_149686_d() {
        return false;
    }

    public boolean func_149662_c() {
        return false;
    }

    protected boolean onBlockRightClicked(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        return false;
    }

    public int getTier(ItemStack itemstack) {
        return 7;
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        this.setSameIcons(par1IconRegister.func_94245_a("clayium:laserreflector"));
    }

    @SideOnly(Side.CLIENT)
    public int func_149701_w() {
        return 1;
    }

    public void func_149719_a(IBlockAccess world, int x, int y, int z) {
        float f = 0.125F;
        switch (UtilDirection.getOrientation(world.func_72805_g(x, y, z))) {
            case UP:
            case DOWN:
                this.func_149676_a(0.0F + f * 2.0F, 0.0F + f, 0.0F + f * 2.0F, 1.0F - f * 2.0F, 1.0F - f, 1.0F - f * 2.0F);
                break;
            case NORTH:
            case SOUTH:
                this.func_149676_a(0.0F + f * 2.0F, 0.0F + f * 2.0F, 0.0F + f, 1.0F - f * 2.0F, 1.0F - f * 2.0F, 1.0F - f);
                break;
            case EAST:
            case WEST:
                this.func_149676_a(0.0F + f, 0.0F + f * 2.0F, 0.0F + f * 2.0F, 1.0F - f, 1.0F - f * 2.0F, 1.0F - f * 2.0F);
                break;
            default:
                this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB func_149633_g(World world, int x, int y, int z) {
        return super.getNormalSelectedBoundingBoxFromPool(world, x, y, z);
    }

    public int getTier(IBlockAccess world, int x, int y, int z) {
        return 7;
    }
}
