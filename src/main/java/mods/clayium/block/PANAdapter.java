package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TilePANAdapter;
import mods.clayium.util.UtilBuilder;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

public class PANAdapter extends ClayContainerTiered implements IPANConductor {
    public PANAdapter(int tier) {
        super(Material.field_151573_f, TilePANAdapter.class, 40, 2, tier);
        this.func_149711_c(2.0F).func_149752_b(2.0F);
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        this.setSameOverlayIcons(par1IconRegister.func_94245_a("clayium:panadapter"));
        super.func_149651_a(par1IconRegister);
    }

    public int getTier(ItemStack itemstack) {
        return Math.max(super.getTier(itemstack), 11);
    }

    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
        TileEntity te = UtilBuilder.safeGetTileEntity(world, x, y, z);
        TileEntity te1 = UtilBuilder.safeGetTileEntity(world, tileX, tileY, tileZ);
        if (te instanceof TilePANAdapter && !(te1 instanceof TilePANAdapter)) {
            ((TilePANAdapter)te).onNeighborChange();
        }
    }
}
