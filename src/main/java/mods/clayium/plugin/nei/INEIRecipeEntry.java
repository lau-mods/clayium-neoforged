package mods.clayium.plugin.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler.CachedRecipe;
import java.util.List;
import net.minecraft.item.ItemStack;

public interface INEIRecipeEntry {
    boolean matchForCraftingRecipe(ItemStack var1);

    boolean matchForUsageRecipe(ItemStack var1);

    void drawExtras();

    CachedRecipe asCachedRecipe();

    List<PositionedStack> getIngredientsToSort();

    TemplateRecipeHandler getHandler();

    void setHandler(TemplateRecipeHandler var1);
}
