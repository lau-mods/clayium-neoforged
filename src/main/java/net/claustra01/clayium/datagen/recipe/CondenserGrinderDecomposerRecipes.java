package net.claustra01.clayium.datagen.recipe;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.CompressionTier;
import net.minecraft.world.level.ItemLike;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.ConfigEnabledCondition;
import net.claustra01.clayium.recipe.ConfigEnabledCondition.Option;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public final class CondenserGrinderDecomposerRecipes {
    public static void buildRecipes(RecipeOutput output) {
        condenser(output);
        grinder(output);
        decomposer(output);
        ItemLike previous = Items.CLAY;
        energeticDecomposition(output, "clay_block", Items.CLAY, Items.CLAY_BALL, 4);
        for (CompressionTier tier : CompressionTier.values()) {
            ItemLike block = compression(tier.id());
            energeticDecomposition(output, tier.id(), block, previous, 9);
            previous = block;
        }
    }

    private static void energeticDecomposition(RecipeOutput output, String id, ItemLike input, ItemLike result, int count) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.EC_DECOMPOSER, 13)
                .requires(input, 1).output(result, count).clayEnergy(1L).processingTicks(1)
                .save(output, ClayiumMod.id("ec_decomposer/" + id));
    }

    private static void condenser(RecipeOutput output) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CONDENSER, 0)
                .requires(Items.CLAY, 9).output(compression("dense_clay"), 1)
                .clayEnergy(1L).processingTicks(4)
                .save(output, ClayiumMod.id("condenser/clay_to_dense_clay"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CONDENSER, 0)
                .requires(compression("dense_clay"), 9).output(compression("compressed_clay"), 1)
                .clayEnergy(1L).processingTicks(4)
                .save(output, ClayiumMod.id("condenser/dense_to_compressed_clay"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CONDENSER, 0)
                .requires(compression("industrial_clay"), 9)
                .output(compression("advanced_industrial_clay"), 1)
                .clayEnergy(100L).processingTicks(4)
                .save(output, ClayiumMod.id("condenser/industrial_to_advanced_industrial_clay"));
        shardRecipe(ClayiumRecipeMethod.CONDENSER, 3)
                .requires(shard("compressed_clay_shard"), 4)
                .output(compression("compressed_clay"), 1)
                .save(output, ClayiumMod.id("condenser/compressed_clay_shard"));
        shardRecipe(ClayiumRecipeMethod.CONDENSER, 6)
                .requires(shard("industrial_clay_shard"), 4)
                .output(compression("industrial_clay"), 1)
                .save(output, ClayiumMod.id("condenser/industrial_clay_shard"));
        shardRecipe(ClayiumRecipeMethod.CONDENSER, 9)
                .requires(shard("advanced_industrial_clay_shard"), 4)
                .output(compression("advanced_industrial_clay"), 1)
                .save(output, ClayiumMod.id("condenser/advanced_industrial_clay_shard"));
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
        for (int count : new int[]{1, 4, 16, 64}) {
            ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.GRINDER, 0)
                    .requires(Items.COBBLESTONE, count)
                    .output(Items.GRAVEL, count)
                    .clayEnergy(1L).processingTicks(10)
                    .save(output, ClayiumMod.id("grinder/cobblestone_to_gravel_" + count));
        }
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.GRINDER, 0)
                .requires(Items.GRAVEL, 1)
                .output(Items.SAND, 1)
                .clayEnergy(10L).processingTicks(10)
                .save(output, ClayiumMod.id("grinder/gravel_to_sand"));
        int[] counts = {1, 4, 16, 64};
        int[] tiers = {4, 4, 5, 6};
        long[] energies = {1L, 2L, 4L, 8L};
        long[] ticks = {2L, 2L, 1L, 1L};
        for (int index = 0; index < counts.length; index++) {
            ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CENTRIFUGE, tiers[index])
                    .requires(Items.GRAVEL, counts[index])
                    .output(compression("dense_clay"), counts[index])
                    .clayEnergy(energies[index]).processingTicks(ticks[index])
                    .save(output, ClayiumMod.id("centrifuge/gravel_to_dense_clay_" + counts[index]));
        }
        oreShard(output, WorldgenRegistration.CLAY_ORE.get(), "compressed_clay_shard", 2, 3);
        oreShard(output, WorldgenRegistration.DEEPSLATE_CLAY_ORE.get(), "compressed_clay_shard", 2, 3);
        oreShard(output, WorldgenRegistration.DENSE_CLAY_ORE.get(), "industrial_clay_shard", 3, 6);
        oreShard(output, WorldgenRegistration.DEEPSLATE_DENSE_CLAY_ORE.get(), "industrial_clay_shard", 3, 6);
        oreShard(output, WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get(), "advanced_industrial_clay_shard", 5, 9);
        oreShard(output, WorldgenRegistration.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get(), "advanced_industrial_clay_shard", 5, 9);
        machine(ClayiumRecipeMethod.GRINDER, 2, 20, 1.0D)
                .requires(Items.CLAY, 1)
                .output(material("clay_dust"), 4)
                .save(output, ClayiumMod.id("grinder/clay"));
        originalClayPartGrinding(output);
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.GRINDER, 0)
                .requires(compression("industrial_clay"), 1)
                .output(material("industrial_clay_dust"), 1)
                .clayEnergy(1L).processingTicks(12)
                .save(output, ClayiumMod.id("grinder/industrial_clay_block"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.GRINDER, 0)
                .requires(compression("advanced_industrial_clay"), 1)
                .output(material("advanced_industrial_clay_dust"), 1)
                .clayEnergy(2L).processingTicks(12)
                .save(output, ClayiumMod.id("grinder/advanced_industrial_clay_block"));
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
        hardcoreAluminiumGrinder(output, "impure_aluminium_ingot", "impure_aluminium_dust", 1);
        hardcoreAluminiumGrinder(output, "impure_aluminium_plate", "impure_aluminium_dust", 1);
        hardcoreAluminiumGrinder(output, "large_impure_aluminium_plate", "impure_aluminium_dust", 4);
        machine(ClayiumRecipeMethod.GRINDER, 6, 40, 1.0D)
                .requires(material("clay_steel_ingot"), 1)
                .output(material("clay_steel_dust"), 1)
                .save(output, ClayiumMod.id("grinder/clay_steel_ingot"));
        machine(ClayiumRecipeMethod.GRINDER, 10, 40, 1.0D)
                .requires(material("antimatter_block"), 1)
                .output(material("antimatter_dust"), 9)
                .save(output, ClayiumMod.id("grinder/antimatter_block"));
    }

    private static void oreShard(RecipeOutput output, Block ore, String shard, int count, int ticks) {
        shardRecipe(ClayiumRecipeMethod.GRINDER, ticks)
                .requires(ore, 1)
                .output(shard(shard), count)
                .save(output, ClayiumMod.id("grinder/" + BuiltInRegistries.BLOCK.getKey(ore).getPath() + "_to_" + shard));
    }

    private static ClayiumRecipeBuilder shardRecipe(ClayiumRecipeMethod method, int ticks) {
        return ClayiumRecipeBuilder.machine(method, 0)
                .clayEnergy(1L)
                .processingTicks(ticks);
    }

    private static void decomposer(RecipeOutput output) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.DECOMPOSER, 0)
                .requires(material("industrial_clay_dust"), 1)
                .output(material("energized_clay_dust"), 3)
                .clayEnergy(1L).processingTicks(60)
                .save(output, ClayiumMod.id("decomposer/industrial_clay_dust"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.DECOMPOSER, 4)
                .requires(material("advanced_industrial_clay_dust"), 1)
                .output(material("energized_clay_dust"), 28)
                .clayEnergy(1_000L).processingTicks(60)
                .save(output, ClayiumMod.id("decomposer/advanced_industrial_clay_dust"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.DECOMPOSER, 0)
                .requires(Items.CLAY, 1)
                .output(Items.CLAY_BALL, 4)
                .clayEnergy(1L).processingTicks(3)
                .save(output, ClayiumMod.id("decomposer/clay_block"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.DECOMPOSER, 0)
                .requires(compression("compressed_clay"), 1)
                .output(compression("dense_clay"), 9)
                .clayEnergy(1L).processingTicks(3)
                .save(output, ClayiumMod.id("decomposer/compressed_clay"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.DECOMPOSER, 0)
                .requires(compression("industrial_clay"), 1)
                .output(compression("compressed_clay"), 9)
                .clayEnergy(1L).processingTicks(10)
                .save(output, ClayiumMod.id("decomposer/industrial_clay"));
    }

    private static void originalClayPartGrinding(RecipeOutput output) {
        String[] shapes = {"plate", "stick", "short_stick", "ring", "small_ring", "gear", "blade",
                "needle", "disc", "small_disc", "cylinder", "pipe", "large_plate", "grinding_head",
                "bearing", "spindle", "cutting_head"};
        int[] inputCounts = {1, 4, 8, 4, 8, 8, 1, 1, 2, 4, 1, 1, 1, 1, 4, 1, 1};
        int[] outputCounts = {1, 1, 1, 5, 1, 9, 1, 2, 3, 1, 2, 1, 4, 16, 5, 4, 9};
        int[] baseTicks = {3, 3, 3, 15, 3, 27, 3, 6, 9, 3, 6, 3, 12, 48, 15, 12, 27};
        for (String prefix : new String[] {"clay", "dense_clay"}) {
            int hardnessMultiplier = prefix.equals("clay") ? 1 : 4;
            for (int index = 0; index < shapes.length; index++) {
                String inputId = clayShapeId(prefix, shapes[index]);
                ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.GRINDER, 0)
                        .requires(material(inputId), inputCounts[index])
                        .output(material(prefix + "_dust"), outputCounts[index])
                        .clayEnergy(1L).processingTicks((long)baseTicks[index] * hardnessMultiplier)
                        .save(output, ClayiumMod.id("grinder/" + inputId + "_to_dust"));
            }
        }
    }

    private static String clayShapeId(String prefix, String shape) {
        return switch (shape) {
            case "short_stick" -> "short_" + prefix + "_stick";
            case "small_disc" -> "small_" + prefix + "_disc";
            case "small_ring" -> "small_" + prefix + "_ring";
            case "large_plate" -> "large_" + prefix + "_plate";
            default -> prefix + "_" + shape;
        };
    }

    private static ClayiumRecipeBuilder machine(
            ClayiumRecipeMethod method, int tier, int ticks, double energy) {
        return ClayiumRecipeBuilder.machine(method, tier)
                .clayEnergy(ClayiumRecipeEnergy.e(energy, tier))
                .processingTicks(ticks);
    }

    private static void hardcoreAluminiumGrinder(
            RecipeOutput output, String input, String dust, int outputCount) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.GRINDER, 0)
                .requires(material(input), 1)
                .output(material(dust), outputCount)
                .clayEnergy(ClayiumRecipeEnergy.e(0.25D, 4))
                .processingTicks(80)
                .when(new ConfigEnabledCondition(Option.HARDCORE_ALUMINIUM))
                .save(output, ClayiumMod.id("grinder/" + input));
    }

    private static net.minecraft.world.level.ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static net.minecraft.world.level.ItemLike compression(String id) {
        return MaterialRegistration.COMPRESSION_BLOCKS.get(id).get();
    }

    private static net.minecraft.world.level.ItemLike shard(String id) {
        return MaterialRegistration.SHARDS.get(id).get();
    }

    private CondenserGrinderDecomposerRecipes() {
    }
}
