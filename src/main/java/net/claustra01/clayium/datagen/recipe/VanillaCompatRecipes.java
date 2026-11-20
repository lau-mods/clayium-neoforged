package net.claustra01.clayium.datagen.recipe;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

public final class VanillaCompatRecipes {
    public static void buildRecipes(RecipeOutput output) {
        rawMetal(output, "iron", material("iron_dust"), Tags.Items.RAW_MATERIALS_IRON,
                Tags.Items.STORAGE_BLOCKS_RAW_IRON, Tags.Items.ORES_IRON);
        rawMetal(output, "gold", material("gold_dust"), Tags.Items.RAW_MATERIALS_GOLD,
                Tags.Items.STORAGE_BLOCKS_RAW_GOLD, Tags.Items.ORES_GOLD);
        rawMetal(output, "copper", material("copper_dust"), Tags.Items.RAW_MATERIALS_COPPER,
                Tags.Items.STORAGE_BLOCKS_RAW_COPPER, Tags.Items.ORES_COPPER);
    }

    private static void rawMetal(RecipeOutput output, String metal, ItemLike dust,
                                 TagKey<Item> raw, TagKey<Item> rawBlock, TagKey<Item> ore) {
        grinder(raw, 1, dust, 1)
                .save(output, ClayiumMod.id("grinder/raw_" + metal));
        grinder(rawBlock, 1, dust, 9)
                .save(output, ClayiumMod.id("grinder/raw_" + metal + "_block"));
        grinder(ore, 1, dust, 2)
                .save(output, ClayiumMod.id("grinder/" + metal + "_ore"));
    }

    private static ClayiumRecipeBuilder grinder(
            TagKey<Item> input, int inputCount, ItemLike output, int outputCount) {
        return ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.GRINDER, 4)
                .requires(Ingredient.of(input), inputCount)
                .output(output, outputCount)
                .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 4))
                .processingTicks(40);
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private VanillaCompatRecipes() {
    }
}
