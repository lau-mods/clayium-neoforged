package mods.clayium.plugin.nei;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.recipe.ShapelessRecipeHandler;
import codechicken.nei.recipe.ShapelessRecipeHandler.CachedShapelessRecipe;
import mods.clayium.util.crafting.ISpecialResultRecipe;
import mods.clayium.util.crafting.RecipeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

public class NEIShapelessSpecialResultRecipe extends ShapelessRecipeHandler {
    public NEIShapelessSpecialResultRecipe() {
        super();
    }

    public void loadCraftingRecipes(ItemStack result) {
        for (IRecipe irecipe : CraftingManager.func_77594_a().func_77592_b()) {
            if (irecipe instanceof ISpecialResultRecipe) {
                RecipeMap[] maps = ((ISpecialResultRecipe)irecipe).getCraftingRecipeMap(result);
                if (maps != null) {
                    for (RecipeMap map : maps) {
                        CachedShapelessRecipe recipe = this.toShapelessRecipe(map);
                        if (recipe != null) {
                            this.arecipes.add(recipe);
                        }
                    }
                }
            }
        }
    }

    public void loadUsageRecipes(ItemStack ingredient) {
        for (IRecipe irecipe : CraftingManager.func_77594_a().func_77592_b()) {
            if (irecipe instanceof ISpecialResultRecipe) {
                RecipeMap[] maps = ((ISpecialResultRecipe)irecipe).getUsageRecipeMap(ingredient);
                if (maps != null) {
                    for (RecipeMap map : maps) {
                        CachedShapelessRecipe recipe = this.toShapelessRecipe(map);
                        if (recipe != null) {
                            recipe.setIngredientPermutation(recipe.ingredients, ingredient);
                            this.arecipes.add(recipe);
                        }
                    }
                }
            }
        }
    }

    public CachedShapelessRecipe toShapelessRecipe(RecipeMap map) {
        if (map != null && "shapeless".equals(map.getRecipeType()) && map.getResults() != null && map.getResults().length != 0) {
            try {
                return new CachedShapelessRecipe(this, map.getIngredients(), map.getResults()[0]);
            } catch (Exception var3) {
                NEIClientConfig.logger.error("Error loading recipe: ", var3);
                return null;
            }
        } else {
            return null;
        }
    }
}
