package mods.clayium.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import mods.clayium.block.CBlocks;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraftforge.common.ForgeHooks;

public class ClayShovel extends ItemSpade {
    protected float efficiencyOnClayBlocks = 32.0F;
    private float efficiencyOnClayOre = 12.0F;

    public ClayShovel() {
        super(ToolMaterial.WOOD);
        this.func_77656_e(500);
        this.func_77637_a(ClayiumCore.creativeTabClayium);
        this.func_77655_b("itemClayShovel");
        this.func_111206_d("clayium:clayshovel");
    }

    public float getDigSpeed(ItemStack stack, Block block, int meta) {
        if (block.func_149688_o() == Material.field_151571_B) {
            return this.efficiencyOnClayBlocks;
        } else if (block == CBlocks.blockClayOre) {
            return this.efficiencyOnClayOre;
        } else {
            return ForgeHooks.isToolEffective(stack, block, meta) ? this.field_77864_a : super.getDigSpeed(stack, block, meta);
        }
    }

    @SideOnly(Side.CLIENT)
    public void func_77624_a(ItemStack itemstack, EntityPlayer player, List list, boolean flag) {
        super.func_77624_a(itemstack, player, list, flag);
        List alist = UtilLocale.localizeTooltip(this.func_77667_c(itemstack) + ".tooltip");
        if (alist != null) {
            list.addAll(alist);
        }
    }
}
