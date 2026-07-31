/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.data;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.recipe.ClayWorkTableOperation;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.data.PackOutput;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ClayiumDataGenerators {
    private ClayiumDataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        event.getGenerator().addProvider(event.includeClient(), new LanguageProvider(packOutput, Clayium.MODID, "en_us") {
            @Override
            protected void addTranslations() {
                for (ClayTier tier : ClayTier.values()) {
                    add(tier.translationKey(), tier.displayName());
                }
                add("block." + Clayium.MODID + ".clay_work_table", "Clay Work Table");
                addBlockNames(this);
                addItemNames(this);
                add("container." + Clayium.MODID + ".clay_work_table", "Clay Work Table");
                add("container." + Clayium.MODID + ".machine", "Clayium Machine");
                add("gui." + Clayium.MODID + ".energy", "%s");
                add("gui." + Clayium.MODID + ".tier", "Tier %s");
                add("message." + Clayium.MODID + ".water_wheel_status",
                        "Flowing water: %s, generation: %s CE/s");
                add("jei." + Clayium.MODID + ".category.clay_work_table", "Clay Work Table");
                add("jei." + Clayium.MODID + ".category.clay_bending_machine", "Clay Bending Machine");
                add("jei." + Clayium.MODID + ".category.elemental_milling_machine", "Elemental Milling Machine");
                add("jei." + Clayium.MODID + ".processing_time", "Time: %s ticks");
                add("jei." + Clayium.MODID + ".required_actions", "Manual actions: %s");
                add("jei." + Clayium.MODID + ".input_count", "Input: %s");
                add("jei." + Clayium.MODID + ".required_tool", "Tool: %s");
                add("jei." + Clayium.MODID + ".tool.roll", "Clay Rolling Pin");
                add("jei." + Clayium.MODID + ".tool.slice", "Clay Slicer or Spatula");
                add("jei." + Clayium.MODID + ".tool.punch", "Clay Spatula");
                add("jei." + Clayium.MODID + ".tool.divide", "Clay Slicer or Spatula");
                add("jei." + Clayium.MODID + ".operation", "Operation: %s");
                add("jei." + Clayium.MODID + ".clay_energy_per_tick", "CE/t: %s");
                add("jei." + Clayium.MODID + ".total_clay_energy", "Total CE: %s");
                add("jei." + Clayium.MODID + ".minimum_tier", "Tier: %s");
                for (net.claustra01.clayium.recipe.ClayWorkTableOperation operation
                        : net.claustra01.clayium.recipe.ClayWorkTableOperation.values()) {
                    add(operation.translationKey(), operation.id());
                }
                for (net.claustra01.clayium.world.level.block.entity.MachineBlockEntity.StopReason reason
                        : net.claustra01.clayium.world.level.block.entity.MachineBlockEntity.StopReason.values()) {
                    add("gui." + Clayium.MODID + ".stop_reason." + reason.name().toLowerCase(), reason.name());
                }
                add(Clayium.MODID + ".config.log_registry_summary", "Log registry summary");
                add(Clayium.MODID + ".config.ce_sync_interval_ticks", "Clay Energy sync interval");
            }
        });
        event.getGenerator().addProvider(
                event.includeServer(),
                new RecipeProvider(packOutput, event.getLookupProvider()) {
                    @Override
                    protected void buildRecipes(RecipeOutput recipeOutput) {
                        buildWorkTableRecipes(recipeOutput);
                        buildCraftingRecipes(recipeOutput);
                        buildMachineRecipes(recipeOutput);
                    }
                });
    }

    private static void addBlockNames(LanguageProvider language) {
        language.add("block." + Clayium.MODID + ".clay_ore", "Clay Ore");
        language.add("block." + Clayium.MODID + ".dense_clay_ore", "Dense Clay Ore");
        language.add("block." + Clayium.MODID + ".large_dense_clay_ore", "Large Dense Clay Ore");
        language.add("block." + Clayium.MODID + ".dense_clay", "Dense Clay");
        language.add("block." + Clayium.MODID + ".compressed_clay", "Compressed Clay");
        language.add("block." + Clayium.MODID + ".raw_clay_machine_hull", "Raw Clay Machine Hull");
        language.add("block." + Clayium.MODID + ".clay_machine_hull", "Clay Machine Hull");
        language.add("block." + Clayium.MODID + ".clay_bending_machine", "Clay Bending Machine");
        language.add("block." + Clayium.MODID + ".elemental_milling_machine", "Elemental Milling Machine");
        language.add("block." + Clayium.MODID + ".clay_water_wheel", "Clay Water Wheel");
    }

    private static void addItemNames(LanguageProvider language) {
        language.add("item." + Clayium.MODID + ".clay_stick", "Clay Stick");
        language.add("item." + Clayium.MODID + ".short_clay_stick", "Short Clay Stick");
        language.add("item." + Clayium.MODID + ".large_clay_ball", "Large Clay Ball");
        language.add("item." + Clayium.MODID + ".clay_disc", "Clay Disc");
        language.add("item." + Clayium.MODID + ".small_clay_disc", "Small Clay Disc");
        language.add("item." + Clayium.MODID + ".clay_plate", "Clay Plate");
        language.add("item." + Clayium.MODID + ".large_clay_plate", "Large Clay Plate");
        language.add("item." + Clayium.MODID + ".clay_blade", "Clay Blade");
        language.add("item." + Clayium.MODID + ".clay_cylinder", "Clay Cylinder");
        language.add("item." + Clayium.MODID + ".clay_ring", "Clay Ring");
        language.add("item." + Clayium.MODID + ".small_clay_ring", "Small Clay Ring");
        language.add("item." + Clayium.MODID + ".clay_gear", "Clay Gear");
        language.add("item." + Clayium.MODID + ".clay_wheel", "Clay Wheel");
        language.add("item." + Clayium.MODID + ".dense_clay_plate", "Dense Clay Plate");
        language.add("item." + Clayium.MODID + ".dense_clay_stick", "Dense Clay Stick");
        language.add("item." + Clayium.MODID + ".dense_clay_gear", "Dense Clay Gear");
        language.add("item." + Clayium.MODID + ".clay_circuit_board", "Clay Circuit Board");
        language.add("item." + Clayium.MODID + ".raw_clay_rolling_pin", "Raw Clay Rolling Pin");
        language.add("item." + Clayium.MODID + ".raw_clay_slicer", "Raw Clay Slicer");
        language.add("item." + Clayium.MODID + ".raw_clay_spatula", "Raw Clay Spatula");
        language.add("item." + Clayium.MODID + ".clay_rolling_pin", "Clay Rolling Pin");
        language.add("item." + Clayium.MODID + ".clay_slicer", "Clay Slicer");
        language.add("item." + Clayium.MODID + ".clay_spatula", "Clay Spatula");
        language.add("item." + Clayium.MODID + ".clay_shovel", "Clay Shovel");
        language.add("item." + Clayium.MODID + ".clay_pickaxe", "Clay Pickaxe");
    }

    private static void buildWorkTableRecipes(RecipeOutput output) {
        work(output, "clay_ball_to_clay_stick", Items.CLAY_BALL, 1,
                ClayiumRegistries.CLAY_STICK.get(), 1, ClayWorkTableOperation.FORM, 4);
        work(output, "large_clay_ball_to_clay_cylinder", ClayiumRegistries.LARGE_CLAY_BALL.get(), 1,
                ClayiumRegistries.CLAY_CYLINDER.get(), 1, ClayWorkTableOperation.FORM, 4);
        work(output, "large_clay_ball_to_clay_disc", ClayiumRegistries.LARGE_CLAY_BALL.get(), 1,
                ClayiumRegistries.CLAY_DISC.get(), 1, ClayWorkTableOperation.CUT, 30);
        work(output, "clay_disc_to_raw_clay_slicer", ClayiumRegistries.CLAY_DISC.get(), 1,
                ClayiumRegistries.RAW_CLAY_SLICER.get(), 1, ClayWorkTableOperation.CUT, 15);
        work(output, "clay_disc_to_clay_plate", ClayiumRegistries.CLAY_DISC.get(), 1,
                ClayiumRegistries.CLAY_PLATE.get(), 1, ClayWorkTableOperation.SLICE, 4);
        work(output, "clay_plate_to_clay_blade", ClayiumRegistries.CLAY_PLATE.get(), 1,
                ClayiumRegistries.CLAY_BLADE.get(), 1, ClayWorkTableOperation.CUT, 10);
        work(output, "clay_plate_to_clay_sticks", ClayiumRegistries.CLAY_PLATE.get(), 1,
                ClayiumRegistries.CLAY_STICK.get(), 4, ClayWorkTableOperation.DIVIDE, 3);
        work(output, "clay_disc_to_clay_ring", ClayiumRegistries.CLAY_DISC.get(), 1,
                ClayiumRegistries.CLAY_RING.get(), 1, ClayWorkTableOperation.PUNCH, 2);
        work(output, "clay_cylinder_to_small_clay_discs", ClayiumRegistries.CLAY_CYLINDER.get(), 1,
                ClayiumRegistries.SMALL_CLAY_DISC.get(), 8, ClayWorkTableOperation.DIVIDE, 7);
        work(output, "small_clay_disc_to_small_clay_ring", ClayiumRegistries.SMALL_CLAY_DISC.get(), 1,
                ClayiumRegistries.SMALL_CLAY_RING.get(), 1, ClayWorkTableOperation.PUNCH, 1);
        work(output, "clay_plates_to_large_clay_plate", ClayiumRegistries.CLAY_PLATE.get(), 6,
                ClayiumRegistries.LARGE_CLAY_PLATE.get(), 1, ClayWorkTableOperation.ROLL, 10);
        work(output, "clay_plates_to_large_clay_ball", ClayiumRegistries.CLAY_PLATE.get(), 3,
                ClayiumRegistries.LARGE_CLAY_BALL.get(), 1, ClayWorkTableOperation.FORM, 40);
    }

    private static void work(
            RecipeOutput output,
            String id,
            ItemLike input,
            int inputCount,
            ItemLike result,
            int resultCount,
            ClayWorkTableOperation operation,
            int actions) {
        output.accept(
                Clayium.id("clay_work_table/" + id),
                new ClayWorkTableRecipe(
                        Ingredient.of(input),
                        inputCount,
                        new ItemStack(result, resultCount),
                        operation,
                        actions,
                        ClayTier.RAW),
                null);
    }

    private static void buildCraftingRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ClayiumRegistries.CLAY_WORK_TABLE.get())
                .define('D', ClayiumRegistries.DENSE_CLAY.get())
                .pattern("DD").pattern("DD")
                .unlockedBy("has_dense_clay", has(ClayiumRegistries.DENSE_CLAY.get()))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ClayiumRegistries.LARGE_CLAY_BALL.get())
                .requires(Items.CLAY_BALL, 8)
                .unlockedBy("has_clay", has(Items.CLAY_BALL))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ClayiumRegistries.SHORT_CLAY_STICK.get(), 2)
                .requires(ClayiumRegistries.CLAY_STICK.get())
                .unlockedBy("has_clay_stick", has(ClayiumRegistries.CLAY_STICK.get()))
                .save(output);
        compression(output, "dense_clay", Items.CLAY, ClayiumRegistries.DENSE_CLAY.get());
        compression(output, "compressed_clay", ClayiumRegistries.DENSE_CLAY.get(), ClayiumRegistries.COMPRESSED_CLAY.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ClayiumRegistries.CLAY_GEAR.get())
                .define('S', ClayiumRegistries.SHORT_CLAY_STICK.get())
                .define('R', ClayiumRegistries.SMALL_CLAY_RING.get())
                .pattern(" S ").pattern("SRS").pattern(" S ")
                .unlockedBy("has_small_clay_ring", has(ClayiumRegistries.SMALL_CLAY_RING.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ClayiumRegistries.CLAY_WHEEL.get())
                .define('P', ClayiumRegistries.CLAY_PLATE.get())
                .define('R', ClayiumRegistries.CLAY_RING.get())
                .pattern(" P ").pattern("PRP").pattern(" P ")
                .unlockedBy("has_clay_ring", has(ClayiumRegistries.CLAY_RING.get()))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ClayiumRegistries.DENSE_CLAY_STICK.get(), 2)
                .requires(ClayiumRegistries.DENSE_CLAY_PLATE.get())
                .unlockedBy("has_dense_clay_plate", has(ClayiumRegistries.DENSE_CLAY_PLATE.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ClayiumRegistries.DENSE_CLAY_GEAR.get())
                .define('P', ClayiumRegistries.DENSE_CLAY_PLATE.get())
                .pattern(" P ").pattern("P P").pattern(" P ")
                .unlockedBy("has_dense_clay_plate", has(ClayiumRegistries.DENSE_CLAY_PLATE.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ClayiumRegistries.RAW_CLAY_ROLLING_PIN.get())
                .define('S', ClayiumRegistries.SHORT_CLAY_STICK.get())
                .define('C', ClayiumRegistries.CLAY_CYLINDER.get())
                .pattern("SCS")
                .unlockedBy("has_clay_cylinder", has(ClayiumRegistries.CLAY_CYLINDER.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ClayiumRegistries.RAW_CLAY_SPATULA.get())
                .define('S', ClayiumRegistries.SHORT_CLAY_STICK.get())
                .define('B', ClayiumRegistries.CLAY_BLADE.get())
                .pattern("SB")
                .unlockedBy("has_clay_blade", has(ClayiumRegistries.CLAY_BLADE.get()))
                .save(output);
        smelt(output, ClayiumRegistries.RAW_CLAY_ROLLING_PIN.get(), ClayiumRegistries.CLAY_ROLLING_PIN.get(), 200);
        smelt(output, ClayiumRegistries.RAW_CLAY_SLICER.get(), ClayiumRegistries.CLAY_SLICER.get(), 200);
        smelt(output, ClayiumRegistries.RAW_CLAY_SPATULA.get(), ClayiumRegistries.CLAY_SPATULA.get(), 200);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ClayiumRegistries.RAW_CLAY_MACHINE_HULL.get())
                .define('P', ClayiumRegistries.LARGE_CLAY_PLATE.get())
                .define('G', ClayiumRegistries.CLAY_GEAR.get())
                .pattern("PPP").pattern("PGP").pattern("PPP")
                .unlockedBy("has_large_clay_plate", has(ClayiumRegistries.LARGE_CLAY_PLATE.get()))
                .save(output);
        smelt(output, ClayiumRegistries.RAW_CLAY_MACHINE_HULL.get(), ClayiumRegistries.CLAY_MACHINE_HULL.get(), 400);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ClayiumRegistries.CLAY_SHOVEL.get())
                .define('P', ClayiumRegistries.CLAY_PLATE.get()).define('S', ClayiumRegistries.CLAY_STICK.get())
                .pattern("P").pattern("S").pattern("S")
                .unlockedBy("has_clay_plate", has(ClayiumRegistries.CLAY_PLATE.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ClayiumRegistries.CLAY_PICKAXE.get())
                .define('P', ClayiumRegistries.DENSE_CLAY_PLATE.get()).define('S', ClayiumRegistries.DENSE_CLAY_STICK.get())
                .pattern("PPP").pattern(" S ").pattern(" S ")
                .unlockedBy("has_dense_clay_plate", has(ClayiumRegistries.DENSE_CLAY_PLATE.get()))
                .save(output);

        machineBlockRecipe(output, ClayiumRegistries.CLAY_BENDING_MACHINE.get(), ClayiumRegistries.CLAY_GEAR.get());
        machineBlockRecipe(output, ClayiumRegistries.ELEMENTAL_MILLING_MACHINE.get(), ClayiumRegistries.DENSE_CLAY_GEAR.get());
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ClayiumRegistries.CLAY_WATER_WHEEL.get())
                .requires(ClayiumRegistries.CLAY_MACHINE_HULL.get())
                .requires(ClayiumRegistries.CLAY_WHEEL.get())
                .unlockedBy("has_clay_machine_hull", has(ClayiumRegistries.CLAY_MACHINE_HULL.get()))
                .save(output);
    }

    private static void compression(RecipeOutput output, String id, ItemLike input, ItemLike outputBlock) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, outputBlock)
                .define('C', input).pattern("CCC").pattern("CCC").pattern("CCC")
                .unlockedBy("has_input", has(input))
                .save(output, Clayium.id(id));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, input, 9)
                .requires(outputBlock)
                .unlockedBy("has_" + id, has(outputBlock))
                .save(output, Clayium.id(id + "_unpack"));
    }

    private static void machineBlockRecipe(RecipeOutput output, ItemLike result, ItemLike gear) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, result)
                .define('H', ClayiumRegistries.CLAY_MACHINE_HULL.get())
                .define('G', gear)
                .define('P', ClayiumRegistries.CLAY_PLATE.get())
                .pattern(" P ").pattern("GHG").pattern(" P ")
                .unlockedBy("has_clay_machine_hull", has(ClayiumRegistries.CLAY_MACHINE_HULL.get()))
                .save(output);
    }

    private static void smelt(RecipeOutput output, ItemLike input, ItemLike result, int time) {
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(input), RecipeCategory.MISC, result, 0.1F, time)
                .unlockedBy("has_input", has(input))
                .save(
                        output,
                        Clayium.id(
                                "smelting/"
                                        + BuiltInRegistries.ITEM.getKey(input.asItem()).getPath()
                                        + "_to_"
                                        + BuiltInRegistries.ITEM.getKey(result.asItem()).getPath()));
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    private static void buildMachineRecipes(RecipeOutput output) {
        machine(output, "bending/clay_block_to_clay_plate", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                Items.CLAY, ClayiumRegistries.CLAY_PLATE.get(), 40, 1);
        machine(output, "bending/dense_clay_to_dense_clay_plate", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                ClayiumRegistries.DENSE_CLAY.get(), ClayiumRegistries.DENSE_CLAY_PLATE.get(), 80, 1);
        machine(output, "milling/dense_clay_plate_to_circuit_board", ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE,
                ClayiumRegistries.DENSE_CLAY_PLATE.get(), ClayiumRegistries.CLAY_CIRCUIT_BOARD.get(), 32, 1);
    }

    private static void machine(
            RecipeOutput output,
            String id,
            net.minecraft.resources.ResourceLocation machine,
            ItemLike input,
            ItemLike result,
            int time,
            long energyPerTick) {
        output.accept(
                Clayium.id(id),
                new MachineRecipe(
                        machine,
                        Ingredient.of(input),
                        new ItemStack(result),
                        time,
                        energyPerTick,
                        ClayTier.CLAY),
                null);
    }
}
