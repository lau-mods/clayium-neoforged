package net.claustra01.clayium.datagen.claytree;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.recipes.RecipeOutput;

public final class ClayTreeRecipes {
    public static void buildRecipes(RecipeOutput output) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.GRINDER, 5)
                .requires(ClayTreeRegistration.CLAY_TREE_LOG.get())
                .output(MaterialRegistration.MATERIAL_ITEMS.get("organic_clay").get(), 1)
                .clayEnergy(1_000L)
                .processingTicks(200)
                .save(output, ClayiumMod.id("grinder/clay_tree_log"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CENTRIFUGE, 6)
                .requires(ClayTreeRegistration.CLAY_TREE_LOG.get())
                .output(MaterialRegistration.MATERIAL_ITEMS.get("advanced_industrial_clay_dust").get(), 16)
                .output(MaterialRegistration.MATERIAL_ITEMS.get("manganese_dust").get(), 5)
                .output(MaterialRegistration.MATERIAL_ITEMS.get("lithium_dust").get(), 3)
                .output(MaterialRegistration.MATERIAL_ITEMS.get("zirconium_dust").get(), 1)
                .clayEnergy(10_000L)
                .processingTicks(400)
                .save(output, ClayiumMod.id("centrifuge/clay_tree_log"));
    }

    private ClayTreeRecipes() {
    }
}
