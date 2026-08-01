/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import java.util.Objects;
import java.util.Optional;
import java.util.List;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/**
 * Resolves recipes from the current level RecipeManager.
 *
 * <p>The caller may retain the returned recipe ID, but must resolve the recipe
 * again from the RecipeManager after a datapack reload.</p>
 */
public final class MachineRecipeLookup {
    private MachineRecipeLookup() {
    }

    public static Optional<RecipeHolder<MachineRecipe>> find(
            Level level,
            ResourceLocation machineId,
            ClayTier availableTier,
            List<ItemStack> inputs) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(machineId, "machineId");
        Objects.requireNonNull(availableTier, "availableTier");
        Objects.requireNonNull(inputs, "inputs");
        if (inputs.isEmpty() || inputs.stream().allMatch(ItemStack::isEmpty)) {
            return Optional.empty();
        }
        if (machineId.equals(ClayiumMachineIds.SMELTER)) {
            MachineRecipeInput recipeInput = new MachineRecipeInput(inputs);
            Optional<RecipeHolder<MachineRecipe>> clayiumRecipe = level.getRecipeManager()
                    .getAllRecipesFor(ClayiumRecipes.MACHINE_RECIPE_TYPE.get())
                    .stream()
                    .filter(holder -> holder.value().machine().equals(machineId))
                    .filter(holder -> availableTier.isAtLeast(holder.value().minimumTier()))
                    .filter(holder -> holder.value().matches(recipeInput, level))
                    .findFirst();
            return clayiumRecipe.isPresent()
                    ? clayiumRecipe
                    : SmelterRecipeAdapter.find(level, inputs.getFirst());
        }

        MachineRecipeInput recipeInput = new MachineRecipeInput(inputs);
        return level.getRecipeManager()
                .getAllRecipesFor(ClayiumRecipes.MACHINE_RECIPE_TYPE.get())
                .stream()
                .filter(holder -> holder.value().machine().equals(machineId))
                .filter(holder -> availableTier.isAtLeast(holder.value().minimumTier()))
                .filter(holder -> holder.value().matches(recipeInput, level))
                .findFirst();
    }

    public static Optional<RecipeHolder<MachineRecipe>> find(
            Level level,
            ResourceLocation machineId,
            ClayTier availableTier,
            ItemStack input) {
        return find(level, machineId, availableTier, List.of(input));
    }
}
