package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.block.tile.TileClayMachines;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ClayMachines extends ClayContainerTiered {
    protected String iconstr;
    protected String recipeId;
    protected String guititle;
    public float multCraftTime = 1.0F;
    public float multConsumingEnergy = 1.0F;

    public ClayMachines(String iconstr, int tier) {
        this(iconstr, tier, TileClayMachines.class, 1);
    }

    public ClayMachines(String iconstr, int tier, Class<? extends TileClayContainer> tileEntityClass, int guiId) {
        this(iconstr, tier, tileEntityClass, guiId, 1);
    }

    public ClayMachines(String iconstr, int tier, Class<? extends TileClayContainer> tileEntityClass, int guiId, int metaMode) {
        this(null, iconstr, tier, tileEntityClass, guiId, metaMode);
    }

    public ClayMachines(String guititle, String iconstr, int tier, Class<? extends TileClayContainer> tileEntityClass, int guiId, int metaMode) {
        this(null, guititle, iconstr, tier, tileEntityClass, guiId, metaMode);
    }

    public ClayMachines(String recipeId, String iconstr, int tier) {
        this(recipeId, iconstr, tier, TileClayMachines.class);
    }

    public ClayMachines(String recipeId, String iconstr, int tier, Class<? extends TileClayContainer> tileEntityClass) {
        this(recipeId, iconstr, tier, tileEntityClass, 1);
    }

    public ClayMachines(String recipeId, String iconstr, int tier, Class<? extends TileClayContainer> tileEntityClass, int guiId) {
        this(recipeId, null, iconstr, tier, tileEntityClass, guiId, 1);
    }

    public ClayMachines(String recipeId, String guititle, String iconstr, int tier, Class<? extends TileClayContainer> tileEntityClass, int guiId, int metaMode) {
        super(Material.field_151573_f, tileEntityClass, metaMode, tier);
        this.iconstr = iconstr;
        this.guititle = guititle;
        this.func_149672_a(Block.field_149777_j);
        this.func_149711_c(2.0F);
        this.func_149752_b(5.0F);
        this.setHarvestLevel("pickaxe", 0);
        this.recipeId = recipeId;
        this.guiId = guiId;
    }

    public TileEntity func_149915_a(World world, int par2) {
        TileClayMachines tile = (TileClayMachines)super.func_149915_a(world, par2);
        tile.setRecipe(this.recipeId);
        tile.containerName(this.guititle);
        tile.initCraftTime = this.multCraftTime;
        tile.initConsumingEnergy = this.multConsumingEnergy;
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

    public String getRecipeId() {
        return this.recipeId;
    }

    public List getTooltip(ItemStack itemStack) {
        List ret = UtilLocale.localizeTooltip("tooltip." + this.recipeId);
        ret.addAll(super.getTooltip(itemStack));
        return ret;
    }
}
