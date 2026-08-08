/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.data;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/** Furnace conversions retained from the original silicon progression. */
public final class MaterialCraftingRecipes {
    private MaterialCraftingRecipes() {}

    public static void build(RecipeOutput output) {
        smelt(output, "impure_silicon_dust", item("impure_silicon_dust"), item("impure_silicon_ingot"));
        smelt(output, "impure_silicon_to_silicone", item("impure_silicon_ingot"), item("silicone_ingot"));
        smelt(output, "silicone_dust", item("silicone_dust"), item("silicone_ingot"));
        smelt(output, "silicon_dust", item("silicon_dust"), item("silicon_ingot"));
        smelt(output, "iron_dust", item("iron_dust"), Items.IRON_INGOT);
        smelt(output, "gold_dust", item("gold_dust"), item("gold_ingot"));
        smelt(output, "lead_dust", item("lead_dust"), item("lead_ingot"));
        smelt(output, "copper_dust", item("copper_dust"), Items.COPPER_INGOT);
        for (String material : new String[]{"impure_silicon", "silicone", "silicon", "aluminium", "clayium", "ultimate_alloy"}) {
            ItemLike block = ClayiumRegistries.MATERIAL_BLOCKS.get(material + "_block").get();
            ItemLike ingot = item(material + "_ingot");
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                    .define('I', CommonMaterialTags.tagFor(ingot).orElseThrow())
                    .pattern("III").pattern("III").pattern("III")
                    .unlockedBy("has_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ingot))
                    .save(output, Clayium.id("material_blocks/" + material));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 9)
                    .requires(CommonMaterialTags.tagFor(block).orElseThrow())
                    .unlockedBy("has_block", InventoryChangeTrigger.TriggerInstance.hasItems(block))
                    .save(output, Clayium.id("material_blocks/" + material + "_unpack"));
        }
        for (DyeColor color : DyeColor.values()) {
            ItemLike colored = ClayiumRegistries.COLORED_SILICONE_BLOCKS
                    .get(color.getSerializedName() + "_silicone_block").get();
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, colored, 8)
                    .define('S', CommonMaterialTags.tagFor(
                            ClayiumRegistries.MATERIAL_BLOCKS.get("silicone_block").get()).orElseThrow())
                    .define('D', CommonMaterialTags.common("dyes/"+color.getSerializedName()))
                    .pattern("SSS").pattern("SDS").pattern("SSS")
                    .unlockedBy("has_silicone", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ClayiumRegistries.MATERIAL_BLOCKS.get("silicone_block").get()))
                    .save(output, Clayium.id("silicone_blocks/" + color.getSerializedName()));
        }
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS,
                        ClayiumRegistries.OTHER_HULL_BLOCKS.get("zk60a_machine_hull").get())
                .define('P', CommonMaterialTags.tagFor(item("zk60a_large_plate")).orElseThrow())
                .define('C', component("precision_circuit"))
                .pattern("PPP").pattern("PCP").pattern("PPP")
                .unlockedBy("has_plate", InventoryChangeTrigger.TriggerInstance.hasItems(item("zk60a_large_plate")))
                .save(output, Clayium.id("machine_hulls/zk60a"));

        highTierHull(output, "clayium", item("clayium_large_plate"), component("clay_core"));
        highTierHull(output, "ultimate", item("ultimate_alloy_large_plate"), component("clay_brain"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("impure_ultimate_alloy_ingot"), 9)
                .requires(CommonMaterialTags.tagFor(item("barium_ingot")).orElseThrow())
                .requires(CommonMaterialTags.tagFor(item("strontium_ingot")).orElseThrow())
                .requires(CommonMaterialTags.tagFor(item("calcium_ingot")).orElseThrow())
                .requires(CommonMaterialTags.tagFor(item("clayium_ingot")).orElseThrow())
                .requires(Ingredient.of(CommonMaterialTags.tagFor(item("aluminium_ingot")).orElseThrow()), 5)
                .unlockedBy("has_clayium", InventoryChangeTrigger.TriggerInstance.hasItems(item("clayium_ingot")))
                .save(output, Clayium.id("materials/impure_ultimate_alloy"));
    }

    private static void highTierHull(RecipeOutput output, String tier, ItemLike plate, ItemLike circuit) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS,
                        ClayiumRegistries.MACHINE_HULL_BLOCKS.get(tier + "_machine_hull").get())
                .define('#', CommonMaterialTags.tagFor(plate).orElseThrow())
                .define('C', circuit)
                .define('E', component("clay_energy_excitor"))
                .pattern("#E#").pattern("#C#").pattern("###")
                .unlockedBy("has_plate", InventoryChangeTrigger.TriggerInstance.hasItems(plate))
                .save(output, Clayium.id("machine_hulls/" + tier));
    }

    private static void smelt(RecipeOutput output, String id, ItemLike input, ItemLike result) {
        SimpleCookingRecipeBuilder.smelting(CommonMaterialTags.ingredient(input), RecipeCategory.MISC, result, 0.0F, 200)
                .unlockedBy("has_input", InventoryChangeTrigger.TriggerInstance.hasItems(input))
                .save(output, Clayium.id("smelting/" + id));
    }

    private static ItemLike item(String id) {
        if (ClayiumRegistries.COMPONENT_ITEMS.containsKey(id)) {
            return ClayiumRegistries.COMPONENT_ITEMS.get(id).get();
        }
        return ClayiumRegistries.MATERIAL_ITEMS.get(id).get();
    }

    private static ItemLike component(String id) {
        return ClayiumRegistries.COMPONENT_ITEMS.get(id).get();
    }

    private static Item dye(DyeColor color) {
        return switch (color) {
            case WHITE -> Items.WHITE_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case LIME -> Items.LIME_DYE;
            case PINK -> Items.PINK_DYE;
            case GRAY -> Items.GRAY_DYE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
            case CYAN -> Items.CYAN_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case BROWN -> Items.BROWN_DYE;
            case GREEN -> Items.GREEN_DYE;
            case RED -> Items.RED_DYE;
            case BLACK -> Items.BLACK_DYE;
        };
    }
}
