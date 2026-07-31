/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** A machine ingredient together with the number of items consumed. */
public record MachineIngredient(Ingredient ingredient, int count) {
    public static final Codec<MachineIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MachineIngredient::ingredient),
            Codec.intRange(1, 64).fieldOf("count").forGetter(MachineIngredient::count)
    ).apply(instance, MachineIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineIngredient> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC,
                    MachineIngredient::ingredient,
                    ByteBufCodecs.VAR_INT,
                    MachineIngredient::count,
                    MachineIngredient::new);

    public MachineIngredient {
        if (ingredient == null || ingredient.isEmpty()) {
            throw new IllegalArgumentException("Machine ingredient must not be empty");
        }
        if (count < 1 || count > 64) {
            throw new IllegalArgumentException("Machine ingredient count must be between 1 and 64");
        }
    }

    public boolean test(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() >= count && ingredient.test(stack);
    }
}
