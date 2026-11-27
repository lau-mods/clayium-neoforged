package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import mods.clayium.block.tile.TileClayEnergyLaser;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ClayEnergyLaser extends ClayNoRecipeMachines {
    public ClayEnergyLaser(int tier) {
        super(null, "clayium:energylaser", tier, TileClayEnergyLaser.class, 2);
        this.guiId = 13;
    }

    @SideOnly(Side.CLIENT)
    public void registerIOIcons(IIconRegister par1IconRegister) {
        this.registerInsertIcons(par1IconRegister, new String[]{"import_energy"});
        this.registerExtractIcons(par1IconRegister, new String[0]);
    }

    public void func_149695_a(World p_149695_1_, int p_149695_2_, int p_149695_3_, int p_149695_4_, Block p_149695_5_) {
        super.func_149695_a(p_149695_1_, p_149695_2_, p_149695_3_, p_149695_4_, p_149695_5_);
        this.updatePower(p_149695_1_, p_149695_2_, p_149695_3_, p_149695_4_);
    }

    public void func_149726_b(World p_149726_1_, int p_149726_2_, int p_149726_3_, int p_149726_4_) {
        super.func_149726_b(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_);
        this.updatePower(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_);
    }

    public void updatePower(World p_149695_1_, int p_149695_2_, int p_149695_3_, int p_149695_4_) {
        if (!p_149695_1_.field_72995_K) {
            TileEntity tile = UtilBuilder.safeGetTileEntity(p_149695_1_, p_149695_2_, p_149695_3_, p_149695_4_);
            if (tile instanceof TileClayEnergyLaser) {
                ((TileClayEnergyLaser)tile)
                    .setPowered(
                        ClayiumCore.cfgInverseClayLaserRSCondition
                            ? !p_149695_1_.func_72864_z(p_149695_2_, p_149695_3_, p_149695_4_)
                            : p_149695_1_.func_72864_z(p_149695_2_, p_149695_3_, p_149695_4_)
                    );
            }
        }
    }

    public List getTooltip(ItemStack itemStack) {
        List ret = UtilLocale.localizeTooltip("tooltip.ClayEnergyLaser");
        ret.addAll(super.getTooltip(itemStack));
        if (UtilLocale.canLocalize("tooltip.ClayEnergyLaser.energyConsumption")) {
            int e = 0;
            switch (this.tier) {
                case 7:
                    e = TileClayEnergyLaser.consumingEnergyBlue;
                    break;
                case 8:
                    e = TileClayEnergyLaser.consumingEnergyGreen;
                    break;
                case 9:
                    e = TileClayEnergyLaser.consumingEnergyRed;
                    break;
                case 10:
                    e = TileClayEnergyLaser.consumingEnergyWhite;
            }

            ret.add(UtilLocale.localizeAndFormat("tooltip.ClayEnergyLaser.energyConsumption", UtilLocale.ClayEnergyNumeral((long)e)));
        }

        return ret;
    }
}
