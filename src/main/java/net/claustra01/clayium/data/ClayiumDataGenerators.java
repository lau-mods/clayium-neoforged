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
import net.claustra01.clayium.machine.ManufacturingMachineCatalog;
import net.claustra01.clayium.machine.SpecializedMachineCatalog;
import net.claustra01.clayium.machine.MaterialCatalog;
import net.claustra01.clayium.machine.ClayComponentCatalog;
import net.claustra01.clayium.logistics.LogisticsCatalog;
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
import net.minecraft.world.item.DyeColor;
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
                add("itemGroup." + Clayium.MODID, "Clayium Neoforged");
                add("block." + Clayium.MODID + ".clay_work_table", "Clay Work Table");
                add("block." + Clayium.MODID + ".clay_crafting_table", "Clay Crafting Table");
                addBlockNames(this);
                addItemNames(this);
                add("container." + Clayium.MODID + ".clay_work_table", "Clay Work Table");
                add("container." + Clayium.MODID + ".clay_crafting_table", "Clay Crafting Table");
                add("container." + Clayium.MODID + ".machine", "Clayium Machine");
                add("gui." + Clayium.MODID + ".energy", "%s");
                add("gui." + Clayium.MODID + ".tier", "Tier %s");
                add("message." + Clayium.MODID + ".water_wheel_status",
                        "Flowing water: %s, generation: %s/s");
                add("message." + Clayium.MODID + ".insert_route",
                        "%s insertion route: %s (-1 = disabled)");
                add("message." + Clayium.MODID + ".extract_route",
                        "%s extraction route: %s (-1 = disabled)");
                add("message." + Clayium.MODID + ".pipe_mode", "Pipe shape: %s");
                add("message." + Clayium.MODID + ".io_saved", "I/O configuration saved");
                add("message." + Clayium.MODID + ".io_loaded", "I/O configuration loaded");
                add("message." + Clayium.MODID + ".filter_applied", "Applied %s");
                add("message." + Clayium.MODID + ".filter_copied", "Copied %s");
                add("message." + Clayium.MODID + ".filter_removed", "Removed %s");
                add("message." + Clayium.MODID + ".filter_copy_cleared", "Cleared the copy flag");
                add("message." + Clayium.MODID + ".filter_broken", "The filter has broken! The filter size is too large!");
                add("tooltip." + Clayium.MODID + ".filter_copy", "Copy");
                add("jei." + Clayium.MODID + ".category.clay_work_table", "Clay Work Table");
                add("jei." + Clayium.MODID + ".category.clay_bending_machine", "Clay Bending Machine");
                add("jei." + Clayium.MODID + ".category.elemental_milling_machine", "Elemental Milling Machine");
                for (String path : java.util.List.of(
                        "wire_drawing_machine", "pipe_drawing_machine", "cutting_machine", "lathe",
                        "condenser", "grinder", "decomposer", "assembler", "inscriber",
                        "centrifuge", "smelter")) {
                    add("jei." + Clayium.MODID + ".category." + path, titleCase(path));
                }
                add("jei." + Clayium.MODID + ".category.energetic_clay_condenser", "Energetic Clay Condenser");
                add("jei." + Clayium.MODID + ".category.chemical_reactor", "Chemical Reactor");
                add("jei." + Clayium.MODID + ".category.electrolysis_reactor", "Electrolysis Reactor");
                add("jei." + Clayium.MODID + ".category.alloy_smelter", "Alloy Smelter");
                add("jei." + Clayium.MODID + ".category.solar_clay_fabricator", "Solar Clay Fabricator");
                add("jei." + Clayium.MODID + ".category.matter_transformer", "Matter Transformer");
                add("jei." + Clayium.MODID + ".category.clay_reactor", "Clay Reactor");
                add("jei." + Clayium.MODID + ".category.clay_fabricator", "Clay Fabricator");
                add("jei." + Clayium.MODID + ".category.clay_blast_furnace", "Clay Blast Furnace");
                add("jei." + Clayium.MODID + ".category.ca_injector", "CA Injector");
                add("jei." + Clayium.MODID + ".category.ca_condenser", "CA Condenser");
                add("jei." + Clayium.MODID + ".category.ca_reactor", "CA Reactor");
                add("jei." + Clayium.MODID + ".category.energetic_clay_decomposer", "Energetic Clay Decomposer");
                add("jei." + Clayium.MODID + ".category.quartz_crucible", "Quartz Crucible");
                add("jei." + Clayium.MODID + ".category.chemical_metal_separator", "Chemical Metal Separator");
                add("jei." + Clayium.MODID + ".chance", "Chance: %s%%");
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
                String[] redstoneModes = {"None", "Emit if idle", "Emit if work scheduled", "Emit if doing work",
                        "Do work", "Do not work", "Start work", "Stop work", "Do work once"};
                for (int mode = 0; mode < redstoneModes.length; mode++) {
                    add("gui." + Clayium.MODID + ".redstone_interface.mode." + mode, redstoneModes[mode]);
                }
                add(Clayium.MODID + ".config.log_registry_summary", "Log registry summary");
                add(Clayium.MODID + ".config.ce_sync_interval_ticks", "Clay Energy sync interval");
                add("gui." + Clayium.MODID + ".resonance", "Resonance: %s");
                add("jei." + Clayium.MODID + ".ca_reactor_condition", "Requires a valid CA Reactor ring and hull");
                add("message." + Clayium.MODID + ".pan_core_status", "PAN network: %s nodes, %s conversions");
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
        language.add("block." + Clayium.MODID + ".deepslate_clay_ore", "Deepslate Clay Ore");
        language.add("block." + Clayium.MODID + ".dense_clay_ore", "Dense Clay Ore");
        language.add("block." + Clayium.MODID + ".deepslate_dense_clay_ore", "Deepslate Dense Clay Ore");
        language.add("block." + Clayium.MODID + ".large_dense_clay_ore", "Large Dense Clay Ore");
        language.add("block." + Clayium.MODID + ".deepslate_large_dense_clay_ore", "Deepslate Large Dense Clay Ore");
        language.add("block." + Clayium.MODID + ".dense_clay", "Dense Clay");
        language.add("block." + Clayium.MODID + ".compressed_clay", "Compressed Clay");
        language.add("block." + Clayium.MODID + ".raw_clay_machine_hull", "Raw Clay Machine Hull");
        language.add("block." + Clayium.MODID + ".clay_machine_hull", "Clay Machine Hull");
        language.add("block." + Clayium.MODID + ".clay_bending_machine", "Clay Bending Machine");
        language.add("block." + Clayium.MODID + ".elemental_milling_machine", "Elemental Milling Machine");
        language.add("block." + Clayium.MODID + ".clay_water_wheel", "Clay Water Wheel");
        language.add("block." + Clayium.MODID + ".dense_clay_water_wheel", "Dense Clay Water Wheel");
        language.add("block." + Clayium.MODID + ".quartz_crucible", "Quartz Crucible");
        ClayiumRegistries.CLAY_ENERGY_LASER_BLOCKS.forEach((id, block) ->
                language.add("block." + Clayium.MODID + "." + id, titleCase(id)));
        ClayiumRegistries.CLAY_LASER_INTERFACE_BLOCKS.forEach((id, block) ->
                language.add("block." + Clayium.MODID + "." + id, titleCase(id)));
        ClayiumRegistries.CLAY_ENERGY_CONVERTER_BLOCKS.forEach((id, block) ->
                language.add("block." + Clayium.MODID + "." + id, titleCase(id)));
        ClayiumRegistries.MACHINE_MODIFIER_BLOCKS.forEach((id, block) ->
                language.add("block." + Clayium.MODID + "." + id, titleCase(id)));
        language.add("block." + Clayium.MODID + ".laser_reflector", "Laser Reflector");
        language.add("block." + Clayium.MODID + ".clay_reactor", "Clay Reactor");
        language.add("block." + Clayium.MODID + ".clay_steel_auto_clay_condenser", "Clay Steel Auto Clay Condenser");
        for (ClayTier tier : new ClayTier[]{ClayTier.CLAY, ClayTier.DENSE_CLAY, ClayTier.SIMPLE,
                ClayTier.BASIC, ClayTier.ADVANCED, ClayTier.PRECISION, ClayTier.CLAY_STEEL}) {
            language.add("block." + Clayium.MODID + "." + tier.id() + "_cobblestone_generator",
                    tier.displayName() + " Cobblestone Generator");
        }
        for (ManufacturingMachineCatalog.Entry entry : ManufacturingMachineCatalog.ENTRIES) {
            language.add(
                    "block." + Clayium.MODID + "." + entry.blockId(),
                    entry.tier().displayName() + " " + entry.displayTypeName());
        }
        for (SpecializedMachineCatalog.Entry entry : SpecializedMachineCatalog.ENTRIES) {
            language.add("block." + Clayium.MODID + "." + entry.blockId(),
                    entry.tier().displayName() + " " + entry.displayTypeName());
        }
        language.add("block." + Clayium.MODID + ".advanced_clay_interface", "Advanced Clay Interface");
        language.add("block." + Clayium.MODID + ".precision_clay_interface", "Precision Clay Interface");
        language.add("block." + Clayium.MODID + ".advanced_redstone_interface", "Advanced Redstone Interface");
        language.add("block." + Clayium.MODID + ".precision_redstone_interface", "Precision Redstone Interface");
        language.add("block." + Clayium.MODID + ".clay_steel_clay_interface", "Clay Steel Clay Interface");
        language.add("block." + Clayium.MODID + ".clay_steel_redstone_interface", "Clay Steel Redstone Interface");
        language.add("block." + Clayium.MODID + ".clayium_clay_interface", "Clayium Clay Interface");
        language.add("block." + Clayium.MODID + ".ultimate_clay_interface", "Ultimate Clay Interface");
        language.add("block." + Clayium.MODID + ".clayium_redstone_interface", "Clayium Redstone Interface");
        language.add("block." + Clayium.MODID + ".ultimate_redstone_interface", "Ultimate Redstone Interface");
        language.add("block." + Clayium.MODID + ".clay_blast_furnace", "Clay Blast Furnace");
        language.add("block." + Clayium.MODID + ".impure_silicon_block", "Block of Impure Silicon");
        language.add("block." + Clayium.MODID + ".silicone_block", "Block of Silicone");
        language.add("block." + Clayium.MODID + ".silicon_block", "Block of Silicon");
        language.add("block." + Clayium.MODID + ".aluminium_block", "Block of Aluminium");
        language.add("block." + Clayium.MODID + ".clay_steel_block", "Block of Clay Steel");
        language.add("block." + Clayium.MODID + ".clayium_block", "Block of Clayium");
        language.add("block." + Clayium.MODID + ".ultimate_alloy_block", "Block of Ultimate Alloy");
        language.add("block." + Clayium.MODID + ".az91d_machine_hull", "AZ91D Machine Hull");
        language.add("block." + Clayium.MODID + ".zk60a_machine_hull", "ZK60A Machine Hull");
        for (DyeColor color : DyeColor.values()) {
            language.add("block." + Clayium.MODID + "." + color.getSerializedName() + "_silicone_block",
                    titleCase(color.getSerializedName()) + " Silicone Block");
        }
        language.add("block." + Clayium.MODID + ".basic_fluid_buffer", "Basic Fluid Buffer");
        language.add("block." + Clayium.MODID + ".advanced_fluid_buffer", "Advanced Fluid Buffer");
        language.add("block." + Clayium.MODID + ".precision_fluid_buffer", "Precision Fluid Buffer");
        language.add("block." + Clayium.MODID + ".clay_steel_fluid_buffer", "Clay Steel Fluid Buffer");
        language.add("block." + Clayium.MODID + ".basic_salt_extractor", "Basic Salt Extractor");
        language.add("block." + Clayium.MODID + ".advanced_salt_extractor", "Advanced Salt Extractor");
        language.add("block." + Clayium.MODID + ".precision_salt_extractor", "Precision Salt Extractor");
        language.add("block." + Clayium.MODID + ".clay_steel_salt_extractor", "Clay Steel Salt Extractor");
        language.add("block." + Clayium.MODID + ".advanced_auto_clay_condenser", "Advanced Auto Clay Condenser");
        language.add("block." + Clayium.MODID + ".advanced_auto_crafter", "Advanced Auto Crafter");
        language.add("block." + Clayium.MODID + ".precision_auto_crafter", "Precision Auto Crafter");
        language.add("block." + Clayium.MODID + ".clay_steel_auto_crafter", "Clay Steel Auto Crafter");
        language.add("block." + Clayium.MODID + ".clayium_auto_crafter", "Clayium Auto Crafter");
        language.add("block." + Clayium.MODID + ".ultimate_auto_crafter", "Ultimate Auto Crafter");
        language.add("block." + Clayium.MODID + ".precision_chemical_metal_separator", "Precision Chemical Metal Separator");
        for (LogisticsCatalog.Entry entry : LogisticsCatalog.ENTRIES) {
            language.add(
                    "block." + Clayium.MODID + "." + entry.blockId(),
                    entry.blockId().equals("storage_container") || entry.blockId().equals("void_container")
                            ? entry.displayTypeName()
                            : entry.tier().displayName() + " " + entry.displayTypeName());
        }
        addBlockName(language, "industrial_clay", "Industrial Clay");
        addBlockName(language, "advanced_industrial_clay", "Advanced Industrial Clay");
        addBlockName(language, "energetic_clay", "Energetic Clay");
        addBlockName(language, "compressed_energetic_clay", "Compressed Energetic Clay");
        addBlockName(language, "double_compressed_energetic_clay", "Double Compressed Energetic Clay");
        addBlockName(language, "triple_compressed_energetic_clay", "Triple Compressed Energetic Clay");
        addBlockName(language, "quadruple_compressed_energetic_clay", "Quadruple Compressed Energetic Clay");
        addBlockName(language, "quintuple_compressed_energetic_clay", "Quintuple Compressed Energetic Clay");
        addBlockName(language, "sextuple_compressed_energetic_clay", "Sextuple Compressed Energetic Clay");
        addBlockName(language, "septuple_compressed_energetic_clay", "Septuple Compressed Energetic Clay");
        addBlockName(language, "octuple_compressed_energetic_clay", "Octuple Compressed Energetic Clay");
        for (String id : ClayiumRegistries.MACHINE_HULL_BLOCKS.keySet()) {
            language.add(
                    "block." + Clayium.MODID + "." + id,
                    titleCase(id.replace("_machine_hull", "")) + " Machine Hull");
        }
        ClayiumRegistries.RESONATOR_BLOCKS.keySet().forEach(id -> addBlockName(language, id, titleCase(id)));
        ClayiumRegistries.CA_REACTOR_COIL_BLOCKS.keySet().forEach(id -> addBlockName(language, id, titleCase(id)));
        ClayiumRegistries.CA_REACTOR_HULL_BLOCKS.keySet().forEach(id -> addBlockName(language, id, titleCase(id)));
        ClayiumRegistries.CA_REACTOR_CORE_BLOCKS.keySet().forEach(id -> addBlockName(language, id, titleCase(id)));
        ClayiumRegistries.PAN_ADAPTER_BLOCKS.keySet().forEach(id -> addBlockName(language, id, titleCase(id)));
        ClayiumRegistries.PAN_DUPLICATOR_BLOCKS.keySet().forEach(id -> addBlockName(language, id, titleCase(id)));
        for (ClayTier tier : new ClayTier[]{ClayTier.ANTIMATTER, ClayTier.PURE_ANTIMATTER, ClayTier.OEC, ClayTier.OPA}) {
            addBlockName(language, tier.id() + "_clay_interface", tier.displayName() + " Clay Interface");
            addBlockName(language, tier.id() + "_redstone_interface", tier.displayName() + " Redstone Interface");
        }
        for (ClayTier tier : new ClayTier[]{ClayTier.CLAYIUM, ClayTier.ULTIMATE, ClayTier.ANTIMATTER,
                ClayTier.PURE_ANTIMATTER, ClayTier.OEC, ClayTier.OPA}) {
            addBlockName(language, tier.id() + "_fluid_buffer", tier.displayName() + " Fluid Buffer");
        }
        addBlockName(language, "resonating_collector", "Resonating Collector");
        addBlockName(language, "pan_cable", "PAN Cable");
        addBlockName(language, "pan_core", "PAN Core");
        addBlockName(language, "antimatter_block", "Block of Antimatter");
        addBlockName(language, "pure_antimatter_block", "Block of Pure Antimatter");
        addBlockName(language, "opa_block", "Block of Octuple Compressed Pure Antimatter");
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
        language.add("item." + Clayium.MODID + ".clay_wrench", "Clay Wrench");
        language.add("item." + Clayium.MODID + ".clay_shovel", "Clay Shovel");
        language.add("item." + Clayium.MODID + ".clay_shovel.tooltip",
                "Can dig Clay Ores and increase drops.");
        language.add("item." + Clayium.MODID + ".clay_pickaxe", "Clay Pickaxe");
        language.add("item." + Clayium.MODID + ".clay_pickaxe.tooltip",
                "Can harvest Clay Ores so fast and increase drops.");
        language.add("item." + Clayium.MODID + ".clay_steel_pickaxe", "Clay Steel Pickaxe");
        language.add("item." + Clayium.MODID + ".clay_steel_shovel", "Clay Steel Shovel");
        language.add("item." + Clayium.MODID + ".clay_steel_tool.mode.area", "Mining area: %s x %s");
        language.add("item." + Clayium.MODID + ".clay_steel_tool.mode.custom", "Custom mining shape: %s blocks");
        language.add("item." + Clayium.MODID + ".clay_steel_tool.area", "Mining area: %s x %s");
        language.add("item." + Clayium.MODID + ".clay_steel_tool.customized", "Customized shape: %s blocks");
        for (ClayComponentCatalog.Entry entry : ClayComponentCatalog.ENTRIES) {
            language.add("item." + Clayium.MODID + "." + entry.id(), entry.displayName());
        }
        for (MaterialCatalog.Entry entry : MaterialCatalog.ENTRIES) {
            language.add("item." + Clayium.MODID + "." + entry.id(), entry.displayName());
        }
        language.add("item." + Clayium.MODID + ".clay_io_tool", "Clay IO Configurator");
        language.add("item." + Clayium.MODID + ".clay_piping_tool", "Clay Piping Configurator");
        language.add("item." + Clayium.MODID + ".io_memory_card", "Memory Card");
        language.add("item." + Clayium.MODID + ".filter_duplicator", "Item Filter Duplicator");
        language.add("item." + Clayium.MODID + ".filter_whitelist", "White List Item Filter");
        language.add("item." + Clayium.MODID + ".filter_blacklist", "Black List Item Filter");
        language.add("item." + Clayium.MODID + ".filter_fuzzy", "Fuzzy Item Filter");
        language.add("item." + Clayium.MODID + ".filter_item_tag", "Item Tag Filter");
        language.add("item." + Clayium.MODID + ".filter_item_name", "Display Name Item Filter");
        language.add("item." + Clayium.MODID + ".filter_translation_key", "Translation Key Item Filter");
        language.add("item." + Clayium.MODID + ".filter_unique_id", "Unique ID Item Filter");
        language.add("item." + Clayium.MODID + ".filter_mod_id", "MOD ID Item Filter");
        language.add("item." + Clayium.MODID + ".filter_item_damage", "Damage Value Item Filter");
        language.add("item." + Clayium.MODID + ".filter_block_state", "Block State Item Filter");
        language.add("item." + Clayium.MODID + ".filter_block_harvestable", "Harvestable Crop Filter");
        language.add("item." + Clayium.MODID + ".filter_copy_name", "%s (Copy)");
    }

    private static String titleCase(String id) {
        return java.util.Arrays.stream(id.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private static void addBlockName(LanguageProvider language, String id, String name) {
        language.add("block." + Clayium.MODID + "." + id, name);
    }

    private static void buildWorkTableRecipes(RecipeOutput output) {
        work(output, "clay_ball_to_clay_stick", Items.CLAY_BALL, 1,
                ClayiumRegistries.CLAY_STICK.get(), 1, ClayWorkTableOperation.FORM, 4);
        work(output, "large_clay_ball_to_clay_cylinder", ClayiumRegistries.LARGE_CLAY_BALL.get(), 1,
                ClayiumRegistries.CLAY_CYLINDER.get(), 1, ClayWorkTableOperation.FORM, 4);
        work(output, "large_clay_ball_to_clay_disc", ClayiumRegistries.LARGE_CLAY_BALL.get(), 1,
                ClayiumRegistries.CLAY_DISC.get(), 1, ClayWorkTableOperation.CUT, 30);
        work(output, "large_clay_ball_to_clay_disc_with_rolling_pin",
                ClayiumRegistries.LARGE_CLAY_BALL.get(), 1,
                ClayiumRegistries.CLAY_DISC.get(), 1, Items.CLAY_BALL, 2,
                ClayWorkTableOperation.ROLL, 4);
        work(output, "clay_disc_to_raw_clay_slicer", ClayiumRegistries.CLAY_DISC.get(), 1,
                ClayiumRegistries.RAW_CLAY_SLICER.get(), 1, ClayWorkTableOperation.CUT, 15);
        work(output, "clay_disc_to_raw_clay_slicer_with_rolling_pin",
                ClayiumRegistries.CLAY_DISC.get(), 1,
                ClayiumRegistries.RAW_CLAY_SLICER.get(), 1, null, 0,
                ClayWorkTableOperation.ROLL, 2);
        work(output, "clay_disc_to_clay_plate", ClayiumRegistries.CLAY_DISC.get(), 1,
                ClayiumRegistries.CLAY_PLATE.get(), 1, Items.CLAY_BALL, 2,
                ClayWorkTableOperation.SLICE, 4);
        work(output, "clay_plate_to_clay_blade", ClayiumRegistries.CLAY_PLATE.get(), 1,
                ClayiumRegistries.CLAY_BLADE.get(), 1, ClayWorkTableOperation.CUT, 10);
        work(output, "clay_plate_to_clay_blade_with_rolling_pin",
                ClayiumRegistries.CLAY_PLATE.get(), 1,
                ClayiumRegistries.CLAY_BLADE.get(), 1, Items.CLAY_BALL, 2,
                ClayWorkTableOperation.ROLL, 1);
        work(output, "clay_plate_to_clay_sticks", ClayiumRegistries.CLAY_PLATE.get(), 1,
                ClayiumRegistries.CLAY_STICK.get(), 4, ClayWorkTableOperation.DIVIDE, 3);
        work(output, "clay_disc_to_clay_ring", ClayiumRegistries.CLAY_DISC.get(), 1,
                ClayiumRegistries.CLAY_RING.get(), 1, ClayiumRegistries.SMALL_CLAY_DISC.get(), 1,
                ClayWorkTableOperation.PUNCH, 2);
        work(output, "clay_cylinder_to_clay_needle", ClayiumRegistries.CLAY_CYLINDER.get(), 1,
                ClayiumRegistries.COMPONENT_ITEMS.get("clay_needle").get(), 1,
                ClayWorkTableOperation.FORM, 3);
        work(output, "clay_cylinder_to_small_clay_discs", ClayiumRegistries.CLAY_CYLINDER.get(), 1,
                ClayiumRegistries.SMALL_CLAY_DISC.get(), 8, ClayWorkTableOperation.DIVIDE, 7);
        work(output, "small_clay_disc_to_small_clay_ring", ClayiumRegistries.SMALL_CLAY_DISC.get(), 1,
                ClayiumRegistries.SMALL_CLAY_RING.get(), 1, ClayiumRegistries.SHORT_CLAY_STICK.get(), 1,
                ClayWorkTableOperation.PUNCH, 1);
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

    private static void work(
            RecipeOutput output,
            String id,
            ItemLike input,
            int inputCount,
            ItemLike result,
            int resultCount,
            ItemLike remainder,
            int remainderCount,
            ClayWorkTableOperation operation,
            int actions) {
        output.accept(
                Clayium.id("clay_work_table/" + id),
                new ClayWorkTableRecipe(
                        Ingredient.of(input),
                        inputCount,
                        new ItemStack(result, resultCount),
                        remainder == null || remainderCount <= 0
                                ? ItemStack.EMPTY
                                : new ItemStack(remainder, remainderCount),
                        operation,
                        actions,
                        ClayTier.RAW),
                null);
    }

    private static void buildCraftingRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ClayiumRegistries.CLAY_CRAFTING_TABLE.get())
                .define('D', ClayiumRegistries.DENSE_CLAY.get())
                .pattern("DDD")
                .unlockedBy("has_dense_clay", has(ClayiumRegistries.DENSE_CLAY.get()))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ClayiumRegistries.DENSE_CLAY.get(), 3)
                .requires(ClayiumRegistries.CLAY_CRAFTING_TABLE.get())
                .unlockedBy("has_clay_crafting_table", has(ClayiumRegistries.CLAY_CRAFTING_TABLE.get()))
                .save(output, Clayium.id("clay_crafting_table_unpack"));
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
        smelt(output, ClayiumRegistries.RAW_CLAY_ROLLING_PIN.get(), ClayiumRegistries.CLAY_ROLLING_PIN.get());
        smelt(output, ClayiumRegistries.RAW_CLAY_SLICER.get(), ClayiumRegistries.CLAY_SLICER.get());
        smelt(output, ClayiumRegistries.RAW_CLAY_SPATULA.get(), ClayiumRegistries.CLAY_SPATULA.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ClayiumRegistries.RAW_CLAY_MACHINE_HULL.get())
                .define('P', ClayiumRegistries.LARGE_CLAY_PLATE.get())
                .define('G', ClayiumRegistries.CLAY_GEAR.get())
                .pattern("PPP").pattern("PGP").pattern("PPP")
                .unlockedBy("has_large_clay_plate", has(ClayiumRegistries.LARGE_CLAY_PLATE.get()))
                .save(output);
        smelt(output, ClayiumRegistries.RAW_CLAY_MACHINE_HULL.get(), ClayiumRegistries.CLAY_MACHINE_HULL.get());

        filterConversion(output, ClayiumRegistries.FILTER_BLACKLIST.get(), ClayiumRegistries.FILTER_WHITELIST.get());
        filterConversion(output, ClayiumRegistries.FILTER_WHITELIST.get(), ClayiumRegistries.FILTER_BLACKLIST.get());
        filterConversion(output, ClayiumRegistries.FILTER_ITEM_NAME.get(), ClayiumRegistries.FILTER_TRANSLATION_KEY.get());
        filterConversion(output, ClayiumRegistries.FILTER_TRANSLATION_KEY.get(), ClayiumRegistries.FILTER_ITEM_TAG.get());
        filterConversion(output, ClayiumRegistries.FILTER_ITEM_TAG.get(), ClayiumRegistries.FILTER_UNIQUE_ID.get());
        filterConversion(output, ClayiumRegistries.FILTER_UNIQUE_ID.get(), ClayiumRegistries.FILTER_MOD_ID.get());
        filterConversion(output, ClayiumRegistries.FILTER_MOD_ID.get(), ClayiumRegistries.FILTER_ITEM_DAMAGE.get());
        filterConversion(output, ClayiumRegistries.FILTER_ITEM_DAMAGE.get(), ClayiumRegistries.FILTER_ITEM_NAME.get());
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ClayiumRegistries.FILTER_DUPLICATOR.get())
                .requires(ClayiumRegistries.FILTER_WHITELIST.get())
                .requires(ClayiumRegistries.FILTER_ITEM_NAME.get())
                .requires(ClayiumRegistries.FILTER_FUZZY.get())
                .unlockedBy("has_filter_whitelist", has(ClayiumRegistries.FILTER_WHITELIST.get()))
                .save(output, Clayium.id("filters/filter_duplicator"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ClayiumRegistries.FILTER_BLOCK_STATE.get())
                .requires(ClayiumRegistries.FILTER_ITEM_DAMAGE.get())
                .requires(Items.COMPARATOR)
                .unlockedBy("has_filter_item_damage", has(ClayiumRegistries.FILTER_ITEM_DAMAGE.get()))
                .save(output, Clayium.id("filters/item_damage_to_block_state"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ClayiumRegistries.FILTER_ITEM_DAMAGE.get())
                .requires(ClayiumRegistries.FILTER_BLOCK_STATE.get())
                .requires(Items.COMPARATOR)
                .unlockedBy("has_filter_block_state", has(ClayiumRegistries.FILTER_BLOCK_STATE.get()))
                .save(output, Clayium.id("filters/block_state_to_item_damage"));

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

        ComponentCraftingRecipes.build(output);
        MaterialCraftingRecipes.build(output);
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

    private static void filterConversion(RecipeOutput output, ItemLike input, ItemLike result) {
        String inputId = BuiltInRegistries.ITEM.getKey(input.asItem()).getPath();
        String resultId = BuiltInRegistries.ITEM.getKey(result.asItem()).getPath();
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
                .requires(input)
                .unlockedBy("has_" + inputId, has(input))
                .save(output, Clayium.id("filters/" + inputId + "_to_" + resultId));
    }

    private static void smelt(RecipeOutput output, ItemLike input, ItemLike result) {
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(input), RecipeCategory.MISC, result, 0.1F, 200)
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
        ClayProcessingRecipes.build(output);
        LogisticsRecipes.build(output);
        MaterialProcessingRecipes.build(output);
        AntimatterProgressionRecipes.build(output);
    }
}
