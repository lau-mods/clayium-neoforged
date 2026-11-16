package net.claustra01.clayium.datagen.material;

import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class MaterialRecipeProvider extends RecipeProvider {
    public MaterialRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        CompressionTier[] tiers = CompressionTier.values();
        for (int index = 0; index < tiers.length; index++) {
            CompressionTier upperTier = tiers[index];
            Item lower = index == 0
                    ? Items.CLAY
                    : MaterialRegistration.COMPRESSION_BLOCKS.get(tiers[index - 1].id()).get().asItem();
            Item upper = MaterialRegistration.COMPRESSION_BLOCKS.get(upperTier.id()).get().asItem();
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, upper)
                    .define('#', lower)
                    .pattern("###")
                    .pattern("###")
                    .pattern("###")
                    .unlockedBy("has_lower_tier", has(lower))
                    .save(recipeOutput, ClayiumMod.id("compression/" + upperTier.id()));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, lower, 9)
                    .requires(upper)
                    .unlockedBy("has_upper_tier", has(upper))
                    .save(recipeOutput, ClayiumMod.id("decompression/" + upperTier.id()));
        }
    }
}
