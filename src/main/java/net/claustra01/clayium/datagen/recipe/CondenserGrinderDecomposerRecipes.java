package net.claustra01.clayium.datagen.recipe;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

public final class CondenserGrinderDecomposerRecipes {
    public static void buildRecipes(RecipeOutput output) {
        condenser(output);
        grinder(output);
        decomposer(output);
    }

    private static void condenser(RecipeOutput output) {
        machine(ClayiumRecipeMethod.CONDENSER, 2, 20, 1.0D)
                .requires(Items.CLAY_BALL, 4)
                .output(Items.CLAY, 1)
                .save(output, ClayiumMod.id("condenser/clay_block"));
        machine(ClayiumRecipeMethod.CONDENSER, 3, 20, 1.0D)
                .requires(material("dense_clay_dust"), 9)
                .output(compression("dense_clay"), 1)
                .save(output, ClayiumMod.id("condenser/dense_clay"));
        machine(ClayiumRecipeMethod.CONDENSER, 4, 20, 1.0D)
                .requires(material("industrial_clay_dust"), 9)
                .output(compression("industrial_clay"), 1)
                .save(output, ClayiumMod.id("condenser/industrial_clay"));
        machine(ClayiumRecipeMethod.CONDENSER, 5, 20, 1.0D)
                .requires(material("silicon_dust"), 9)
                .output(material("silicon_block"), 1)
                .save(output, ClayiumMod.id("condenser/silicon"));
        machine(ClayiumRecipeMethod.CONDENSER, 10, 20, 1.0D)
                .requires(material("antimatter_dust"), 9)
                .output(material("antimatter_block"), 1)
                .save(output, ClayiumMod.id("condenser/antimatter"));
    }

    private static void grinder(RecipeOutput output) {
        machine(ClayiumRecipeMethod.GRINDER, 2, 20, 1.0D)
                .requires(Items.CLAY, 1)
                .output(material("clay_dust"), 4)
                .save(output, ClayiumMod.id("grinder/clay"));
        machine(ClayiumRecipeMethod.GRINDER, 3, 40, 1.0D)
                .requires(Items.QUARTZ, 1)
                .output(material("quartz_dust"), 1)
                .save(output, ClayiumMod.id("grinder/quartz"));
        machine(ClayiumRecipeMethod.GRINDER, 4, 40, 1.0D)
                .requires(Items.IRON_INGOT, 1)
                .output(material("iron_dust"), 1)
                .save(output, ClayiumMod.id("grinder/iron_ingot"));
        machine(ClayiumRecipeMethod.GRINDER, 5, 40, 1.0D)
                .requires(material("aluminium_ingot"), 1)
                .output(material("aluminium_dust"), 1)
                .save(output, ClayiumMod.id("grinder/aluminium_ingot"));
        machine(ClayiumRecipeMethod.GRINDER, 6, 40, 1.0D)
                .requires(material("clay_steel_ingot"), 1)
                .output(material("clay_steel_dust"), 1)
                .save(output, ClayiumMod.id("grinder/clay_steel_ingot"));
        machine(ClayiumRecipeMethod.GRINDER, 10, 40, 1.0D)
                .requires(material("antimatter_block"), 1)
                .output(material("antimatter_dust"), 9)
                .save(output, ClayiumMod.id("grinder/antimatter_block"));
    }

    private static void decomposer(RecipeOutput output) {
        machine(ClayiumRecipeMethod.DECOMPOSER, 2, 20, 1.0D)
                .requires(Items.CLAY, 1)
                .output(Items.CLAY_BALL, 4)
                .save(output, ClayiumMod.id("decomposer/clay_block"));
        machine(ClayiumRecipeMethod.DECOMPOSER, 3, 20, 1.0D)
                .requires(compression("compressed_clay"), 1)
                .output(compression("dense_clay"), 9)
                .save(output, ClayiumMod.id("decomposer/compressed_clay"));
        machine(ClayiumRecipeMethod.DECOMPOSER, 4, 20, 1.0D)
                .requires(compression("industrial_clay"), 1)
                .output(compression("compressed_clay"), 9)
                .save(output, ClayiumMod.id("decomposer/industrial_clay"));
    }

    private static ClayiumRecipeBuilder machine(
            ClayiumRecipeMethod method, int tier, int ticks, double energy) {
        return ClayiumRecipeBuilder.machine(method, tier)
                .clayEnergy(ClayiumRecipeEnergy.e(energy, tier))
                .processingTicks(ticks);
    }

    private static net.minecraft.world.level.ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static net.minecraft.world.level.ItemLike compression(String id) {
        return MaterialRegistration.COMPRESSION_BLOCKS.get(id).get();
    }

    private CondenserGrinderDecomposerRecipes() {
    }
}
