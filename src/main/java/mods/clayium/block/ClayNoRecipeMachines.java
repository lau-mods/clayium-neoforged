package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.block.tile.TileClayContainerTiered;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ClayNoRecipeMachines extends ClayContainerTiered {
    protected String iconstr;
    private String guititle;

    public ClayNoRecipeMachines(
        String guititle, String iconstr, String machineIconStr, int tier, Class<? extends TileClayContainer> tileEntityClass, int metaMode
    ) {
        this(guititle, iconstr, machineIconStr, tier, tileEntityClass, 1, metaMode);
    }

    public ClayNoRecipeMachines(String guititle, String iconstr, int tier, Class<? extends TileClayContainer> tileEntityClass, int metaMode) {
        this(guititle, iconstr, tier, tileEntityClass, 1, metaMode);
    }

    public ClayNoRecipeMachines(
        String guititle, String iconstr, String machineIconStr, int tier, Class<? extends TileClayContainer> tileEntityClass, int guiId, int metaMode
    ) {
        super(Material.field_151573_f, tileEntityClass, machineIconStr, guiId, metaMode, tier);
        this.initClayNoRecipeMachines(iconstr, guititle);
    }

    public ClayNoRecipeMachines(String guititle, String iconstr, int tier, Class<? extends TileClayContainer> tileEntityClass, int guiId, int metaMode) {
        super(Material.field_151573_f, tileEntityClass, guiId, metaMode, tier);
        this.initClayNoRecipeMachines(iconstr, guititle);
    }

    private void initClayNoRecipeMachines(String iconstr, String guititle) {
        this.iconstr = iconstr;
        this.guititle = guititle;
        this.func_149672_a(Block.field_149777_j);
        this.func_149711_c(2.0F);
        this.func_149752_b(5.0F);
        this.setHarvestLevel("pickaxe", 0);
    }

    public TileEntity func_149915_a(World world, int par2) {
        TileClayContainerTiered tile = (TileClayContainerTiered)super.func_149915_a(world, par2);
        tile.containerName(this.guititle);
        return tile;
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        super.func_149651_a(par1IconRegister);
        if (this.iconstr != null && !this.iconstr.equals("")) {
            this.FrontOverlayIcon = par1IconRegister.func_94245_a(this.iconstr);
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerIOIcons(IIconRegister par1IconRegister) {
        this.registerInsertIcons(par1IconRegister, new String[]{"import", "import_energy"});
        this.registerExtractIcons(par1IconRegister, new String[]{"export"});
    }
}
