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
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/** Material, chemical, and specialized-machine recipes through Precision tier. */
public final class MaterialProcessingRecipes {
    private MaterialProcessingRecipes() {}

    public static void build(RecipeOutput output) {
        fluidBuffers(output);
        chemicalReactor(output);
        electrolysis(output);
        alloySmelter(output);
        solarFabricator(output);
        matterTransformer(output);
        materialProcessing(output);
        machineConstruction(output);
    }

    private static void fluidBuffers(RecipeOutput output) {
        for (int tier = 4; tier <= 7; tier++) {
            ClayTier clayTier = ClayTier.byLegacyIndex(tier);
            machine(output, "fluid_buffer/" + clayTier.id(), ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.LOGISTICS_BLOCKS.get(clayTier.id() + "_buffer").get(), 1),
                            ingredient(ClayiumRegistries.COMPONENT_ITEMS.get("dense_clay_pipe").get(), 4)),
                    List.of(stack(ClayiumRegistries.FLUID_BUFFER_BLOCKS.get(clayTier.id() + "_fluid_buffer").get(), 1)),
                    40, energy(tier), ClayTier.BASIC);
        }
    }

    private static void chemicalReactor(RecipeOutput output) {
        machine(output, "chemical/salt_and_calcareous_clay", ClayiumMachineIds.CHEMICAL_REACTOR,
                List.of(ingredient(item("salt_dust"), 2), ingredient(component("calcareous_clay_dust"), 1)),
                List.of(stack(item("calcium_chloride_dust"), 1), stack(item("sodium_carbonate_dust"), 1)),
                120, energy(5), ClayTier.RAW);
        machine(output, "chemical/sodium_carbonate_and_clay", ClayiumMachineIds.CHEMICAL_REACTOR,
                List.of(ingredient(item("sodium_carbonate_dust"), 1), ingredient(component("clay_dust"), 1)),
                List.of(stack(item("quartz_dust"), 1)), 120, energy(4), ClayTier.RAW);
        machine(output, "chemical/quartz_and_coal", ClayiumMachineIds.CHEMICAL_REACTOR,
                List.of(ingredient(item("quartz_dust"), 1),
                        new MachineIngredient(Ingredient.of(Items.COAL, Items.CHARCOAL), 1)),
                List.of(stack(item("impure_silicon_ingot"), 1)), 120, energy(4), ClayTier.RAW);
        machine(output, "chemical/dense_clay_separation", ClayiumMachineIds.CHEMICAL_REACTOR,
                List.of(ingredient(component("dense_clay_dust"), 1)),
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
        for (String material : List.of("manganese", "calcium", "potassium", "hafnium", "strontium", "barium")) {
            machine(output, "electrolysis/" + material, ClayiumMachineIds.ELECTROLYSIS_REACTOR,
                    List.of(ingredient(item("impure_" + material + "_dust"), 1)),
                    List.of(stack(item(material + "_dust"), 1)),
                    300, 1_000_000, ClayTier.CLAY_STEEL);
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

    private static void matterTransformer(RecipeOutput output) {
        transform(output, "lithium_to_sodium", item("lithium_ingot"), item("sodium_ingot"), 7, 10);
        transform(output, "sodium_to_potassium", item("sodium_ingot"), item("potassium_ingot"), 7, 30);
        transform(output, "potassium_to_rubidium", item("potassium_ingot"), item("rubidium_ingot"), 8, 10);
        transform(output, "rubidium_to_caesium", item("rubidium_ingot"), item("caesium_ingot"), 8, 20);
        transform(output, "caesium_to_francium", item("caesium_ingot"), item("francium_ingot"), 8, 30);
        transform(output, "francium_to_radium", item("francium_ingot"), item("radium_ingot"), 8, 50);
        transform(output, "radium_to_actinium", item("radium_ingot"), item("actinium_ingot"), 9, 10);
        transform(output, "actinium_to_thorium", item("actinium_ingot"), item("thorium_ingot"), 9, 20);
        transform(output, "thorium_to_protactinium", item("thorium_ingot"), item("protactinium_ingot"), 9, 30);
        transform(output, "protactinium_to_uranium", item("protactinium_ingot"), item("uranium_ingot"), 9, 50);
        transform(output, "uranium_to_neptunium", item("uranium_ingot"), item("neptunium_ingot"), 9, 80);

        transform(output, "beryllium_to_magnesium", item("beryllium_ingot"), item("magnesium_ingot"), 7, 10);
        transform(output, "magnesium_to_calcium", item("magnesium_ingot"), item("calcium_ingot"), 7, 20);
        transform(output, "calcium_to_strontium", item("calcium_ingot"), item("strontium_ingot"), 7, 30);
        transform(output, "strontium_to_barium", item("strontium_ingot"), item("barium_ingot"), 7, 50);
        transform(output, "barium_to_lanthanum", item("barium_ingot"), item("lanthanum_ingot"), 8, 10);
        transform(output, "lanthanum_to_cerium", item("lanthanum_ingot"), item("cerium_ingot"), 8, 30);
        transform(output, "cerium_to_praseodymium", item("cerium_ingot"), item("praseodymium_ingot"), 8, 90);
        transform(output, "praseodymium_to_neodymium", item("praseodymium_ingot"), item("neodymium_ingot"), 9, 20);

        transform(output, "zirconium_to_titanium", item("zirconium_ingot"), item("titanium_ingot"), 8, 60);
        transform(output, "titanium_to_vanadium", item("titanium_ingot"), item("vanadium_ingot"), 9, 60);
        transform(output, "manganese_to_iron", item("manganese_ingot"), Items.IRON_INGOT, 7, 90);
        transform(output, "iron_to_cobalt", Items.IRON_INGOT, item("cobalt_ingot"), 8, 30);
        transform(output, "cobalt_to_nickel", item("cobalt_ingot"), item("nickel_ingot"), 8, 90);
        transform(output, "nickel_to_palladium", item("nickel_ingot"), item("palladium_ingot"), 9, 40);
        transform(output, "zinc_to_copper", item("zinc_ingot"), item("copper_ingot"), 8, 20);
        transform(output, "copper_to_silver", item("copper_ingot"), item("silver_ingot"), 9, 10);
        transform(output, "silver_to_gold", item("silver_ingot"), item("gold_ingot"), 9, 50);
        transform(output, "hafnium_to_tantalum", item("hafnium_ingot"), item("tantalum_ingot"), 8, 70);
        transform(output, "tantalum_to_tungsten", item("tantalum_ingot"), item("tungsten_ingot"), 9, 40);
        transform(output, "lead_to_tin", item("lead_ingot"), item("tin_ingot"), 7, 50);
        transform(output, "tin_to_antimony", item("tin_ingot"), item("antimony_ingot"), 8, 20);
        transform(output, "antimony_to_bismuth", item("antimony_ingot"), item("bismuth_ingot"), 9, 10);
        transform(output, "silicon_to_phosphorus", item("silicon_dust"), item("phosphorus_dust"), 7, 10);
        transform(output, "phosphorus_to_sulfur", item("phosphorus_dust"), item("sulfur_dust"), 7, 30);
    }

    private static void transform(RecipeOutput output, String id, ItemLike input, ItemLike result,
                                  int tier, long multiplier) {
        machine(output, "matter_transformer/" + id, ClayiumMachineIds.MATTER_TRANSFORMER,
                List.of(ingredient(input, 1)), List.of(stack(result, 1)),
                200, Math.multiplyExact(multiplier, energy(tier)), ClayTier.byLegacyIndex(tier));
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
                component("impure_silicon_plate"), 1, 20);
        bending(output, "impure_silicon_plates_to_large_plate", component("impure_silicon_plate"), 4,
                item("impure_silicon_large_plate"), 1, 40);
        bending(output, "silicone_ingot_to_plate", item("silicone_ingot"), 1,
                item("silicone_plate"), 1, 4);
        bending(output, "silicone_plates_to_large_plate", item("silicone_plate"), 4,
                item("silicone_large_plate"), 1, 8);
        bending(output, "silicon_ingot_to_plate", item("silicon_ingot"), 1,
                component("silicon_plate"), 1, 20);
        bending(output, "silicon_plates_to_large_plate", component("silicon_plate"), 4,
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
        grinder(output, "impure_silicon_plate", component("impure_silicon_plate"), 1, item("impure_silicon_dust"), 1, 80);
        grinder(output, "impure_silicon_large_plate", item("impure_silicon_large_plate"), 1, item("impure_silicon_dust"), 4, 80);
        grinder(output, "silicone_ingot", item("silicone_ingot"), 1, item("silicone_dust"), 1, 16);
        grinder(output, "silicone_plate", item("silicone_plate"), 1, item("silicone_dust"), 1, 16);
        grinder(output, "silicone_large_plate", item("silicone_large_plate"), 1, item("silicone_dust"), 4, 16);
        grinder(output, "silicon_ingot", item("silicon_ingot"), 1, item("silicon_dust"), 1, 80);
        grinder(output, "silicon_plate", component("silicon_plate"), 1, item("silicon_dust"), 1, 80);
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

        bending(output, "clay_steel_ingot_to_plate", item("clay_steel_ingot"), 1,
                item("clay_steel_plate"), 1, 60);
        bending(output, "clay_steel_plates_to_large_plate", item("clay_steel_plate"), 4,
                item("clay_steel_large_plate"), 1, 120);
        grinder(output, "clay_steel_ingot", item("clay_steel_ingot"), 1, item("clay_steel_dust"), 1, 240);
        grinder(output, "clay_steel_plate", item("clay_steel_plate"), 1, item("clay_steel_dust"), 1, 240);
        grinder(output, "clay_steel_large_plate", item("clay_steel_large_plate"), 1, item("clay_steel_dust"), 4, 240);
        grinder(output, "iron_ingot", Items.IRON_INGOT, 1, item("iron_dust"), 1, 240);
        grinder(output, "steel_ingot", item("steel_ingot"), 1, item("steel_dust"), 1, 240);

        machine(output, "blast_furnace/industrial_clay_and_manganese", ClayiumMachineIds.CLAY_BLAST_FURNACE,
                List.of(ingredient(component("industrial_clay_dust"), 2), ingredient(item("impure_manganese_dust"), 1)),
                List.of(stack(item("clay_steel_ingot"), 2)), 200, 50_000, ClayTier.PRECISION);
        machine(output, "blast_furnace/advanced_clay_and_manganese", ClayiumMachineIds.CLAY_BLAST_FURNACE,
                List.of(ingredient(component("advanced_industrial_clay_dust"), 1), ingredient(item("impure_manganese_dust"), 1)),
                List.of(stack(item("clay_steel_ingot"), 1)), 5, 500_000, ClayTier.CLAY_STEEL);
        machine(output, "blast_furnace/impure_silicon", ClayiumMachineIds.CLAY_BLAST_FURNACE,
                List.of(ingredient(component("advanced_industrial_clay_dust"), 1), ingredient(item("impure_silicon_ingot"), 1)),
                List.of(stack(item("silicon_ingot"), 1)), 100, 100_000, ClayTier.CLAY_STEEL);
        machine(output, "blast_furnace/steel", ClayiumMachineIds.CLAY_BLAST_FURNACE,
                List.of(new MachineIngredient(Ingredient.of(Items.IRON_INGOT), 1),
                        new MachineIngredient(Ingredient.of(Items.COAL, Items.CHARCOAL), 2)),
                List.of(stack(item("steel_ingot"), 1)), 500, 10_000, ClayTier.PRECISION);
        machine(output, "blast_furnace/steel_from_iron_dust", ClayiumMachineIds.CLAY_BLAST_FURNACE,
                List.of(new MachineIngredient(Ingredient.of(ItemTags.create(
                                ResourceLocation.fromNamespaceAndPath("c", "dusts/iron"))), 1),
                        new MachineIngredient(Ingredient.of(Items.COAL, Items.CHARCOAL), 2)),
                List.of(stack(item("steel_ingot"), 1)), 500, 10_000, ClayTier.PRECISION);
        for (String material : List.of("clay_steel", "steel", "beryllium", "manganese", "calcium", "potassium", "hafnium", "strontium", "barium")) {
            ClayTier required = List.of("calcium", "potassium").contains(material)
                    ? ClayTier.ADVANCED
                    : material.equals("clay_steel") || material.equals("steel")
                            || material.equals("beryllium") || material.equals("hafnium")
                            ? ClayTier.PRECISION : ClayTier.CLAY_STEEL;
            long ce = required == ClayTier.CLAY_STEEL ? 200_000 : energy(required.progressionIndex());
            int time = required == ClayTier.CLAY_STEEL ? 1_000 : 500;
            machine(output, "blast_furnace/" + material + "_dust", ClayiumMachineIds.CLAY_BLAST_FURNACE,
                    List.of(ingredient(item(material + "_dust"), 1)),
                    List.of(stack(item(material + "_ingot"), 1)), time, ce, required);
        }
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
        for (int tier = 4; tier <= 7; tier++) {
            ClayTier clayTier = ClayTier.byLegacyIndex(tier);
            machine(output, "salt_extractor/" + clayTier.id(), ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.LOGISTICS_BLOCKS.get(clayTier.id() + "_buffer").get(), 1),
                            ingredient(component("basic_circuit"), 1)),
                    List.of(stack(ClayiumRegistries.SALT_EXTRACTOR_BLOCKS.get(clayTier.id() + "_salt_extractor").get(), 1)),
                    40, energy(tier), ClayTier.BASIC);
        }
        machine(output, "machine/basic_chemical_reactor", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("basic_machine_hull").get(), 1), ingredient(component("basic_circuit"), 1)),
                List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("basic_chemical_reactor").get(), 1)),
                120, energy(4), ClayTier.BASIC);
        machine(output, "machine/advanced_chemical_reactor", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("advanced_machine_hull").get(), 1), ingredient(component("basic_circuit"), 2)),
                List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("advanced_chemical_reactor").get(), 1)),
                120, energy(5), ClayTier.BASIC);
        machine(output, "machine/precision_electrolysis_reactor", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("advanced_chemical_reactor").get(), 1), ingredient(component("precision_circuit"), 1)),
                List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("precision_electrolysis_reactor").get(), 1)),
                40, energy(6), ClayTier.BASIC);
        machine(output, "machine/precision_alloy_smelter", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("precision_smelter").get(), 1),
                        ingredient(component("precision_circuit"), 1)),
                List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("precision_alloy_smelter").get(), 1)),
                40, energy(6), ClayTier.BASIC);
        machine(output, "machine/az91d_machine_hull", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(item("az91d_large_plate"), 4), ingredient(component("precision_circuit"), 1)),
                List.of(stack(ClayiumRegistries.OTHER_HULL_BLOCKS.get("az91d_machine_hull").get(), 1)),
                120, energy(6), ClayTier.BASIC);
        for (String prefix : List.of("advanced", "precision", "clay_steel")) {
            ClayTier tier = ClayTier.byIdOrRaw(prefix);
            machine(output, "machine/" + prefix + "_clay_interface", ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get(prefix + "_machine_hull").get(), 1),
                            ingredient(ClayiumRegistries.LOGISTICS_BLOCKS.get("precision_buffer").get(), 1)),
                    List.of(stack(ClayiumRegistries.MACHINE_INTERFACE_BLOCKS.get(prefix + "_clay_interface").get(), 1)),
                    40, energy(tier.progressionIndex()), ClayTier.BASIC);
            machine(output, "machine/" + prefix + "_redstone_interface", ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.MACHINE_INTERFACE_BLOCKS.get(prefix + "_clay_interface").get(), 1),
                            ingredient(component("energetic_clay_dust"), 16)),
                    List.of(stack(ClayiumRegistries.REDSTONE_INTERFACE_BLOCKS.get(prefix + "_redstone_interface").get(), 1)),
                    40, energy(tier.progressionIndex()), ClayTier.BASIC);
        }
        machine(output, "machine/advanced_solar_clay_fabricator_mk1", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("advanced_machine_hull").get(), 1),
                        ingredient(component("silicon_plate"), 8)),
                List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("advanced_solar_clay_fabricator_mk1").get(), 1)),
                120, energy(5), ClayTier.BASIC);
        machine(output, "machine/precision_solar_clay_fabricator_mk2", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("precision_machine_hull").get(), 1),
                        ingredient(component("silicon_plate"), 16)),
                List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("precision_solar_clay_fabricator_mk2").get(), 1)),
                120, energy(6), ClayTier.BASIC);
        machine(output, "machine/clay_steel_electrolysis_reactor", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("advanced_chemical_reactor").get(), 1),
                        ingredient(component("integrated_circuit"), 1)),
                List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("clay_steel_electrolysis_reactor").get(), 1)),
                40, energy(7), ClayTier.PRECISION);
        machine(output, "machine/clay_steel_lithium_solar_clay_fabricator", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("clay_steel_machine_hull").get(), 1),
                        ingredient(component("silicon_plate"), 16)),
                List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("clay_steel_lithium_solar_clay_fabricator").get(), 1)),
                120, energy(7), ClayTier.PRECISION);
        machine(output, "machine/clay_blast_furnace", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("precision_smelter").get(), 1),
                        ingredient(ClayiumRegistries.MACHINE_INTERFACE_BLOCKS.get("precision_clay_interface").get(), 1)),
                List.of(stack(ClayiumRegistries.CLAY_BLAST_FURNACE.get(), 1)),
                120, energy(6), ClayTier.PRECISION);
        machine(output, "machine/clay_steel_bending_machine", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("clay_steel_machine_hull").get(), 1),
                        ingredient(ClayiumRegistries.DENSE_CLAY_PLATE.get(), 9)),
                List.of(stack(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("clay_steel_bending_machine").get(), 1)),
                120, energy(7), ClayTier.PRECISION);
        machine(output, "machine/clay_steel_smelter", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("clay_steel_machine_hull").get(), 1),
                        ingredient(component("simple_circuit"), 4)),
                List.of(stack(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("clay_steel_smelter").get(), 1)),
                120, energy(7), ClayTier.PRECISION);
        machine(output, "machine/laser_parts", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(component("clay_energy_excitor"), 1),
                        ingredient(component("integrated_circuit"), 1)),
                List.of(stack(component("laser_parts"), 1)), 20, energy(6), ClayTier.PRECISION);
        machine(output, "machine/advanced_auto_clay_condenser", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.LOGISTICS_BLOCKS.get("advanced_buffer").get(), 1),
                        ingredient(component("advanced_circuit"), 1)),
                List.of(stack(ClayiumRegistries.ADVANCED_AUTO_CLAY_CONDENSER.get(), 1)),
                40, energy(5), ClayTier.BASIC);
        machine(output, "machine/advanced_auto_crafter", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("basic_assembler").get(), 1),
                        ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("advanced_machine_hull").get(), 1)),
                List.of(stack(ClayiumRegistries.AUTO_CRAFTER_BLOCKS.get("advanced_auto_crafter").get(), 1)),
                40, energy(5), ClayTier.BASIC);
        machine(output, "machine/precision_auto_crafter", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.AUTO_CRAFTER_BLOCKS.get("advanced_auto_crafter").get(), 1),
                        ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("precision_machine_hull").get(), 1)),
                List.of(stack(ClayiumRegistries.AUTO_CRAFTER_BLOCKS.get("precision_auto_crafter").get(), 1)),
                40, energy(6), ClayTier.BASIC);
        machine(output, "machine/clay_steel_auto_crafter", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.AUTO_CRAFTER_BLOCKS.get("precision_auto_crafter").get(), 1),
                        ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("clay_steel_machine_hull").get(), 1)),
                List.of(stack(ClayiumRegistries.AUTO_CRAFTER_BLOCKS.get("clay_steel_auto_crafter").get(), 1)),
                40, energy(7), ClayTier.PRECISION);
        machine(output, "machine/precision_chemical_metal_separator", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("advanced_chemical_reactor").get(), 1),
                        ingredient(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("precision_smelter").get(), 1)),
                List.of(stack(ClayiumRegistries.PRECISION_CHEMICAL_METAL_SEPARATOR.get(), 1)),
                40, energy(6), ClayTier.BASIC);
        machine(output, "machine/manipulator", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(item("az91d_ingot"), 16), ingredient(component("precision_circuit"), 1)),
                List.of(stack(item("manipulator"), 1)), 20, energy(4), ClayTier.BASIC);
    }

    private static void machine(RecipeOutput output, String id, net.minecraft.resources.ResourceLocation machine,
                                List<MachineIngredient> inputs, List<ItemStack> outputs,
                                int time, long energy, ClayTier minimumTier) {
        output.accept(Clayium.id(id), new MachineRecipe(
                machine, new ArrayList<>(inputs), new ArrayList<>(outputs), time, energy, minimumTier), null);
    }

    private static ItemLike item(String id) { return ClayiumRegistries.MATERIAL_ITEMS.get(id).get(); }
    private static ItemLike component(String id) { return ClayiumRegistries.COMPONENT_ITEMS.get(id).get(); }
    private static MachineIngredient ingredient(ItemLike item, int count) {
        return new MachineIngredient(Ingredient.of(item), count);
    }
    private static ItemStack stack(ItemLike item, int count) { return new ItemStack(item, count); }
    private static long energy(int tier) { return (long) Math.pow(10, tier - 2); }
}
