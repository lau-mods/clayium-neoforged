/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.data;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.recipe.MachineIngredient;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Original Phase 6 recipes through Precision tier. */
public final class Phase6MachineRecipes {
    private Phase6MachineRecipes() {}

    public static void build(RecipeOutput output) {
        fluidBuffers(output);
        chemicalReactor(output);
        electrolysis(output);
        alloySmelter(output);
        solarFabricator(output);
        materialProcessing(output);
        machineConstruction(output);
    }

    private static void fluidBuffers(RecipeOutput output) {
        for (int tier = 4; tier <= 6; tier++) {
            ClayTier clayTier = ClayTier.byLegacyIndex(tier);
            machine(output, "fluid_buffer/" + clayTier.id(), ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.PHASE5_LOGISTICS_BLOCKS.get(clayTier.id() + "_buffer").get(), 1),
                            ingredient(ClayiumRegistries.PHASE4_ITEMS.get("dense_clay_pipe").get(), 4)),
                    List.of(stack(ClayiumRegistries.FLUID_BUFFER_BLOCKS.get(clayTier.id() + "_fluid_buffer").get(), 1)),
                    40, energy(tier), ClayTier.BASIC);
        }
    }

    private static void chemicalReactor(RecipeOutput output) {
        machine(output, "chemical/salt_and_calcareous_clay", ClayiumMachineIds.CHEMICAL_REACTOR,
                List.of(ingredient(item("salt_dust"), 2), ingredient(phase4("calcareous_clay_dust"), 1)),
                List.of(stack(item("calcium_chloride_dust"), 1), stack(item("sodium_carbonate_dust"), 1)),
                120, energy(5), ClayTier.RAW);
        machine(output, "chemical/sodium_carbonate_and_clay", ClayiumMachineIds.CHEMICAL_REACTOR,
                List.of(ingredient(item("sodium_carbonate_dust"), 1), ingredient(phase4("clay_dust"), 1)),
                List.of(stack(item("quartz_dust"), 1)), 120, energy(4), ClayTier.RAW);
        machine(output, "chemical/quartz_and_coal", ClayiumMachineIds.CHEMICAL_REACTOR,
                List.of(ingredient(item("quartz_dust"), 1),
                        new MachineIngredient(Ingredient.of(Items.COAL, Items.CHARCOAL), 1)),
                List.of(stack(item("impure_silicon_ingot"), 1)), 120, energy(4), ClayTier.RAW);
        machine(output, "chemical/dense_clay_separation", ClayiumMachineIds.CHEMICAL_REACTOR,
                List.of(ingredient(phase4("dense_clay_dust"), 1)),
                List.of(stack(item("impure_silicon_dust"), 1), stack(item("impure_aluminium_dust"), 1)),
                30, energy(5), ClayTier.ADVANCED);
    }

    private static void electrolysis(RecipeOutput output) {
        for (String material : List.of("aluminium", "magnesium", "sodium", "lithium", "zirconium", "zinc")) {
            machine(output, "electrolysis/" + material, ClayiumMachineIds.ELECTROLYSIS_REACTOR,
                    List.of(ingredient(item("impure_" + material + "_dust"), 1)),
                    List.of(stack(item(material + "_dust"), 1)),
                    100, energy(6), ClayTier.PRECISION);
        }
    }

    private static void alloySmelter(RecipeOutput output) {
        machine(output, "alloy_smelter/zincalminium", ClayiumMachineIds.ALLOY_SMELTER,
                List.of(ingredient(item("zinc_ingot"), 9), ingredient(item("aluminium_ingot"), 1)),
                List.of(stack(item("zincalminium_ingot"), 10)), 50, energy(6), ClayTier.PRECISION);
        machine(output, "alloy_smelter/az91d", ClayiumMachineIds.ALLOY_SMELTER,
                List.of(ingredient(item("magnesium_ingot"), 9), ingredient(item("zincalminium_ingot"), 1)),
                List.of(stack(item("az91d_ingot"), 10)), 500, energy(7), ClayTier.PRECISION);
        machine(output, "alloy_smelter/zinconium", ClayiumMachineIds.ALLOY_SMELTER,
                List.of(ingredient(item("zinc_ingot"), 9), ingredient(item("zirconium_ingot"), 1)),
                List.of(stack(item("zinconium_ingot"), 10)), 50, energy(7), ClayTier.PRECISION);
        machine(output, "alloy_smelter/zk60a", ClayiumMachineIds.ALLOY_SMELTER,
                List.of(ingredient(item("magnesium_ingot"), 19), ingredient(item("zinconium_ingot"), 1)),
                List.of(stack(item("zk60a_ingot"), 20)), 500, energy(7), ClayTier.PRECISION);
    }

    private static void solarFabricator(RecipeOutput output) {
        for (int level = 0; level <= 6; level++) {
            ClayTier minimumTier = level <= 4 ? ClayTier.ADVANCED : ClayTier.PRECISION;
            int time = solarTime(level, level <= 4 ? 4.0D : 3.0D, level <= 4 ? 4 : 6,
                    level <= 4 ? 5_000.0D : 50_000.0D);
            machine(output, "solar_fabricator/level_" + level + "_to_" + (level + 1),
                    ClayiumMachineIds.SOLAR_CLAY_FABRICATOR,
                    List.of(ingredient(compressedClay(level), 1)),
                    List.of(stack(compressedClay(level + 1), 1)), time, 0, minimumTier);
            if (level == 2) {
                machine(output, "solar_fabricator/sand_to_3", ClayiumMachineIds.SOLAR_CLAY_FABRICATOR,
                        List.of(ingredient(Items.SAND, 1)), List.of(stack(compressedClay(3), 1)),
                        time, 0, minimumTier);
            }
        }
    }

    private static int solarTime(int inputLevel, double base, int acceptableTier, double efficiency) {
        double multiplier = Math.pow(10.0D, acceptableTier + 1) * (base - 1.0D)
                / (base * (Math.pow(base, acceptableTier) - 1.0D)) / (efficiency / 20.0D);
        return Math.max(1, (int) (Math.pow(base, inputLevel) * multiplier));
    }

    private static ItemLike compressedClay(int level) {
        return switch (level) {
            case 0 -> Items.CLAY;
            case 1 -> ClayiumRegistries.DENSE_CLAY.get();
            case 2 -> ClayiumRegistries.COMPRESSED_CLAY.get();
            case 3 -> ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("industrial_clay").get();
            case 4 -> ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("advanced_industrial_clay").get();
            case 5 -> ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("energetic_clay").get();
            case 6 -> ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("compressed_energetic_clay").get();
            case 7 -> ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("double_compressed_energetic_clay").get();
            default -> throw new IllegalArgumentException("Unsupported compressed clay level: " + level);
        };
    }

    private static void materialProcessing(RecipeOutput output) {
        bending(output, "impure_silicon_ingot_to_plate", item("impure_silicon_ingot"), 1,
                phase4("impure_silicon_plate"), 1, 20);
        bending(output, "impure_silicon_plates_to_large_plate", phase4("impure_silicon_plate"), 4,
                item("impure_silicon_large_plate"), 1, 40);
        bending(output, "silicone_ingot_to_plate", item("silicone_ingot"), 1,
                item("silicone_plate"), 1, 4);
        bending(output, "silicone_plates_to_large_plate", item("silicone_plate"), 4,
                item("silicone_large_plate"), 1, 8);
        bending(output, "silicon_ingot_to_plate", item("silicon_ingot"), 1,
                phase4("silicon_plate"), 1, 20);
        bending(output, "silicon_plates_to_large_plate", phase4("silicon_plate"), 4,
                item("silicon_large_plate"), 1, 40);
        bending(output, "aluminium_ingot_to_plate", item("aluminium_ingot"), 1,
                item("aluminium_plate"), 1, 20);
        bending(output, "aluminium_plates_to_large_plate", item("aluminium_plate"), 4,
                item("aluminium_large_plate"), 1, 40);
        bending(output, "impure_aluminium_ingot_to_plate", item("impure_aluminium_ingot"), 1,
                item("impure_aluminium_plate"), 1, 20);
        bending(output, "impure_aluminium_plates_to_large_plate", item("impure_aluminium_plate"), 4,
                item("impure_aluminium_large_plate"), 1, 40);

        machine(output, "smelter/aluminium_dust", ClayiumMachineIds.SMELTER,
                List.of(ingredient(item("aluminium_dust"), 1)),
                List.of(stack(item("aluminium_ingot"), 1)), 200, 500, ClayTier.ADVANCED);
        machine(output, "smelter/zinc_dust", ClayiumMachineIds.SMELTER,
                List.of(ingredient(item("zinc_dust"), 1)),
                List.of(stack(item("zinc_ingot"), 1)), 200, 500, ClayTier.ADVANCED);
        machine(output, "smelter/sodium_dust", ClayiumMachineIds.SMELTER,
                List.of(ingredient(item("sodium_dust"), 1)),
                List.of(stack(item("sodium_ingot"), 1)), 200, 500, ClayTier.ADVANCED);
        machine(output, "smelter/impure_aluminium_dust", ClayiumMachineIds.SMELTER,
                List.of(ingredient(item("impure_aluminium_dust"), 1)),
                List.of(stack(item("impure_aluminium_ingot"), 1)), 200, 500, ClayTier.ADVANCED);
        for (String material : List.of("magnesium", "lithium", "zirconium", "zincalminium", "zinconium", "az91d", "zk60a")) {
            machine(output, "smelter/" + material + "_dust", ClayiumMachineIds.SMELTER,
                    List.of(ingredient(item(material + "_dust"), 1)),
                    List.of(stack(item(material + "_ingot"), 1)), 400, 2_000, ClayTier.PRECISION);
        }

        bending(output, "az91d_ingot_to_plate", item("az91d_ingot"), 1,
                item("az91d_plate"), 1, 20);
        bending(output, "az91d_plates_to_large_plate", item("az91d_plate"), 4,
                item("az91d_large_plate"), 1, 40);
        bending(output, "zk60a_ingot_to_plate", item("zk60a_ingot"), 1,
                item("zk60a_plate"), 1, 20);
        bending(output, "zk60a_plates_to_large_plate", item("zk60a_plate"), 4,
                item("zk60a_large_plate"), 1, 40);

        grinder(output, "impure_silicon_ingot", item("impure_silicon_ingot"), 1, item("impure_silicon_dust"), 1, 80);
        grinder(output, "impure_silicon_plate", phase4("impure_silicon_plate"), 1, item("impure_silicon_dust"), 1, 80);
        grinder(output, "impure_silicon_large_plate", item("impure_silicon_large_plate"), 1, item("impure_silicon_dust"), 4, 80);
        grinder(output, "silicone_ingot", item("silicone_ingot"), 1, item("silicone_dust"), 1, 16);
        grinder(output, "silicone_plate", item("silicone_plate"), 1, item("silicone_dust"), 1, 16);
        grinder(output, "silicone_large_plate", item("silicone_large_plate"), 1, item("silicone_dust"), 4, 16);
        grinder(output, "silicon_ingot", item("silicon_ingot"), 1, item("silicon_dust"), 1, 80);
        grinder(output, "silicon_plate", phase4("silicon_plate"), 1, item("silicon_dust"), 1, 80);
        grinder(output, "silicon_large_plate", item("silicon_large_plate"), 1, item("silicon_dust"), 4, 80);
        grinder(output, "aluminium_ingot", item("aluminium_ingot"), 1, item("aluminium_dust"), 1, 80);
        grinder(output, "aluminium_plate", item("aluminium_plate"), 1, item("aluminium_dust"), 1, 80);
        grinder(output, "aluminium_large_plate", item("aluminium_large_plate"), 1, item("aluminium_dust"), 4, 80);
        grinder(output, "impure_aluminium_ingot", item("impure_aluminium_ingot"), 1, item("impure_aluminium_dust"), 1, 80);
        grinder(output, "impure_aluminium_plate", item("impure_aluminium_plate"), 1, item("impure_aluminium_dust"), 1, 80);
        grinder(output, "impure_aluminium_large_plate", item("impure_aluminium_large_plate"), 1, item("impure_aluminium_dust"), 4, 80);
        grinder(output, "magnesium_ingot", item("magnesium_ingot"), 1, item("magnesium_dust"), 1, 80);
        grinder(output, "sodium_ingot", item("sodium_ingot"), 1, item("sodium_dust"), 1, 80);
        grinder(output, "lithium_ingot", item("lithium_ingot"), 1, item("lithium_dust"), 1, 80);
        grinder(output, "zinc_ingot", item("zinc_ingot"), 1, item("zinc_dust"), 1, 80);
        grinder(output, "zincalminium_ingot", item("zincalminium_ingot"), 1, item("zincalminium_dust"), 1, 80);
        grinder(output, "zinconium_ingot", item("zinconium_ingot"), 1, item("zinconium_dust"), 1, 80);
        grinder(output, "az91d_ingot", item("az91d_ingot"), 1, item("az91d_dust"), 1, 80);
        grinder(output, "az91d_plate", item("az91d_plate"), 1, item("az91d_dust"), 1, 80);
        grinder(output, "az91d_large_plate", item("az91d_large_plate"), 1, item("az91d_dust"), 4, 80);
        grinder(output, "zk60a_ingot", item("zk60a_ingot"), 1, item("zk60a_dust"), 1, 80);
        grinder(output, "zk60a_plate", item("zk60a_plate"), 1, item("zk60a_dust"), 1, 80);
        grinder(output, "zk60a_large_plate", item("zk60a_large_plate"), 1, item("zk60a_dust"), 4, 80);
    }

    private static void bending(RecipeOutput output, String id, ItemLike input, int inputCount,
                                ItemLike result, int resultCount, int time) {
        machine(output, "bending/" + id, ClayiumMachineIds.CLAY_BENDING_MACHINE,
                List.of(ingredient(input, inputCount)), List.of(stack(result, resultCount)),
                time, 100, ClayTier.BASIC);
    }

    private static void grinder(RecipeOutput output, String id, ItemLike input, int inputCount,
                                ItemLike result, int resultCount, int time) {
        machine(output, "grinder/" + id, ClayiumMachineIds.GRINDER,
                List.of(ingredient(input, inputCount)), List.of(stack(result, resultCount)),
                time, 25, ClayTier.BASIC);
    }

    private static void machineConstruction(RecipeOutput output) {
        machine(output, "machine/quartz_crucible", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(item("quartz_dust"), 16)),
                List.of(stack(ClayiumRegistries.QUARTZ_CRUCIBLE.get(), 1)),
                20, 1_000, ClayTier.RAW);
        for (int tier = 4; tier <= 6; tier++) {
            ClayTier clayTier = ClayTier.byLegacyIndex(tier);
            machine(output, "salt_extractor/" + clayTier.id(), ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.PHASE5_LOGISTICS_BLOCKS.get(clayTier.id() + "_buffer").get(), 1),
                            ingredient(phase4("basic_circuit"), 1)),
                    List.of(stack(ClayiumRegistries.SALT_EXTRACTOR_BLOCKS.get(clayTier.id() + "_salt_extractor").get(), 1)),
                    40, energy(tier), ClayTier.BASIC);
        }
        machine(output, "machine/basic_chemical_reactor", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("basic_machine_hull").get(), 1), ingredient(phase4("basic_circuit"), 1)),
                List.of(stack(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("basic_chemical_reactor").get(), 1)),
                120, energy(4), ClayTier.BASIC);
        machine(output, "machine/advanced_chemical_reactor", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("advanced_machine_hull").get(), 1), ingredient(phase4("basic_circuit"), 2)),
                List.of(stack(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("advanced_chemical_reactor").get(), 1)),
                120, energy(5), ClayTier.BASIC);
        machine(output, "machine/precision_electrolysis_reactor", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("advanced_chemical_reactor").get(), 1), ingredient(phase4("precision_circuit"), 1)),
                List.of(stack(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("precision_electrolysis_reactor").get(), 1)),
                40, energy(6), ClayTier.BASIC);
        machine(output, "machine/precision_alloy_smelter", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.PHASE4_MACHINE_BLOCKS.get("precision_smelter").get(), 1),
                        ingredient(phase4("precision_circuit"), 1)),
                List.of(stack(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("precision_alloy_smelter").get(), 1)),
                40, energy(6), ClayTier.BASIC);
        machine(output, "machine/az91d_machine_hull", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(item("az91d_large_plate"), 4), ingredient(phase4("precision_circuit"), 1)),
                List.of(stack(ClayiumRegistries.OTHER_HULL_BLOCKS.get("az91d_machine_hull").get(), 1)),
                120, energy(6), ClayTier.BASIC);
        for (String prefix : List.of("advanced", "precision")) {
            ClayTier tier = prefix.equals("advanced") ? ClayTier.ADVANCED : ClayTier.PRECISION;
            machine(output, "machine/" + prefix + "_clay_interface", ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get(prefix + "_machine_hull").get(), 1),
                            ingredient(ClayiumRegistries.PHASE5_LOGISTICS_BLOCKS.get("precision_buffer").get(), 1)),
                    List.of(stack(ClayiumRegistries.PHASE6_INTERFACE_BLOCKS.get(prefix + "_clay_interface").get(), 1)),
                    40, energy(tier.progressionIndex()), ClayTier.BASIC);
            machine(output, "machine/" + prefix + "_redstone_interface", ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.PHASE6_INTERFACE_BLOCKS.get(prefix + "_clay_interface").get(), 1),
                            ingredient(phase4("energetic_clay_dust"), 16)),
                    List.of(stack(ClayiumRegistries.PHASE6_INTERFACE_BLOCKS.get(prefix + "_redstone_interface").get(), 1)),
                    40, energy(tier.progressionIndex()), ClayTier.BASIC);
        }
        machine(output, "machine/advanced_solar_clay_fabricator_mk1", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("advanced_machine_hull").get(), 1),
                        ingredient(phase4("silicon_plate"), 8)),
                List.of(stack(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("advanced_solar_clay_fabricator_mk1").get(), 1)),
                120, energy(5), ClayTier.BASIC);
        machine(output, "machine/precision_solar_clay_fabricator_mk2", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("precision_machine_hull").get(), 1),
                        ingredient(phase4("silicon_plate"), 16)),
                List.of(stack(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("precision_solar_clay_fabricator_mk2").get(), 1)),
                120, energy(6), ClayTier.BASIC);
        machine(output, "machine/advanced_auto_clay_condenser", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.PHASE5_LOGISTICS_BLOCKS.get("advanced_buffer").get(), 1),
                        ingredient(phase4("advanced_circuit"), 1)),
                List.of(stack(ClayiumRegistries.SPECIAL_MACHINE_BLOCKS.get("advanced_auto_clay_condenser").get(), 1)),
                40, energy(5), ClayTier.BASIC);
        machine(output, "machine/advanced_auto_crafter", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.PHASE4_MACHINE_BLOCKS.get("basic_assembler").get(), 1),
                        ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("advanced_machine_hull").get(), 1)),
                List.of(stack(ClayiumRegistries.SPECIAL_MACHINE_BLOCKS.get("advanced_auto_crafter").get(), 1)),
                40, energy(5), ClayTier.BASIC);
        machine(output, "machine/precision_auto_crafter", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.SPECIAL_MACHINE_BLOCKS.get("advanced_auto_crafter").get(), 1),
                        ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("precision_machine_hull").get(), 1)),
                List.of(stack(ClayiumRegistries.SPECIAL_MACHINE_BLOCKS.get("precision_auto_crafter").get(), 1)),
                40, energy(6), ClayTier.BASIC);
        machine(output, "machine/precision_chemical_metal_separator", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("advanced_chemical_reactor").get(), 1),
                        ingredient(ClayiumRegistries.PHASE4_MACHINE_BLOCKS.get("precision_smelter").get(), 1)),
                List.of(stack(ClayiumRegistries.SPECIAL_MACHINE_BLOCKS.get("precision_chemical_metal_separator").get(), 1)),
                40, energy(6), ClayTier.BASIC);
        machine(output, "machine/manipulator", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(item("az91d_ingot"), 16), ingredient(phase4("precision_circuit"), 1)),
                List.of(stack(item("manipulator"), 1)), 20, energy(4), ClayTier.BASIC);
    }

    private static void machine(RecipeOutput output, String id, net.minecraft.resources.ResourceLocation machine,
                                List<MachineIngredient> inputs, List<ItemStack> outputs,
                                int time, long energy, ClayTier minimumTier) {
        output.accept(Clayium.id("phase6/" + id), new MachineRecipe(
                machine, new ArrayList<>(inputs), new ArrayList<>(outputs), time, energy, minimumTier), null);
    }

    private static ItemLike item(String id) { return ClayiumRegistries.PHASE6_ITEMS.get(id).get(); }
    private static ItemLike phase4(String id) { return ClayiumRegistries.PHASE4_ITEMS.get(id).get(); }
    private static MachineIngredient ingredient(ItemLike item, int count) {
        return new MachineIngredient(Ingredient.of(item), count);
    }
    private static ItemStack stack(ItemLike item, int count) { return new ItemStack(item, count); }
    private static long energy(int tier) { return (long) Math.pow(10, tier - 2); }
}
