package net.claustra01.clayium.datagen.recipe;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.chemical.ChemicalMetalSeparation;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public final class ChemicalRecipes {
    private static final List<Transformation> TRANSFORMATIONS = List.of(
            transform("lithium", "sodium", 7, 10),
            transform("sodium", "potassium", 7, 30),
            transform("potassium", "rubidium", 8, 10),
            transform("rubidium", "caesium", 8, 20),
            transform("caesium", "francium", 8, 30),
            transform("francium", "radium", 8, 50),
            transform("radium", "actinium", 9, 10),
            transform("actinium", "thorium", 9, 20),
            transform("thorium", "protactinium", 9, 30),
            transform("protactinium", "uranium", 9, 50),
            transform("uranium", "neptunium", 9, 80),
            transform("neptunium", "plutonium", 10, 20),
            transform("plutonium", "americium", 11, 30),
            transform("americium", "curium", 12, 50),
            transform("beryllium", "magnesium", 7, 10),
            transform("magnesium", "calcium", 7, 20),
            transform("calcium", "strontium", 7, 30),
            transform("strontium", "barium", 7, 50),
            transform("barium", "lanthanum", 8, 10),
            transform("lanthanum", "cerium", 8, 30),
            transform("cerium", "praseodymium", 8, 90),
            transform("praseodymium", "neodymium", 9, 20),
            transform("neodymium", "promethium", 10, 10),
            transform("promethium", "samarium", 11, 20),
            transform("samarium", "europium", 12, 60),
            transform("zirconium", "titanium", 8, 60),
            transform("titanium", "vanadium", 9, 60),
            transform("cobalt", "nickel", 8, 90),
            transform("nickel", "palladium", 9, 40),
            transform("gold", "platinum", 10, 30),
            transform("platinum", "iridium", 11, 10),
            transform("iridium", "osmium", 11, 30),
            transform("osmium", "rhenium", 12, 10),
            transform("hafnium", "tantalum", 8, 70),
            transform("tantalum", "tungsten", 9, 40),
            transform("tungsten", "molybdenum", 10, 20),
            transform("molybdenum", "chrome", 11, 10),
            transform("lead", "tin", 7, 50),
            transform("tin", "antimony", 8, 20),
            transform("antimony", "bismuth", 9, 10));

    public static void buildRecipes(RecipeOutput output) {
        chemicalReactorRecipes(output);
        metalSeparatorRecipe(output);
        matterTransformerRecipes(output);
        fluidGameTestRecipe(output);
    }

    private static void chemicalReactorRecipes(RecipeOutput output) {
        chemical(4, 120, 5)
                .requires(material("salt"), 2)
                .requires(material("calcareous_clay_dust"), 1)
                .output(material("calcium_chloride_dust"), 1)
                .output(material("sodium_carbonate_dust"), 1)
                .save(output, ClayiumMod.id("chemical_reactor/salt_and_calcareous_clay"));
        chemical(4, 120, 4)
                .requires(material("sodium_carbonate_dust"), 1)
                .requires(material("clay_dust"), 1)
                .output(material("quartz_dust"), 1)
                .save(output, ClayiumMod.id("chemical_reactor/sodium_carbonate_and_clay"));
        chemical(4, 120, 4)
                .requires(material("quartz_dust"), 1)
                .requires(Ingredient.of(Items.COAL, Items.CHARCOAL), 1)
                .output(material("impure_silicon_ingot"), 1)
                .save(output, ClayiumMod.id("chemical_reactor/quartz_and_coal"));
        chemical(5, 30, 5)
                .requires(material("dense_clay_dust"), 1)
                .output(material("impure_silicon_dust"), 1)
                .output(material("aluminium_dust"), 1)
                .save(output, ClayiumMod.id("chemical_reactor/dense_clay_separation"));
        chemical(8, 1, 8, 10.0D)
                .requires(material("salt"), 1)
                .requires(material("industrial_clay_dust"), 1)
                .output(material("quartz_dust"), 1)
                .output(material("calcium_chloride_dust"), 1)
                .save(output, ClayiumMod.id("chemical_reactor/industrial_clay_and_salt"));
        chemical(8, 1, 8, 10.0D)
                .requires(material("quartz_dust"), 1)
                .requires(material("industrial_clay_dust"), 1)
                .output(material("impure_silicon_ingot"), 1)
                .save(output, ClayiumMod.id("chemical_reactor/industrial_clay_and_quartz"));
        chemical(5, 10, 5)
                .requires(material("energized_clay_dust"), 1)
                .output(material("impure_redstone"), 1)
                .output(material("impure_glowstone_dust"), 1)
                .save(output, ClayiumMod.id("chemical_reactor/energetic_clay_separation"));
    }

    private static void metalSeparatorRecipe(RecipeOutput output) {
        ClayiumRecipeBuilder builder = ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CHEMICAL_METAL_SEPARATOR, 6)
                .requires(material("industrial_clay_dust"), 1)
                .clayEnergy(5_000L)
                .processingTicks(40);
        ChemicalMetalSeparation.PRODUCTS.forEach(product -> builder.randomOutput(
                material(product.itemId()), 1, product.weight(), 1.0F));
        builder.save(output, ClayiumMod.id("chemical_metal_separator/industrial_clay"));
    }

    private static void matterTransformerRecipes(RecipeOutput output) {
        TRANSFORMATIONS.forEach(recipe -> transformer(recipe.tier(), recipe.multiplier(), 200)
                .requires(material(recipe.input() + "_ingot"), 1)
                .output(material(recipe.output() + "_ingot"), 1)
                .save(output, ClayiumMod.id("matter_transformer/" + recipe.input() + "_to_" + recipe.output())));
        transformer(7, 1, 200)
                .requires(material("industrial_clay_dust"), 1)
                .output(material("carbon_dust"), 1)
                .save(output, ClayiumMod.id("matter_transformer/industrial_clay_to_carbon"));
        transformer(7, 90, 200)
                .requires(material("manganese_ingot"), 1)
                .output(Items.IRON_INGOT, 1)
                .save(output, ClayiumMod.id("matter_transformer/manganese_to_iron"));
        transformer(8, 30, 200)
                .requires(Items.IRON_INGOT, 1)
                .output(material("cobalt_ingot"), 1)
                .save(output, ClayiumMod.id("matter_transformer/iron_to_cobalt"));
        transformer(8, 20, 200)
                .requires(material("zinc_ingot"), 1)
                .output(Items.COPPER_INGOT, 1)
                .save(output, ClayiumMod.id("matter_transformer/zinc_to_copper"));
        transformer(9, 10, 200)
                .requires(Items.COPPER_INGOT, 1)
                .output(material("silver_ingot"), 1)
                .save(output, ClayiumMod.id("matter_transformer/copper_to_silver"));
        transformer(9, 50, 200)
                .requires(material("silver_ingot"), 1)
                .output(Items.GOLD_INGOT, 1)
                .save(output, ClayiumMod.id("matter_transformer/silver_to_gold"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.MATTER_TRANSFORMER, 11)
                .requires(Items.DIAMOND, 1)
                .output(Items.EMERALD, 1)
                .clayEnergy(5L * ClayiumRecipeEnergy.e(1.0D, 10) + ClayiumRecipeEnergy.e(1.0D, 11))
                .processingTicks(200)
                .save(output, ClayiumMod.id("matter_transformer/diamond_to_emerald"));
        transformer(7, 10, 200)
                .requires(material("silicon_dust"), 1)
                .output(material("phosphorus_dust"), 1)
                .save(output, ClayiumMod.id("matter_transformer/silicon_to_phosphorus"));
        naturalTransformations(output);
    }

    private static void naturalTransformations(RecipeOutput output) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.MATTER_TRANSFORMER, 9)
                .requires(material("carbon_dust"), 1)
                .output(material("charcoal_dust"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 8) + ClayiumRecipeEnergy.e(1.0D, 9))
                .processingTicks(200)
                .save(output, ClayiumMod.id("matter_transformer/carbon_to_charcoal"));
        natural(output, "charcoal_to_coal", material("charcoal_dust"), 1, material("coal_dust"), 1, 10, 200);
        natural(output, "coal_to_lapis", material("coal_dust"), 1, Items.LAPIS_LAZULI, 1, 10, 200);
        natural(output, "cobblestone_to_netherrack", Blocks.COBBLESTONE, 1, Blocks.NETHERRACK, 1, 9, 20);
        natural(output, "netherrack_to_end_stone", Blocks.NETHERRACK, 1, Blocks.END_STONE, 1, 11, 20);

        chain(output, "anvil", 20, new ItemLike[]{Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL},
                new int[]{0, 8, 8});
        chain(output, "stone_brick", 20,
                new ItemLike[]{Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS},
                new int[]{0, 8, 8});
        chain(output, "ground", 20,
                new ItemLike[]{Blocks.DIRT, Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.MYCELIUM},
                new int[]{0, 7, 8, 9});
        chain(output, "log", 80,
                new ItemLike[]{Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG,
                        Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.CHERRY_LOG, Blocks.MANGROVE_LOG},
                new int[]{0, 7, 7, 8, 8, 8, 8, 8});
        chain(output, "leaves", 20,
                new ItemLike[]{Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
                        Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.CHERRY_LEAVES, Blocks.MANGROVE_LEAVES,
                        Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES},
                new int[]{0, 7, 7, 8, 8, 8, 8, 8, 8, 8});
        chain(output, "sapling", 20,
                new ItemLike[]{Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING,
                        Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING,
                        Blocks.CHERRY_SAPLING, Blocks.MANGROVE_PROPAGULE, Blocks.AZALEA, Blocks.FLOWERING_AZALEA},
                new int[]{0, 7, 7, 8, 8, 8, 8, 8, 8, 8});
        chain(output, "seed", 20,
                new ItemLike[]{Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.COCOA_BEANS,
                        Items.NETHER_WART},
                new int[]{0, 8, 8, 8, 8});
        chain(output, "crop", 20, new ItemLike[]{Items.WHEAT, Items.CARROT, Items.POTATO},
                new int[]{0, 8, 8});
        chain(output, "small_plant", 20,
                new ItemLike[]{Blocks.SHORT_GRASS, Blocks.FERN, Blocks.DEAD_BUSH, Blocks.VINE, Blocks.LILY_PAD},
                new int[]{0, 7, 7, 8, 9});
        chain(output, "flower", 20,
                new ItemLike[]{Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM,
                        Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP,
                        Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.TALL_GRASS,
                        Blocks.LARGE_FERN, Blocks.ROSE_BUSH, Blocks.PEONY},
                new int[]{0, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8});

        natural(output, "sugar_cane_to_cactus", Items.SUGAR_CANE, 1, Blocks.CACTUS, 1, 8, 20);
        natural(output, "rotten_flesh_to_porkchop", Items.ROTTEN_FLESH, 32, Items.PORKCHOP, 1, 9, 200);
        natural(output, "porkchop_to_beef", Items.PORKCHOP, 1, Items.BEEF, 1, 9, 200);
        natural(output, "beef_to_chicken", Items.BEEF, 2, Items.CHICKEN, 1, 9, 200);
        natural(output, "leather_to_wool", Items.LEATHER, 1, Blocks.WHITE_WOOL, 4, 9, 80);
        natural(output, "wool_to_feather", Blocks.WHITE_WOOL, 1, Items.FEATHER, 4, 9, 80);
        natural(output, "bone_to_blaze_rod", Items.BONE, 64, Items.BLAZE_ROD, 1, 9, 200);
        natural(output, "blaze_rod_to_ender_pearl", Items.BLAZE_ROD, 4, Items.ENDER_PEARL, 1, 9, 200);
        natural(output, "slime_ball_to_egg", Items.SLIME_BALL, 1, Items.EGG, 1, 8, 100);
        natural(output, "egg_to_ink_sac", Items.EGG, 1, Items.INK_SAC, 1, 8, 100);
        natural(output, "ink_sac_to_spider_eye", Items.INK_SAC, 1, Items.SPIDER_EYE, 1, 9, 100);
        natural(output, "gravel_to_flint", Blocks.GRAVEL, 1, Items.FLINT, 1, 7, 1_000);
    }

    private static void chain(RecipeOutput output, String id, int ticks, ItemLike[] values, int[] tiers) {
        for (int index = 1; index < values.length; index++) {
            natural(output, id + "_" + index, values[index - 1], 1, values[index], 1, tiers[index], ticks);
        }
    }

    private static void natural(RecipeOutput output, String id, ItemLike input, int inputCount,
                                ItemLike result, int resultCount, int tier, int ticks) {
        transformer(tier, 1, ticks)
                .requires(input, inputCount)
                .output(result, resultCount)
                .save(output, ClayiumMod.id("matter_transformer/" + id));
    }

    private static void fluidGameTestRecipe(RecipeOutput output) {
        chemical(4, 20, 4)
                .requires(material("clay_dust"), 1)
                .requiresFluid(new FluidStack(Fluids.WATER, 250))
                .output(material("quartz_dust"), 1)
                .save(output, ClayiumMod.id("gametest/chemical_fluid_input"));
    }

    private static ClayiumRecipeBuilder chemical(int tier, int ticks, int energyTier) {
        return chemical(tier, ticks, energyTier, 1.0D);
    }

    private static ClayiumRecipeBuilder chemical(int tier, int ticks, int energyTier, double energyMultiplier) {
        return ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CHEMICAL_REACTOR, tier)
                .clayEnergy(ClayiumRecipeEnergy.e(energyMultiplier, energyTier))
                .processingTicks(ticks);
    }

    private static ClayiumRecipeBuilder transformer(int tier, long multiplier, int ticks) {
        return ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.MATTER_TRANSFORMER, tier)
                .clayEnergy(ClayiumRecipeEnergy.e(multiplier, tier))
                .processingTicks(ticks);
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static Transformation transform(String input, String output, int tier, long multiplier) {
        return new Transformation(input, output, tier, multiplier);
    }

    private record Transformation(String input, String output, int tier, long multiplier) {
    }

    private ChemicalRecipes() {
    }
}
