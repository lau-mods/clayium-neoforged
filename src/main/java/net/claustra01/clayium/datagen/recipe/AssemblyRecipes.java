package net.claustra01.clayium.datagen.recipe;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.series.AssemblySeries;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public final class AssemblyRecipes {
    public static void buildRecipes(RecipeOutput output) {
        circuits(output);
        assemblyParts(output);
        centrifuge(output);
        machineHulls(output);
        machines(output);
    }

    private static void circuits(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, component("clay_circuit"))
                .define('-', material("dense_clay_stick"))
                .define('*', material("dense_clay_gear"))
                .define('o', material("dense_clay_ring"))
                .define('#', component("clay_circuit_board"))
                .pattern("-*-").pattern("o#o").pattern("-*-")
                .unlockedBy("has_board", InventoryChangeTrigger.TriggerInstance.hasItems(
                        component("clay_circuit_board")))
                .save(output, ClayiumMod.id("assembly_parts/clay_circuit"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, component("simple_circuit"))
                .define('-', material("energized_clay_dust"))
                .define('#', component("clay_circuit_board"))
                .pattern("---").pattern("-#-").pattern("---")
                .unlockedBy("has_board", InventoryChangeTrigger.TriggerInstance.hasItems(
                        component("clay_circuit_board")))
                .save(output, ClayiumMod.id("assembly_parts/simple_circuit"));

        machine(ClayiumRecipeMethod.INSCRIBER, 0, 2L, 20)
                .requires(component("cee_board")).requires(material("energized_clay_dust"), 32)
                .output(component("cee_circuit"), 1)
                .save(output, ClayiumMod.id("inscriber/cee_circuit"));
        machine(ClayiumRecipeMethod.INSCRIBER, 0, 2L, 20)
                .requires(component("clay_circuit_board")).requires(material("dense_clay_dust"), 6)
                .output(component("clay_circuit"), 1)
                .save(output, ClayiumMod.id("inscriber/clay_circuit"));
        machine(ClayiumRecipeMethod.INSCRIBER, 0, 2L, 20)
                .requires(component("clay_circuit_board")).requires(material("energized_clay_dust"), 32)
                .output(component("basic_circuit"), 1)
                .save(output, ClayiumMod.id("inscriber/basic_circuit"));
        machine(ClayiumRecipeMethod.INSCRIBER, 0, 100L, 120)
                .requires(material("impure_silicon_plate")).requires(material("energized_clay_dust"), 32)
                .output(component("advanced_circuit"), 1)
                .save(output, ClayiumMod.id("inscriber/advanced_circuit"));
        machine(ClayiumRecipeMethod.INSCRIBER, 0, 1_000L, 120)
                .requires(material("silicon_plate")).requires(material("energized_clay_dust"), 32)
                .output(component("precision_circuit"), 1)
                .save(output, ClayiumMod.id("inscriber/precision_circuit"));
    }

    private static void assemblyParts(RecipeOutput output) {
        machine(ClayiumRecipeMethod.ASSEMBLER, 0, 8L, 20)
                .requires(component("cee_circuit")).requires(material("industrial_clay_plate"), 3)
                .output(component("clay_energy_excitor"), 1)
                .save(output, ClayiumMod.id("assembler/clay_energy_excitor"));
        machine(ClayiumRecipeMethod.ASSEMBLER, 6, 10_000L, 1_200)
                .requires(component("precision_circuit")).requires(material("energized_clay_dust"), 32)
                .output(component("integrated_circuit"), 1)
                .save(output, ClayiumMod.id("assembler/integrated_circuit"));
        machine(ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 6), 20)
                .requires(component("clay_energy_excitor")).requires(component("integrated_circuit"))
                .output(component("laser_parts"), 1)
                .save(output, ClayiumMod.id("assembler/laser_parts"));
        machine(ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 6), 432_000)
                .requires(material("beryllium_dust"), 8).requires(component("integrated_circuit"))
                .output(component("synchronous_parts"), 1)
                .save(output, ClayiumMod.id("assembler/synchronous_parts"));
        machine(ClayiumRecipeMethod.ASSEMBLER, 4, ClayiumRecipeEnergy.e(1.0D, 4), 20)
                .requires(material("az91_d_alloy_ingot"), 16).requires(component("precision_circuit"))
                .output(component("manipulator_mk_i"), 1)
                .save(output, ClayiumMod.id("assembler/manipulator_mk_i"));
        machine(ClayiumRecipeMethod.ASSEMBLER, 4, ClayiumRecipeEnergy.e(1.0D, 8), 20)
                .requires(component("manipulator_mk_i"), 16).requires(component("clay_core"), 6)
                .output(component("manipulator_mk_ii"), 1)
                .save(output, ClayiumMod.id("assembler/manipulator_mk_ii"));
        machine(ClayiumRecipeMethod.ASSEMBLER, 10, ClayiumRecipeEnergy.e(1.0D, 12), 20)
                .requires(component("manipulator_mk_ii"), 64).requires(component("clay_anima"), 6)
                .output(component("manipulator_mk_iii"), 1)
                .save(output, ClayiumMod.id("assembler/manipulator_mk_iii"));
        machine(ClayiumRecipeMethod.REACTOR, 7, ClayiumRecipeEnergy.e(10.0D, 7), 8_000_000)
                .requires(component("integrated_circuit"), 6).requires(material("excited_clay_dust"))
                .output(component("clay_core"), 1)
                .save(output, ClayiumMod.id("reactor/clay_core"));
        machine(ClayiumRecipeMethod.ASSEMBLER, 3, 10L, 20)
                .requires(material("clay_stick"), 5)
                .output(material("clay_gear"), 1)
                .save(output, ClayiumMod.id("assembler/clay_gear"));
        machine(ClayiumRecipeMethod.ASSEMBLER, 3, 100L, 20)
                .requires(material("large_dense_clay_plate")).requires(Items.CLAY_BALL, 8)
                .output(material("dense_clay_spindle"), 1)
                .save(output, ClayiumMod.id("assembler/dense_clay_spindle"));
    }

    private static void centrifuge(RecipeOutput output) {
        machine(ClayiumRecipeMethod.CENTRIFUGE, 0, 4L, 20)
                .requires(material("clay_dust"), 9)
                .output(material("dense_clay_dust"), 1)
                .save(output, ClayiumMod.id("centrifuge/dense_clay_dust"));
        machine(ClayiumRecipeMethod.CENTRIFUGE, 0, 4L, 20)
                .requires(material("dense_clay_dust"), 2)
                .output(material("clay_dust"), 9)
                .output(material("calcareous_clay_dust"), 1)
                .save(output, ClayiumMod.id("centrifuge/calcareous_clay"));
        machine(ClayiumRecipeMethod.CENTRIFUGE, 0, 4L, 20)
                .requires(material("industrial_clay_dust"), 2)
                .output(material("energized_clay_dust"), 12)
                .output(material("clay_dust"), 8)
                .output(material("dense_clay_dust"), 8)
                .output(material("industrial_clay_dust"), 1)
                .save(output, ClayiumMod.id("centrifuge/industrial_clay"));
        machine(ClayiumRecipeMethod.CENTRIFUGE, 4, 10_000L, 12)
                .requires(material("advanced_industrial_clay_dust"), 2)
                .output(material("energized_clay_dust"), 64)
                .output(material("clay_dust"), 64)
                .output(material("dense_clay_dust"), 64)
                .output(material("industrial_clay_dust"), 12)
                .save(output, ClayiumMod.id("centrifuge/advanced_industrial_clay"));
        machine(ClayiumRecipeMethod.CENTRIFUGE, 4, 1L, 2)
                .requires(Blocks.CLAY, 1).output(material("dense_clay"), 1)
                .save(output, ClayiumMod.id("centrifuge/dense_clay_block"));
    }

    private static void machineHulls(RecipeOutput output) {
        hull(output, 3, material("large_industrial_clay_plate"), component("simple_circuit"), false);
        hull(output, 4, material("large_advanced_industrial_clay_plate"), component("basic_circuit"), true);
        ShapedRecipeBuilder.shaped(
                        RecipeCategory.BUILDING_BLOCKS,
                        AssemblyMachineRegistration.MACHINE_HULLS.get(5).get())
                .define('#', material("large_impure_silicon_plate"))
                .define('*', material("large_silicone_plate"))
                .define('C', component("advanced_circuit"))
                .define('E', component("clay_energy_excitor"))
                .pattern("#E#").pattern("*C*").pattern("#*#")
                .unlockedBy("has_plate", InventoryChangeTrigger.TriggerInstance.hasItems(
                        material("large_impure_silicon_plate")))
                .save(output, ClayiumMod.id("machine_hull/tier_5"));
        hull(output, 6, material("large_aluminium_plate"), component("precision_circuit"), true);
        hull(output, 10, material("large_antimatter_plate"), component("clay_spirit"), true);
    }

    private static void hull(
            RecipeOutput output,
            int tier,
            ItemLike plate,
            ItemLike circuit,
            boolean requiresExcitor) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(
                        RecipeCategory.BUILDING_BLOCKS,
                        AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get())
                .define('#', plate)
                .define('C', circuit)
                .pattern(requiresExcitor ? "#E#" : "###")
                .pattern("#C#")
                .pattern("###");
        if (requiresExcitor) {
            builder.define('E', component("clay_energy_excitor"));
        }
        builder.unlockedBy("has_plate", InventoryChangeTrigger.TriggerInstance.hasItems(plate))
                .save(output, ClayiumMod.id("machine_hull/tier_" + tier));
    }

    private static void machines(RecipeOutput output) {
        for (AssemblyMachineRegistration.MachineDefinition definition
                : AssemblyMachineRegistration.MACHINES.values()) {
            int tier = definition.tier();
            if (tier <= 4) {
                ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(
                                RecipeCategory.BUILDING_BLOCKS, definition.block().get())
                        .define('#', AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get())
                        .define('o', material("dense_clay_spindle"))
                        .define('*', material("dense_clay_gear"))
                        .pattern(definition.series() == AssemblySeries.ASSEMBLER ? "*C*" : "*o*")
                        .pattern(definition.series() == AssemblySeries.INSCRIBER ? "C#C" : "o#o")
                        .pattern(definition.series() == AssemblySeries.CENTRIFUGE ? "*o*" : "*C*");
                if (definition.series() != AssemblySeries.CENTRIFUGE) {
                    builder.define('C', component(tier == 3 ? "simple_circuit" : "basic_circuit"));
                }
                builder.unlockedBy("has_hull", InventoryChangeTrigger.TriggerInstance.hasItems(
                                AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get()))
                        .save(output, ClayiumMod.id("assembly_machine/" + definition.id()));
                continue;
            }
            ClayiumRecipeBuilder builder = machine(ClayiumRecipeMethod.ASSEMBLER,
                    tier == 10 ? 6 : 4, ClayiumRecipeEnergy.e(1.0D, tier),
                    definition.series() == AssemblySeries.CENTRIFUGE ? 120 : 40)
                    .requires(AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get());
            if (tier == 10) {
                builder.requires(machine(AssemblySeries.ASSEMBLER, 6));
            } else if (definition.series() == AssemblySeries.ASSEMBLER) {
                builder.requires(material("dense_clay_gear"), 4);
            } else {
                builder.requires(material("dense_clay_spindle"), Math.max(tier - 4, 1));
            }
            builder.output(definition.block().get(), 1)
                    .save(output, ClayiumMod.id("assembly_machine/" + definition.id()));
        }
    }

    private static ClayiumRecipeBuilder machine(
            ClayiumRecipeMethod method, int requiredTier, long energy, int ticks) {
        return ClayiumRecipeBuilder.machine(method, requiredTier)
                .clayEnergy(energy)
                .processingTicks(ticks);
    }

    private static ItemLike machine(AssemblySeries series, int tier) {
        return AssemblyMachineRegistration.MACHINES.values().stream()
                .filter(definition -> definition.series() == series && definition.tier() == tier)
                .findFirst().orElseThrow().block().get();
    }

    private static ItemLike component(String id) {
        return MaterialRegistration.COMPONENTS.get(id).get();
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private AssemblyRecipes() {
    }
}
