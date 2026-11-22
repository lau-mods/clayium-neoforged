package net.claustra01.clayium.datagen.progression;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

public final class ProgressionRecipeProvider implements DataProvider {
    private final RecipeProvider recipes;

    public ProgressionRecipeProvider(PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider) {
        recipes = new RecipeProvider(output, lookupProvider) {
            @Override
            protected void buildRecipes(RecipeOutput output) {
                MachineProgressionRecipes.buildRecipes(output);
                TransportProgressionRecipes.buildRecipes(output);
                WorldProgressionRecipes.buildRecipes(output);
                CaPanProgressionRecipes.buildRecipes(output);
                EquipmentProgressionRecipes.buildRecipes(output);
                MetalChestProgressionRecipes.buildRecipes(output);
            }
        };
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return recipes.run(output);
    }

    @Override
    public String getName() {
        return "Clayium progression recipes";
    }
}
