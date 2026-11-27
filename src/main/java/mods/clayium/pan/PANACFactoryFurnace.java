package mods.clayium.pan;

import java.util.Map.Entry;
import mods.clayium.util.crafting.IItemPattern;
import mods.clayium.util.crafting.ItemPatternItemStack;
import mods.clayium.util.crafting.SmeltingRecipe;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.world.World;

public class PANACFactoryFurnace implements IPANAdapterConversionFactory {
    public PANACFactoryFurnace() {
        super();
    }

    public boolean match(World world, int x, int y, int z) {
        return world.func_147439_a(x, y, z) == Blocks.field_150460_al;
    }

    public IPANConversion getConversion(IPANAdapter adapter) {
        ItemStack ingred = null;

        for (ItemStack p : adapter.getPatternItems()) {
            if (p != null) {
                if (ingred != null) {
                    return null;
                }

                ingred = p.func_77946_l();
            }
        }

        if (ingred == null) {
            return null;
        } else {
            for (Entry entry : FurnaceRecipes.func_77602_a().func_77599_b().entrySet()) {
                if (this.func_151397_a(ingred, (ItemStack)entry.getKey())) {
                    ingred = (ItemStack)entry.getKey();
                    ItemStack result = (ItemStack)entry.getValue();
                    return new PANConversion(
                        new IItemPattern[]{new ItemPatternItemStack(ingred)},
                        new ItemStack[]{result},
                        (double)(SmeltingRecipe.timeSmelting * SmeltingRecipe.energySmelting)
                    );
                }
            }

            return null;
        }
    }

    private boolean func_151397_a(ItemStack p_151397_1_, ItemStack p_151397_2_) {
        return p_151397_2_.func_77973_b() == p_151397_1_.func_77973_b()
            && (p_151397_2_.func_77960_j() == 32767 || p_151397_2_.func_77960_j() == p_151397_1_.func_77960_j());
    }
}
