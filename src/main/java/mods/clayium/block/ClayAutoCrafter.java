package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileAutoCrafter;
import net.minecraft.client.renderer.texture.IIconRegister;

public class ClayAutoCrafter extends ClayNoRecipeMachines {
    public ClayAutoCrafter(int tier) {
        super("", "", tier, TileAutoCrafter.class, 1);
        this.guiId = 17;
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        super.func_149651_a(par1IconRegister);
        this.UpOverlayIcon = par1IconRegister.func_94245_a("clayium:autocraftertop");
        this.FrontOverlayIcon = this.BackOverlayIcon = this.RightOverlayIcon = this.LeftOverlayIcon = par1IconRegister.func_94245_a("clayium:autocrafterside");
    }
}
