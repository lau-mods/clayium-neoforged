package net.claustra01.clayium.datagen.recipe;

import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.CIRCUIT_PATHS;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.COMPRESSED_CLAY_PATHS;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.MACHINE_HULL_PATHS;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.blockItem;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.circuit;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.commonTag;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.counted;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.countedTag;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.e;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.item;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.machineHull;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.matStack;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.stack;

import java.util.List;
import net.claustra01.clayium.material.CMaterial;
import net.claustra01.clayium.material.CMaterials;
import net.claustra01.clayium.material.CShape;
import net.claustra01.clayium.recipe.ClayiumConfigCondition;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

/**
 * Ports all 1.7.10 CRecipes Assembler, Inscriber, MillingMachine, Reactor, BlastFurnace, and CAInjector
 * machine/circuit/tool/gadget/PAN/RF-converter construction recipes.
 */
public final class ClayMachineBuildRecipeGenerator {
    private static final String CAT_ASSEMBLER = "assembler";
    private static final String CAT_INSCRIBER = "inscriber";
    private static final String CAT_MILLING = "milling_machine";
    private static final String CAT_BLAST = "blast_furnace";
    private static final String CAT_REACTOR = "reactor";
    private static final String CAT_CA_INJECTOR = "ca_injector";

    private ClayMachineBuildRecipeGenerator() {}

    public static void build(ClayiumRecipeHelper h) {
        buildToolsFiltersAndGadgets(h);
        buildCircuitsAndCoreComponents(h);
        buildTieredMachinesAndDevices(h);
        buildEndgameDevicesPanAndInjectors(h);
    }

    private static void buildToolsFiltersAndGadgets(ClayiumRecipeHelper h) {
        // Piping & Synchronization Tools (L328-397)
        h.machine(CAT_ASSEMBLER, List.of(counted(item("clay_rolling_pin")), counted(item("clay_slicer"))), 6,
                List.of(stack("clay_io_configurator")), e(6), 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("clay_spatula")), counted(item("clay_wrench"))), 6,
                List.of(stack("clay_piping_configurator")), e(6), 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("clay_io_configurator")), counted(circuit(6, 2))), 6,
                List.of(stack("memory_card")), e(6), 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.DENSE_CLAY, CShape.SPINDLE), counted(circuit(6, 2))), 6,
                List.of(stack("direction_memory_card")), e(6), 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.AZ91D_ALLOY, CShape.PLATE, 3), counted(item("synchronous_parts"), 2)), 6,
                List.of(stack("synchronizer")), e(6), 20L);

        // Filters (L398-400)
        h.machine(CAT_ASSEMBLER, List.of(counted(circuit(5, 1)), counted(CMaterials.IND_CLAY, CShape.PLATE, 3)), 4,
                List.of(stack("whitelist_item_filter")), 8L, 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(circuit(6, 1)), counted(CMaterials.IND_CLAY, CShape.PLATE, 3)), 4,
                List.of(stack("display_name_item_filter")), 8L, 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(circuit(7, 1)), counted(CMaterials.IND_CLAY, CShape.PLATE, 3)), 4,
                List.of(stack("fuzzy_item_filter")), 8L, 20L);

        // Gadgets & Manipulators (L423-488)
        h.machine(CAT_ASSEMBLER, List.of(counted(Items.LEATHER, 4), counted(CMaterials.AZ91D_ALLOY, CShape.PLATE, 8)), 4,
                List.of(stack("clay_gadget_holder")), e(6), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.IND_CLAY, CShape.PLATE, 8), counted(CMaterials.AZ91D_ALLOY, CShape.PLATE, 4)), 4,
                List.of(stack("gadget_blank")), e(6), 120L);

        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_blank")), counted(blockItem("antimatter_overclocker"))), 10,
                List.of(stack("gadget_antimatter_overclock")), e(10), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_antimatter_overclock")), counted(blockItem("pure_antimatter_overclocker"))), 10,
                List.of(stack("gadget_pure_antimatter_overclock")), e(11), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_pure_antimatter_overclock")), counted(blockItem("oec_overclocker"))), 10,
                List.of(stack("gadget_oec_overclock")), e(12), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_oec_overclock")), counted(blockItem("opa_overclocker"))), 10,
                List.of(stack("gadget_opa_overclock")), e(13), 120L);

        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_blank")), counted(circuit(12, 16))), 10,
                List.of(stack("gadget_flight0")), e(12), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_flight0")), counted(circuit(13, 16))), 10,
                List.of(stack("gadget_flight1")), e(13), 1200L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_flight1")), counted(blockItem("opa_overclocker"), 16)), 10,
                List.of(stack("gadget_flight2")), e(14), 12000L);

        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_blank")), counted(circuit(6, 4))), 4,
                List.of(stack("gadget_health0")), e(6), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_health0")), counted(circuit(10, 4))), 4,
                List.of(stack("gadget_health1")), e(10), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_health1")), counted(circuit(12, 4))), 10,
                List.of(stack("gadget_health2")), e(12), 120L);

        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_blank")), counted(circuit(7, 2))), 4,
                List.of(stack("gadget_auto_eat0")), e(7), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_antimatter_overclock")), counted(circuit(10, 4))), 10,
                List.of(stack("gadget_repeatedly_attack")), e(10), 120L);

        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.AZ91D_ALLOY, CShape.INGOT, 16), counted(circuit(6, 1))), 4,
                List.of(stack("manipulator1")), e(4), 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("manipulator1"), 16), counted(circuit(8, 6))), 4,
                List.of(stack("manipulator2")), e(8), 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("manipulator2"), 64), counted(circuit(12, 6))), 10,
                List.of(stack("manipulator3")), e(12), 20L);

        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_blank")), counted(item("manipulator1"))), 4,
                List.of(stack("gadget_long_arm0")), e(4), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_long_arm0")), counted(item("manipulator2"))), 4,
                List.of(stack("gadget_long_arm1")), e(8), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("gadget_long_arm1")), counted(item("manipulator3"))), 10,
                List.of(stack("gadget_long_arm2")), e(12), 120L);
    }

    private static void buildCircuitsAndCoreComponents(ClayiumRecipeHelper h) {
        // Hull & Circuit builds (L1323-1325, 1400-1515)
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.AZ91D_ALLOY, CShape.LARGE_PLATE), counted(circuit(6, 1))), 4,
                List.of(stack("az91d_alloy_hull")), e(6), 120L);

        h.simple(CAT_MILLING, matStack(CMaterials.DENSE_CLAY, CShape.PLATE), stack("clay_circuit_board"), 1L, 32L);
        h.simple(CAT_MILLING, matStack(CMaterials.IND_CLAY, CShape.PLATE), stack("clay_circuit_board"), 1L, 1L);
        h.simple(CAT_MILLING, matStack(CMaterials.ADVIND_CLAY, CShape.PLATE), 3, stack("cee_board"), 2L, 32L);

        h.machine(CAT_ASSEMBLER, List.of(counted(item("cee_circuit")), counted(CMaterials.IND_CLAY, CShape.PLATE, 3)), 0,
                List.of(stack("cee")), 8L, 20L);
        h.machine(CAT_INSCRIBER, List.of(counted(item("cee_board")), counted(CMaterials.ENG_CLAY, CShape.DUST, 32)), 0,
                List.of(stack("cee_circuit")), 2L, 20L);
        h.machine(CAT_INSCRIBER, List.of(counted(item("clay_circuit_board")), counted(CMaterials.DENSE_CLAY, CShape.DUST, 6)), 0,
                List.of(stack("clay_circuit")), 2L, 20L);
        h.machine(CAT_INSCRIBER, List.of(counted(item("clay_circuit_board")), counted(CMaterials.ENG_CLAY, CShape.DUST, 32)), 0,
                List.of(stack("basic_circuit")), 2L, 20L);
        h.machine(CAT_INSCRIBER, List.of(counted(CMaterials.IMPURE_SILICON, CShape.PLATE), counted(CMaterials.ENG_CLAY, CShape.DUST, 32)), 0,
                List.of(stack("advanced_circuit")), 100L, 120L);
        h.machine(CAT_INSCRIBER, List.of(counted(CMaterials.SILICON, CShape.PLATE), counted(CMaterials.ENG_CLAY, CShape.DUST, 32)), 0,
                List.of(stack("precision_circuit")), 1000L, 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("precision_circuit")), counted(CMaterials.ENG_CLAY, CShape.DUST, 32)), 6,
                List.of(stack("integrated_circuit")), 10000L, 1200L);

        h.machine(CAT_ASSEMBLER, List.of(counted(item("cee")), counted(item("integrated_circuit"))), 6,
                List.of(stack("laser_parts")), e(6), 20L);
        h.machine(CAT_ASSEMBLER, List.of(countedTag(CMaterials.BERYLLIUM, CShape.DUST, 8), counted(item("integrated_circuit"))), 6,
                List.of(stack("synchronous_parts")), e(6), 432000L);
        h.machine(CAT_REACTOR, List.of(countedTag(CMaterials.PURE_ANTIMATTER, CShape.GEM, 8), counted(item("integrated_circuit"))), 11,
                List.of(stack("teleportation_parts")), e(11), 10000000000000L);

        h.machine(CAT_REACTOR, List.of(counted(item("integrated_circuit"), 6), counted(CMaterials.EXC_CLAY, CShape.DUST)), 7,
                List.of(stack("clay_core")), e(10.0, 7), 8000000L);
        h.machine(CAT_REACTOR, List.of(counted(item("clay_core"), 6), counted(CMaterials.EXC_CLAY, CShape.DUST, 12)), 8,
                List.of(stack("clay_brain")), e(10.0, 8), 4000000000L);
        h.machine(CAT_REACTOR, List.of(counted(item("clay_brain"), 6), counted(CMaterials.EXC_CLAY, CShape.DUST, 32)), 9,
                List.of(stack("clay_spirit")), e(10.0, 9), 10000000000000L);
        h.machine(CAT_REACTOR, List.of(counted(item("clay_spirit"), 6), countedTag(CMaterials.ANTIMATTER, CShape.GEM, 4)), 10,
                List.of(stack("clay_soul")), e(10.0, 10), 10000000000000L);
        h.machine(CAT_REACTOR, List.of(counted(item("clay_soul"), 6), countedTag(CMaterials.ANTIMATTER, CShape.GEM, 16)), 11,
                List.of(stack("clay_anima")), e(30.0, 11), 100000000000000L);
        h.machine(CAT_REACTOR, List.of(counted(item("clay_anima"), 6), countedTag(CMaterials.ANTIMATTER, CShape.GEM, 64)), 12,
                List.of(stack("clay_psyche")), e(90.0, 12), 1000000000000000L);
    }

    private static void buildTieredMachinesAndDevices(ClayiumRecipeHelper h) {
        String[] mats = {
                null, "clay", "dense_clay", "industrial_clay", "advanced_industrial_clay",
                "impure_silicon", "aluminium", "clay_steel", "clayium", "ultimate_alloy",
                "antimatter", "pure_antimatter", "octuple_energetic_clay", "octuple_pure_antimatter"
        };
        CMaterial[] matEnums = {
                null, CMaterials.CLAY, CMaterials.DENSE_CLAY, CMaterials.IND_CLAY, CMaterials.ADVIND_CLAY,
                CMaterials.IMPURE_SILICON, CMaterials.ALUMINIUM, CMaterials.CLAY_STEEL, CMaterials.CLAYIUM, CMaterials.ULTIMATE_ALLOY,
                CMaterials.ANTIMATTER, CMaterials.PURE_ANTIMATTER, CMaterials.OCTUPLE_CLAY, CMaterials.OCTUPLE_PURE_ANTIMATTER
        };
        String[] prefixes = {
                "", "clay", "dense_clay", "simple", "basic", "advanced", "precision",
                "clay_steel", "clayium", "ultimate", "antimatter", "pure_antimatter", "oec", "opa"
        };

        RecipeOutput capsuleOut = h.withConfig(ClayiumConfigCondition.ENABLE_FLUID_CAPSULE, true);
        RecipeOutput hcAlOut = h.withConfig(ClayiumConfigCondition.HARDCORE_ALUMINIUM, true);
        RecipeOutput normalAlOut = h.withConfig(ClayiumConfigCondition.HARDCORE_ALUMINIUM, false);

        for (int i = 1; i <= 13; i++) {
            String p = prefixes[i];
            ItemStack hull = machineHull(i);

            if (i == 1) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.DENSE_CLAY, CShape.CUTTING_HEAD)), 4,
                        List.of(stack("elemental_milling_machine")), e(i), 120L);
            }
            if (i <= 3) {
                h.machine(CAT_ASSEMBLER, List.of(counted(matEnums[i], CShape.LARGE_PLATE), counted(circuit(3, 1))), 4,
                        List.of(stack(p + "_cobblestone_generator")), e(i), 40L);
            }
            if (i <= 4) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.DENSE_CLAY, CShape.PLATE, 3)), 4,
                        List.of(stack(p + "_bending_machine")), e(i), 120L);
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.DENSE_CLAY, CShape.PIPE, 2)), 4,
                        List.of(stack(p + "_wire_drawing_machine")), e(i), 120L);
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.DENSE_CLAY, CShape.CYLINDER, 2)), 4,
                        List.of(stack(p + "_pipe_drawing_machine")), e(i), 120L);
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.CLAY, CShape.CUTTING_HEAD)), 4,
                        List.of(stack(p + "_cutting_machine")), e(i), 120L);
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.CLAY, CShape.SPINDLE)), 4,
                        List.of(stack(p + "_lathe")), e(i), 120L);
            }
            if (i >= 2) {
                if (i <= 3) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(matEnums[i], CShape.LARGE_PLATE)), 4,
                            List.of(stack(p + "_condenser")), e(i), 120L);
                }
                if (i <= 4) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.CLAY, CShape.GEAR, 4)), 4,
                            List.of(stack(p + "_decomposer")), e(i), 120L);
                }
                if (i <= 6) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.DENSE_CLAY, CShape.GRINDING_HEAD, 1)), 4,
                            List.of(stack(p + "_grinder")), e(i), 120L);
                }
            }
            if (i == 3) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(item("cee"), 2)), 4,
                        List.of(stack("energetic_clay_condenser")), e(i), 120L);
            }
            if (i >= 3) {
                if (i <= 4) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.DENSE_CLAY, CShape.CUTTING_HEAD)), 4,
                            List.of(stack(p + "_milling_machine")), e(i), 120L);
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.DENSE_CLAY, CShape.GEAR, 4)), 4,
                            List.of(stack(p + "_assembler")), e(i), 40L);
                    h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_assembler")), counted(circuit(4, 1))), 4,
                            List.of(stack(p + "_inscriber")), e(i), 40L);
                }
                if (i <= 6) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.DENSE_CLAY, CShape.SPINDLE, Math.max(i - 4, 1))), 4,
                            List.of(stack(p + "_centrifuge")), e(i), 120L);
                }
            }
            if (i == 4) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(item("cee"), 2)), 4,
                        List.of(stack("energetic_clay_condenser_mk2")), e(i), 120L);
            }
            if (i >= 4) {
                if (i == 6) {
                    h.machine(normalAlOut, CAT_ASSEMBLER,
                            List.of(counted(CMaterials.ALUMINIUM, CShape.PLATE), counted(circuit(i, 1))), 4,
                            List.of(stack(p + "_buffer", 16)), e(i), 40L, 0);
                    h.machine(hcAlOut, CAT_ASSEMBLER,
                            List.of(counted(CMaterials.IMPURE_ALUMINIUM, CShape.PLATE), counted(circuit(i, 1))), 4,
                            List.of(stack(p + "_buffer", 16)), e(i), 40L, 0);
                    h.machine(normalAlOut, CAT_ASSEMBLER,
                            List.of(counted(blockItem(p + "_buffer"), 6), counted(CMaterials.ALUMINIUM, CShape.LARGE_PLATE)), 4,
                            List.of(stack(p + "_multitrack_buffer")), e(i), 40L, 0);
                    h.machine(hcAlOut, CAT_ASSEMBLER,
                            List.of(counted(blockItem(p + "_buffer"), 6), counted(CMaterials.IMPURE_ALUMINIUM, CShape.LARGE_PLATE)), 4,
                            List.of(stack(p + "_multitrack_buffer")), e(i), 40L, 0);
                } else {
                    h.machine(CAT_ASSEMBLER, List.of(counted(matEnums[i], CShape.PLATE), counted(circuit(i, 1))), 4,
                            List.of(stack(p + "_buffer", 16)), e(i), 40L);
                    h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_buffer"), 6), counted(matEnums[i], CShape.LARGE_PLATE)), 4,
                            List.of(stack(p + "_multitrack_buffer")), e(i), 40L);
                }
                h.machine(capsuleOut, CAT_ASSEMBLER,
                        List.of(counted(blockItem(p + "_buffer")), counted(item("fluid_capsule_1"), 4)), 4,
                        List.of(stack(p + "_fluid_translator")), e(i), 40L, 0);

                if (i <= 7) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_buffer")), counted(circuit(3, 1))), 4,
                            List.of(stack(p + "_cobblestone_generator")), e(i), 40L);
                    h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_buffer")), counted(circuit(4, 1))), 4,
                            List.of(stack(p + "_salt_extractor")), e(i), 40L);
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(circuit(3, i - 3))), 4,
                            List.of(stack(p + "_smelter")), e(i), 120L);
                    h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.AZ91D_ALLOY, CShape.PLATE, 4), counted(circuit(i, 1))), 4,
                            List.of(stack("clay_shooter_" + (i - 4))), e(i), 40L);
                }
                if (i <= 5) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem(p + "_buffer"))), 4,
                            List.of(stack(p + "_condenser")), e(i), 120L);
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(circuit(4, i - 3))), 4,
                            List.of(stack(p + "_chemical_reactor")), e(i), 120L);
                }
            }
            if (i >= 5) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem("precision_buffer"))), 4,
                        List.of(stack(p + "_clay_interface")), e(i), 40L);
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_clay_interface")), counted(CMaterials.ENG_CLAY, CShape.DUST, 16)), 4,
                        List.of(stack(p + "_redstone_interface")), e(i), 40L);
                if (i <= 7) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.DENSE_CLAY, CShape.PLATE, (i - 4) * 3)), 4,
                            List.of(stack(p + "_bending_machine")), e(i), 120L);
                }
            }
            if (i == 5) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), countedTag(CMaterials.SILICON, CShape.PLATE, 8)), 4,
                        List.of(stack("solar_clay_fabricator_mk1")), e(i), 120L);
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_buffer")), counted(circuit(5, 1))), 4,
                        List.of(stack("auto_clay_condenser")), e(i), 40L);
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("basic_assembler")), counted(hull)), 4,
                        List.of(stack(p + "_auto_crafter")), e(i), 40L);
                h.machine(capsuleOut, CAT_ASSEMBLER,
                        List.of(counted(blockItem(p + "_fluid_translator"), 2), counted(hull)), 4,
                        List.of(stack("advanced_fluid_transfer_machine")), e(i), 40L, 0);
            }
            if (i >= 6 && i <= 9) {
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("advanced_chemical_reactor")), counted(circuit(i, 1))), 4,
                        List.of(stack(p + "_electrolysis_reactor")), e(i), 40L);
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(prefixes[i - 1] + "_auto_crafter")), counted(hull)), 4,
                        List.of(stack(p + "_auto_crafter")), e(i), 40L);
            }
            if (i == 6) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), countedTag(CMaterials.SILICON, CShape.PLATE, 16)), 4,
                        List.of(stack("solar_clay_fabricator_mk2")), e(i), 120L);
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_smelter")), counted(blockItem(p + "_clay_interface"))), 4,
                        List.of(stack("clay_blast_furnace")), e(i), 120L);
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("advanced_chemical_reactor")), counted(blockItem(p + "_smelter"))), 4,
                        List.of(stack("chemical_metal_separator")), e(i), 40L);
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(CMaterials.DENSE_CLAY, CShape.GEAR, 4)), 4,
                        List.of(stack("precision_assembler")), e(i), 40L);
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("basic_assembler")), counted(circuit(i, 1))), 4,
                        List.of(stack("precision_assembler")), e(i), 40L);
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_smelter")), counted(circuit(i, 1))), 4,
                        List.of(stack("precision_alloy_smelter")), e(i), 40L);
            }
            if (i == 7) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), countedTag(CMaterials.SILICON, CShape.PLATE, 16)), 4,
                        List.of(stack("lithium_solar_clay_fabricator")), e(i), 120L);
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_buffer")), counted(circuit(5, 1))), 4,
                        List.of(stack("auto_clay_condenser_mk2")), e(i), 40L);
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem(p + "_clay_laser_interface"))), 6,
                        List.of(stack("clay_reactor")), e(i), 1200L);
            }
            if (i >= 7) {
                h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_buffer")), counted(item("laser_parts"))), 6,
                        List.of(stack(p + "_clay_laser_interface")), e(i), 120L);
                if (i <= 9) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_buffer")), counted(hull)), 6,
                            List.of(stack(p + "_distributor")), e(i), 120L);
                    h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_buffer")), counted(circuit(7, 1))), 6,
                            List.of(stack(p + "_distributor")), e(i), 120L);
                    h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("clay_reactor")), counted(blockItem(p + "_electrolysis_reactor"))), 6,
                            List.of(stack(p + "_matter_transformer")), e(i), 120L);
                }
                if (i <= 10) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(item("laser_parts"), 4)), 6,
                            List.of(stack(p + "_clay_energy_laser")), e(i), 480L);
                }
            }
            if (i == 8) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem("advanced_chemical_reactor"))), 4,
                        List.of(stack(p + "_chemical_reactor")), e(i), 480L);
            }
            if (i >= 8 && i <= 9) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem(prefixes[i - 1] + "_smelter"), 16)), 6,
                        List.of(stack(p + "_smelter")), e(i), 2000L);
            }
            if (i == 9) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem("clay_steel_bending_machine"), 4)), 6,
                        List.of(stack(p + "_bending_machine")), e(i), 480L);
            }
            if (i >= 9) {
                if (i == 9) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem(p + "_matter_transformer"), 16)), 6,
                            List.of(stack(p + "_ca_condenser")), e(i), 480L);
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem("clay_reactor"), 16)), 6,
                            List.of(stack(p + "_ca_injector")), e(i), 480L);
                } else {
                    if (i <= 11) {
                        h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem(p + "_matter_transformer"), 16)), 10,
                                List.of(stack(p + "_ca_condenser")), e(i), 480L);
                    }
                    h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem(prefixes[i - 1] + "_ca_injector"), 2)), 10,
                            List.of(stack(p + "_ca_injector")), e(i), 480L);
                }
            }
            if (i == 10) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem("precision_assembler"))), 6,
                        List.of(stack(p + "_assembler")), e(i), 40L);
            }
            if (i >= 10) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem("clay_reactor"), 16)), 10,
                        List.of(stack(p + "_ca_reactor_core")), e(i), 120L);
                if (i <= 12) {
                    h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(p + "_ca_injector")), counted(blockItem("clay_reactor"))), 10,
                            List.of(stack(p + "_matter_transformer")), e(i), 120L);
                }
            }
            if (i == 13) {
                h.machine(CAT_ASSEMBLER, List.of(counted(hull), counted(blockItem("opa_ca_reactor_coil"))), 10,
                        List.of(stack("energetic_clay_decomposer")), e(i), 120L);
            }
        }
        // Keep mats reference cleanly consumed
        if (mats.length == 0) {
            throw new AssertionError();
        }
    }

    private static void buildEndgameDevicesPanAndInjectors(ClayiumRecipeHelper h) {
        // RF/FE Generators (gated on enable_rf_generator, Tiers 4..13)
        RecipeOutput rfOut = h.withConfig(ClayiumConfigCondition.ENABLE_RF_GENERATOR, true);
        String[] rfPrefixes = {
                "basic", "advanced", "precision", "clay_steel", "clayium",
                "ultimate", "antimatter", "pure_antimatter", "oec", "opa"
        };
        for (int idx = 0; idx < rfPrefixes.length; idx++) {
            int tier = 4 + idx;
            String ifacePath = tier >= 5 ? rfPrefixes[idx] + "_redstone_interface" : "advanced_redstone_interface";
            h.machine(rfOut, CAT_ASSEMBLER,
                    List.of(counted(machineHull(tier)), counted(blockItem(ifacePath))), 4,
                    List.of(stack(rfPrefixes[idx] + "_rf_generator")), e(tier), 120L, 0);
        }

        // Quartz Crucible, Laser Reflector, Clay Fabricators, Resonators, Storage Upgrades, Overclockers, Coils, PAN
        h.machine(CAT_ASSEMBLER, List.of(countedTag(CMaterials.QUARTZ, CShape.DUST, 16)), 0,
                List.of(stack("quartz_crucible")), 1000L, 20L);
        h.machine(CAT_BLAST, List.of(counted(CMaterials.IND_CLAY, CShape.DUST), countedTag(CMaterials.QUARTZ, CShape.DUST, 8)), 7,
                List.of(stack("laser_reflector")), e(0.2, 7), 100L);

        h.machine(CAT_REACTOR, List.of(counted(machineHull(8)), counted(blockItem("lithium_solar_clay_fabricator"))), 8,
                List.of(stack("clay_fabricator_mk1")), e(3.0, 8), 100000000L);
        h.machine(CAT_REACTOR, List.of(counted(machineHull(9)), counted(blockItem("lithium_solar_clay_fabricator"))), 9,
                List.of(stack("clay_fabricator_mk2")), e(3.0, 9), 100000000000L);
        h.machine(CAT_REACTOR, List.of(counted(blockItem("clay_fabricator_mk2"), 64), counted(blockItem("opa_overclocker"), 16)), 13,
                List.of(stack("clay_fabricator_mk3")), e(10.0, 13), 1000000000000000000L);

        // Resonators
        h.machine(CAT_CA_INJECTOR, List.of(counted(machineHull(9)), counted(CMaterials.ANTIMATTER, CShape.GEM, 8)), 9,
                List.of(stack("antimatter_resonator")), e(2.0, 9), 4000L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(blockItem("antimatter_resonator"), 16), counted(CMaterials.ANTIMATTER, CShape.GEM, 64)), 11,
                List.of(stack("pure_antimatter_resonator")), e(2.0, 11), 4000L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(blockItem("pure_antimatter_resonator"), 16), counted(CMaterials.ANTIMATTER, CShape.GEM, 64)), 12,
                List.of(stack("oec_resonator")), e(2.0, 12), 4000L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(blockItem("oec_resonator"), 16), counted(CMaterials.ANTIMATTER, CShape.GEM, 64)), 13,
                List.of(stack("opa_resonator")), e(2.0, 13), 4000L);

        // Energy Storage Upgrades
        h.machine(CAT_CA_INJECTOR, List.of(counted(blockItem("antimatter_overclocker")), counted(CMaterials.ANTIMATTER, CShape.GEM, 8)), 9,
                List.of(stack("antimatter_energy_storage_upgrade")), e(2.0, 9), 4000L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(blockItem("antimatter_energy_storage_upgrade"), 16), counted(CMaterials.ANTIMATTER, CShape.GEM, 64)), 11,
                List.of(stack("pure_antimatter_energy_storage_upgrade")), e(2.0, 11), 4000L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(blockItem("pure_antimatter_energy_storage_upgrade"), 16), counted(CMaterials.ANTIMATTER, CShape.GEM, 64)), 12,
                List.of(stack("oec_energy_storage_upgrade")), e(2.0, 12), 4000L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(blockItem("oec_energy_storage_upgrade"), 16), counted(CMaterials.ANTIMATTER, CShape.GEM, 64)), 13,
                List.of(stack("opa_energy_storage_upgrade")), e(2.0, 13), 4000L);

        // Overclockers
        h.machine(CAT_REACTOR, List.of(counted(machineHull(10)), counted(blockItem("antimatter_resonator"), 8)), 10,
                List.of(stack("antimatter_overclocker")), e(5.0, 10), 10000000000000L);
        h.machine(CAT_REACTOR, List.of(new net.claustra01.clayium.recipe.CountedIngredient(
                net.minecraft.world.item.crafting.Ingredient.of(blockItem(MACHINE_HULL_PATHS[11])), 4), counted(blockItem("pure_antimatter_resonator"), 16)), 11,
                List.of(stack("pure_antimatter_overclocker")), e(5.0, 11), 100000000000000L);
        h.machine(CAT_REACTOR, List.of(new net.claustra01.clayium.recipe.CountedIngredient(
                net.minecraft.world.item.crafting.Ingredient.of(blockItem(MACHINE_HULL_PATHS[12])), 16), counted(blockItem("oec_resonator"), 32)), 12,
                List.of(stack("oec_overclocker")), e(5.0, 12), 1000000000000000L);
        h.machine(CAT_REACTOR, List.of(new net.claustra01.clayium.recipe.CountedIngredient(
                net.minecraft.world.item.crafting.Ingredient.of(blockItem(MACHINE_HULL_PATHS[13])), 64), counted(blockItem("opa_resonator"), 64)), 13,
                List.of(stack("opa_overclocker")), e(5.0, 13), 1000000000000000L);

        // CA Reactor Coils
        h.machine(CAT_REACTOR, List.of(counted(CMaterials.ANTIMATTER, CShape.PLATE, 6), countedTag(CMaterials.PLATINUM, CShape.INGOT)), 10,
                List.of(stack("antimatter_ca_reactor_coil")), e(10), 10000000000000L);
        h.machine(CAT_REACTOR, List.of(counted(CMaterials.PURE_ANTIMATTER, CShape.PLATE, 6), countedTag(CMaterials.IRIDIUM, CShape.INGOT, 4)), 11,
                List.of(stack("pure_antimatter_ca_reactor_coil")), e(11), 100000000000000L);

        RecipeOutput normalOs = h.withConfig(ClayiumConfigCondition.HARDCORE_OSMIUM, false);
        RecipeOutput hcOs = h.withConfig(ClayiumConfigCondition.HARDCORE_OSMIUM, true);
        h.machine(normalOs, CAT_REACTOR,
                List.of(counted(CMaterials.OCTUPLE_CLAY, CShape.PLATE, 6), countedTag(CMaterials.OSMIUM, CShape.INGOT, 16)), 12,
                List.of(stack("oec_ca_reactor_coil")), e(12), 1000000000000000L, 0);
        h.machine(hcOs, CAT_REACTOR,
                List.of(counted(CMaterials.OCTUPLE_CLAY, CShape.PLATE, 6), countedTag(CMaterials.IMPURE_OSMIUM, CShape.INGOT, 16)), 12,
                List.of(stack("oec_ca_reactor_coil")), e(12), 1000000000000000L, 0);

        h.machine(CAT_REACTOR, List.of(counted(CMaterials.OCTUPLE_PURE_ANTIMATTER, CShape.PLATE, 6), countedTag(CMaterials.RHENIUM, CShape.INGOT, 64)), 13,
                List.of(stack("opa_ca_reactor_coil")), e(13), 1000000000000000L);

        // PAN & Storage/Area/Teleportation (L2231-2331)
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.PURE_ANTIMATTER, CShape.GEM, 3), counted(Tags.Items.GLASS_BLOCKS, 2)), 10,
                List.of(stack("pan_cable", 12)), e(10), 2L);
        h.machine(CAT_REACTOR, List.of(counted(blockItem("antimatter_pan_adapter"), 4), counted(circuit(11, 1))), 11,
                List.of(stack("pan_core")), e(11), 100000000000000L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("antimatter_resonator")), counted(blockItem("pan_cable"), 6)), 10,
                List.of(stack("antimatter_pan_adapter")), e(10), 60L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("pure_antimatter_resonator")), counted(blockItem("pan_cable"), 6)), 10,
                List.of(stack("pure_antimatter_pan_adapter")), e(11), 60L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("oec_resonator")), counted(blockItem("pan_cable"), 6)), 10,
                List.of(stack("oec_pan_adapter")), e(12), 60L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("opa_resonator")), counted(blockItem("pan_cable"), 6)), 10,
                List.of(stack("opa_pan_adapter")), e(13), 60L);
        h.machine(CAT_ASSEMBLER, List.of(counted(machineHull(4)), counted(blockItem("pan_cable"), 4)), 10,
                List.of(stack("basic_pan_duplicator")), e(10), 20L);

        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("az91d_alloy_hull"), 4), counted(blockItem("precision_clay_interface"))), 4,
                List.of(stack("storage_container", 4)), e(6), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("az91d_alloy_hull"), 4), counted(blockItem("precision_redstone_interface"))), 4,
                List.of(stack("vacuum_container", 4)), e(6), 120L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("az91d_alloy_hull")), counted(circuit(8, 4))), 6,
                List.of(stack("auto_trader")), e(8), 120L);

        h.machine(CAT_ASSEMBLER, List.of(counted(Items.CLAY), counted(circuit(7, 1))), 6, List.of(stack("clay_marker")), e(7), 480L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(COMPRESSED_CLAY_PATHS[0])), counted(circuit(8, 1))), 6, List.of(stack("clay_open_pit_marker")), e(8), 480L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem(COMPRESSED_CLAY_PATHS[1])), counted(circuit(8, 1))), 6, List.of(stack("clay_ground_leveling_marker")), e(8), 480L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("clay_open_pit_marker")), counted(blockItem("clay_ground_leveling_marker"))), 6, List.of(stack("clay_prism_marker")), e(8), 480L);

        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("zk60a_alloy_hull")), counted(circuit(7, 64))), 6, List.of(stack("area_collector")), e(7), 6000L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("az91d_alloy_hull")), counted(circuit(6, 1))), 6, List.of(stack("miner")), e(6), 60L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("zk60a_alloy_hull")), counted(circuit(8, 64))), 6, List.of(stack("area_miner")), e(8), 6000L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("area_miner")), counted(circuit(9, 64))), 6, List.of(stack("advanced_area_miner")), e(9), 6000L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("advanced_area_miner")), counted(circuit(10, 64))), 6, List.of(stack("area_replacer")), e(10), 6000L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("az91d_alloy_hull")), counted(circuit(5, 1))), 6, List.of(stack("activator")), e(6), 60L);
        h.machine(CAT_ASSEMBLER, List.of(counted(blockItem("area_collector")), counted(circuit(8, 1))), 6, List.of(stack("area_activator")), e(8), 6000L);
        h.machine(CAT_ASSEMBLER, List.of(counted(item("clay_shooter_3")), counted(item("teleportation_parts"))), 10, List.of(stack("instant_teleporter")), e(11), 3000L);

        // Machine upgrade chains via CA Injector (L2332-2402)
        emitInjectorChain(h, h.output(), new String[]{null, "clay_bending_machine", "dense_clay_bending_machine", "simple_bending_machine", "basic_bending_machine", "advanced_bending_machine", "precision_bending_machine", "clay_steel_bending_machine", null, "ultimate_bending_machine"});
        emitInjectorChain(h, h.output(), new String[]{null, "clay_wire_drawing_machine", "dense_clay_wire_drawing_machine", "simple_wire_drawing_machine", "basic_wire_drawing_machine"});
        emitInjectorChain(h, h.output(), new String[]{null, "clay_pipe_drawing_machine", "dense_clay_pipe_drawing_machine", "simple_pipe_drawing_machine", "basic_pipe_drawing_machine"});
        emitInjectorChain(h, h.output(), new String[]{null, "clay_cutting_machine", "dense_clay_cutting_machine", "simple_cutting_machine", "basic_cutting_machine"});
        emitInjectorChain(h, h.output(), new String[]{null, "clay_lathe", "dense_clay_lathe", "simple_lathe", "basic_lathe"});
        emitInjectorChain(h, h.output(), new String[]{null, "clay_cobblestone_generator", "dense_clay_cobblestone_generator", "simple_cobblestone_generator", "basic_cobblestone_generator", "advanced_cobblestone_generator", "precision_cobblestone_generator", "clay_steel_cobblestone_generator"});
        emitInjectorChain(h, h.output(), new String[]{null, null, "dense_clay_condenser", "simple_condenser", "basic_condenser", "advanced_condenser", null, null, null, null, "antimatter_condenser"});
        emitInjectorChain(h, h.output(), new String[]{null, null, "dense_clay_grinder", "simple_grinder", "basic_grinder", "advanced_grinder", "precision_grinder", null, null, null, "antimatter_grinder"});
        emitInjectorChain(h, h.output(), new String[]{null, null, "dense_clay_decomposer", "simple_decomposer", "basic_decomposer"});
        emitInjectorChain(h, h.output(), new String[]{null, "elemental_milling_machine", null, "simple_milling_machine", "basic_milling_machine"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, "simple_assembler", "basic_assembler", null, "precision_assembler", null, null, null, "antimatter_assembler"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, "simple_inscriber", "basic_inscriber"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, "simple_centrifuge", "basic_centrifuge", "advanced_centrifuge", "precision_centrifuge"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, "basic_salt_extractor", "advanced_salt_extractor", "precision_salt_extractor", "clay_steel_salt_extractor"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, "basic_smelter", "advanced_smelter", "precision_smelter", "clay_steel_smelter", "clayium_smelter", "ultimate_smelter"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, "basic_buffer", "advanced_buffer", "precision_buffer", "clay_steel_buffer", "clayium_buffer", "ultimate_buffer", "antimatter_buffer", "pure_antimatter_buffer", "oec_buffer", "opa_buffer"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, "basic_chemical_reactor", "advanced_chemical_reactor", null, null, "clayium_chemical_reactor"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, null, null, "precision_alloy_smelter"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, null, null, "precision_electrolysis_reactor", "clay_steel_electrolysis_reactor", "clayium_electrolysis_reactor", "ultimate_electrolysis_reactor"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, null, null, null, "clay_steel_clay_energy_laser", "clayium_clay_energy_laser", "ultimate_clay_energy_laser", "antimatter_clay_energy_laser"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, null, null, null, "clay_steel_distributor", "clayium_distributor", "ultimate_distributor"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, null, "advanced_auto_crafter", "precision_auto_crafter", "clay_steel_auto_crafter", "clayium_auto_crafter", "ultimate_auto_crafter"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, null, null, null, "clay_steel_matter_transformer", "clayium_matter_transformer", "ultimate_matter_transformer", "antimatter_matter_transformer", "pure_antimatter_matter_transformer", "oec_matter_transformer"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, null, null, null, null, null, "ultimate_ca_condenser", "antimatter_ca_condenser", "pure_antimatter_ca_condenser"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, null, null, null, null, null, "ultimate_ca_injector", "antimatter_ca_injector", "pure_antimatter_ca_injector", "oec_ca_injector", "opa_ca_injector"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, "energetic_clay_condenser", "energetic_clay_condenser_mk2", "auto_clay_condenser", null, "auto_clay_condenser_mk2"});
        emitInjectorChain(h, h.output(), new String[]{null, null, null, null, null, "solar_clay_fabricator_mk1", "solar_clay_fabricator_mk2", "lithium_solar_clay_fabricator", "clay_fabricator_mk1", "clay_fabricator_mk2"});
        emitInjectorChain(h, h.output(), new String[]{null, "clay_water_wheel", "dense_clay_water_wheel"});

        RecipeOutput ifaceOut = h.withConfig(ClayiumConfigCondition.ENABLE_INJECTOR_RECIPE_OF_INTERFACE, true);
        emitInjectorChain(h, ifaceOut, new String[]{null, null, null, null, null, "advanced_clay_interface", "precision_clay_interface", "clay_steel_clay_interface", "clayium_clay_interface", "ultimate_clay_interface", "antimatter_clay_interface", "pure_antimatter_clay_interface", "oec_clay_interface", "opa_clay_interface"});
        emitInjectorChain(h, ifaceOut, new String[]{null, null, null, null, null, "advanced_redstone_interface", "precision_redstone_interface", "clay_steel_redstone_interface", "clayium_redstone_interface", "ultimate_redstone_interface", "antimatter_redstone_interface", "pure_antimatter_redstone_interface", "oec_redstone_interface", "opa_redstone_interface"});
        emitInjectorChain(h, ifaceOut, new String[]{null, null, null, null, null, null, null, "clay_steel_clay_laser_interface", "clayium_clay_laser_interface", "ultimate_clay_laser_interface", "antimatter_clay_laser_interface", "pure_antimatter_clay_laser_interface", "oec_clay_laser_interface", "opa_clay_laser_interface"});
    }

    private static void emitInjectorChain(ClayiumRecipeHelper h, RecipeOutput targetOut, String[] pathsByTier) {
        int prevTier = -1;
        int n = 0;
        for (int i = 0; i < pathsByTier.length; i++) {
            n = (int) ((double) n + Math.pow(1.3F, i));
            if (n >= 64) {
                n = 64;
            }
            if (pathsByTier[i] != null) {
                if (prevTier != -1) {
                    h.machine(targetOut, CAT_CA_INJECTOR,
                            List.of(counted(blockItem(pathsByTier[prevTier])), counted(CMaterials.ANTIMATTER, CShape.GEM, n)),
                            i, List.of(stack(pathsByTier[i])), e(3.0, i), 4000L, 0);
                }
                prevTier = i;
                n = 0;
            }
        }
    }
}
