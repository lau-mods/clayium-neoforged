package net.claustra01.clayium.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** Original ProgressionRate call sites that cannot be represented by tier-wide machine multipliers. */
public final class ProgressionRateRules {
    private static final Set<String> SCALED_RECIPE_PATHS = Set.of(
            "bending_machine/small_clay_disc",
            "bending_machine/clay_disc",
            "bending_machine/clay_plate",
            "bending_machine/large_clay_plate",
            "bending_machine/dense_clay_plate",
            "bending_machine/large_dense_clay_plate",
            "bending_machine/industrial_clay_plate",
            "bending_machine/large_industrial_clay_plate",
            "bending_machine/advanced_industrial_clay_plate",
            "bending_machine/large_advanced_industrial_clay_plate",
            "bending_machine/clay_blade",
            "bending_machine/dense_clay_blade",
            "wire_drawing_machine/clay_stick",
            "wire_drawing_machine/clay_cylinder_to_sticks",
            "wire_drawing_machine/clay_pipe_to_sticks",
            "wire_drawing_machine/dense_clay_cylinder_to_sticks",
            "wire_drawing_machine/dense_clay_pipe_to_sticks",
            "wire_drawing_machine/clay_disc_to_stick",
            "wire_drawing_machine/dense_clay_disc_to_stick",
            "pipe_drawing_machine/clay_pipe",
            "pipe_drawing_machine/dense_clay_pipe",
            "cutting_machine/clay_disc",
            "cutting_machine/short_clay_stick",
            "cutting_machine/small_clay_disc",
            "cutting_machine/short_dense_clay_stick",
            "cutting_machine/small_dense_clay_disc",
            "cutting_machine/clay_cylinder_to_discs",
            "cutting_machine/large_clay_plate_to_discs",
            "cutting_machine/dense_clay_cylinder_to_discs",
            "cutting_machine/large_dense_clay_plate_to_discs",
            "lathe/short_clay_stick",
            "lathe/clay_ring",
            "lathe/dense_clay_ring",
            "lathe/clay_cylinder",
            "lathe/clay_needle",
            "lathe/clay_needle_to_sticks",
            "lathe/small_clay_ring",
            "lathe/dense_clay_cylinder",
            "lathe/dense_clay_needle",
            "lathe/dense_clay_needle_to_sticks",
            "lathe/small_dense_clay_ring",
            "assembler/clay_gear",
            "assembler/clay_spindle",
            "assembler/clay_grinding_head",
            "assembler/clay_cutting_head",
            "assembler/dense_clay_spindle",
            "assembler/dense_clay_gear_from_sticks",
            "assembler/dense_clay_gear_from_short_sticks",
            "assembler/dense_clay_grinding_head",
            "assembler/dense_clay_cutting_head",
            "decomposer/industrial_clay_dust",
            "decomposer/advanced_industrial_clay_dust",
            "centrifuge/dense_clay_dust",
            "centrifuge/calcareous_clay",
            "centrifuge/industrial_clay",
            "centrifuge/advanced_industrial_clay",
            "grinder/clay_ore_to_compressed_clay_shard",
            "grinder/deepslate_clay_ore_to_compressed_clay_shard",
            "grinder/dense_clay_ore_to_industrial_clay_shard",
            "grinder/deepslate_dense_clay_ore_to_industrial_clay_shard",
            "grinder/large_dense_clay_ore_to_advanced_industrial_clay_shard",
            "grinder/deepslate_large_dense_clay_ore_to_advanced_industrial_clay_shard",
            "condenser/compressed_clay_shard",
            "condenser/industrial_clay_shard",
            "condenser/advanced_industrial_clay_shard",
            "progression/reactor/antimatter_seed",
            "ca_condenser/antimatter",
            "ca_reactor/pure_antimatter");

    private static final Set<String> SCALED_OUTPUT_PATHS = Set.of(
            "grinder/clay_ore_to_compressed_clay_shard",
            "grinder/deepslate_clay_ore_to_compressed_clay_shard",
            "grinder/dense_clay_ore_to_industrial_clay_shard",
            "grinder/deepslate_dense_clay_ore_to_industrial_clay_shard",
            "grinder/large_dense_clay_ore_to_advanced_industrial_clay_shard",
            "grinder/deepslate_large_dense_clay_ore_to_advanced_industrial_clay_shard");

    private static final Set<String> SCALED_STACK_RECIPE_PATHS = Set.of(
            "grinder/cobblestone_to_gravel_1",
            "grinder/cobblestone_to_gravel_4",
            "grinder/cobblestone_to_gravel_16",
            "grinder/cobblestone_to_gravel_64",
            "centrifuge/gravel_to_dense_clay_1",
            "centrifuge/gravel_to_dense_clay_4",
            "centrifuge/gravel_to_dense_clay_16",
            "centrifuge/gravel_to_dense_clay_64");

    public static long adjustedRecipeTicks(
            ResourceLocation recipeId, ClayiumRecipeMethod method, long baseTicks) {
        if (method == ClayiumRecipeMethod.CLAY_WORK_TABLE || isOriginalScaledRecipe(recipeId)) {
            return ClayiumConfig.divideProgressionTicks(baseTicks);
        }
        return baseTicks;
    }

    public static List<ItemStack> adjustedRecipeOutputs(
            ResourceLocation recipeId, List<ItemStack> baseOutputs) {
        if (!isClayiumPath(recipeId, SCALED_OUTPUT_PATHS)) {
            return baseOutputs;
        }
        List<ItemStack> adjusted = new ArrayList<>(baseOutputs.size());
        for (ItemStack output : baseOutputs) {
            ItemStack copy = output.copy();
            copy.setCount(Math.min(copy.getMaxStackSize(),
                    ClayiumConfig.multiplyProgressionCount(copy.getCount())));
            adjusted.add(copy);
        }
        return List.copyOf(adjusted);
    }

    public static ClayiumMachineRecipe adjustedRecipe(
            ResourceLocation recipeId, ClayiumMachineRecipe recipe) {
        if (!isClayiumPath(recipeId, SCALED_STACK_RECIPE_PATHS)) {
            return recipe;
        }
        List<SizedIngredient> inputs = recipe.itemInputs().stream()
                .map(input -> new SizedIngredient(
                        input.ingredient(), ClayiumConfig.multiplyProgressionCount(input.count())))
                .toList();
        List<ItemStack> outputs = recipe.itemOutputs().stream().map(stack -> {
            ItemStack copy = stack.copy();
            copy.setCount(ClayiumConfig.multiplyProgressionCount(copy.getCount()));
            return copy;
        }).toList();
        return new ClayiumMachineRecipe(
                recipe.processingMethod(), recipe.requiredTier(), inputs, recipe.fluidInputs(), outputs,
                recipe.fluidOutputs(), recipe.clayEnergy(), recipe.processingTicks(), recipe.environment(),
                recipe.randomOutputs());
    }

    /** Returns the same recipe values that execution uses after all original call-site adjustments. */
    public static ClayiumMachineRecipe adjustedRecipeForDisplay(
            ResourceLocation recipeId, ClayiumMachineRecipe recipe) {
        ClayiumMachineRecipe stacks = adjustedRecipe(recipeId, recipe);
        List<ItemStack> outputs = adjustedRecipeOutputs(recipeId, stacks.itemOutputs());
        long ticks = adjustedRecipeTicks(recipeId, stacks.processingMethod(), stacks.processingTicks());
        if (outputs == stacks.itemOutputs() && ticks == stacks.processingTicks()) {
            return stacks;
        }
        return new ClayiumMachineRecipe(
                stacks.processingMethod(), stacks.requiredTier(), stacks.itemInputs(), stacks.fluidInputs(), outputs,
                stacks.fluidOutputs(), stacks.clayEnergy(), ticks, stacks.environment(), stacks.randomOutputs());
    }

    public static boolean requiresExactItemCount(ResourceLocation recipeId) {
        return isClayiumPath(recipeId, SCALED_STACK_RECIPE_PATHS);
    }

    public static boolean isOriginalScaledRecipe(ResourceLocation recipeId) {
        return isClayiumPath(recipeId, SCALED_RECIPE_PATHS);
    }

    private static boolean isClayiumPath(ResourceLocation id, Set<String> paths) {
        return id.getNamespace().equals(ClayiumMod.MOD_ID) && paths.contains(id.getPath());
    }

    private ProgressionRateRules() {
    }
}
