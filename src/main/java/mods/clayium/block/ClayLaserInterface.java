package mods.clayium.block;

import java.util.List;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.block.tile.TileClayLaserInterface;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ClayLaserInterface extends ClayContainerTiered {
    protected String iconstr;

    public ClayLaserInterface(int tier) {
        this("clayium:laserinterface", TileClayLaserInterface.class, tier);
    }

    public ClayLaserInterface(String iconstr, Class<? extends TileClayLaserInterface> tileEntityClass, int tier) {
        super(Material.field_151573_f, tileEntityClass, 2, tier);
        this.iconstr = iconstr;
        this.func_149672_a(Block.field_149777_j);
        this.func_149711_c(2.0F);
        this.func_149752_b(5.0F);
        this.setHarvestLevel("pickaxe", 0);
    }

    public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.func_71045_bC();
        TileClayContainer te = (TileClayContainer)UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (te.isUsable(itemStack, player, side, hitX, hitY, hitZ)) {
            te.useItem(itemStack, player, side, hitX, hitY, hitZ);
        }

        return false;
    }

    public void func_149651_a(IIconRegister par1IconRegister) {
        super.func_149651_a(par1IconRegister);
        this.FrontOverlayIcon = par1IconRegister.func_94245_a(this.iconstr);
    }

    public List getTooltip(ItemStack itemStack) {
        List ret = UtilLocale.localizeTooltip("tooltip.LaserInterface");
        ret.addAll(super.getTooltip(itemStack));
        return ret;
    }
}
