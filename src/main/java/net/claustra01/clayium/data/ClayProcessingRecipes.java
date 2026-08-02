/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.data;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.machine.ManufacturingMachineCatalog;
import net.claustra01.clayium.recipe.MachineIngredient;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Clay and component processing recipes represented by the common recipe model. */
public final class ClayProcessingRecipes {
    private ClayProcessingRecipes() {
    }

    public static void build(RecipeOutput output) {
        bending(output);
        milling(output);
        wireDrawing(output);
        pipeDrawing(output);
        cutting(output);
        lathe(output);
        condenser(output);
        decomposer(output);
        grinder(output);
        assembler(output);
        inscriber(output);
        centrifuge(output);
        energeticClayCondenser(output);
    }

    private static void bending(RecipeOutput output) {
        one(output, "bending/clay_ball_to_small_disc", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                Items.CLAY_BALL, 1, ClayiumRegistries.SMALL_CLAY_DISC.get(), 1, 1, 3, 0);
        one(output, "bending/large_clay_ball_to_disc", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                ClayiumRegistries.LARGE_CLAY_BALL.get(), 1, ClayiumRegistries.CLAY_DISC.get(), 1, 1, 2, 0);
        one(output, "bending/clay_disc_to_raw_slicer", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                ClayiumRegistries.CLAY_DISC.get(), 1, ClayiumRegistries.RAW_CLAY_SLICER.get(), 1, 1, 1, 0);
        one(output, "bending/clay_block_to_plate", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                Items.CLAY, 1, ClayiumRegistries.CLAY_PLATE.get(), 1, 1, 1, 0);
        one(output, "bending/clay_plates_to_large_plate", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                ClayiumRegistries.CLAY_PLATE.get(), 4, ClayiumRegistries.LARGE_CLAY_PLATE.get(), 1, 1, 4, 0);
        one(output, "bending/clay_cylinder_to_blades", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                ClayiumRegistries.CLAY_CYLINDER.get(), 1, ClayiumRegistries.CLAY_BLADE.get(), 2, 1, 4, 0);
        one(output, "bending/dense_clay_to_plate", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                ClayiumRegistries.DENSE_CLAY.get(), 1, ClayiumRegistries.DENSE_CLAY_PLATE.get(), 1, 1, 4, 0);
        one(output, "bending/dense_plates_to_large_plate", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                ClayiumRegistries.DENSE_CLAY_PLATE.get(), 4, item("dense_clay_large_plate"), 1, 1, 8, 0);
        one(output, "bending/dense_cylinder_to_blades", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                item("dense_clay_cylinder"), 1, item("dense_clay_blade"), 2, 1, 8, 0);
        one(output, "bending/industrial_clay_to_plate", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                block("industrial_clay"), 1, item("industrial_clay_plate"), 1, 2, 4, 2);
        one(output, "bending/industrial_plates_to_large_plate", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                item("industrial_clay_plate"), 4, item("industrial_clay_large_plate"), 1, 2, 8, 2);
        one(output, "bending/advanced_industrial_clay_to_plate", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                block("advanced_industrial_clay"), 1, item("advanced_industrial_clay_plate"), 1, 4, 4, 2);
        one(output, "bending/advanced_industrial_plates_to_large_plate", ClayiumMachineIds.CLAY_BENDING_MACHINE,
                item("advanced_industrial_clay_plate"), 4, item("advanced_industrial_clay_large_plate"), 1, 4, 8, 2);
    }

    private static void milling(RecipeOutput output) {
        one(output, "milling/dense_clay_plate_to_circuit_board", ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE,
                ClayiumRegistries.DENSE_CLAY_PLATE.get(), 1, ClayiumRegistries.CLAY_CIRCUIT_BOARD.get(), 1, 1, 32, 0);
        one(output, "milling/industrial_clay_plate_to_circuit_board", ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE,
                item("industrial_clay_plate"), 1, ClayiumRegistries.CLAY_CIRCUIT_BOARD.get(), 1, 1, 1, 0);
        one(output, "milling/advanced_industrial_plate_to_cee_board", ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE,
                item("advanced_industrial_clay_plate"), 1, item("cee_board"), 1, 2, 32, 3);
    }

    private static void wireDrawing(RecipeOutput output) {
        one(output, "wire_drawing/clay_ball_to_stick", ClayiumMachineIds.WIRE_DRAWING_MACHINE,
                Items.CLAY_BALL, 1, ClayiumRegistries.CLAY_STICK.get(), 1, 1, 1, 0);
        one(output, "wire_drawing/clay_cylinder_to_sticks", ClayiumMachineIds.WIRE_DRAWING_MACHINE,
                ClayiumRegistries.CLAY_CYLINDER.get(), 1, ClayiumRegistries.CLAY_STICK.get(), 8, 1, 3, 0);
        one(output, "wire_drawing/clay_pipe_to_sticks", ClayiumMachineIds.WIRE_DRAWING_MACHINE,
                item("clay_pipe"), 1, ClayiumRegistries.CLAY_STICK.get(), 4, 1, 2, 0);
        one(output, "wire_drawing/small_clay_disc_to_stick", ClayiumMachineIds.WIRE_DRAWING_MACHINE,
                ClayiumRegistries.SMALL_CLAY_DISC.get(), 1, ClayiumRegistries.CLAY_STICK.get(), 1, 1, 1, 0);
        one(output, "wire_drawing/dense_cylinder_to_sticks", ClayiumMachineIds.WIRE_DRAWING_MACHINE,
                item("dense_clay_cylinder"), 1, ClayiumRegistries.DENSE_CLAY_STICK.get(), 8, 1, 6, 0);
        one(output, "wire_drawing/dense_pipe_to_sticks", ClayiumMachineIds.WIRE_DRAWING_MACHINE,
                item("dense_clay_pipe"), 1, ClayiumRegistries.DENSE_CLAY_STICK.get(), 4, 1, 4, 0);
        one(output, "wire_drawing/small_dense_disc_to_stick", ClayiumMachineIds.WIRE_DRAWING_MACHINE,
                item("dense_clay_small_disc"), 1, ClayiumRegistries.DENSE_CLAY_STICK.get(), 1, 1, 2, 0);
    }

    private static void pipeDrawing(RecipeOutput output) {
        one(output, "pipe_drawing/clay_cylinder_to_pipes", ClayiumMachineIds.PIPE_DRAWING_MACHINE,
                ClayiumRegistries.CLAY_CYLINDER.get(), 1, item("clay_pipe"), 2, 1, 3, 0);
        one(output, "pipe_drawing/dense_cylinder_to_pipes", ClayiumMachineIds.PIPE_DRAWING_MACHINE,
                item("dense_clay_cylinder"), 1, item("dense_clay_pipe"), 2, 1, 6, 0);
    }

    private static void cutting(RecipeOutput output) {
        one(output, "cutting/large_clay_ball_to_disc", ClayiumMachineIds.CUTTING_MACHINE,
                ClayiumRegistries.LARGE_CLAY_BALL.get(), 1, ClayiumRegistries.CLAY_DISC.get(), 1, 1, 2, 0);
        one(output, "cutting/clay_cylinder_to_small_discs", ClayiumMachineIds.CUTTING_MACHINE,
                ClayiumRegistries.CLAY_CYLINDER.get(), 1, ClayiumRegistries.SMALL_CLAY_DISC.get(), 8, 1, 2, 0);
        one(output, "cutting/large_clay_plate_to_discs", ClayiumMachineIds.CUTTING_MACHINE,
                ClayiumRegistries.LARGE_CLAY_PLATE.get(), 1, ClayiumRegistries.CLAY_DISC.get(), 2, 1, 3, 0);
        one(output, "cutting/clay_plate_to_small_discs", ClayiumMachineIds.CUTTING_MACHINE,
                ClayiumRegistries.CLAY_PLATE.get(), 1, ClayiumRegistries.SMALL_CLAY_DISC.get(), 4, 1, 3, 0);
        one(output, "cutting/clay_stick_to_short_sticks", ClayiumMachineIds.CUTTING_MACHINE,
                ClayiumRegistries.CLAY_STICK.get(), 1, ClayiumRegistries.SHORT_CLAY_STICK.get(), 2, 1, 1, 0);
        one(output, "cutting/dense_cylinder_to_small_discs", ClayiumMachineIds.CUTTING_MACHINE,
                item("dense_clay_cylinder"), 1, item("dense_clay_small_disc"), 8, 1, 4, 0);
        one(output, "cutting/large_dense_plate_to_discs", ClayiumMachineIds.CUTTING_MACHINE,
                item("dense_clay_large_plate"), 1, item("dense_clay_disc"), 2, 1, 6, 0);
        one(output, "cutting/dense_plate_to_small_discs", ClayiumMachineIds.CUTTING_MACHINE,
                ClayiumRegistries.DENSE_CLAY_PLATE.get(), 1, item("dense_clay_small_disc"), 4, 1, 6, 0);
        one(output, "cutting/dense_stick_to_short_sticks", ClayiumMachineIds.CUTTING_MACHINE,
                ClayiumRegistries.DENSE_CLAY_STICK.get(), 1, item("dense_clay_short_stick"), 2, 1, 2, 0);
    }

    private static void lathe(RecipeOutput output) {
        one(output, "lathe/clay_ball_to_short_stick", ClayiumMachineIds.LATHE,
                Items.CLAY_BALL, 1, ClayiumRegistries.SHORT_CLAY_STICK.get(), 1, 1, 1, 0);
        one(output, "lathe/large_clay_ball_to_cylinder", ClayiumMachineIds.LATHE,
                ClayiumRegistries.LARGE_CLAY_BALL.get(), 1, ClayiumRegistries.CLAY_CYLINDER.get(), 1, 1, 4, 0);
        one(output, "lathe/clay_cylinder_to_needle", ClayiumMachineIds.LATHE,
                ClayiumRegistries.CLAY_CYLINDER.get(), 1, item("clay_needle"), 1, 1, 3, 0);
        one(output, "lathe/clay_needle_to_sticks", ClayiumMachineIds.LATHE,
                item("clay_needle"), 1, ClayiumRegistries.CLAY_STICK.get(), 6, 1, 3, 0);
        one(output, "lathe/clay_disc_to_ring", ClayiumMachineIds.LATHE,
                ClayiumRegistries.CLAY_DISC.get(), 1, ClayiumRegistries.CLAY_RING.get(), 1, 1, 2, 0);
        one(output, "lathe/small_clay_disc_to_ring", ClayiumMachineIds.LATHE,
                ClayiumRegistries.SMALL_CLAY_DISC.get(), 1, ClayiumRegistries.SMALL_CLAY_RING.get(), 1, 1, 1, 0);
        one(output, "lathe/dense_clay_to_cylinder", ClayiumMachineIds.LATHE,
                ClayiumRegistries.DENSE_CLAY.get(), 2, item("dense_clay_cylinder"), 1, 1, 4, 0);
        one(output, "lathe/dense_cylinder_to_needle", ClayiumMachineIds.LATHE,
                item("dense_clay_cylinder"), 1, item("dense_clay_needle"), 1, 1, 6, 0);
        one(output, "lathe/dense_needle_to_sticks", ClayiumMachineIds.LATHE,
                item("dense_clay_needle"), 1, ClayiumRegistries.DENSE_CLAY_STICK.get(), 6, 1, 6, 0);
        one(output, "lathe/dense_disc_to_ring", ClayiumMachineIds.LATHE,
                item("dense_clay_disc"), 1, item("dense_clay_ring"), 1, 1, 4, 0);
        one(output, "lathe/small_dense_disc_to_ring", ClayiumMachineIds.LATHE,
                item("dense_clay_small_disc"), 1, item("dense_clay_small_ring"), 1, 1, 2, 0);
    }

    private static void condenser(RecipeOutput output) {
        one(output, "condenser/compressed_clay_shards", ClayiumMachineIds.CONDENSER,
                item("compressed_clay_shard"), 4, ClayiumRegistries.COMPRESSED_CLAY.get(), 1, 1, 3, 0);
        one(output, "condenser/industrial_clay_shards", ClayiumMachineIds.CONDENSER,
                item("industrial_clay_shard"), 4, block("industrial_clay"), 1, 1, 6, 0);
        one(output, "condenser/advanced_industrial_clay_shards", ClayiumMachineIds.CONDENSER,
                item("advanced_industrial_clay_shard"), 4, block("advanced_industrial_clay"), 1, 1, 9, 0);
        one(output, "condenser/clay_dust_to_block", ClayiumMachineIds.CONDENSER,
                item("clay_dust"), 1, Items.CLAY, 1, 1, 3, 0);
        one(output, "condenser/dense_clay_dust_to_block", ClayiumMachineIds.CONDENSER,
                item("dense_clay_dust"), 1, ClayiumRegistries.DENSE_CLAY.get(), 1, 1, 6, 0);
        ItemLike[] levels = {
            Items.CLAY,
            ClayiumRegistries.DENSE_CLAY.get(),
            ClayiumRegistries.COMPRESSED_CLAY.get(),
            block("industrial_clay"),
            block("advanced_industrial_clay"),
            block("energetic_clay"),
            block("compressed_energetic_clay"),
            block("double_compressed_energetic_clay"),
            block("triple_compressed_energetic_clay"),
            block("quadruple_compressed_energetic_clay"),
            block("quintuple_compressed_energetic_clay"),
            block("sextuple_compressed_energetic_clay"),
            block("septuple_compressed_energetic_clay"),
            block("octuple_compressed_energetic_clay")
        };
        long[] energy = {1, 1, 10, 100, 100, 1_000, 10_000, 100_000, 1_000_000,
                10_000_000, 100_000_000, 1_000_000_000, 1_000_000_000};
        int[] time = {4, 4, 4, 4, 16, 16, 13, 10, 8, 6, 4, 3, 25};
        int[] tier = {0, 0, 0, 0, 4, 4, 4, 5, 5, 5, 5, 5, 5};
        for (int index = 0; index < levels.length - 1; index++) {
            one(output, "condenser/level_" + index + "_to_" + (index + 1), ClayiumMachineIds.CONDENSER,
                    levels[index], 9, levels[index + 1], 1, energy[index], time[index], tier[index]);
        }
    }

    private static void decomposer(RecipeOutput output) {
        one(output, "decomposer/clay_block_to_balls", ClayiumMachineIds.DECOMPOSER,
                Items.CLAY, 1, Items.CLAY_BALL, 4, 1, 3, 0);
        one(output, "decomposer/dense_clay_to_clay", ClayiumMachineIds.DECOMPOSER,
                ClayiumRegistries.DENSE_CLAY.get(), 1, Items.CLAY, 9, 1, 3, 0);
        one(output, "decomposer/compressed_clay_to_dense", ClayiumMachineIds.DECOMPOSER,
                ClayiumRegistries.COMPRESSED_CLAY.get(), 1, ClayiumRegistries.DENSE_CLAY.get(), 9, 1, 3, 0);
        one(output, "decomposer/industrial_to_compressed", ClayiumMachineIds.DECOMPOSER,
                block("industrial_clay"), 1, ClayiumRegistries.COMPRESSED_CLAY.get(), 9, 1, 10, 0);
        one(output, "decomposer/advanced_to_industrial", ClayiumMachineIds.DECOMPOSER,
                block("advanced_industrial_clay"), 1, block("industrial_clay"), 9, 1, 20, 0);
        one(output, "decomposer/industrial_dust_to_energetic", ClayiumMachineIds.DECOMPOSER,
                item("industrial_clay_dust"), 1, item("energetic_clay_dust"), 3, 1, 60, 0);
        one(output, "decomposer/advanced_dust_to_energetic", ClayiumMachineIds.DECOMPOSER,
                item("advanced_industrial_clay_dust"), 1, item("energetic_clay_dust"), 28, 1_000, 60, 4);
    }

    private static void grinder(RecipeOutput output) {
        one(output, "grinder/clay_ore_to_shards", ClayiumMachineIds.GRINDER,
                ClayiumRegistries.CLAY_ORE.get(), 1, item("compressed_clay_shard"), 2, 1, 3, 0);
        one(output, "grinder/deepslate_clay_ore_to_shards", ClayiumMachineIds.GRINDER,
                ClayiumRegistries.DEEPSLATE_CLAY_ORE.get(), 1, item("compressed_clay_shard"), 2, 1, 3, 0);
        one(output, "grinder/dense_clay_ore_to_shards", ClayiumMachineIds.GRINDER,
                ClayiumRegistries.DENSE_CLAY_ORE.get(), 1, item("industrial_clay_shard"), 3, 1, 6, 0);
        one(output, "grinder/deepslate_dense_clay_ore_to_shards", ClayiumMachineIds.GRINDER,
                ClayiumRegistries.DEEPSLATE_DENSE_CLAY_ORE.get(), 1, item("industrial_clay_shard"), 3, 1, 6, 0);
        one(output, "grinder/large_dense_clay_ore_to_shards", ClayiumMachineIds.GRINDER,
                ClayiumRegistries.LARGE_DENSE_CLAY_ORE.get(), 1,
                item("advanced_industrial_clay_shard"), 5, 1, 9, 0);
        one(output, "grinder/deepslate_large_dense_clay_ore_to_shards", ClayiumMachineIds.GRINDER,
                ClayiumRegistries.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get(), 1,
                item("advanced_industrial_clay_shard"), 5, 1, 9, 0);
        one(output, "grinder/clay_block_to_dust", ClayiumMachineIds.GRINDER,
                Items.CLAY, 1, item("clay_dust"), 1, 1, 3, 0);
        one(output, "grinder/dense_clay_block_to_dust", ClayiumMachineIds.GRINDER,
                ClayiumRegistries.DENSE_CLAY.get(), 1, item("dense_clay_dust"), 1, 1, 6, 0);
        String[][] forms = {
            {"plate", "1", "dust", "1", "3"},
            {"stick", "4", "dust", "1", "3"},
            {"short_stick", "8", "dust", "1", "3"},
            {"ring", "4", "dust", "5", "15"},
            {"small_ring", "8", "dust", "1", "3"},
            {"gear", "8", "dust", "9", "27"},
            {"blade", "1", "dust", "1", "3"},
            {"needle", "1", "dust", "2", "6"},
            {"disc", "2", "dust", "3", "9"},
            {"small_disc", "4", "dust", "1", "3"},
            {"cylinder", "1", "dust", "2", "6"},
            {"pipe", "1", "dust", "1", "3"},
            {"large_plate", "1", "dust", "4", "12"},
            {"grinding_head", "1", "dust", "16", "48"},
            {"bearing", "4", "dust", "5", "15"},
            {"spindle", "1", "dust", "4", "12"},
            {"cutting_head", "1", "dust", "9", "27"}
        };
        for (String material : List.of("clay", "dense_clay")) {
            int timeMultiplier = material.equals("clay") ? 1 : 4;
            for (String[] form : forms) {
                ItemLike input = materialForm(material, form[0]);
                ItemLike dust = item(material + "_dust");
                one(output, "grinder/" + material + "_" + form[0] + "_to_dust", ClayiumMachineIds.GRINDER,
                        input, Integer.parseInt(form[1]), dust, Integer.parseInt(form[3]),
                        1, Integer.parseInt(form[4]) * timeMultiplier, 0);
            }
        }
        one(output, "grinder/industrial_clay_block_to_dust", ClayiumMachineIds.GRINDER,
                block("industrial_clay"), 1, item("industrial_clay_dust"), 1, 1, 12, 0);
        one(output, "grinder/advanced_industrial_clay_block_to_dust", ClayiumMachineIds.GRINDER,
                block("advanced_industrial_clay"), 1, item("advanced_industrial_clay_dust"), 1, 2, 12, 0);
    }

    private static void assembler(RecipeOutput output) {
        String[] generatorMaterials = {"", "clay", "dense_clay", "industrial_clay"};
        for (int tier = 1; tier <= 3; tier++) {
            ItemLike largePlate = tier == 1 ? ClayiumRegistries.LARGE_CLAY_PLATE.get()
                    : item(generatorMaterials[tier] + "_large_plate");
            two(output, "assembler/cobblestone_generator_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    largePlate, 1, item("simple_circuit"), 1,
                    cobblestoneGenerator(tier), 1, tierEnergy(tier), 40, 4);
        }
        for (int tier = 4; tier <= 7; tier++) {
            String buffer = ClayTier.byLegacyIndex(tier).id() + "_buffer";
            two(output, "assembler/cobblestone_generator_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    ClayiumRegistries.LOGISTICS_BLOCKS.get(buffer).get(), 1, item("simple_circuit"), 1,
                    cobblestoneGenerator(tier), 1, tierEnergy(tier), 40, 4);
        }
        two(output, "assembler/energetic_clay_condenser", ClayiumMachineIds.ASSEMBLER,
                hull(3), 1, item("clay_energy_excitor"), 2,
                machineByTypeAndTier("energetic_clay_condenser", 3), 1, tierEnergy(3), 120, 4);
        two(output, "assembler/energetic_clay_condenser_mk2", ClayiumMachineIds.ASSEMBLER,
                hull(4), 1, item("clay_energy_excitor"), 2,
                machineByTypeAndTier("energetic_clay_condenser", 4), 1, tierEnergy(4), 120, 4);
        one(output, "assembler/clay_sticks_to_gear", ClayiumMachineIds.ASSEMBLER,
                ClayiumRegistries.CLAY_STICK.get(), 5, ClayiumRegistries.CLAY_GEAR.get(), 1, 10, 20, 3);
        one(output, "assembler/short_clay_sticks_to_gear", ClayiumMachineIds.ASSEMBLER,
                ClayiumRegistries.SHORT_CLAY_STICK.get(), 9, ClayiumRegistries.CLAY_GEAR.get(), 1, 10, 20, 3);
        two(output, "assembler/clay_spindle", ClayiumMachineIds.ASSEMBLER,
                ClayiumRegistries.LARGE_CLAY_PLATE.get(), 1, Items.CLAY_BALL, 8,
                item("clay_spindle"), 1, 10, 20, 3);
        two(output, "assembler/clay_grinding_head", ClayiumMachineIds.ASSEMBLER,
                ClayiumRegistries.LARGE_CLAY_PLATE.get(), 1, Items.CLAY, 8,
                item("clay_grinding_head"), 1, 10, 20, 3);
        two(output, "assembler/clay_cutting_head", ClayiumMachineIds.ASSEMBLER,
                ClayiumRegistries.LARGE_CLAY_PLATE.get(), 1, ClayiumRegistries.CLAY_PLATE.get(), 8,
                item("clay_cutting_head"), 1, 10, 20, 3);
        one(output, "assembler/dense_sticks_to_gear", ClayiumMachineIds.ASSEMBLER,
                ClayiumRegistries.DENSE_CLAY_STICK.get(), 5, ClayiumRegistries.DENSE_CLAY_GEAR.get(), 1, 10, 20, 3);
        one(output, "assembler/short_dense_sticks_to_gear", ClayiumMachineIds.ASSEMBLER,
                item("dense_clay_short_stick"), 9, ClayiumRegistries.DENSE_CLAY_GEAR.get(), 1, 10, 20, 3);
        two(output, "assembler/dense_spindle", ClayiumMachineIds.ASSEMBLER,
                item("dense_clay_large_plate"), 1, Items.CLAY_BALL, 8,
                item("dense_clay_spindle"), 1, 100, 20, 3);
        two(output, "assembler/dense_grinding_head", ClayiumMachineIds.ASSEMBLER,
                item("dense_clay_large_plate"), 1, ClayiumRegistries.DENSE_CLAY.get(), 8,
                item("dense_clay_grinding_head"), 1, 100, 20, 3);
        two(output, "assembler/dense_cutting_head", ClayiumMachineIds.ASSEMBLER,
                item("dense_clay_large_plate"), 1, ClayiumRegistries.DENSE_CLAY_PLATE.get(), 8,
                item("dense_clay_cutting_head"), 1, 100, 20, 3);
        two(output, "assembler/clay_energy_excitor", ClayiumMachineIds.ASSEMBLER,
                item("cee_circuit"), 1, item("industrial_clay_plate"), 3,
                item("clay_energy_excitor"), 1, 8, 20, 0);
        two(output, "assembler/integrated_circuit", ClayiumMachineIds.ASSEMBLER,
                item("precision_circuit"), 1, item("energetic_clay_dust"), 32,
                item("integrated_circuit"), 1, 10_000, 1_200, 6);
        assemblerMachineRecipes(output);
    }

    private static void assemblerMachineRecipes(RecipeOutput output) {
        two(output, "assembler/machines/elemental_milling_machine", ClayiumMachineIds.ASSEMBLER,
                hull(1), 1, item("dense_clay_cutting_head"), 1,
                ClayiumRegistries.ELEMENTAL_MILLING_MACHINE.get(), 1, tierEnergy(1), 120, 4);

        for (int tier = 1; tier <= 4; tier++) {
            two(output, "assembler/machines/bending_machine_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, ClayiumRegistries.DENSE_CLAY_PLATE.get(), 3,
                    machineByTypeAndTier("bending_machine", tier), 1, tierEnergy(tier), 120, 4);
            two(output, "assembler/machines/wire_drawing_machine_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, item("dense_clay_pipe"), 2,
                    machineByTypeAndTier("wire_drawing_machine", tier), 1, tierEnergy(tier), 120, 4);
            two(output, "assembler/machines/pipe_drawing_machine_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, item("dense_clay_cylinder"), 2,
                    machineByTypeAndTier("pipe_drawing_machine", tier), 1, tierEnergy(tier), 120, 4);
            two(output, "assembler/machines/cutting_machine_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, item("clay_cutting_head"), 1,
                    machineByTypeAndTier("cutting_machine", tier), 1, tierEnergy(tier), 120, 4);
            two(output, "assembler/machines/lathe_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, item("clay_spindle"), 1,
                    machineByTypeAndTier("lathe", tier), 1, tierEnergy(tier), 120, 4);
        }
        for (int tier = 5; tier <= 6; tier++) {
            two(output, "assembler/machines/bending_machine_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, ClayiumRegistries.DENSE_CLAY_PLATE.get(), (tier - 4) * 3,
                    machineByTypeAndTier("bending_machine", tier), 1, tierEnergy(tier), 120, 4);
        }
        for (int tier = 2; tier <= 6; tier++) {
            two(output, "assembler/machines/grinder_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, item("dense_clay_grinding_head"), 1,
                    machineByTypeAndTier("grinder", tier), 1, tierEnergy(tier), 120, 4);
        }
        for (int tier = 2; tier <= 4; tier++) {
            two(output, "assembler/machines/decomposer_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, ClayiumRegistries.CLAY_GEAR.get(), 4,
                    machineByTypeAndTier("decomposer", tier), 1, tierEnergy(tier), 120, 4);
        }
        for (int tier = 2; tier <= 3; tier++) {
            ItemLike materialLargePlate = tier == 2
                    ? item("dense_clay_large_plate")
                    : item("industrial_clay_large_plate");
            two(output, "assembler/machines/condenser_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, materialLargePlate, 1,
                    machineByTypeAndTier("condenser", tier), 1, tierEnergy(tier), 120, 4);
        }
        for (int tier = 4; tier <= 5; tier++) {
            two(output, "assembler/machines/condenser_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1,
                    ClayiumRegistries.LOGISTICS_BLOCKS.get(ClayTier.byLegacyIndex(tier).id() + "_buffer").get(), 1,
                    machineByTypeAndTier("condenser", tier), 1, tierEnergy(tier), 120, 4);
        }
        for (int tier = 3; tier <= 4; tier++) {
            two(output, "assembler/machines/milling_machine_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, item("dense_clay_cutting_head"), 1,
                    machineByTypeAndTier("milling_machine", tier), 1, tierEnergy(tier), 120, 4);
            two(output, "assembler/machines/assembler_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, ClayiumRegistries.DENSE_CLAY_GEAR.get(), 4,
                    machineByTypeAndTier("assembler", tier), 1, tierEnergy(tier), 40, 4);
            two(output, "assembler/machines/inscriber_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    machineByTypeAndTier("assembler", tier), 1, item("basic_circuit"), 1,
                    machineByTypeAndTier("inscriber", tier), 1, tierEnergy(tier), 40, 4);
        }
        for (int tier = 3; tier <= 6; tier++) {
            two(output, "assembler/machines/centrifuge_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, item("dense_clay_spindle"), Math.max(tier - 4, 1),
                    machineByTypeAndTier("centrifuge", tier), 1, tierEnergy(tier), 120, 4);
        }
        for (int tier = 4; tier <= 6; tier++) {
            two(output, "assembler/machines/smelter_tier_" + tier, ClayiumMachineIds.ASSEMBLER,
                    hull(tier), 1, item("simple_circuit"), tier - 3,
                    machineByTypeAndTier("smelter", tier), 1, tierEnergy(tier), 120, 4);
        }
        two(output, "assembler/machines/assembler_tier_6", ClayiumMachineIds.ASSEMBLER,
                hull(6), 1, ClayiumRegistries.DENSE_CLAY_GEAR.get(), 4,
                machineByTypeAndTier("assembler", 6), 1, tierEnergy(6), 40, 4);
        two(output, "assembler/machines/assembler_tier_6_upgrade", ClayiumMachineIds.ASSEMBLER,
                machineByTypeAndTier("assembler", 4), 1, item("precision_circuit"), 1,
                machineByTypeAndTier("assembler", 6), 1, tierEnergy(6), 40, 4);
    }

    private static void inscriber(RecipeOutput output) {
        two(output, "inscriber/cee_circuit", ClayiumMachineIds.INSCRIBER,
                item("cee_board"), 1, item("energetic_clay_dust"), 32,
                item("cee_circuit"), 1, 2, 20, 0);
        two(output, "inscriber/clay_circuit", ClayiumMachineIds.INSCRIBER,
                ClayiumRegistries.CLAY_CIRCUIT_BOARD.get(), 1, item("dense_clay_dust"), 6,
                item("clay_circuit"), 1, 2, 20, 0);
        two(output, "inscriber/basic_circuit", ClayiumMachineIds.INSCRIBER,
                ClayiumRegistries.CLAY_CIRCUIT_BOARD.get(), 1, item("energetic_clay_dust"), 32,
                item("basic_circuit"), 1, 2, 20, 0);
        two(output, "inscriber/advanced_circuit", ClayiumMachineIds.INSCRIBER,
                item("impure_silicon_plate"), 1, item("energetic_clay_dust"), 32,
                item("advanced_circuit"), 1, 100, 120, 0);
        two(output, "inscriber/precision_circuit", ClayiumMachineIds.INSCRIBER,
                item("silicon_plate"), 1, item("energetic_clay_dust"), 32,
                item("precision_circuit"), 1, 1_000, 120, 0);
    }

    private static void centrifuge(RecipeOutput output) {
        many(output, "centrifuge/clay_dust", ClayiumMachineIds.CENTRIFUGE,
                List.of(ingredient(item("clay_dust"), 9)),
                List.of(stack(item("dense_clay_dust"), 1)), 4, 20, 0);
        many(output, "centrifuge/dense_clay_dust", ClayiumMachineIds.CENTRIFUGE,
                List.of(ingredient(item("dense_clay_dust"), 2)),
                List.of(stack(item("clay_dust"), 9), stack(item("calcareous_clay_dust"), 1)), 4, 20, 0);
        many(output, "centrifuge/industrial_clay_dust", ClayiumMachineIds.CENTRIFUGE,
                List.of(ingredient(item("industrial_clay_dust"), 2)),
                List.of(
                        stack(item("energetic_clay_dust"), 12),
                        stack(item("clay_dust"), 8),
                        stack(item("dense_clay_dust"), 8),
                        stack(item("industrial_clay_dust"), 1)),
                4, 20, 0);
        many(output, "centrifuge/advanced_industrial_clay_dust", ClayiumMachineIds.CENTRIFUGE,
                List.of(ingredient(item("advanced_industrial_clay_dust"), 2)),
                List.of(
                        stack(item("energetic_clay_dust"), 64),
                        stack(item("clay_dust"), 64),
                        stack(item("dense_clay_dust"), 64),
                        stack(item("industrial_clay_dust"), 12)),
                10_000, 12, 4);
    }

    private static void energeticClayCondenser(RecipeOutput output) {
        String[] levels = {"advanced_industrial_clay", "energetic_clay", "compressed_energetic_clay", "double_compressed_energetic_clay",
                "triple_compressed_energetic_clay", "quadruple_compressed_energetic_clay",
                "quintuple_compressed_energetic_clay", "sextuple_compressed_energetic_clay",
                "septuple_compressed_energetic_clay", "octuple_compressed_energetic_clay"};
        long[] energy = {1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 10_000_000};
        int[] time = {16, 32, 64, 64, 64, 64, 64, 64, 64};
        for (int index = 0; index < levels.length - 1; index++) {
            int minimumTier = index < 3 ? 3 : 4;
            one(output, "energetic_clay_condenser/" + levels[index], ClayiumMachineIds.ENERGETIC_CLAY_CONDENSER,
                    block(levels[index]), 9, block(levels[index + 1]), 1,
                    energy[index], time[index], minimumTier);
        }
    }

    private static ItemLike materialForm(String material, String form) {
        if (material.equals("clay")) {
            return switch (form) {
                case "plate" -> ClayiumRegistries.CLAY_PLATE.get();
                case "stick" -> ClayiumRegistries.CLAY_STICK.get();
                case "short_stick" -> ClayiumRegistries.SHORT_CLAY_STICK.get();
                case "ring" -> ClayiumRegistries.CLAY_RING.get();
                case "small_ring" -> ClayiumRegistries.SMALL_CLAY_RING.get();
                case "gear" -> ClayiumRegistries.CLAY_GEAR.get();
                case "blade" -> ClayiumRegistries.CLAY_BLADE.get();
                case "disc" -> ClayiumRegistries.CLAY_DISC.get();
                case "small_disc" -> ClayiumRegistries.SMALL_CLAY_DISC.get();
                case "cylinder" -> ClayiumRegistries.CLAY_CYLINDER.get();
                case "large_plate" -> ClayiumRegistries.LARGE_CLAY_PLATE.get();
                default -> item("clay_" + form);
            };
        }
        return switch (form) {
            case "plate" -> ClayiumRegistries.DENSE_CLAY_PLATE.get();
            case "stick" -> ClayiumRegistries.DENSE_CLAY_STICK.get();
            case "gear" -> ClayiumRegistries.DENSE_CLAY_GEAR.get();
            default -> item("dense_clay_" + form);
        };
    }

    private static ItemLike item(String id) {
        return ClayiumRegistries.COMPONENT_ITEMS.get(id).get();
    }

    private static ItemLike block(String id) {
        return ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get(id).get();
    }

    private static ItemLike hull(int tier) {
        return switch (tier) {
            case 1 -> ClayiumRegistries.CLAY_MACHINE_HULL.get();
            case 2 -> ClayiumRegistries.MACHINE_HULL_BLOCKS.get("dense_clay_machine_hull").get();
            case 3 -> ClayiumRegistries.MACHINE_HULL_BLOCKS.get("simple_machine_hull").get();
            case 4 -> ClayiumRegistries.MACHINE_HULL_BLOCKS.get("basic_machine_hull").get();
            case 5 -> ClayiumRegistries.MACHINE_HULL_BLOCKS.get("advanced_machine_hull").get();
            case 6 -> ClayiumRegistries.MACHINE_HULL_BLOCKS.get("precision_machine_hull").get();
            case 7 -> ClayiumRegistries.MACHINE_HULL_BLOCKS.get("clay_steel_machine_hull").get();
            default -> throw new IllegalArgumentException("Unsupported machine hull tier " + tier);
        };
    }

    private static ItemLike machineByTypeAndTier(String typeId, int tier) {
        if (typeId.equals("bending_machine") && tier == 1) {
            return ClayiumRegistries.CLAY_BENDING_MACHINE.get();
        }
        return ManufacturingMachineCatalog.ENTRIES.stream()
                .filter(entry -> entry.typeId().equals(typeId)
                        && entry.tier().progressionIndex() == tier)
                .findFirst()
                .map(entry -> ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get(entry.blockId()).get())
                .orElseThrow(() -> new IllegalStateException(
                        "Missing manufacturing machine " + typeId + " tier " + tier));
    }

    private static ItemLike cobblestoneGenerator(int tier) {
        String id = ClayTier.byLegacyIndex(tier).id() + "_cobblestone_generator";
        return ClayiumRegistries.COBBLESTONE_GENERATOR_BLOCKS.get(id).get();
    }

    private static long tierEnergy(int tier) {
        return tier <= 2 ? 1 : (long) Math.pow(10, tier - 2);
    }

    private static MachineIngredient ingredient(ItemLike item, int count) {
        return new MachineIngredient(Ingredient.of(item), count);
    }

    private static ItemStack stack(ItemLike item, int count) {
        return new ItemStack(item, count);
    }

    private static void one(
            RecipeOutput output,
            String id,
            ResourceLocation machine,
            ItemLike input,
            int inputCount,
            ItemLike result,
            int resultCount,
            long energy,
            int time,
            int minimumTier) {
        many(
                output,
                id,
                machine,
                List.of(ingredient(input, inputCount)),
                List.of(stack(result, resultCount)),
                energy,
                time,
                minimumTier);
    }

    private static void two(
            RecipeOutput output,
            String id,
            ResourceLocation machine,
            ItemLike inputA,
            int countA,
            ItemLike inputB,
            int countB,
            ItemLike result,
            int resultCount,
            long energy,
            int time,
            int minimumTier) {
        many(
                output,
                id,
                machine,
                List.of(ingredient(inputA, countA), ingredient(inputB, countB)),
                List.of(stack(result, resultCount)),
                energy,
                time,
                minimumTier);
    }

    private static void many(
            RecipeOutput output,
            String id,
            ResourceLocation machine,
            List<MachineIngredient> ingredients,
            List<ItemStack> results,
            long energy,
            int time,
            int minimumTier) {
        output.accept(
                Clayium.id(id),
                new MachineRecipe(
                        machine,
                        new ArrayList<>(ingredients),
                        new ArrayList<>(results),
                        time,
                        energy,
                        ClayTier.byLegacyIndex(minimumTier)),
                null);
    }
}
