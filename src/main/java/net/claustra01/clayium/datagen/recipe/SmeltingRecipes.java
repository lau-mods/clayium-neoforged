package net.claustra01.clayium.datagen.recipe;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.ConfigEnabledCondition;
import net.claustra01.clayium.recipe.ConfigEnabledCondition.Option;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;

public final class SmeltingRecipes {
    private static final List<SmeltingRecipe> SMELTING = List.of(
            smelting("aluminium", 5, 0.5D, 200),
            smelting("sodium", 5, 0.5D, 200),
            smelting("zinc", 5, 0.5D, 200),
            smelting("nickel", 5, 0.5D, 200),
            smelting("magnesium", 6, 0.2D, 400),
            smelting("lithium", 6, 0.2D, 400),
            smelting("zirconium", 6, 0.2D, 400),
            smelting("zinc_aluminium_compound", 6, 0.2D, 400),
            smelting("zinc_zirconium_compound", 6, 0.2D, 400),
            smelting("az91_d_alloy", 6, 0.2D, 400),
            smelting("zk60_a_alloy", 6, 0.2D, 400));

    private static final List<ElectrolysisRecipe> ELECTROLYSIS = List.of(
            electrolysis("aluminium", 6, 1.0D, 100),
            electrolysis("magnesium", 6, 1.0D, 100),
            electrolysis("sodium", 6, 1.0D, 100),
            electrolysis("lithium", 6, 1.0D, 100),
            electrolysis("zirconium", 6, 1.0D, 100),
            electrolysis("zinc", 6, 1.0D, 100),
            electrolysis("manganese", 7, 10.0D, 300),
            electrolysis("potassium", 7, 10.0D, 300),
            electrolysis("hafnium", 7, 10.0D, 300),
            electrolysis("strontium", 7, 10.0D, 300),
            electrolysis("barium", 7, 10.0D, 300),
            electrolysis("calcium", 7, 10.0D, 300),
            electrolysis("iron", 8, 10.0D, 1_000),
            electrolysis("lead", 8, 10.0D, 1_000),
            electrolysis("copper", 8, 10.0D, 1_000),
            electrolysis("nickel", 9, 10.0D, 3_000),
            electrolysis("beryllium", 9, 10.0D, 3_000),
            electrolysis("chrome", 9, 10.0D, 3_000),
            electrolysis("titanium", 9, 10.0D, 3_000));

    public static void buildRecipes(RecipeOutput output) {
        SMELTING.forEach(recipe -> machine(ClayiumRecipeMethod.SMELTER, recipe.tier(), recipe.energy(), recipe.ticks())
                .requires(common("dusts/" + recipe.material()), 1)
                .output(material(recipe.material() + "_ingot"), 1)
                .save(output, ClayiumMod.id("smelter/" + recipe.material())));
        machine(ClayiumRecipeMethod.SMELTER, 5, 0.5D, 200)
                .requires(material("impure_aluminium_dust"), 1)
                .output(material("impure_aluminium_ingot"), 1)
                .when(new ConfigEnabledCondition(Option.HARDCORE_ALUMINIUM))
                .save(output, ClayiumMod.id("smelter/impure_aluminium"));
        for (String material : List.of("impure_silicon", "silicon", "iron", "gold", "lead",
                "copper", "bronze", "brass", "electrum", "invar")) {
            SimpleCookingRecipeBuilder.smelting(common("dusts/" + material), RecipeCategory.MISC,
                            material(material + "_ingot"), 0.0F, 200)
                    .unlockedBy("has_dust", InventoryChangeTrigger.TriggerInstance.hasItems(material(material + "_dust")))
                    .save(output, ClayiumMod.id("smelting/" + material));
        }
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(material("impure_silicon_ingot")),
                        RecipeCategory.MISC, material("silicone_ingot"), 0.0F, 200)
                .unlockedBy("has_impure_silicon", InventoryChangeTrigger.TriggerInstance.hasItems(
                        material("impure_silicon_ingot")))
                .save(output, ClayiumMod.id("smelting/silicone"));
        alloyRecipes(output);
        ELECTROLYSIS.forEach(recipe -> machine(
                        ClayiumRecipeMethod.ELECTROLYSIS_REACTOR,
                        recipe.tier(),
                        recipe.energy(),
                        recipe.ticks())
                .requires(common("dusts/impure_" + recipe.material()), 1)
                .output(material(recipe.material() + "_dust"), 1)
                .save(output, ClayiumMod.id("electrolysis_reactor/" + recipe.material())));
    }

    private static void alloyRecipes(RecipeOutput output) {
        alloy(6, 1.0D, 50)
                .requires(common("ingots/zinc"), 9)
                .requires(common("ingots/aluminium"), 1)
                .output(material("zinc_aluminium_compound_ingot"), 10)
                .save(output, ClayiumMod.id("alloy_smelter/zinc_aluminium_compound"));
        alloy(7, 3.0D, 50)
                .requires(common("ingots/zinc"), 9)
                .requires(common("ingots/zirconium"), 1)
                .output(material("zinc_zirconium_compound_ingot"), 10)
                .save(output, ClayiumMod.id("alloy_smelter/zinc_zirconium_compound"));
        alloy(7, 1.0D, 500)
                .requires(common("ingots/magnesium"), 9)
                .requires(common("ingots/zinc_aluminium_compound"), 1)
                .output(material("az91_d_alloy_ingot"), 10)
                .save(output, ClayiumMod.id("alloy_smelter/az91_d_alloy"));
        alloy(7, 3.0D, 500)
                .requires(common("ingots/magnesium"), 19)
                .requires(common("ingots/zinc_zirconium_compound"), 1)
                .output(material("zk60_a_alloy_ingot"), 20)
                .save(output, ClayiumMod.id("alloy_smelter/zk60_a_alloy"));
        alloy(4, 1.0D, 40)
                .requires(ingotOrDust("copper"), 3)
                .requires(ingotOrDust("tin"), 1)
                .output(material("bronze_ingot"), 4)
                .save(output, ClayiumMod.id("alloy_smelter/bronze"));
        alloy(4, 1.0D, 40)
                .requires(ingotOrDust("copper"), 3)
                .requires(ingotOrDust("zinc"), 1)
                .output(material("brass_ingot"), 4)
                .save(output, ClayiumMod.id("alloy_smelter/brass"));
        alloy(4, 1.0D, 40)
                .requires(ingotOrDust("gold"), 1)
                .requires(ingotOrDust("silver"), 1)
                .output(material("electrum_ingot"), 2)
                .save(output, ClayiumMod.id("alloy_smelter/electrum"));
        alloy(4, 1.0D, 40)
                .requires(ingotOrDust("iron"), 2)
                .requires(ingotOrDust("nickel"), 1)
                .output(material("invar_ingot"), 3)
                .save(output, ClayiumMod.id("alloy_smelter/invar"));
    }

    private static ClayiumRecipeBuilder alloy(int energyTier, double energy, int ticks) {
        return machine(ClayiumRecipeMethod.ALLOY_SMELTER, 6, energy, energyTier, ticks);
    }

    private static ClayiumRecipeBuilder machine(
            ClayiumRecipeMethod method, int tier, double energy, int ticks) {
        return machine(method, tier, energy, tier, ticks);
    }

    private static ClayiumRecipeBuilder machine(
            ClayiumRecipeMethod method, int tier, double energy, int energyTier, int ticks) {
        return ClayiumRecipeBuilder.machine(method, tier)
                .clayEnergy(ClayiumRecipeEnergy.e(energy, energyTier))
                .processingTicks(ticks);
    }

    private static Ingredient common(String path) {
        return Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path)));
    }

    private static Ingredient ingotOrDust(String material) {
        return CompoundIngredient.of(common("ingots/" + material), common("dusts/" + material));
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static SmeltingRecipe smelting(String material, int tier, double energy, int ticks) {
        return new SmeltingRecipe(material, tier, energy, ticks);
    }

    private static ElectrolysisRecipe electrolysis(String material, int tier, double energy, int ticks) {
        return new ElectrolysisRecipe(material, tier, energy, ticks);
    }

    private record SmeltingRecipe(String material, int tier, double energy, int ticks) {
    }

    private record ElectrolysisRecipe(String material, int tier, double energy, int ticks) {
    }

    private SmeltingRecipes() {
    }
}
