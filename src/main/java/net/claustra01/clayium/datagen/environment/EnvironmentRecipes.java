package net.claustra01.clayium.datagen.environment;

import java.util.Optional;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.recipes.RecipeOutput;

public final class EnvironmentRecipes {
    public static void buildRecipes(RecipeOutput output) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.SALT_EXTRACTOR, 4)
                .output(MaterialRegistration.MATERIAL_ITEMS.get("salt").get(), 1)
                .clayEnergy(100L)
                .processingTicks(20)
                .environment(new ClayiumMachineRecipe.EnvironmentCondition(
                        Optional.empty(), Optional.empty(), Optional.empty(), false, true))
                .save(output, ClayiumMod.id("salt_extractor/salt"));
    }

    private EnvironmentRecipes() {
    }
}
