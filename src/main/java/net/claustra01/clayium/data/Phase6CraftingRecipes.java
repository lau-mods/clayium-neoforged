/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.data;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Furnace conversions retained from the original silicon progression. */
public final class Phase6CraftingRecipes {
    private Phase6CraftingRecipes() {}

    public static void build(RecipeOutput output) {
        smelt(output, "impure_silicon_dust", item("impure_silicon_dust"), item("impure_silicon_ingot"));
        smelt(output, "impure_silicon_to_silicone", item("impure_silicon_ingot"), item("silicone_ingot"));
        smelt(output, "silicone_dust", item("silicone_dust"), item("silicone_ingot"));
        smelt(output, "silicon_dust", item("silicon_dust"), item("silicon_ingot"));
    }

    private static void smelt(RecipeOutput output, String id, ItemLike input, ItemLike result) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, result, 0.0F, 200)
                .unlockedBy("has_input", InventoryChangeTrigger.TriggerInstance.hasItems(input))
                .save(output, Clayium.id("smelting/" + id));
    }

    private static ItemLike item(String id) {
        return ClayiumRegistries.PHASE6_ITEMS.get(id).get();
    }
}
