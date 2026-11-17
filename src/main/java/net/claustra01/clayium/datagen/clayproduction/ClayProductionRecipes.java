package net.claustra01.clayium.datagen.clayproduction;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.machine.clayproduction.ClayProductionRules;
import net.claustra01.clayium.machine.clayproduction.ClayProductionType;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class ClayProductionRecipes {
    public static void buildRecipes(RecipeOutput output) {
        for (int inputLevel = 4; inputLevel < 13; inputLevel++) {
            int requiredTier = inputLevel <= 6 ? 3 : 4;
            ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.EC_CONDENSER, requiredTier)
                    .requires(ClayProductionRules.itemForLevel(inputLevel), 9)
                    .output(ClayProductionRules.itemForLevel(inputLevel + 1), 1)
                    .clayEnergy(ClayiumRecipeEnergy.e(Math.pow(10.0D, inputLevel - (requiredTier == 3 ? 5 : 6)), requiredTier))
                    .processingTicks(inputLevel == 4 ? 16 : inputLevel == 5 ? 32 : 64)
                    .save(output, ClayiumMod.id("clayproduction/ec_condenser_level_" + inputLevel));
        }

        machineRecipe(output, ClayProductionType.ENERGETIC_CLAY_CONDENSER_MK_I,
                WorkbenchRegistration.CLAY_MACHINE_HULL.get(), Items.REDSTONE);
        machineRecipe(output, ClayProductionType.ENERGETIC_CLAY_CONDENSER_MK_II,
                block(ClayProductionType.ENERGETIC_CLAY_CONDENSER_MK_I), compression("energetic_clay"));
        machineRecipe(output, ClayProductionType.AUTOMATIC_CLAY_CONDENSER_MK_I,
                block(ClayProductionType.ENERGETIC_CLAY_CONDENSER_MK_II), compression("compressed_energetic_clay"));
        machineRecipe(output, ClayProductionType.AUTOMATIC_CLAY_CONDENSER_MK_II,
                block(ClayProductionType.AUTOMATIC_CLAY_CONDENSER_MK_I), compression("double_compressed_energetic_clay"));
        machineRecipe(output, ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_I,
                block(ClayProductionType.AUTOMATIC_CLAY_CONDENSER_MK_I), Items.DAYLIGHT_DETECTOR);
        machineRecipe(output, ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_II,
                block(ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_I), compression("compressed_energetic_clay"));
        machineRecipe(output, ClayProductionType.LITHIUM_SOLAR_CLAY_FABRICATOR,
                block(ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_II), material("lithium_ingot"));
        machineRecipe(output, ClayProductionType.CLAY_FABRICATOR_MK_I,
                block(ClayProductionType.LITHIUM_SOLAR_CLAY_FABRICATOR), compression("triple_compressed_energetic_clay"));
        machineRecipe(output, ClayProductionType.CLAY_FABRICATOR_MK_II,
                block(ClayProductionType.CLAY_FABRICATOR_MK_I), compression("quadruple_compressed_energetic_clay"));
        machineRecipe(output, ClayProductionType.CLAY_FABRICATOR_MK_III,
                block(ClayProductionType.CLAY_FABRICATOR_MK_II), compression("octuple_compressed_energetic_clay"));
    }

    private static void machineRecipe(
            RecipeOutput output, ClayProductionType type, ItemLike center, ItemLike progressionMaterial) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block(type))
                .define('H', center)
                .define('P', progressionMaterial)
                .define('R', Items.REDSTONE)
                .pattern("PRP").pattern("PHP").pattern("PRP")
                .unlockedBy("has_progression_material", has(progressionMaterial))
                .save(output, ClayiumMod.id("clayproduction/" + type.id()));
    }

    private static ItemLike block(ClayProductionType type) {
        return ClayProductionRegistration.BLOCKS.get(type).get();
    }

    private static ItemLike compression(String id) {
        return MaterialRegistration.COMPRESSION_BLOCKS.get(id).get();
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    private ClayProductionRecipes() {
    }
}
