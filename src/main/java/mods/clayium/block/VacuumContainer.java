package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileVacuumContainer;
import mods.clayium.gui.TextureExtra;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public class VacuumContainer extends ClayContainer implements ITieredBlock {
    public VacuumContainer(Material material, String iconstr) {
        super(material, TileVacuumContainer.class, iconstr, 1);
        this.guiId = 22;
        this.func_149672_a(Block.field_149777_j);
        this.func_149711_c(2.0F);
        this.func_149752_b(5.0F);
        this.setHarvestLevel("pickaxe", 0);
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        super.func_149651_a(par1IconRegister);
        this.BackIcon = this.LeftIcon = this.RightIcon = new TextureExtra("clayium:vacuumcontainerside_", this.machineIconStr, "clayium:vacuumcontainerside")
            .register(par1IconRegister);
        this.FrontIcon = new TextureExtra("clayium:vacuumcontainer_", this.machineIconStr, "clayium:vacuumcontainer").register(par1IconRegister);
        this.UpIcon = new TextureExtra("clayium:vacuumcontainertop_", this.machineIconStr, "clayium:vacuumcontainertop").register(par1IconRegister);
    }

    @SideOnly(Side.CLIENT)
    public void registerIOIcons(IIconRegister par1IconRegister) {
        this.registerInsertIcons(par1IconRegister, new String[]{"import"});
        this.registerExtractIcons(par1IconRegister, new String[]{"export"});
    }

    public int getTier(ItemStack itemstack) {
        return 6;
    }

    public int getTier(IBlockAccess world, int x, int y, int z) {
        return 6;
    }
}
