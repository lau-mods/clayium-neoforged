package mods.clayium.util.crafting;

import net.minecraft.item.ItemStack;

public interface ISpecialResultRecipe {
    RecipeMap[] getUsageRecipeMap(ItemStack var1);

    RecipeMap[] getCraftingRecipeMap(ItemStack var1);
}
