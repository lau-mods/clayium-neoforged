package mods.clayium.util.crafting;

import java.util.Arrays;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;

public class ShapelessRecipesWithNBT extends ShapelessRecipes {
    public ShapelessRecipesWithNBT(ItemStack output, ItemStack input) {
        super(output, Arrays.asList(input));
    }

    public ItemStack func_77572_b(InventoryCrafting inv) {
        ItemStack output = super.func_77572_b(inv);
        NBTTagCompound tag = null;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ItemStack itemstack = inv.func_70463_b(j, i);
                if (itemstack != null) {
                    boolean flag = false;

                    for (Object e : this.field_77579_b) {
                        ItemStack itemstack1 = (ItemStack)e;
                        if (itemstack.func_77973_b() == itemstack1.func_77973_b()
                            && (itemstack1.func_77960_j() == 32767 || itemstack.func_77960_j() == itemstack1.func_77960_j())) {
                            tag = itemstack.func_77978_p();
                        }
                    }
                }
            }
        }

        output.func_77982_d(tag);
        return output;
    }
}
