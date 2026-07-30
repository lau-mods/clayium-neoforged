/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import java.util.Objects;
import java.util.Optional;
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
            ItemStack input) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(machineId, "machineId");
        Objects.requireNonNull(availableTier, "availableTier");
        Objects.requireNonNull(input, "input");
        if (input.isEmpty()) {
            return Optional.empty();
        }

        MachineRecipeInput recipeInput = new MachineRecipeInput(input);
        return level.getRecipeManager()
                .getAllRecipesFor(ClayiumRecipes.MACHINE_RECIPE_TYPE.get())
                .stream()
                .filter(holder -> holder.value().machine().equals(machineId))
                .filter(holder -> availableTier.isAtLeast(holder.value().minimumTier()))
                .filter(holder -> holder.value().matches(recipeInput, level))
                .findFirst();
    }
}
