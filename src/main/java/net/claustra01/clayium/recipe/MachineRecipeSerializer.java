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

public enum MachineRecipeSerializer implements RecipeSerializer<MachineRecipe> {
    INSTANCE;

    @Override
    public MapCodec<MachineRecipe> codec() {
        return MachineRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec() {
        return MachineRecipe.STREAM_CODEC;
    }
}
