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
        machineConstruction(output);
    }

    private static void fluidBuffers(RecipeOutput output) {
        for (int tier = 4; tier <= 6; tier++) {
            ClayTier clayTier = ClayTier.byLegacyIndex(tier);
            machine(output, "fluid_buffer/" + clayTier.id(), ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.PHASE5_LOGISTICS_BLOCKS.get(clayTier.id() + "_buffer").get(), 1),
                            ingredient(ClayiumRegistries.PHASE4_ITEMS.get("dense_clay_pipe").get(), 4)),
                    List.of(stack(ClayiumRegistries.FLUID_BUFFER_BLOCKS.get(clayTier.id() + "_fluid_buffer").get(), 1)),
                    40, energy(tier), clayTier);
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

    private static void machineConstruction(RecipeOutput output) {
        for (int tier = 4; tier <= 6; tier++) {
            ClayTier clayTier = ClayTier.byLegacyIndex(tier);
            machine(output, "salt_extractor/" + clayTier.id(), ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.PHASE5_LOGISTICS_BLOCKS.get(clayTier.id() + "_buffer").get(), 1),
                            ingredient(phase4("basic_circuit"), 1)),
                    List.of(stack(ClayiumRegistries.SALT_EXTRACTOR_BLOCKS.get(clayTier.id() + "_salt_extractor").get(), 1)),
                    40, energy(tier), clayTier);
        }
        machine(output, "machine/basic_chemical_reactor", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("basic_machine_hull").get(), 1), ingredient(phase4("basic_circuit"), 1)),
                List.of(stack(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("basic_chemical_reactor").get(), 1)),
                120, energy(4), ClayTier.BASIC);
        machine(output, "machine/advanced_chemical_reactor", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("advanced_machine_hull").get(), 1), ingredient(phase4("basic_circuit"), 2)),
                List.of(stack(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("advanced_chemical_reactor").get(), 1)),
                120, energy(5), ClayTier.ADVANCED);
        machine(output, "machine/precision_electrolysis_reactor", ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("advanced_chemical_reactor").get(), 1), ingredient(phase4("precision_circuit"), 1)),
                List.of(stack(ClayiumRegistries.PHASE6_MACHINE_BLOCKS.get("precision_electrolysis_reactor").get(), 1)),
                40, energy(6), ClayTier.PRECISION);
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
