package mods.clayium.block;

import mods.clayium.core.ClayiumCore;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public class MachineHull extends BlockDamaged implements ITieredBlock {
    public MachineHull(int maxMeta) {
        super(Material.field_151573_f, maxMeta);
        this.func_149658_d("clayium:machinehull");
        this.func_149663_c("blockMachineHull").func_149647_a(ClayiumCore.creativeTabClayium);
        this.func_149711_c(2.0F).func_149752_b(5.0F).func_149672_a(Block.field_149777_j);
        this.setHarvestLevel("pickaxe", 0);
    }

    public int getTier(ItemStack itemstack) {
        return itemstack.func_77960_j() + 1;
    }

    public int getTier(IBlockAccess world, int x, int y, int z) {
        return world.func_72805_g(x, y, z) + 1;
    }
}
