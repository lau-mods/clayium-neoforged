package net.claustra01.clayium.datagen.careactor;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe.ProgressionRate;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.recipes.RecipeOutput;

public final class CaReactorRecipes {
    public static void buildRecipes(RecipeOutput output) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CA_REACTOR, 10)
                .requires(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get())
                .output(MaterialRegistration.MATERIAL_ITEMS.get("pure_antimatter").get(), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 10))
                .processingTicks(20)
                .progressionRate(ProgressionRate.TICKS)
                .save(output, ClayiumMod.id("ca_reactor/pure_antimatter"));
    }

    private CaReactorRecipes() {
    }
}
