package net.claustra01.clayium.datagen.progression;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.storage.MetalChestType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.claustra01.clayium.datagen.tag.VanillaCompatTags;

public final class MetalChestProgressionRecipes {
    public static void buildRecipes(RecipeOutput output) {
        for (int index = 0; index < MetalChestType.ALL.size(); index++) {
            MetalChestType type = MetalChestType.ALL.get(index);
            String registeredMaterial = type.id().substring(0, type.id().length() - "_chest".length());
            ItemLike ingot = MaterialRegistration.MATERIAL_ITEMS.get(registeredMaterial + "_ingot").get();
            ItemLike result = StorageRegistration.METAL_CHESTS.get(type.id()).block().get();

            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, result)
                    .define('I', ingot)
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .pattern("III")
                    .pattern("ICI")
                    .pattern("III")
                    .unlockedBy("has_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ingot))
                    .save(output, ClayiumMod.id("progression/metal_chest/" + type.id() + "_from_wooden"));
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, result)
                    .define('I', ingot)
                    .define('C', VanillaCompatTags.METAL_CHESTS)
                    .pattern("III")
                    .pattern("ICI")
                    .pattern("III")
                    .unlockedBy("has_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ingot))
                    .save(output, ClayiumMod.id("progression/metal_chest/" + type.id() + "_from_metal"));
        }
    }

    private MetalChestProgressionRecipes() {
    }
}
