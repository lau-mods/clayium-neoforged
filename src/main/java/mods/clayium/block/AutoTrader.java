package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileAutoTrader;
import net.minecraft.client.renderer.texture.IIconRegister;

public class AutoTrader extends ClayNoRecipeMachines {
    public AutoTrader(int tier) {
        super(null, null, "clayium:az91dhull", tier, TileAutoTrader.class, 1);
        this.guiId = 19;
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        super.func_149651_a(par1IconRegister);
        this.UpOverlayIcon = par1IconRegister.func_94245_a("clayium:autotradertop");
        this.FrontOverlayIcon = this.BackOverlayIcon = this.RightOverlayIcon = this.LeftOverlayIcon = par1IconRegister.func_94245_a("clayium:autotraderside");
    }

    @SideOnly(Side.CLIENT)
    public void registerIOIcons(IIconRegister par1IconRegister) {
        this.registerInsertIcons(par1IconRegister, new String[]{"import_1", "import_2", "import", "import_energy"});
        this.registerExtractIcons(par1IconRegister, new String[]{"export"});
    }
}
