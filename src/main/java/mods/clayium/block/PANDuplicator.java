package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TilePANDuplicator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;

public class PANDuplicator extends ClayAssembler implements IPANConductor {
    public PANDuplicator(int tier) {
        super("", "clayium:panduplicator", tier, TilePANDuplicator.class);
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        this.setSameOverlayIcons(par1IconRegister.func_94245_a("clayium:pancasing"));
        super.func_149651_a(par1IconRegister);
    }

    public int getTier(ItemStack itemstack) {
        int tier = super.getTier(itemstack);
        return tier <= 8 ? 11 : (tier <= 12 ? 12 : 13);
    }
}
