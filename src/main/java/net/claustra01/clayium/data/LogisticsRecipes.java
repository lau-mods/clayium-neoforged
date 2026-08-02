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

/** Assembler recipes for logistics devices, filters, and configuration tools. */
public final class LogisticsRecipes {
    private LogisticsRecipes() {
    }

    public static void build(RecipeOutput output) {
        logisticsTier(output, "basic", "industrial_clay_plate", "industrial_clay_large_plate", "basic_circuit", 4, 100);
        logisticsTier(output, "advanced", material("impure_silicon_plate"), material("impure_silicon_large_plate"),
                item("advanced_circuit"), 1_000);
        logisticsTier(output, "precision", material("aluminium_plate"), material("aluminium_large_plate"),
                item("precision_circuit"), 10_000);
        logisticsTier(output, "clay_steel", material("clay_steel_plate"), material("clay_steel_large_plate"),
                item("integrated_circuit"), 100_000);
        logisticsTier(output, "clayium", material("clayium_plate"), material("clayium_large_plate"),
                item("clay_core"), 1_000_000);
        logisticsTier(output, "ultimate", material("ultimate_alloy_plate"), material("ultimate_alloy_large_plate"),
                item("clay_brain"), 10_000_000);
        recipe(output, "logistics/clay_steel_distributor",
                List.of(ingredient(ClayiumRegistries.LOGISTICS_BLOCKS.get("clay_steel_buffer").get(), 1),
                        ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get("clay_steel_machine_hull").get(), 1)),
                ClayiumRegistries.LOGISTICS_BLOCKS.get("clay_steel_distributor").get(), 1,
                100_000, 120, 6);
        recipe(output, "logistics/clay_steel_distributor_integrated_circuit",
                List.of(ingredient(ClayiumRegistries.LOGISTICS_BLOCKS.get("clay_steel_buffer").get(), 1),
                        ingredient(item("integrated_circuit"), 1)),
                ClayiumRegistries.LOGISTICS_BLOCKS.get("clay_steel_distributor").get(), 1,
                100_000, 120, 6);
        for (String tier : List.of("clayium", "ultimate")) {
            long energy = tier.equals("clayium") ? 1_000_000L : 10_000_000L;
            ItemLike distributor = ClayiumRegistries.LOGISTICS_BLOCKS.get(tier + "_distributor").get();
            ItemLike buffer = ClayiumRegistries.LOGISTICS_BLOCKS.get(tier + "_buffer").get();
            recipe(output, "logistics/" + tier + "_distributor",
                    List.of(ingredient(buffer, 1),
                            ingredient(ClayiumRegistries.MACHINE_HULL_BLOCKS.get(tier + "_machine_hull").get(), 1)),
                    distributor, 1, energy, 120, 6);
            recipe(output, "logistics/" + tier + "_distributor_integrated_circuit",
                    List.of(ingredient(buffer, 1), ingredient(item("integrated_circuit"), 1)),
                    distributor, 1, energy, 120, 6);
        }
        recipe(output, "logistics/tools/clay_io_tool",
                List.of(ingredient(ClayiumRegistries.CLAY_ROLLING_PIN.get(), 1),
                        ingredient(ClayiumRegistries.CLAY_SLICER.get(), 1)),
                ClayiumRegistries.CLAY_IO_TOOL.get(), 1, 10_000, 20, 6);
        recipe(output, "logistics/tools/clay_piping_tool",
                List.of(ingredient(ClayiumRegistries.CLAY_SPATULA.get(), 1),
                        ingredient(ClayiumRegistries.CLAY_WRENCH.get(), 1)),
                ClayiumRegistries.CLAY_PIPING_TOOL.get(), 1, 10_000, 20, 6);
        recipe(output, "logistics/tools/io_memory_card",
                List.of(ingredient(ClayiumRegistries.CLAY_IO_TOOL.get(), 1),
                        ingredient(item("precision_circuit"), 2)),
                ClayiumRegistries.IO_MEMORY_CARD.get(), 1, 10_000, 20, 6);
        recipe(output, "logistics/tools/filter_whitelist",
                List.of(ingredient(item("industrial_clay_plate"), 3),
                        ingredient(item("advanced_circuit"), 1)),
                ClayiumRegistries.FILTER_WHITELIST.get(), 1, 8, 20, 4);
        recipe(output, "logistics/tools/filter_item_name",
                List.of(ingredient(item("industrial_clay_plate"), 3),
                        ingredient(item("precision_circuit"), 1)),
                ClayiumRegistries.FILTER_ITEM_NAME.get(), 1, 8, 20, 4);
        recipe(output, "logistics/tools/filter_fuzzy",
                List.of(ingredient(item("industrial_clay_plate"), 3),
                        ingredient(item("integrated_circuit"), 1)),
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
        logisticsTier(output, tier, item(plate), item(largePlate), item(circuit), energy);
    }

    private static void logisticsTier(
            RecipeOutput output,
            String tier,
            ItemLike plate,
            ItemLike largePlate,
            ItemLike circuit,
            long energy) {
        ItemLike buffer = ClayiumRegistries.LOGISTICS_BLOCKS.get(tier + "_buffer").get();
        ItemLike multitrack = ClayiumRegistries.LOGISTICS_BLOCKS.get(tier + "_multitrack_buffer").get();
        recipe(output, "logistics/" + tier + "_buffer",
                List.of(ingredient(plate, 1), ingredient(circuit, 1)),
                buffer, 16, energy, 40, 4);
        recipe(output, "logistics/" + tier + "_multitrack_buffer",
                List.of(ingredient(buffer, 6), ingredient(largePlate, 1)),
                multitrack, 1, energy, 40, 4);
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
        return ClayiumRegistries.COMPONENT_ITEMS.get(id).get();
    }

    private static ItemLike material(String id) {
        if (ClayiumRegistries.COMPONENT_ITEMS.containsKey(id)) return ClayiumRegistries.COMPONENT_ITEMS.get(id).get();
        return ClayiumRegistries.MATERIAL_ITEMS.get(id).get();
    }
}
