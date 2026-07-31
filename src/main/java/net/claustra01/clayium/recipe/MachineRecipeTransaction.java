/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.world.item.ItemStack;

/** Validates every input, output, tier, and CE condition before committing. */
public final class MachineRecipeTransaction {
    private MachineRecipeTransaction() {
    }

    public enum FailureReason {
        NONE,
        EMPTY_INPUT,
        INPUT_MISMATCH,
        TIER_TOO_LOW,
        INSUFFICIENT_CLAY_ENERGY,
        OUTPUT_BLOCKED
    }

    public record Result(
            FailureReason failureReason,
            List<ItemStack> remainingInputs,
            List<ItemStack> resultingOutputs,
            long clayEnergyConsumed) {
        public Result {
            Objects.requireNonNull(failureReason, "failureReason");
            remainingInputs = copyStacks(remainingInputs);
            resultingOutputs = copyStacks(resultingOutputs);
            if (clayEnergyConsumed < 0) {
                throw new IllegalArgumentException("Consumed Clay Energy must not be negative");
            }
        }

        public boolean successful() {
            return failureReason == FailureReason.NONE;
        }

        @Override
        public List<ItemStack> remainingInputs() {
            return copyStacks(remainingInputs);
        }

        @Override
        public List<ItemStack> resultingOutputs() {
            return copyStacks(resultingOutputs);
        }
    }

    public static Result execute(
            MachineRecipe recipe,
            List<ItemStack> inputs,
            List<ItemStack> outputs,
            ClayTier availableTier,
            ClayEnergyStorage clayEnergy,
            boolean simulate) {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(inputs, "inputs");
        Objects.requireNonNull(outputs, "outputs");
        Objects.requireNonNull(availableTier, "availableTier");
        Objects.requireNonNull(clayEnergy, "clayEnergy");

        if (inputs.isEmpty() || inputs.stream().allMatch(ItemStack::isEmpty)) {
            return failure(FailureReason.EMPTY_INPUT, inputs, outputs);
        }
        MachineRecipeInput recipeInput = new MachineRecipeInput(inputs);
        var match = recipe.matchInputSlots(recipeInput);
        if (match.isEmpty()) {
            return failure(FailureReason.INPUT_MISMATCH, inputs, outputs);
        }
        if (!availableTier.isAtLeast(recipe.minimumTier())) {
            return failure(FailureReason.TIER_TOO_LOW, inputs, outputs);
        }
        if (outputs.size() < recipe.results().size()) {
            return failure(FailureReason.OUTPUT_BLOCKED, inputs, outputs);
        }

        long totalEnergy = Math.multiplyExact(recipe.processingTimeTicks(), recipe.clayEnergyPerTick());
        if (clayEnergy.extract(totalEnergy, true) != totalEnergy) {
            return failure(FailureReason.INSUFFICIENT_CLAY_ENERGY, inputs, outputs);
        }

        List<ItemStack> nextOutputs = copyStacks(outputs);
        List<ItemStack> recipeResults = recipe.results();
        for (int index = 0; index < recipeResults.size(); index++) {
            ItemStack result = recipeResults.get(index);
            ItemStack current = nextOutputs.get(index);
            if (current.isEmpty()) {
                nextOutputs.set(index, result.copy());
            } else if (!ItemStack.isSameItemSameComponents(current, result)
                    || result.getCount() > current.getMaxStackSize() - current.getCount()) {
                return failure(FailureReason.OUTPUT_BLOCKED, inputs, outputs);
            } else {
                current.grow(result.getCount());
            }
        }

        List<ItemStack> nextInputs = copyStacks(inputs);
        int[] matchedSlots = match.get();
        for (int ingredientIndex = 0; ingredientIndex < recipe.ingredients().size(); ingredientIndex++) {
            nextInputs.get(matchedSlots[ingredientIndex])
                    .shrink(recipe.ingredients().get(ingredientIndex).count());
        }

        if (!simulate && clayEnergy.extract(totalEnergy, false) != totalEnergy) {
            throw new IllegalStateException("Clay Energy changed between transaction validation and commit");
        }
        return new Result(FailureReason.NONE, nextInputs, nextOutputs, totalEnergy);
    }

    private static Result failure(
            FailureReason reason,
            List<ItemStack> inputs,
            List<ItemStack> outputs) {
        return new Result(reason, inputs, outputs, 0);
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        Objects.requireNonNull(stacks, "stacks");
        List<ItemStack> copies = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copies.add(Objects.requireNonNull(stack, "stack").copy());
        }
        return copies;
    }
}
