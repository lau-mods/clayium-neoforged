package net.claustra01.clayium.recipe;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.ClayiumConfig;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public final class ProgressionRateRules {
    public static long adjustedRecipeTicks(ClayiumMachineRecipe recipe) {
        return recipe.progressionRate().scalesTicks()
                ? ClayiumConfig.divideProgressionTicks(recipe.processingTicks())
                : recipe.processingTicks();
    }

    public static List<ItemStack> adjustedRecipeOutputs(ClayiumMachineRecipe recipe) {
        return adjustedRecipeOutputs(recipe, recipe.itemOutputs());
    }

    public static List<ItemStack> adjustedRecipeOutputs(
            ClayiumMachineRecipe recipe, List<ItemStack> baseOutputs) {
        if (!recipe.progressionRate().scalesOutputs()) {
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

    public static ClayiumMachineRecipe adjustedRecipe(ClayiumMachineRecipe recipe) {
        if (!recipe.progressionRate().scalesStacks()) {
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
        return copy(recipe, inputs, outputs, recipe.processingTicks());
    }

    public static ClayiumMachineRecipe adjustedRecipeForDisplay(ClayiumMachineRecipe recipe) {
        ClayiumMachineRecipe stacks = adjustedRecipe(recipe);
        List<ItemStack> outputs = adjustedRecipeOutputs(stacks);
        long ticks = adjustedRecipeTicks(stacks);
        if (outputs == stacks.itemOutputs() && ticks == stacks.processingTicks()) {
            return stacks;
        }
        return copy(stacks, stacks.itemInputs(), outputs, ticks);
    }

    public static boolean requiresExactItemCount(ClayiumMachineRecipe recipe) {
        return recipe.progressionRate().scalesStacks();
    }

    private static ClayiumMachineRecipe copy(
            ClayiumMachineRecipe recipe,
            List<SizedIngredient> inputs,
            List<ItemStack> outputs,
            long ticks) {
        return new ClayiumMachineRecipe(
                recipe.processingMethod(), recipe.requiredTier(), inputs, recipe.fluidInputs(), outputs,
                recipe.fluidOutputs(), recipe.clayEnergy(), ticks, recipe.environment(), recipe.randomOutputs(),
                recipe.progressionRate());
    }

    private ProgressionRateRules() {
    }
}
