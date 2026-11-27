package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileClayChunkLoader;
import mods.clayium.util.UtilBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ClayChunkLoader extends ClayContainerTiered {
    public ClayChunkLoader(int tier) {
        super(Material.field_151573_f, TileClayChunkLoader.class, 0, tier);
        this.func_149672_a(Block.field_149777_j);
        this.func_149711_c(6.0F);
        this.func_149752_b(25.0F);
        this.setHarvestLevel("pickaxe", 0);
    }

    public void func_149749_a(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (te != null && te instanceof IClayChunkLoader) {
            ((IClayChunkLoader)te).releaseTicket();
        }

        super.func_149749_a(world, x, y, z, block, meta);
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        this.setSameIcons(par1IconRegister.func_94245_a("clayium:zk60ahull"));
        this.setSameOverlayIcons(par1IconRegister.func_94245_a("clayium:chunkloader"));
    }
}
