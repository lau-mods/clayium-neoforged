package mods.clayium.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import mods.clayium.util.UtilLocale;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemTiered extends Item implements ITieredItem {
    protected int baseTier = -1;

    public ItemTiered() {
        super();
    }

    @SideOnly(Side.CLIENT)
    public void func_77624_a(ItemStack itemstack, EntityPlayer player, List list, boolean flag) {
        if (this.getTier(itemstack) >= 0) {
            list.add(getTieredToolTip(this.getTier(itemstack)));
        }

        List alist = UtilLocale.localizeTooltip(this.func_77667_c(itemstack) + ".tooltip");
        if (alist != null) {
            list.addAll(alist);
        }
    }

    public static String getTieredToolTip(int tier) {
        return EnumChatFormatting.WHITE + "Tier " + tier + EnumChatFormatting.RESET;
    }

    public EnumRarity func_77613_e(ItemStack itemstack) {
        switch (this.getTier(itemstack)) {
            case 4:
            case 5:
            case 6:
            case 7:
                return EnumRarity.uncommon;
            case 8:
            case 9:
            case 10:
            case 11:
                return EnumRarity.rare;
            case 12:
            case 13:
            case 14:
            case 15:
                return EnumRarity.epic;
            default:
                return EnumRarity.common;
        }
    }

    public ItemTiered setBaseTier(int tier) {
        this.baseTier = tier;
        return this;
    }

    public int getTier(ItemStack itemstack) {
        return this.baseTier;
    }
}
