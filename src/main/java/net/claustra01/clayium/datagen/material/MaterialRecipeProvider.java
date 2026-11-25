package net.claustra01.clayium.datagen.material;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.material.MaterialBlockDefinition;
import net.claustra01.clayium.material.SiliconeBlockDefinition;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;

public final class MaterialRecipeProvider {
    public static void buildRecipes(RecipeOutput recipeOutput) {
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
                    .unlockedBy("has_lower_tier", InventoryChangeTrigger.TriggerInstance.hasItems(lower))
                    .save(recipeOutput, ClayiumMod.id("compression/" + upperTier.id()));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, lower, 9)
                    .requires(upper)
                    .unlockedBy("has_upper_tier", InventoryChangeTrigger.TriggerInstance.hasItems(upper))
                    .save(recipeOutput, ClayiumMod.id("decompression/" + upperTier.id()));
        }
        for (MaterialBlockDefinition definition : MaterialBlockDefinition.ORIGINAL_METAL_BLOCKS) {
            String materialId = definition.id().substring(0, definition.id().length() - "_block".length());
            Item ingot = materialItem(materialId + "_ingot");
            Item block = MaterialRegistration.METAL_BLOCKS.get(definition.id()).get().asItem();
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                    .define('#', ingot)
                    .pattern("###")
                    .pattern("###")
                    .pattern("###")
                    .unlockedBy("has_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ingot))
                    .save(recipeOutput, ClayiumMod.id("material_block/" + definition.id()));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 9)
                    .requires(block)
                    .unlockedBy("has_block", InventoryChangeTrigger.TriggerInstance.hasItems(block))
                    .save(recipeOutput, ClayiumMod.id("material_ingot/" + materialId));
        }
        Item silicone = MaterialRegistration.MATERIAL_ITEMS.get("silicone_block").get();
        for (SiliconeBlockDefinition definition : SiliconeBlockDefinition.ORIGINAL_COLORS) {
            TagKey<Item> dye = TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "dyes/" + definition.color().getName()));
            Item output = MaterialRegistration.SILICONE_BLOCKS.get(definition.id()).get().asItem();
            ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, output, 8)
                    .requires(silicone, 8)
                    .requires(dye)
                    .unlockedBy("has_silicone", InventoryChangeTrigger.TriggerInstance.hasItems(silicone))
                    .save(recipeOutput, ClayiumMod.id("silicone/" + definition.color().getName()));
        }
    }

    private static Item materialItem(String id) {
        return MaterialRegistration.isVanillaBacked(id)
                ? Items.COPPER_INGOT : MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private MaterialRecipeProvider() {
    }
}
