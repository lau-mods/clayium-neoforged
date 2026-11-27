package mods.clayium.block.itemblock;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import mods.clayium.item.IClayEnergy;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemBlockCompressedClay extends ItemBlockDamaged implements IClayEnergy {
    public ItemBlockCompressedClay(Block block) {
        super(block);
    }

    public long getClayEnergy(ItemStack itemStack) {
        return itemStack.func_77960_j() >= 4 ? (long)Math.pow(10.0, (double)(itemStack.func_77960_j() + 1)) : 0L;
    }

    public int getTier(ItemStack itemstack) {
        return itemstack.func_77960_j();
    }

    @SideOnly(Side.CLIENT)
    public void func_77624_a(ItemStack itemstack, EntityPlayer player, List list, boolean flag) {
        super.func_77624_a(itemstack, player, list, flag);
        list.add(UtilLocale.ClayEnergyNumeral(this.getClayEnergy(itemstack)) + "CE");
    }
}
