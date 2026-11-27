package net.claustra01.clayium.datagen;

import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.datagen.recipe.ClayMachineBuildRecipeGenerator;
import net.claustra01.clayium.datagen.recipe.ClayMaterialMachineRecipeGenerator;
import net.claustra01.clayium.datagen.recipe.ClayWorkTableRecipeGenerator;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper;
import net.claustra01.clayium.datagen.recipe.VanillaCraftingAndSmeltingGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

/**
 * Root RecipeProvider emitting all vanilla crafting/smelting, ClayWorkTable, ChemicalMetalSeparator,
 * and ClayMachine recipes into data/clayium_neoforged/recipe/.
 */
public class ClayiumRecipeProvider extends RecipeProvider {
    public ClayiumRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ClayiumRecipeHelper helper = new ClayiumRecipeHelper(output);
        ClayWorkTableRecipeGenerator.build(helper);
        VanillaCraftingAndSmeltingGenerator.build(helper);
        ClayMaterialMachineRecipeGenerator.build(helper);
        ClayMachineBuildRecipeGenerator.build(helper);
    }
}
