/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import java.util.List;
import java.util.Optional;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

/**
 * Presents every currently loaded furnace-smelting recipe as an original-style
 * Clayium Smelter recipe. This keeps datapack and other-mod furnace recipes
 * interoperable just as the 1.7.10 implementation delegated to FurnaceRecipes.
 */
public final class SmelterRecipeAdapter {
    public static final long CLAY_ENERGY_PER_TICK = 4;
    public static final int PROCESSING_TIME_TICKS = 200;

    private SmelterRecipeAdapter() {
    }

    public static Optional<RecipeHolder<MachineRecipe>> find(Level level, ItemStack input) {
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level)
                .map(holder -> adapt(holder, level));
    }

    public static List<MachineRecipe> all(Level level) {
        return level.getRecipeManager()
                .getAllRecipesFor(RecipeType.SMELTING)
                .stream()
                .map(holder -> adapt(holder, level).value())
                .toList();
    }

    private static RecipeHolder<MachineRecipe> adapt(RecipeHolder<SmeltingRecipe> holder, Level level) {
        ItemStack result = holder.value().getResultItem(level.registryAccess()).copy();
        MachineRecipe recipe = new MachineRecipe(
                ClayiumMachineIds.SMELTER,
                List.of(new MachineIngredient(holder.value().getIngredients().getFirst(), 1)),
                List.of(result),
                PROCESSING_TIME_TICKS,
                CLAY_ENERGY_PER_TICK,
                ClayTier.RAW);
        ResourceLocation source = holder.id();
        return new RecipeHolder<>(
                Clayium.id("smelter/" + source.getNamespace() + "/" + source.getPath()),
                recipe);
    }
}
