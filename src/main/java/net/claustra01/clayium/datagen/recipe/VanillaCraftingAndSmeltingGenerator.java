package net.claustra01.clayium.datagen.recipe;

import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.COMPRESSED_CLAY_PATHS;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.COMPRESSED_PURE_ANTIMATTERS;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.blockItem;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.commonTag;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.hasClay;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.item;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.matItem;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.matTag;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.CMaterial;
import net.claustra01.clayium.material.CMaterials;
import net.claustra01.clayium.material.CShape;
import net.claustra01.clayium.recipe.AutoEatGadgetSwitchRecipe;
import net.claustra01.clayium.recipe.ClayiumConfigCondition;
import net.claustra01.clayium.recipe.FluidCapsuleContainerRecipe;
import net.claustra01.clayium.recipe.FluidCapsuleTransferRecipe;
import net.claustra01.clayium.recipe.StorageContainerUpgradeRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/**
 * Ports all 1.7.10 CRecipes vanilla crafting/smelting recipes, MetalBlock pairs, MetalChest recipes,
 * silicone dyeing, and special dynamic crafting recipes.
 */
public final class VanillaCraftingAndSmeltingGenerator {
    private static final CMaterial[] COMPACT_INGOT_MATERIALS = {
            CMaterials.IMPURE_SILICON,
            CMaterials.SILICONE,
            CMaterials.SILICON,
            CMaterials.ALUMINIUM,
            CMaterials.CLAY_STEEL,
            CMaterials.CLAYIUM,
            CMaterials.ULTIMATE_ALLOY
    };

    private static final CMaterial[] COMPACT_GEM_MATERIALS = {
            CMaterials.ANTIMATTER,
            CMaterials.PURE_ANTIMATTER,
            CMaterials.OCTUPLE_PURE_ANTIMATTER
    };

    private static final CMaterial[] METAL_BLOCK_MATERIALS = {
            CMaterials.MAGNESIUM,
            CMaterials.SODIUM,
            CMaterials.LITHIUM,
            CMaterials.ZIRCONIUM,
            CMaterials.ZINC,
            CMaterials.MANGANESE,
            CMaterials.CALCIUM,
            CMaterials.POTASSIUM,
            CMaterials.NICKEL,
            CMaterials.BERYLLIUM,
            CMaterials.LEAD,
            CMaterials.HAFNIUM,
            CMaterials.CHROME,
            CMaterials.TITANIUM,
            CMaterials.STRONTIUM,
            CMaterials.BARIUM,
            CMaterials.AZ91D_ALLOY,
            CMaterials.ZK60A_ALLOY,
            CMaterials.RUBIDIUM,
            CMaterials.CAESIUM,
            CMaterials.FRANCIUM,
            CMaterials.RADIUM,
            CMaterials.ACTINIUM,
            CMaterials.THORIUM,
            CMaterials.PROTACTINIUM,
            CMaterials.URANIUM,
            CMaterials.NEPTUNIUM,
            CMaterials.PLUTONIUM,
            CMaterials.AMERICIUM,
            CMaterials.CURIUM,
            CMaterials.LANTHANUM,
            CMaterials.CERIUM,
            CMaterials.PRASEODYMIUM,
            CMaterials.NEODYMIUM,
            CMaterials.PROMETHIUM,
            CMaterials.SAMARIUM,
            CMaterials.EUROPIUM,
            CMaterials.VANADIUM,
            CMaterials.COBALT,
            CMaterials.PALLADIUM,
            CMaterials.PLATINUM,
            CMaterials.IRIDIUM,
            CMaterials.OSMIUM,
            CMaterials.RHENIUM,
            CMaterials.TANTALUM,
            CMaterials.TUNGSTEN,
            CMaterials.MOLYBDENUM,
            CMaterials.ANTIMONY,
            CMaterials.BISMUTH,
            CMaterials.BRASS,
            CMaterials.ELECTRUM,
            CMaterials.INVAR,
            CMaterials.STEEL,
            CMaterials.COPPER,
            CMaterials.TIN,
            CMaterials.SILVER,
            CMaterials.BRONZE
    };

    private static final CMaterial[] CHEST_MATERIALS = {
            CMaterials.SILICON, CMaterials.ALUMINIUM, CMaterials.CLAY_STEEL, CMaterials.CLAYIUM,
            CMaterials.ULTIMATE_ALLOY, CMaterials.MAGNESIUM, CMaterials.SODIUM, CMaterials.LITHIUM,
            CMaterials.ZIRCONIUM, CMaterials.ZINC, CMaterials.MANGANESE, CMaterials.CALCIUM,
            CMaterials.POTASSIUM, CMaterials.NICKEL, CMaterials.BERYLLIUM, CMaterials.LEAD,
            CMaterials.HAFNIUM, CMaterials.CHROME, CMaterials.TITANIUM, CMaterials.STRONTIUM,
            CMaterials.BARIUM, CMaterials.AZ91D_ALLOY, CMaterials.ZK60A_ALLOY, CMaterials.RUBIDIUM,
            CMaterials.CAESIUM, CMaterials.FRANCIUM, CMaterials.RADIUM, CMaterials.ACTINIUM,
            CMaterials.THORIUM, CMaterials.PROTACTINIUM, CMaterials.URANIUM, CMaterials.NEPTUNIUM,
            CMaterials.PLUTONIUM, CMaterials.AMERICIUM, CMaterials.CURIUM, CMaterials.LANTHANUM,
            CMaterials.CERIUM, CMaterials.PRASEODYMIUM, CMaterials.NEODYMIUM, CMaterials.PROMETHIUM,
            CMaterials.SAMARIUM, CMaterials.EUROPIUM, CMaterials.VANADIUM, CMaterials.COBALT,
            CMaterials.PALLADIUM, CMaterials.PLATINUM, CMaterials.IRIDIUM, CMaterials.OSMIUM,
            CMaterials.RHENIUM, CMaterials.TANTALUM, CMaterials.TUNGSTEN, CMaterials.MOLYBDENUM,
            CMaterials.ANTIMONY, CMaterials.BISMUTH, CMaterials.BRASS, CMaterials.ELECTRUM,
            CMaterials.INVAR
    };

    private static final CMaterial[] SMELTING_DUST_MATERIALS = {
            CMaterials.IMPURE_SILICON,
            CMaterials.SILICON,
            CMaterials.SILICONE,
            CMaterials.IRON,
            CMaterials.GOLD,
            CMaterials.LEAD,
            CMaterials.COPPER,
            CMaterials.BRONZE,
            CMaterials.BRASS,
            CMaterials.ELECTRUM,
            CMaterials.INVAR
    };

    private VanillaCraftingAndSmeltingGenerator() {}

    public static void build(ClayiumRecipeHelper h) {
        RecipeOutput out = h.output();
        buildMainMaterialsAndParts(h, out);
        buildToolsAndFilters(h, out);
        buildHullsAndCircuits(h, out);
        buildMachinesChestsAndPan(h, out);
        buildStorageBlocksAndDyes(h, out);
        buildSmelting(h, out);
        buildSpecialRecipes(h);
    }

    private static void buildMainMaterialsAndParts(ClayiumRecipeHelper h, RecipeOutput out) {
        // Compressed Clay 0 <-> Clay Block, Compressed Clay 1 <-> Compressed Clay 0
        shaped3x3(h, out, "crafting", item(COMPRESSED_CLAY_PATHS[0]), Blocks.CLAY, "dense_clay_from_clay");
        shapeless(h, out, "crafting", Blocks.CLAY, 9, item(COMPRESSED_CLAY_PATHS[0]), "clay_from_dense_clay");
        shaped3x3(h, out, "crafting", item(COMPRESSED_CLAY_PATHS[1]), item(COMPRESSED_CLAY_PATHS[0]), "compressed_clay_from_dense_clay");
        shapeless(h, out, "crafting", item(COMPRESSED_CLAY_PATHS[0]), 9, item(COMPRESSED_CLAY_PATHS[1]), "dense_clay_from_compressed_clay");

        // Compressed pure antimatter unpacking 1..8 -> 9x lower
        for (int i = 0; i < 8; i++) {
            shapeless(h, out, "crafting",
                    matItem(COMPRESSED_PURE_ANTIMATTERS[i], CShape.GEM), 9,
                    matItem(COMPRESSED_PURE_ANTIMATTERS[i + 1], CShape.GEM),
                    "unpack_" + COMPRESSED_PURE_ANTIMATTERS[i].idPrefix());
        }

        // Clay parts hand shaping
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, matItem(CMaterials.CLAY, CShape.LARGE_BALL))
                .requires(matItem(CMaterials.CLAY, CShape.BALL), 8)
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "clay_large_ball"));

        shapeless(h, out, "crafting", matItem(CMaterials.CLAY, CShape.SHORT_STICK), 2, matItem(CMaterials.CLAY, CShape.STICK), "clay_short_stick_from_stick");
        shapeless(h, out, "crafting", matItem(CMaterials.CLAY, CShape.SMALL_RING), 1, matItem(CMaterials.CLAY, CShape.SHORT_STICK), "clay_small_ring_from_short_stick");
        shapeless(h, out, "crafting", matItem(CMaterials.CLAY, CShape.SHORT_STICK), 1, matItem(CMaterials.CLAY, CShape.SMALL_RING), "clay_short_stick_from_small_ring");
        shapeless(h, out, "crafting", matItem(CMaterials.CLAY, CShape.RING), 1, matItem(CMaterials.CLAY, CShape.CYLINDER), "clay_ring_from_cylinder");
        shapeless(h, out, "crafting", matItem(CMaterials.CLAY, CShape.PIPE), 1, matItem(CMaterials.CLAY, CShape.PLATE), "clay_pipe_from_plate");
        shaped3x3(h, out, "crafting", matItem(CMaterials.CLAY, CShape.LARGE_PLATE), matItem(CMaterials.CLAY, CShape.PLATE), "clay_large_plate");

        // Clay / Dense Clay complex parts (Gear, CuttingHead, Bearing, Spindle, GrindingHead, WaterWheel)
        for (CMaterial mat : new CMaterial[]{CMaterials.CLAY, CMaterials.DENSE_CLAY}) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, matItem(mat, CShape.GEAR))
                    .pattern("iii").pattern("ioi").pattern("iii")
                    .define('i', matItem(mat, CShape.SHORT_STICK))
                    .define('o', matItem(mat, CShape.SMALL_RING))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", mat.idPrefix() + "_gear"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, matItem(mat, CShape.CUTTING_HEAD))
                    .pattern("iii").pattern("ioi").pattern("iii")
                    .define('i', matItem(mat, CShape.BLADE))
                    .define('o', matItem(mat, CShape.RING))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", mat.idPrefix() + "_cutting_head"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, matItem(mat, CShape.BEARING))
                    .pattern("iii").pattern("ioi").pattern("iii")
                    .define('i', matItem(mat, CShape.BALL))
                    .define('o', matItem(mat, CShape.RING))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", mat.idPrefix() + "_bearing"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, matItem(mat, CShape.SPINDLE))
                    .pattern("0#0").pattern("ioO").pattern("0#0")
                    .define('i', matItem(mat, CShape.STICK))
                    .define('o', matItem(mat, CShape.BEARING))
                    .define('O', matItem(mat, CShape.RING))
                    .define('#', matItem(mat, CShape.PLATE))
                    .define('0', matItem(mat, CShape.SMALL_RING))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", mat.idPrefix() + "_spindle"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, matItem(mat, CShape.GRINDING_HEAD))
                    .pattern("iii").pattern("ioi").pattern("iii")
                    .define('i', matItem(mat, CShape.NEEDLE))
                    .define('o', matItem(mat, CShape.RING))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", mat.idPrefix() + "_grinding_head"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, matItem(mat, CShape.WATER_WHEEL))
                    .pattern("###").pattern("#o#").pattern("###")
                    .define('#', matItem(mat, CShape.PLATE))
                    .define('o', matItem(mat, CShape.RING))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", mat.idPrefix() + "_wheel"));
        }

        // Clay part recycling to clay balls
        shapeless(h, out, "crafting", Items.CLAY_BALL, 3, matItem(CMaterials.CLAY, CShape.RING), "clay_ball_from_ring");
        shapeless(h, out, "crafting", Items.CLAY_BALL, 3, matItem(CMaterials.CLAY, CShape.GEAR), "clay_ball_from_gear");
        shapeless(h, out, "crafting", Items.CLAY_BALL, 3, matItem(CMaterials.CLAY, CShape.BLADE), "clay_ball_from_blade");
        shapeless(h, out, "crafting", Items.CLAY_BALL, 5, matItem(CMaterials.CLAY, CShape.NEEDLE), "clay_ball_from_needle");
    }

    private static void buildToolsAndFilters(ClayiumRecipeHelper h, RecipeOutput out) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, item("raw_clay_rolling_pin"))
                .pattern("-=-")
                .define('-', matItem(CMaterials.CLAY, CShape.SHORT_STICK))
                .define('=', matItem(CMaterials.CLAY, CShape.CYLINDER))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "raw_clay_rolling_pin"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, item("raw_clay_spatula"))
                .pattern("-=")
                .define('-', matItem(CMaterials.CLAY, CShape.SHORT_STICK))
                .define('=', matItem(CMaterials.CLAY, CShape.BLADE))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "raw_clay_spatula"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, item("clay_shovel"))
                .pattern("#").pattern("|").pattern("|")
                .define('#', matItem(CMaterials.CLAY, CShape.PLATE))
                .define('|', matItem(CMaterials.CLAY, CShape.STICK))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "clay_shovel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, item("clay_pickaxe"))
                .pattern("###").pattern(" | ").pattern(" | ")
                .define('#', matItem(CMaterials.DENSE_CLAY, CShape.PLATE))
                .define('|', matItem(CMaterials.DENSE_CLAY, CShape.STICK))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "clay_pickaxe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, item("clay_wrench"))
                .pattern("# #").pattern(" o ").pattern(" | ")
                .define('#', matItem(CMaterials.DENSE_CLAY, CShape.BLADE))
                .define('|', matItem(CMaterials.DENSE_CLAY, CShape.STICK))
                .define('o', matItem(CMaterials.DENSE_CLAY, CShape.SPINDLE))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "clay_wrench"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, item("clay_steel_pickaxe"))
                .pattern("###").pattern(" | ").pattern(" | ")
                .define('#', matTag(CMaterials.CLAY_STEEL, CShape.INGOT))
                .define('|', matItem(CMaterials.DENSE_CLAY, CShape.STICK))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "clay_steel_pickaxe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, item("clay_steel_shovel"))
                .pattern("#").pattern("|").pattern("|")
                .define('#', matTag(CMaterials.CLAY_STEEL, CShape.INGOT))
                .define('|', matItem(CMaterials.DENSE_CLAY, CShape.STICK))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "clay_steel_shovel"));

        // 12 filter conversions (CRecipes L401-414)
        shapeless(h, out, "crafting", item("whitelist_item_filter"), 1, item("blacklist_item_filter"), "whitelist_filter_from_blacklist");
        shapeless(h, out, "crafting", item("blacklist_item_filter"), 1, item("whitelist_item_filter"), "blacklist_filter_from_whitelist");
        shapeless(h, out, "crafting", item("translation_key_item_filter"), 1, item("display_name_item_filter"), "translation_filter_from_display");
        shapeless(h, out, "crafting", item("tag_item_filter"), 1, item("translation_key_item_filter"), "tag_filter_from_translation");
        shapeless(h, out, "crafting", item("registry_id_item_filter"), 1, item("tag_item_filter"), "registry_filter_from_tag");
        shapeless(h, out, "crafting", item("mod_id_item_filter"), 1, item("registry_id_item_filter"), "mod_id_filter_from_registry");
        shapeless(h, out, "crafting", item("damage_value_item_filter"), 1, item("mod_id_item_filter"), "damage_filter_from_mod_id");
        shapeless(h, out, "crafting", item("display_name_item_filter"), 1, item("damage_value_item_filter"), "display_filter_from_damage");
        shapeless(h, out, "crafting", item("fuzzy_item_filter"), 1, item("fuzzy_item_filter"), "fuzzy_filter_reset");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("item_filter_duplicator"))
                .requires(item("whitelist_item_filter"))
                .requires(item("display_name_item_filter"))
                .requires(item("fuzzy_item_filter"))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "item_filter_duplicator"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("block_state_item_filter"))
                .requires(item("damage_value_item_filter"))
                .requires(Blocks.CLAY)
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "block_state_filter_from_damage"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("damage_value_item_filter"))
                .requires(item("block_state_item_filter"))
                .requires(Blocks.CLAY)
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "damage_filter_from_block_state"));

        // Fluid Capsule 1 mB
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("fluid_capsule_1"))
                .pattern(" # ").pattern("# #").pattern(" # ")
                .define('#', blockItem(COMPRESSED_CLAY_PATHS[0]))
                .unlockedBy("has_clay", hasClay())
                .save(h.withConfig(ClayiumConfigCondition.ENABLE_FLUID_CAPSULE, true),
                        h.nextId("crafting", "fluid_capsule_1"));
    }

    private static void buildHullsAndCircuits(ClayiumRecipeHelper h, RecipeOutput out) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("raw_clay_machine_hull"))
                .pattern("###").pattern("#o#").pattern("###")
                .define('#', matItem(CMaterials.CLAY, CShape.LARGE_PLATE))
                .define('o', matItem(CMaterials.CLAY, CShape.GEAR))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "raw_clay_machine_hull"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("dense_clay_machine_hull"))
                .pattern("###").pattern("#C#").pattern("###")
                .define('#', matItem(CMaterials.DENSE_CLAY, CShape.LARGE_PLATE))
                .define('C', item("clay_circuit"))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "dense_clay_machine_hull"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("simple_machine_hull"))
                .pattern("###").pattern("#C#").pattern("###")
                .define('#', matItem(CMaterials.IND_CLAY, CShape.LARGE_PLATE))
                .define('C', item("simple_circuit"))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "simple_machine_hull"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("basic_machine_hull"))
                .pattern("#E#").pattern("#C#").pattern("###")
                .define('#', matItem(CMaterials.ADVIND_CLAY, CShape.LARGE_PLATE))
                .define('C', item("basic_circuit"))
                .define('E', item("cee"))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "basic_machine_hull"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("advanced_machine_hull"))
                .pattern("#E#").pattern("*C*").pattern("#*#")
                .define('#', matItem(CMaterials.IMPURE_SILICON, CShape.LARGE_PLATE))
                .define('*', matItem(CMaterials.SILICONE, CShape.LARGE_PLATE))
                .define('C', item("advanced_circuit"))
                .define('E', item("cee"))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "advanced_machine_hull"));

        CMaterial[] upperHullMats = {
                CMaterials.ALUMINIUM,
                CMaterials.CLAY_STEEL,
                CMaterials.CLAYIUM,
                CMaterials.ULTIMATE_ALLOY,
                CMaterials.ANTIMATTER,
                CMaterials.PURE_ANTIMATTER,
                CMaterials.OCTUPLE_CLAY,
                CMaterials.OCTUPLE_PURE_ANTIMATTER
        };
        for (int idx = 0; idx < upperHullMats.length; idx++) {
            int tier = 6 + idx;
            String hullPath = ClayiumRecipeHelper.MACHINE_HULL_PATHS[tier];
            String circuitPath = ClayiumRecipeHelper.CIRCUIT_PATHS[tier];
            CMaterial mat = upperHullMats[idx];
            if (tier == 6) {
                // HardcoreAluminium=false -> Aluminium, HardcoreAluminium=true -> ImpureAluminium
                RecipeOutput normalOut = h.withConfig(ClayiumConfigCondition.HARDCORE_ALUMINIUM, false);
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(hullPath))
                        .pattern("#E#").pattern("#C#").pattern("###")
                        .define('#', matItem(CMaterials.ALUMINIUM, CShape.LARGE_PLATE))
                        .define('C', item(circuitPath))
                        .define('E', item("cee"))
                        .unlockedBy("has_clay", hasClay())
                        .save(normalOut, h.nextId("crafting", hullPath));

                RecipeOutput hcOut = h.withConfig(ClayiumConfigCondition.HARDCORE_ALUMINIUM, true);
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(hullPath))
                        .pattern("#E#").pattern("#C#").pattern("###")
                        .define('#', matItem(CMaterials.IMPURE_ALUMINIUM, CShape.LARGE_PLATE))
                        .define('C', item(circuitPath))
                        .define('E', item("cee"))
                        .unlockedBy("has_clay", hasClay())
                        .save(hcOut, h.nextId("crafting", hullPath + "_hardcore"));
            } else {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(hullPath))
                        .pattern("#E#").pattern("#C#").pattern("###")
                        .define('#', matItem(mat, CShape.LARGE_PLATE))
                        .define('C', item(circuitPath))
                        .define('E', item("cee"))
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", hullPath));
            }
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("zk60a_alloy_hull"))
                .pattern("###").pattern("#C#").pattern("###")
                .define('#', matItem(CMaterials.ZK60A_ALLOY, CShape.LARGE_PLATE))
                .define('C', item("precision_circuit"))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "zk60a_alloy_hull"));

        CMaterial[] reactorHullMats = {
                CMaterials.RUBIDIUM, CMaterials.CERIUM, CMaterials.TANTALUM, CMaterials.PRASEODYMIUM,
                CMaterials.PROTACTINIUM, CMaterials.NEPTUNIUM, CMaterials.PROMETHIUM, CMaterials.SAMARIUM,
                CMaterials.CURIUM, CMaterials.EUROPIUM
        };
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("ca_reactor_hull_rank_1"))
                .pattern("#I#").pattern("#H#").pattern("###")
                .define('#', matItem(CMaterials.ANTIMATTER, CShape.GEM))
                .define('H', blockItem(ClayiumRecipeHelper.MACHINE_HULL_PATHS[10]))
                .define('I', matTag(reactorHullMats[0], CShape.INGOT))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "ca_reactor_hull_rank_1"));

        for (int i = 0; i <= 8; i++) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("ca_reactor_hull_rank_" + (i + 2)))
                    .pattern("#I#").pattern("#H#").pattern("###")
                    .define('#', matItem(COMPRESSED_PURE_ANTIMATTERS[i], CShape.GEM))
                    .define('H', blockItem("ca_reactor_hull_rank_" + (i + 1)))
                    .define('I', matTag(reactorHullMats[i + 1], CShape.INGOT))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", "ca_reactor_hull_rank_" + (i + 2)));
        }

        // Circuits L1380-1399
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("clay_circuit"))
                .pattern("-*-").pattern("o#o").pattern("-*-")
                .define('-', matItem(CMaterials.DENSE_CLAY, CShape.STICK))
                .define('*', matItem(CMaterials.DENSE_CLAY, CShape.GEAR))
                .define('o', matItem(CMaterials.DENSE_CLAY, CShape.RING))
                .define('#', item("clay_circuit_board"))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "clay_circuit"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("simple_circuit"))
                .pattern("---").pattern("-#-").pattern("---")
                .define('-', matItem(CMaterials.ENG_CLAY, CShape.DUST))
                .define('#', item("clay_circuit_board"))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "simple_circuit"));
    }

    private static void buildMachinesChestsAndPan(ClayiumRecipeHelper h, RecipeOutput out) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("clay_work_table"))
                .pattern("##").pattern("##")
                .define('#', matItem(CMaterials.DENSE_CLAY, CShape.BLOCK))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "clay_work_table"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("clay_crafting_table"))
                .pattern("###")
                .define('#', matItem(CMaterials.DENSE_CLAY, CShape.BLOCK))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "clay_crafting_table"));

        shapeless(h, out, "crafting", matItem(CMaterials.DENSE_CLAY, CShape.BLOCK), 3,
                blockItem("clay_crafting_table"), "dense_clay_from_clay_crafting_table");

        String[] tierNames = {"", "clay", "dense_clay", "simple", "basic"};
        CMaterial[] cmats = {CMaterials.CLAY, CMaterials.CLAY, CMaterials.DENSE_CLAY, CMaterials.DENSE_CLAY, CMaterials.DENSE_CLAY};

        for (int i = 1; i <= 4; i++) {
            String p = tierNames[i];
            Item hull = blockItem(ClayiumRecipeHelper.MACHINE_HULL_PATHS[i]);
            CMaterial cmat = cmats[i];
            Item ckt = item(ClayiumRecipeHelper.CIRCUIT_PATHS[i]);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_bending_machine"))
                    .pattern("o-*").pattern("P#P").pattern("o-*")
                    .define('#', hull)
                    .define('o', matItem(cmat, CShape.SPINDLE))
                    .define('-', matItem(cmat, CShape.CYLINDER))
                    .define('*', matItem(cmat, CShape.GEAR))
                    .define('P', matItem(cmat, CShape.PLATE))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", p + "_bending_machine"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_wire_drawing_machine"))
                    .pattern("*o*").pattern("=#=").pattern("*o*")
                    .define('#', hull)
                    .define('o', matItem(cmat, CShape.SPINDLE))
                    .define('*', matItem(cmat, CShape.GEAR))
                    .define('=', matItem(cmat, CShape.PIPE))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", p + "_wire_drawing_machine"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_pipe_drawing_machine"))
                    .pattern("*o*").pattern("-#=").pattern("*o*")
                    .define('#', hull)
                    .define('o', matItem(cmat, CShape.SPINDLE))
                    .define('-', matItem(cmat, CShape.CYLINDER))
                    .define('*', matItem(cmat, CShape.GEAR))
                    .define('=', matItem(cmat, CShape.PIPE))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", p + "_pipe_drawing_machine"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_cutting_machine"))
                    .pattern("P*P").pattern("o#|").pattern("P*P")
                    .define('#', hull)
                    .define('o', matItem(cmat, CShape.SPINDLE))
                    .define('*', matItem(cmat, CShape.GEAR))
                    .define('P', matItem(cmat, CShape.PLATE))
                    .define('|', matItem(cmat, CShape.CUTTING_HEAD))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", p + "_cutting_machine"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_lathe"))
                    .pattern("P*P").pattern("-#o").pattern("P*P")
                    .define('#', hull)
                    .define('o', matItem(cmat, CShape.SPINDLE))
                    .define('*', matItem(cmat, CShape.GEAR))
                    .define('P', matItem(cmat, CShape.PLATE))
                    .define('-', matItem(cmat, CShape.STICK))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", p + "_lathe"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_cobblestone_generator"))
                    .pattern(" * ").pattern("=#=").pattern(" * ")
                    .define('#', hull)
                    .define('*', matItem(cmat, CShape.GEAR))
                    .define('=', matItem(cmat, CShape.PIPE))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", p + "_cobblestone_generator"));

            if (i == 1) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("elemental_milling_machine"))
                        .pattern("P0P").pattern("o#o").pattern("P*P")
                        .define('#', hull)
                        .define('o', matItem(CMaterials.DENSE_CLAY, CShape.SPINDLE))
                        .define('*', matItem(CMaterials.DENSE_CLAY, CShape.GEAR))
                        .define('P', matItem(CMaterials.DENSE_CLAY, CShape.PLATE))
                        .define('0', matItem(CMaterials.DENSE_CLAY, CShape.CUTTING_HEAD))
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", "elemental_milling_machine"));

                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, blockItem("clay_water_wheel"))
                        .requires(hull)
                        .requires(matItem(CMaterials.CLAY, CShape.WATER_WHEEL))
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", "clay_water_wheel"));
            }

            if (i >= 2) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_grinder"))
                        .pattern("P0P").pattern("o#o").pattern("P*P")
                        .define('#', hull)
                        .define('o', matItem(cmat, CShape.SPINDLE))
                        .define('*', matItem(cmat, CShape.GEAR))
                        .define('P', matItem(cmat, CShape.PLATE))
                        .define('0', matItem(cmat, CShape.GRINDING_HEAD))
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", p + "_grinder"));

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_decomposer"))
                        .pattern("*o*").pattern("C#C").pattern("*=*")
                        .define('#', hull)
                        .define('o', matItem(cmat, CShape.SPINDLE))
                        .define('*', matItem(cmat, CShape.GEAR))
                        .define('=', matItem(cmat, CShape.PIPE))
                        .define('C', ckt)
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", p + "_decomposer"));

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_condenser"))
                        .pattern("*P*").pattern("P#P").pattern("*P*")
                        .define('#', hull)
                        .define('*', matItem(cmat, CShape.GEAR))
                        .define('P', matItem(cmat, CShape.PLATE))
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", p + "_condenser"));
            }

            if (i == 2) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, blockItem("dense_clay_water_wheel"))
                        .requires(hull)
                        .requires(matItem(CMaterials.DENSE_CLAY, CShape.WATER_WHEEL))
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", "dense_clay_water_wheel"));
            }

            if (i >= 3) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_centrifuge"))
                        .pattern("*o*").pattern("o#o").pattern("*o*")
                        .define('#', hull)
                        .define('o', matItem(cmat, CShape.SPINDLE))
                        .define('*', matItem(cmat, CShape.GEAR))
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", p + "_centrifuge"));

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_inscriber"))
                        .pattern("*o*").pattern("C#C").pattern("*C*")
                        .define('#', hull)
                        .define('o', matItem(cmat, CShape.SPINDLE))
                        .define('*', matItem(cmat, CShape.GEAR))
                        .define('C', ckt)
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", p + "_inscriber"));

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_assembler"))
                        .pattern("*C*").pattern("o#o").pattern("*C*")
                        .define('#', hull)
                        .define('o', matItem(cmat, CShape.SPINDLE))
                        .define('*', matItem(cmat, CShape.GEAR))
                        .define('C', ckt)
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", p + "_assembler"));

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(p + "_milling_machine"))
                        .pattern("P0P").pattern("o#o").pattern("P*P")
                        .define('#', hull)
                        .define('o', matItem(cmat, CShape.SPINDLE))
                        .define('*', matItem(cmat, CShape.GEAR))
                        .define('P', matItem(cmat, CShape.PLATE))
                        .define('0', matItem(cmat, CShape.CUTTING_HEAD))
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", p + "_milling_machine"));
            }

            if (i == 3) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("energetic_clay_condenser"))
                        .pattern("P*P").pattern("E#E").pattern("PCP")
                        .define('#', hull)
                        .define('P', matItem(cmat, CShape.PLATE))
                        .define('*', matItem(cmat, CShape.GEAR))
                        .define('C', item(ClayiumRecipeHelper.CIRCUIT_PATHS[4]))
                        .define('E', item("cee"))
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", "energetic_clay_condenser"));
            }
            if (i == 4) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("energetic_clay_condenser_mk2"))
                        .pattern("P*P").pattern("E#E").pattern("PCP")
                        .define('#', hull)
                        .define('P', matItem(cmat, CShape.PLATE))
                        .define('*', matItem(cmat, CShape.GEAR))
                        .define('C', item(ClayiumRecipeHelper.CIRCUIT_PATHS[5]))
                        .define('E', item("cee"))
                        .unlockedBy("has_clay", hasClay())
                        .save(out, h.nextId("crafting", "energetic_clay_condenser_mk2"));
            }
        }

        // Clay Chunk Loader
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem("clay_chunk_loader"))
                .pattern("PCP").pattern("C#C").pattern("PCP")
                .define('#', blockItem("az91d_alloy_hull"))
                .define('C', item(ClayiumRecipeHelper.CIRCUIT_PATHS[6]))
                .define('P', item(ClayiumRecipeHelper.CIRCUIT_PATHS[5]))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "clay_chunk_loader"));

        // PAN Duplicators Tier 5..13
        String[] panDuplicatorPaths = {
                "basic_pan_duplicator",
                "advanced_pan_duplicator",
                "precision_pan_duplicator",
                "clay_steel_pan_duplicator",
                "clayium_pan_duplicator",
                "ultimate_pan_duplicator",
                "antimatter_pan_duplicator",
                "pure_antimatter_pan_duplicator",
                "oec_pan_duplicator",
                "opa_pan_duplicator"
        };
        CMaterial[] panDupMats = {
                CMaterials.RUBIDIUM, CMaterials.LANTHANUM, CMaterials.CAESIUM,
                CMaterials.FRANCIUM, CMaterials.RADIUM, CMaterials.TANTALUM,
                CMaterials.BISMUTH, CMaterials.ACTINIUM, CMaterials.VANADIUM
        };
        for (int i = 0; i <= 8; i++) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blockItem(panDuplicatorPaths[i + 1]))
                    .pattern("#I#").pattern("DMD").pattern("#I#")
                    .define('#', matItem(COMPRESSED_PURE_ANTIMATTERS[i], CShape.GEM))
                    .define('D', blockItem(panDuplicatorPaths[i]))
                    .define('I', matTag(panDupMats[i], CShape.INGOT))
                    .define('M', blockItem(ClayiumRecipeHelper.MACHINE_HULL_PATHS[i + 5]))
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", panDuplicatorPaths[i + 1]));
        }

        // Metal Chests x 57 (2 recipes each: vanilla chest core & any metal_chest core)
        TagKey<Item> metalChestsTag = ItemTags.create(ClayiumMod.id("metal_chests"));
        for (CMaterial mat : CHEST_MATERIALS) {
            String chestPath = mat.idPrefix() + "_chest";
            if (mat == CMaterials.OSMIUM) {
                RecipeOutput normalOut = h.withConfig(ClayiumConfigCondition.HARDCORE_OSMIUM, false);
                emitMetalChestRecipes(h, normalOut, chestPath, matTag(CMaterials.OSMIUM, CShape.INGOT), metalChestsTag, "");

                RecipeOutput hcOut = h.withConfig(ClayiumConfigCondition.HARDCORE_OSMIUM, true);
                emitMetalChestRecipes(h, hcOut, chestPath, matTag(CMaterials.IMPURE_OSMIUM, CShape.INGOT), metalChestsTag, "_hardcore");
            } else {
                emitMetalChestRecipes(h, out, chestPath, matTag(mat, CShape.INGOT), metalChestsTag, "");
            }
        }
    }

    private static void emitMetalChestRecipes(
            ClayiumRecipeHelper h,
            RecipeOutput out,
            String chestPath,
            TagKey<Item> ingotTag,
            TagKey<Item> metalChestsTag,
            String suffix) {
        Item chest = blockItem(chestPath);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, chest)
                .pattern("###").pattern("#*#").pattern("###")
                .define('#', ingotTag)
                .define('*', Items.CHEST)
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", chestPath + "_from_chest" + suffix));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, chest)
                .pattern("###").pattern("#*#").pattern("###")
                .define('#', ingotTag)
                .define('*', metalChestsTag)
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", chestPath + "_upgrade" + suffix));
    }

    private static void buildStorageBlocksAndDyes(ClayiumRecipeHelper h, RecipeOutput out) {
        for (CMaterial mat : COMPACT_INGOT_MATERIALS) {
            emitStorageCompactPair(h, out, mat, CShape.INGOT);
        }
        for (CMaterial mat : COMPACT_GEM_MATERIALS) {
            emitStorageCompactPair(h, out, mat, CShape.GEM);
        }
        for (CMaterial mat : METAL_BLOCK_MATERIALS) {
            emitStorageCompactPair(h, out, mat, CShape.INGOT);
        }

        // OctupleEnergeticClay block <-> duodecuple (meta 12 = octuple_compressed_energetic_clay)
        Item octupleBlock = blockItem("octuple_energetic_clay_block");
        Item octupleClay = blockItem(COMPRESSED_CLAY_PATHS[12]);
        shaped3x3(h, out, "crafting", octupleBlock, octupleClay, "octuple_energetic_clay_block");
        shapeless(h, out, "crafting", octupleClay, 9, octupleBlock, "octuple_compressed_energetic_clay_from_block");

        // 16 colored silicone blocks
        TagKey<Item> siliconeBlockTag = commonTag("storage_blocks/silicone");
        for (DyeColor color : DyeColor.values()) {
            String blockPath = color.getName() + "_silicone_block";
            TagKey<Item> dyeTag = commonTag("dyes/" + color.getName());
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, blockItem(blockPath), 8)
                    .pattern("###").pattern("#*#").pattern("###")
                    .define('#', siliconeBlockTag)
                    .define('*', dyeTag)
                    .unlockedBy("has_clay", hasClay())
                    .save(out, h.nextId("crafting", blockPath));
        }

        // Gunpowder L3580: 2x saltpeter + sulfur + charcoal -> 2x gunpowder
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.GUNPOWDER, 2)
                .requires(Ingredient.of(matTag(CMaterials.SALTPETER, CShape.DUST)), 2)
                .requires(matTag(CMaterials.SULFUR, CShape.DUST))
                .requires(matTag(CMaterials.CHARCOAL, CShape.DUST))
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", "gunpowder"));
    }

    private static void emitStorageCompactPair(ClayiumRecipeHelper h, RecipeOutput out, CMaterial mat, CShape itemShape) {
        Item block = matItem(mat, CShape.BLOCK);
        Item unit = matItem(mat, itemShape);
        TagKey<Item> unitTag = matTag(mat, itemShape);
        TagKey<Item> blockTag = matTag(mat, CShape.BLOCK);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block)
                .requires(Ingredient.of(unitTag), 9)
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", mat.idPrefix() + "_block_from_" + itemShape.idSuffix()));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, unit, 9)
                .requires(blockTag)
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("crafting", mat.idPrefix() + "_" + itemShape.idSuffix() + "_from_block"));
    }

    private static void buildSmelting(ClayiumRecipeHelper h, RecipeOutput out) {
        smelt(h, out, item("raw_clay_rolling_pin"), item("clay_rolling_pin"), 0.1F, "clay_rolling_pin");
        smelt(h, out, item("raw_clay_slicer"), item("clay_slicer"), 0.1F, "clay_slicer");
        smelt(h, out, item("raw_clay_spatula"), item("clay_spatula"), 0.1F, "clay_spatula");
        smelt(h, out, blockItem("raw_clay_machine_hull"), blockItem("clay_machine_hull"), 0.1F, "clay_machine_hull");
        smelt(h, out, matItem(CMaterials.IMPURE_SILICON, CShape.INGOT), matItem(CMaterials.SILICONE, CShape.INGOT), 0.0F, "silicone_ingot_from_impure_silicon");

        for (CMaterial mat : SMELTING_DUST_MATERIALS) {
            smelt(h, out, matItem(mat, CShape.DUST), matItem(mat, CShape.INGOT), 0.0F, mat.idPrefix() + "_ingot_from_dust");
        }
    }

    private static void buildSpecialRecipes(ClayiumRecipeHelper h) {
        SpecialRecipeBuilder.special(AutoEatGadgetSwitchRecipe::new)
                .save(h.output(), ClayiumMod.id("auto_eat_gadget_switch"));
        SpecialRecipeBuilder.special(StorageContainerUpgradeRecipe::new)
                .save(h.output(), ClayiumMod.id("storage_container_upgrade"));
        SpecialRecipeBuilder.special(FluidCapsuleTransferRecipe::new)
                .save(h.withConfig(ClayiumConfigCondition.ENABLE_FLUID_CAPSULE, true), ClayiumMod.id("fluid_capsule_transfer"));
        SpecialRecipeBuilder.special(FluidCapsuleContainerRecipe::new)
                .save(h.withConfig(ClayiumConfigCondition.ENABLE_FLUID_CAPSULE, true), ClayiumMod.id("fluid_capsule_container"));
    }

    private static void shaped3x3(ClayiumRecipeHelper h, RecipeOutput out, String cat, ItemLike result, ItemLike input, String slug) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
                .pattern("###").pattern("###").pattern("###")
                .define('#', input)
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId(cat, slug));
    }

    private static void shapeless(ClayiumRecipeHelper h, RecipeOutput out, String cat, ItemLike result, int count, ItemLike input, String slug) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result, count)
                .requires(input)
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId(cat, slug));
    }

    private static void smelt(ClayiumRecipeHelper h, RecipeOutput out, ItemLike input, ItemLike output, float exp, String slug) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, output, exp, 200)
                .unlockedBy("has_clay", hasClay())
                .save(out, h.nextId("smelting", slug));
    }
}
