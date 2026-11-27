package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileAreaActivator;
import net.minecraft.client.renderer.texture.IIconRegister;

public class AreaActivator extends AreaMiner {
    public AreaActivator(int tier) {
        super(tier, "clayium:areaactivator", TileAreaActivator.class, 24);
    }

    @SideOnly(Side.CLIENT)
    public void registerIOIcons(IIconRegister par1IconRegister) {
        this.registerInsertIcons(par1IconRegister, new String[]{"import", "import_energy"});
        this.registerExtractIcons(par1IconRegister, new String[]{"export"});
    }
}
