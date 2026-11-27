package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import mods.clayium.block.tile.TileClayContainerInterface;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ClayInterface extends ClayContainerTiered {
    protected String iconstr;
    static int nestCounter = 0;
    protected IIcon DefaultInsertIcon;
    protected IIcon DefaultExtractIcon;
    protected IIcon DefaultInsertPipeIcon;
    protected IIcon DefaultExtractPipeIcon;

    public ClayInterface(int tier) {
        this("clayium:interface", TileClayContainerInterface.class, tier);
    }

    public ClayInterface(String iconstr, Class<? extends TileClayContainerInterface> tileEntityClass, int tier) {
        super(Material.field_151573_f, tileEntityClass, 0, tier);
        this.iconstr = iconstr;
        this.func_149672_a(Block.field_149777_j);
        this.func_149711_c(2.0F);
        this.func_149752_b(5.0F);
        this.setHarvestLevel("pickaxe", 0);
    }

    protected boolean onBlockRightClicked(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        world.func_147471_g(x, y, z);
        TileEntity te = UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (te instanceof TileClayContainerInterface) {
            TileClayContainerInterface ti = (TileClayContainerInterface)te;
            if (ti.isSynced()) {
                this.openGui(99, world, x, y, z, player);
                return true;
            }
        }

        return false;
    }

    public void func_149651_a(IIconRegister par1IconRegister) {
        super.func_149651_a(par1IconRegister);
        this.setSameOverlayIcons(par1IconRegister.func_94245_a("clayium:interface"));
    }

    @SideOnly(Side.CLIENT)
    public void registerIOIcons(IIconRegister par1IconRegister) {
        this.DefaultInsertIcon = par1IconRegister.func_94245_a("clayium:import");
        this.DefaultExtractIcon = par1IconRegister.func_94245_a("clayium:export");
        this.DefaultInsertPipeIcon = par1IconRegister.func_94245_a("clayium:import_p");
        this.DefaultExtractPipeIcon = par1IconRegister.func_94245_a("clayium:export_p");
    }

    @SideOnly(Side.CLIENT)
    public IIcon[] getInsertIcons(IBlockAccess world, int x, int y, int z) {
        nestCounter++;
        TileClayContainerInterface te = (TileClayContainerInterface)UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (te.isSynced() && nestCounter < 10) {
            Block block = te.getCoreWorld() == null
                ? null
                : te.getCoreWorld().func_147439_a(te.getCoreBlockXCoord() + x, te.getCoreBlockYCoord() + y, te.getCoreBlockZCoord() + z);
            if (!(block instanceof ClayContainer)) {
                nestCounter--;
                return new IIcon[]{this.DefaultInsertIcon, this.DefaultInsertIcon, this.DefaultInsertIcon, this.DefaultInsertIcon};
            } else {
                IIcon[] iicons = ((ClayContainer)block)
                    .getInsertIcons(te.func_145831_w(), te.getCoreBlockXCoord() + x, te.getCoreBlockYCoord() + y, te.getCoreBlockZCoord() + z);
                nestCounter--;
                return iicons;
            }
        } else {
            nestCounter--;
            return this.InsertIcons;
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon[] getExtractIcons(IBlockAccess world, int x, int y, int z) {
        nestCounter++;
        TileClayContainerInterface te = (TileClayContainerInterface)UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (te.isSynced() && nestCounter < 10) {
            Block block = te.getCoreWorld() == null
                ? null
                : te.getCoreWorld().func_147439_a(te.getCoreBlockXCoord() + x, te.getCoreBlockYCoord() + y, te.getCoreBlockZCoord() + z);
            if (!(block instanceof ClayContainer)) {
                nestCounter--;
                return new IIcon[]{this.DefaultExtractIcon, this.DefaultExtractIcon, this.DefaultExtractIcon, this.DefaultExtractIcon};
            } else {
                IIcon[] iicons = ((ClayContainer)block)
                    .getExtractIcons(te.func_145831_w(), te.getCoreBlockXCoord() + x, te.getCoreBlockYCoord() + y, te.getCoreBlockZCoord() + z);
                nestCounter--;
                return iicons;
            }
        } else {
            nestCounter--;
            return this.ExtractIcons;
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon[] getInsertPipeIcons(IBlockAccess world, int x, int y, int z) {
        nestCounter++;
        TileClayContainerInterface te = (TileClayContainerInterface)UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (te.isSynced() && nestCounter < 10) {
            Block block = te.getCoreWorld() == null
                ? null
                : te.getCoreWorld().func_147439_a(te.getCoreBlockXCoord() + x, te.getCoreBlockYCoord() + y, te.getCoreBlockZCoord() + z);
            if (!(block instanceof ClayContainer)) {
                nestCounter--;
                return new IIcon[]{this.DefaultInsertPipeIcon, this.DefaultInsertPipeIcon, this.DefaultInsertPipeIcon, this.DefaultInsertPipeIcon};
            } else {
                IIcon[] iicons = ((ClayContainer)block)
                    .getInsertPipeIcons(te.func_145831_w(), te.getCoreBlockXCoord() + x, te.getCoreBlockYCoord() + y, te.getCoreBlockZCoord() + z);
                nestCounter--;
                return iicons;
            }
        } else {
            nestCounter--;
            return this.InsertIcons;
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon[] getExtractPipeIcons(IBlockAccess world, int x, int y, int z) {
        nestCounter++;
        TileClayContainerInterface te = (TileClayContainerInterface)UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (te.isSynced() && nestCounter < 10) {
            Block block = te.getCoreWorld() == null
                ? null
                : te.getCoreWorld().func_147439_a(te.getCoreBlockXCoord() + x, te.getCoreBlockYCoord() + y, te.getCoreBlockZCoord() + z);
            if (!(block instanceof ClayContainer)) {
                nestCounter--;
                return new IIcon[]{this.DefaultExtractPipeIcon, this.DefaultExtractPipeIcon, this.DefaultExtractPipeIcon, this.DefaultExtractPipeIcon};
            } else {
                IIcon[] iicons = ((ClayContainer)block)
                    .getExtractPipeIcons(te.func_145831_w(), te.getCoreBlockXCoord() + x, te.getCoreBlockYCoord() + y, te.getCoreBlockZCoord() + z);
                nestCounter--;
                return iicons;
            }
        } else {
            nestCounter--;
            return this.ExtractIcons;
        }
    }

    public List getTooltip(ItemStack itemStack) {
        List ret = UtilLocale.localizeTooltip("tooltip.Interface");
        ret.addAll(super.getTooltip(itemStack));
        return ret;
    }
}
