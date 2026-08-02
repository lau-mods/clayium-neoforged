/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.data;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.machine.ManufacturingMachineCatalog;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/** Original shaped recipes for components, circuits, hulls, and machines. */
public final class ComponentCraftingRecipes {
    private ComponentCraftingRecipes() {
    }

    public static void build(RecipeOutput output) {
        originalClayConversions(output);
        materialComponents(output, false);
        materialComponents(output, true);
        circuits(output);
        hulls(output);
        machines(output);
        shaped(output, "clay_wrench", ClayiumRegistries.CLAY_WRENCH.get(),
                "B B", " S ", " | ",
                'B', item("dense_clay_blade"),
                'S', item("dense_clay_spindle"),
                '|', ClayiumRegistries.DENSE_CLAY_STICK.get());
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ClayiumRegistries.CLAY_WATER_WHEEL.get())
                .requires(ClayiumRegistries.CLAY_MACHINE_HULL.get())
                .requires(item("clay_water_wheel_component"))
                .unlockedBy("has_clay_machine_hull", has(ClayiumRegistries.CLAY_MACHINE_HULL.get()))
                .save(output, Clayium.id("clay_water_wheel"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ClayiumRegistries.DENSE_CLAY_WATER_WHEEL.get())
                .requires(hull(2))
                .requires(item("dense_clay_water_wheel_component"))
                .unlockedBy("has_dense_clay_machine_hull", has(hull(2)))
                .save(output, Clayium.id("dense_clay_water_wheel"));
    }

    private static void originalClayConversions(RecipeOutput output) {
        shapeless(output, "small_clay_ring_from_short_stick", ClayiumRegistries.SMALL_CLAY_RING.get(), 1,
                ClayiumRegistries.SHORT_CLAY_STICK.get(), 1);
        shapeless(output, "short_clay_stick_from_small_ring", ClayiumRegistries.SHORT_CLAY_STICK.get(), 1,
                ClayiumRegistries.SMALL_CLAY_RING.get(), 1);
        shapeless(output, "clay_ring_from_cylinder", ClayiumRegistries.CLAY_RING.get(), 1,
                ClayiumRegistries.CLAY_CYLINDER.get(), 1);
        shapeless(output, "clay_pipe_from_plate", item("clay_pipe"), 1,
                ClayiumRegistries.CLAY_PLATE.get(), 1);
        shaped(output, "large_clay_plate", ClayiumRegistries.LARGE_CLAY_PLATE.get(),
                "###", "###", "###", '#', ClayiumRegistries.CLAY_PLATE.get());
        shapeless(output, "clay_balls_from_ring", Items.CLAY_BALL, 3,
                ClayiumRegistries.CLAY_RING.get(), 1);
        shapeless(output, "clay_balls_from_gear", Items.CLAY_BALL, 3,
                ClayiumRegistries.CLAY_GEAR.get(), 1);
        shapeless(output, "clay_balls_from_blade", Items.CLAY_BALL, 3,
                ClayiumRegistries.CLAY_BLADE.get(), 1);
        shapeless(output, "clay_balls_from_needle", Items.CLAY_BALL, 5,
                item("clay_needle"), 1);
    }

    private static void materialComponents(RecipeOutput output, boolean dense) {
        String prefix = dense ? "dense_clay" : "clay";
        ItemLike ball = Items.CLAY_BALL;
        ItemLike shortStick = dense ? item("dense_clay_short_stick") : ClayiumRegistries.SHORT_CLAY_STICK.get();
        ItemLike smallRing = dense ? item("dense_clay_small_ring") : ClayiumRegistries.SMALL_CLAY_RING.get();
        ItemLike ring = dense ? item("dense_clay_ring") : ClayiumRegistries.CLAY_RING.get();
        ItemLike blade = dense ? item("dense_clay_blade") : ClayiumRegistries.CLAY_BLADE.get();
        ItemLike stick = dense ? ClayiumRegistries.DENSE_CLAY_STICK.get() : ClayiumRegistries.CLAY_STICK.get();
        ItemLike plate = dense ? ClayiumRegistries.DENSE_CLAY_PLATE.get() : ClayiumRegistries.CLAY_PLATE.get();
        ItemLike needle = item(prefix + "_needle");
        ItemLike gear = dense ? ClayiumRegistries.DENSE_CLAY_GEAR.get() : ClayiumRegistries.CLAY_GEAR.get();

        shaped(output, prefix + "_gear", gear, "iii", "ioi", "iii",
                'i', shortStick, 'o', smallRing);
        shaped(output, prefix + "_cutting_head", item(prefix + "_cutting_head"), "iii", "ioi", "iii",
                'i', blade, 'o', ring);
        shaped(output, prefix + "_bearing", item(prefix + "_bearing"), "iii", "ioi", "iii",
                'i', ball, 'o', ring);
        shaped(output, prefix + "_spindle", item(prefix + "_spindle"), "0#0", "ioO", "0#0",
                'i', stick, 'o', item(prefix + "_bearing"), 'O', ring, '#', plate, '0', smallRing);
        shaped(output, prefix + "_grinding_head", item(prefix + "_grinding_head"), "iii", "ioi", "iii",
                'i', needle, 'o', ring);
        shaped(output, prefix + "_water_wheel_component", item(prefix + "_water_wheel_component"),
                "###", "#o#", "###", '#', plate, 'o', ring);
    }

    private static void circuits(RecipeOutput output) {
        shaped(output, "clay_circuit", item("clay_circuit"), "-*-", "o#o", "-*-",
                '-', ClayiumRegistries.DENSE_CLAY_STICK.get(),
                '*', ClayiumRegistries.DENSE_CLAY_GEAR.get(),
                'o', item("dense_clay_ring"),
                '#', ClayiumRegistries.CLAY_CIRCUIT_BOARD.get());
        shaped(output, "simple_circuit", item("simple_circuit"), "---", "-#-", "---",
                '-', item("energetic_clay_dust"),
                '#', ClayiumRegistries.CLAY_CIRCUIT_BOARD.get());
    }

    private static void hulls(RecipeOutput output) {
        shaped(output, "dense_clay_machine_hull", hull(2), "###", "#C#", "###",
                '#', item("dense_clay_large_plate"), 'C', item("clay_circuit"));
        shaped(output, "simple_machine_hull", hull(3), "###", "#C#", "###",
                '#', item("industrial_clay_large_plate"), 'C', item("simple_circuit"));
        shaped(output, "basic_machine_hull", hull(4), "#E#", "#C#", "###",
                '#', item("advanced_industrial_clay_large_plate"),
                'C', item("basic_circuit"),
                'E', item("clay_energy_excitor"));
        shaped(output, "advanced_machine_hull", hull(5), "#E#", "*C*", "#*#",
                '#', material("impure_silicon_large_plate"),
                '*', material("silicone_large_plate"),
                'C', item("advanced_circuit"),
                'E', item("clay_energy_excitor"));
        shaped(output, "precision_machine_hull", hull(6), "#E#", "#C#", "###",
                '#', material("aluminium_large_plate"),
                'C', item("precision_circuit"),
                'E', item("clay_energy_excitor"));
        shaped(output, "clay_steel_machine_hull", hull(7), "#E#", "#C#", "###",
                '#', material("clay_steel_large_plate"),
                'C', item("integrated_circuit"),
                'E', item("clay_energy_excitor"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, material("clay_steel_ingot"))
                .requires(ClayiumRegistries.COMPRESSED_CLAY.get(), 2)
                .requires(Items.IRON_INGOT)
                .unlockedBy("has_compressed_clay", has(ClayiumRegistries.COMPRESSED_CLAY.get()))
                .save(output, Clayium.id("clay_steel_ingot"));
        shaped(output, "clay_steel_block", ClayiumRegistries.MATERIAL_BLOCKS.get("clay_steel_block").get(),
                "###", "###", "###", '#', material("clay_steel_ingot"));
        shaped(output, "clay_steel_pickaxe", ClayiumRegistries.CLAY_STEEL_PICKAXE.get(),
                "###", " | ", " | ", '#', material("clay_steel_ingot"),
                '|', ClayiumRegistries.DENSE_CLAY_STICK.get());
        shaped(output, "clay_steel_shovel", ClayiumRegistries.CLAY_STEEL_SHOVEL.get(),
                " # ", " | ", " | ", '#', material("clay_steel_ingot"),
                '|', ClayiumRegistries.DENSE_CLAY_STICK.get());
        shapeless(output, "clay_steel_ingots_from_block", material("clay_steel_ingot"), 9,
                ClayiumRegistries.MATERIAL_BLOCKS.get("clay_steel_block").get(), 1);
    }

    private static void machines(RecipeOutput output) {
        for (int tier = 1; tier <= 4; tier++) {
            String material = switch (tier) {
                case 1 -> "clay";
                case 2 -> "dense_clay";
                case 3 -> "industrial_clay";
                default -> "advanced_industrial_clay";
            };
            // Original CRecipes uses clay forms for tier 1 and dense-clay mechanism
            // parts for tiers 2-4; the hull material alone advances with the tier.
            ItemLike gear = tier == 1 ? ClayiumRegistries.CLAY_GEAR.get() : ClayiumRegistries.DENSE_CLAY_GEAR.get();
            ItemLike pipe = tier == 1 ? item("clay_pipe") : item("dense_clay_pipe");
            shaped(output, material + "_cobblestone_generator",
                    ClayiumRegistries.COBBLESTONE_GENERATOR_BLOCKS.get(
                            ClayTier.byLegacyIndex(tier).id() + "_cobblestone_generator").get(),
                    " * ", "=#=", " * ", '#', hull(tier), '*', gear, '=', pipe);
        }
        shaped(output, "clay_bending_machine", ClayiumRegistries.CLAY_BENDING_MACHINE.get(),
                "o-*", "P#P", "o-*",
                '#', ClayiumRegistries.CLAY_MACHINE_HULL.get(),
                'o', item("clay_spindle"),
                '-', ClayiumRegistries.CLAY_CYLINDER.get(),
                '*', ClayiumRegistries.CLAY_GEAR.get(),
                'P', ClayiumRegistries.CLAY_PLATE.get());
        shaped(output, "elemental_milling_machine", ClayiumRegistries.ELEMENTAL_MILLING_MACHINE.get(),
                "P0P", "o#o", "P*P",
                '#', ClayiumRegistries.CLAY_MACHINE_HULL.get(),
                'o', item("dense_clay_spindle"),
                '*', ClayiumRegistries.DENSE_CLAY_GEAR.get(),
                'P', ClayiumRegistries.DENSE_CLAY_PLATE.get(),
                '0', item("dense_clay_cutting_head"));
        for (ManufacturingMachineCatalog.Entry entry : ManufacturingMachineCatalog.ENTRIES) {
            int tier = entry.tier().progressionIndex();
            if (tier > 4) {
                continue;
            }
            ItemLike machine = ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get(entry.blockId()).get();
            ItemLike machineHull = hull(tier);
            boolean denseComponents = tier >= 2;
            String material = denseComponents ? "dense_clay" : "clay";
            ItemLike plate = denseComponents
                    ? ClayiumRegistries.DENSE_CLAY_PLATE.get()
                    : ClayiumRegistries.CLAY_PLATE.get();
            ItemLike gear = denseComponents
                    ? ClayiumRegistries.DENSE_CLAY_GEAR.get()
                    : ClayiumRegistries.CLAY_GEAR.get();
            ItemLike spindle = item(material + "_spindle");
            ItemLike cylinder = denseComponents ? item("dense_clay_cylinder") : ClayiumRegistries.CLAY_CYLINDER.get();
            ItemLike pipe = item(material + "_pipe");
            ItemLike circuit = switch (tier) {
                case 3 -> item("simple_circuit");
                case 4 -> item("basic_circuit");
                default -> item("clay_circuit");
            };

            switch (entry.typeId()) {
                case "bending_machine" -> shaped(output, entry.blockId(), machine, "o-*", "P#P", "o-*",
                        '#', machineHull, 'o', spindle, '-', cylinder, '*', gear, 'P', plate);
                case "wire_drawing_machine" -> shaped(output, entry.blockId(), machine, "*o*", "=#=", "*o*",
                        '#', machineHull, 'o', spindle, '*', gear, '=', pipe);
                case "pipe_drawing_machine" -> shaped(output, entry.blockId(), machine, "*o*", "-#=", "*o*",
                        '#', machineHull, 'o', spindle, '-', cylinder, '*', gear, '=', pipe);
                case "cutting_machine" -> shaped(output, entry.blockId(), machine, "P*P", "o#|", "P*P",
                        '#', machineHull, 'o', spindle, '*', gear, 'P', plate,
                        '|', item(material + "_cutting_head"));
                case "lathe" -> shaped(output, entry.blockId(), machine, "P*P", "-#o", "P*P",
                        '#', machineHull, 'o', spindle, '*', gear, 'P', plate, '-', stick(material));
                case "condenser" -> shaped(output, entry.blockId(), machine, "*P*", "P#P", "*P*",
                        '#', machineHull, '*', gear, 'P', plate);
                case "grinder" -> shaped(output, entry.blockId(), machine, "P0P", "o#o", "P*P",
                        '#', machineHull, 'o', spindle, '*', gear, 'P', plate,
                        '0', item(material + "_grinding_head"));
                case "decomposer" -> shaped(output, entry.blockId(), machine, "*o*", "C#C", "*=*",
                        '#', machineHull, 'o', spindle, '*', gear, '=', pipe, 'C', circuit);
                case "assembler" -> shaped(output, entry.blockId(), machine, "*C*", "o#o", "*C*",
                        '#', machineHull, 'o', spindle, '*', gear, 'C', circuit);
                case "inscriber" -> shaped(output, entry.blockId(), machine, "*o*", "C#C", "*C*",
                        '#', machineHull, 'o', spindle, '*', gear, 'C', circuit);
                case "centrifuge" -> shaped(output, entry.blockId(), machine, "*o*", "o#o", "*o*",
                        '#', machineHull, 'o', spindle, '*', gear);
                case "milling_machine" -> shaped(output, entry.blockId(), machine, "P0P", "o#o", "P*P",
                        '#', machineHull, 'o', spindle, '*', gear, 'P', plate,
                        '0', item(material + "_cutting_head"));
                case "smelter" -> {
                    // In the original this machine is assembled in the Assembler, not a crafting grid.
                }
                case "energetic_clay_condenser" -> shaped(output, entry.blockId(), machine,
                        "P*P", "E#E", "PCP",
                        '#', machineHull,
                        'P', tier == 3 ? item("industrial_clay_plate") : item("advanced_industrial_clay_plate"),
                        '*', ClayiumRegistries.DENSE_CLAY_GEAR.get(),
                        'C', circuit,
                        'E', item("clay_energy_excitor"));
                default -> throw new IllegalStateException("Unhandled manufacturing machine type: " + entry.typeId());
            }
        }
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
            default -> throw new IllegalArgumentException("No crafting-grid hull for tier " + tier);
        };
    }

    private static ItemLike stick(String material) {
        return material.equals("clay")
                ? ClayiumRegistries.CLAY_STICK.get()
                : ClayiumRegistries.DENSE_CLAY_STICK.get();
    }

    private static ItemLike item(String id) {
        return ClayiumRegistries.COMPONENT_ITEMS.get(id).get();
    }

    private static ItemLike material(String id) {
        return ClayiumRegistries.MATERIAL_ITEMS.get(id).get();
    }

    private static net.minecraft.advancements.Criterion<net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance>
            has(ItemLike item) {
        return net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    private static void shaped(
            RecipeOutput output,
            String id,
            ItemLike result,
            String row1,
            String row2,
            String row3,
            Object... definitions) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
                .pattern(row1)
                .pattern(row2)
                .pattern(row3);
        for (int index = 0; index < definitions.length; index += 2) {
            builder.define((Character) definitions[index], (ItemLike) definitions[index + 1]);
        }
        builder.unlockedBy("has_component", has((ItemLike) definitions[1]))
                .save(output, Clayium.id(id));
    }

    private static void shapeless(
            RecipeOutput output,
            String id,
            ItemLike result,
            int resultCount,
            ItemLike input,
            int inputCount) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result, resultCount)
                .requires(input, inputCount)
                .unlockedBy("has_input", has(input))
                .save(output, Clayium.id(id));
    }
}
