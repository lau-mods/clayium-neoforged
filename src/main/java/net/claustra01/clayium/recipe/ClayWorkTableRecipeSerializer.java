/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public enum ClayWorkTableRecipeSerializer implements RecipeSerializer<ClayWorkTableRecipe> {
    INSTANCE;

    @Override
    public MapCodec<ClayWorkTableRecipe> codec() {
        return ClayWorkTableRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ClayWorkTableRecipe> streamCodec() {
        return ClayWorkTableRecipe.STREAM_CODEC;
    }
}
