package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileSolarClayFabricator;
import net.minecraft.client.renderer.texture.IIconRegister;

public class SolarClayFabricator extends ClayNoRecipeMachines {
    public SolarClayFabricator(String guititle, int tier) {
        super(guititle, "", tier, TileSolarClayFabricator.class, 2);
        this.guiId = 1;
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        super.func_149651_a(par1IconRegister);
        this.UpOverlayIcon = par1IconRegister.func_94245_a("clayium:solar");
    }

    @SideOnly(Side.CLIENT)
    public void registerIOIcons(IIconRegister par1IconRegister) {
        this.registerInsertIcons(par1IconRegister, new String[]{"import"});
        this.registerExtractIcons(par1IconRegister, new String[]{"export"});
    }
}
