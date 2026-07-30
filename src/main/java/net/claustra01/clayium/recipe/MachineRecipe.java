/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.tier.ClayTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** First data-driven recipe model used by the common machine processor. */
public record MachineRecipe(
        ResourceLocation machine,
        Ingredient ingredient,
        ItemStack result,
        int processingTimeTicks,
        long clayEnergyPerTick,
        ClayTier minimumTier
) implements Recipe<MachineRecipeInput> {
    private static final Codec<Long> NON_NEGATIVE_LONG = Codec.LONG.comapFlatMap(
            value -> value < 0 ? com.mojang.serialization.DataResult.error(() -> "Value must not be negative") : com.mojang.serialization.DataResult.success(value),
            value -> value);
    public static final com.mojang.serialization.MapCodec<MachineRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("machine").forGetter(MachineRecipe::machine),
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MachineRecipe::ingredient),
            ItemStack.CODEC.fieldOf("result").forGetter(MachineRecipe::result),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("processing_time_ticks").forGetter(MachineRecipe::processingTimeTicks),
            NON_NEGATIVE_LONG.fieldOf("clay_energy_per_tick").forGetter(MachineRecipe::clayEnergyPerTick),
            ClayTier.CODEC.fieldOf("minimum_tier").forGetter(MachineRecipe::minimumTier)
    ).apply(instance, MachineRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, MachineRecipe::machine,
            Ingredient.CONTENTS_STREAM_CODEC, MachineRecipe::ingredient,
            ItemStack.STREAM_CODEC, MachineRecipe::result,
            ByteBufCodecs.VAR_INT, MachineRecipe::processingTimeTicks,
            ByteBufCodecs.VAR_LONG, MachineRecipe::clayEnergyPerTick,
            ClayTier.STREAM_CODEC, MachineRecipe::minimumTier,
            MachineRecipe::new);

    public MachineRecipe {
        if (machine == null) {
            throw new IllegalArgumentException("Machine recipe machine ID must not be null");
        }
        if (ingredient == null || ingredient.isEmpty()) {
            throw new IllegalArgumentException("Machine recipe ingredient must not be empty");
        }
        if (result == null || result.isEmpty() || result.getCount() > result.getMaxStackSize()) {
            throw new IllegalArgumentException("Machine recipe result must be a valid stack");
        }
        result = result.copy();
        if (processingTimeTicks < 1) {
            throw new IllegalArgumentException("processingTimeTicks must be positive");
        }
        if (clayEnergyPerTick < 0) {
            throw new IllegalArgumentException("clayEnergyPerTick must not be negative");
        }
        if (minimumTier == null) {
            throw new IllegalArgumentException("minimumTier must not be null");
        }
        try {
            Math.multiplyExact(processingTimeTicks, clayEnergyPerTick);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Machine recipe total Clay Energy overflows a long", exception);
        }
    }

    @Override
    public ItemStack result() {
        return result.copy();
    }

    @Override
    public boolean matches(MachineRecipeInput input, Level level) {
        return input != null && !input.isEmpty() && ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(MachineRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ClayiumRecipes.MACHINE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ClayiumRecipes.MACHINE_RECIPE_TYPE.get();
    }
}
