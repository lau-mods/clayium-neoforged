/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import java.util.Objects;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.world.item.ItemStack;

/** Performs all validation before returning the item and CE state of a completion. */
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
            ItemStack remainingInput,
            ItemStack resultingOutput,
            long clayEnergyConsumed) {
        public Result {
            Objects.requireNonNull(failureReason, "failureReason");
            remainingInput = Objects.requireNonNull(remainingInput, "remainingInput").copy();
            resultingOutput = Objects.requireNonNull(resultingOutput, "resultingOutput").copy();
            if (clayEnergyConsumed < 0) {
                throw new IllegalArgumentException("Consumed Clay Energy must not be negative");
            }
        }

        public boolean successful() {
            return failureReason == FailureReason.NONE;
        }

        @Override
        public ItemStack remainingInput() {
            return remainingInput.copy();
        }

        @Override
        public ItemStack resultingOutput() {
            return resultingOutput.copy();
        }
    }

    public static Result execute(
            MachineRecipe recipe,
            ItemStack input,
            ItemStack output,
            ClayTier availableTier,
            ClayEnergyStorage clayEnergy,
            boolean simulate) {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(availableTier, "availableTier");
        Objects.requireNonNull(clayEnergy, "clayEnergy");

        if (input.isEmpty()) {
            return failure(FailureReason.EMPTY_INPUT, input, output);
        }
        if (!recipe.ingredient().test(input)) {
            return failure(FailureReason.INPUT_MISMATCH, input, output);
        }
        if (!availableTier.isAtLeast(recipe.minimumTier())) {
            return failure(FailureReason.TIER_TOO_LOW, input, output);
        }

        long totalEnergy = Math.multiplyExact(recipe.processingTimeTicks(), recipe.clayEnergyPerTick());
        if (clayEnergy.extract(totalEnergy, true) != totalEnergy) {
            return failure(FailureReason.INSUFFICIENT_CLAY_ENERGY, input, output);
        }

        ItemStack recipeResult = recipe.result();
        ItemStack nextOutput = output.copy();
        if (nextOutput.isEmpty()) {
            nextOutput = recipeResult;
        } else if (!ItemStack.isSameItemSameComponents(nextOutput, recipeResult)
                || recipeResult.getCount() > nextOutput.getMaxStackSize() - nextOutput.getCount()) {
            return failure(FailureReason.OUTPUT_BLOCKED, input, output);
        } else {
            nextOutput.grow(recipeResult.getCount());
        }

        ItemStack nextInput = input.copy();
        nextInput.shrink(1);
        if (!simulate && clayEnergy.extract(totalEnergy, false) != totalEnergy) {
            throw new IllegalStateException("Clay Energy changed between transaction validation and commit");
        }
        return new Result(FailureReason.NONE, nextInput, nextOutput, totalEnergy);
    }

    private static Result failure(FailureReason reason, ItemStack input, ItemStack output) {
        return new Result(reason, input, output, 0);
    }
}
