/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Phase 5 recipes whose material tiers are already reachable in the current vertical slice. */
public final class Phase5MachineRecipes {
    private Phase5MachineRecipes() {
    }

    public static void build(RecipeOutput output) {
        logisticsTier(output, "basic", "industrial_clay_plate", "industrial_clay_large_plate", "basic_circuit", 4, 100);
        logisticsTier(output, "advanced", "advanced_industrial_clay_plate", "advanced_industrial_clay_large_plate",
                "advanced_circuit", 5, 1_000);
        recipe(output, "phase5/tools/clay_io_tool",
                List.of(ingredient(ClayiumRegistries.CLAY_ROLLING_PIN.get(), 1),
                        ingredient(ClayiumRegistries.CLAY_SLICER.get(), 1)),
                ClayiumRegistries.CLAY_IO_TOOL.get(), 1, 10_000, 20, 6);
        recipe(output, "phase5/tools/clay_piping_tool",
                List.of(ingredient(ClayiumRegistries.CLAY_SPATULA.get(), 1),
                        ingredient(ClayiumRegistries.CLAY_WRENCH.get(), 1)),
                ClayiumRegistries.CLAY_PIPING_TOOL.get(), 1, 10_000, 20, 6);
        recipe(output, "phase5/tools/io_memory_card",
                List.of(ingredient(ClayiumRegistries.CLAY_IO_TOOL.get(), 1),
                        ingredient(item("precision_circuit"), 2)),
                ClayiumRegistries.IO_MEMORY_CARD.get(), 1, 10_000, 20, 6);
        recipe(output, "phase5/tools/filter_whitelist",
                List.of(ingredient(item("industrial_clay_plate"), 3),
                        ingredient(item("basic_circuit"), 1)),
                ClayiumRegistries.FILTER_WHITELIST.get(), 1, 8, 20, 4);
        recipe(output, "phase5/tools/filter_item_name",
                List.of(ingredient(item("industrial_clay_plate"), 3),
                        ingredient(item("advanced_circuit"), 1)),
                ClayiumRegistries.FILTER_ITEM_NAME.get(), 1, 8, 20, 4);
        recipe(output, "phase5/tools/filter_fuzzy",
                List.of(ingredient(item("industrial_clay_plate"), 3),
                        ingredient(item("precision_circuit"), 1)),
                ClayiumRegistries.FILTER_FUZZY.get(), 1, 8, 20, 4);
    }

    private static void logisticsTier(
            RecipeOutput output,
            String tier,
            String plate,
            String largePlate,
            String circuit,
            int minimumTier,
            long energy) {
        ItemLike buffer = ClayiumRegistries.PHASE5_LOGISTICS_BLOCKS.get(tier + "_buffer").get();
        ItemLike multitrack = ClayiumRegistries.PHASE5_LOGISTICS_BLOCKS.get(tier + "_multitrack_buffer").get();
        recipe(output, "phase5/" + tier + "_buffer",
                List.of(ingredient(item(plate), 1), ingredient(item(circuit), 1)),
                buffer, 16, energy, 40, minimumTier);
        recipe(output, "phase5/" + tier + "_multitrack_buffer",
                List.of(ingredient(buffer, 6), ingredient(item(largePlate), 1)),
                multitrack, 1, energy, 40, minimumTier);
    }

    private static void recipe(
            RecipeOutput output,
            String id,
            List<MachineIngredient> ingredients,
            ItemLike result,
            int count,
            long energy,
            int time,
            int tier) {
        output.accept(
                Clayium.id(id),
                new MachineRecipe(
                        ClayiumMachineIds.ASSEMBLER,
                        new ArrayList<>(ingredients),
                        new ArrayList<>(List.of(new ItemStack(result, count))),
                        time,
                        energy,
                        ClayTier.byLegacyIndex(tier)),
                null);
    }

    private static MachineIngredient ingredient(ItemLike item, int count) {
        return new MachineIngredient(Ingredient.of(item), count);
    }

    private static ItemLike item(String id) {
        return ClayiumRegistries.PHASE4_ITEMS.get(id).get();
    }
}
