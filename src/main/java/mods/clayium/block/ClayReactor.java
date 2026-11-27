package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.block.tile.TileClayReactor;
import mods.clayium.util.UtilBuilder;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class ClayReactor extends ClayMachines {
    private IIcon[] FrontOverlayIcons;

    public ClayReactor(String recipeId, String iconstr, int tier) {
        this(recipeId, iconstr, tier, TileClayReactor.class);
    }

    public ClayReactor(String recipeId, String iconstr, int tier, Class<? extends TileClayContainer> tileEntityClass) {
        this(recipeId, null, iconstr, tier, tileEntityClass, 8, 1);
    }

    public ClayReactor(String recipeId, String guititle, String iconstr, int tier, Class<? extends TileClayContainer> tileEntityClass, int guiId, int metaMode) {
        super(recipeId, guititle, iconstr, tier, tileEntityClass, guiId, metaMode);
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        super.func_149651_a(par1IconRegister);
        this.FrontOverlayIcons = new IIcon[]{this.FrontOverlayIcon, par1IconRegister.func_94245_a(this.iconstr + "_1")};
    }

    @SideOnly(Side.CLIENT)
    public void registerIOIcons(IIconRegister par1IconRegister) {
        this.registerInsertIcons(par1IconRegister, new String[]{"import_1", "import_2", "import", "import_energy"});
        this.registerExtractIcons(par1IconRegister, new String[]{"export_1", "export_2", "export"});
    }

    @SideOnly(Side.CLIENT)
    public IIcon getOverlayIcon(IBlockAccess world, int x, int y, int z, int side) {
        if (UtilBuilder.safeGetTileEntity(world, x, y, z) instanceof TileClayReactor) {
            TileClayReactor te = (TileClayReactor)UtilBuilder.safeGetTileEntity(world, x, y, z);
            this.FrontOverlayIcon = this.FrontOverlayIcons[te.constructed ? 1 : 0];
        }

        IIcon iicon = super.getOverlayIcon(world, x, y, z, side);
        this.FrontOverlayIcon = this.FrontOverlayIcons[0];
        return iicon;
    }
}
