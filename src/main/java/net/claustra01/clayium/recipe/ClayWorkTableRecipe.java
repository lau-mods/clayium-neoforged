/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Data-driven recipe for a repeated manual Clay Work Table operation. */
public record ClayWorkTableRecipe(
        Ingredient ingredient,
        int inputCount,
        ItemStack result,
        ClayWorkTableOperation operation,
        int requiredActions,
        ClayTier minimumTier
) implements Recipe<MachineRecipeInput> {
    public static final com.mojang.serialization.MapCodec<ClayWorkTableRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ClayWorkTableRecipe::ingredient),
                    Codec.intRange(1, 64).optionalFieldOf("input_count", 1).forGetter(ClayWorkTableRecipe::inputCount),
                    ItemStack.CODEC.fieldOf("result").forGetter(ClayWorkTableRecipe::result),
                    ClayWorkTableOperation.CODEC.fieldOf("operation").forGetter(ClayWorkTableRecipe::operation),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("required_actions").forGetter(ClayWorkTableRecipe::requiredActions),
                    ClayTier.CODEC.fieldOf("minimum_tier").forGetter(ClayWorkTableRecipe::minimumTier)
            ).apply(instance, ClayWorkTableRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClayWorkTableRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, ClayWorkTableRecipe::ingredient,
                    ByteBufCodecs.VAR_INT, ClayWorkTableRecipe::inputCount,
                    ItemStack.STREAM_CODEC, ClayWorkTableRecipe::result,
                    ClayWorkTableOperation.STREAM_CODEC, ClayWorkTableRecipe::operation,
                    ByteBufCodecs.VAR_INT, ClayWorkTableRecipe::requiredActions,
                    ClayTier.STREAM_CODEC, ClayWorkTableRecipe::minimumTier,
                    ClayWorkTableRecipe::new);

    public ClayWorkTableRecipe {
        if (ingredient == null || ingredient.isEmpty()) {
            throw new IllegalArgumentException("Clay Work Table ingredient must not be empty");
        }
        if (result == null || result.isEmpty() || result.getCount() > result.getMaxStackSize()) {
            throw new IllegalArgumentException("Clay Work Table result must be a valid stack");
        }
        if (operation == null || minimumTier == null || inputCount < 1 || inputCount > 64 || requiredActions < 1) {
            throw new IllegalArgumentException("Clay Work Table recipe parameters are invalid");
        }
        result = result.copy();
    }

    @Override
    public ItemStack result() {
        return result.copy();
    }

    @Override
    public boolean matches(MachineRecipeInput input, Level level) {
        return input != null
                && !input.isEmpty()
                && input.getItem(0).getCount() >= inputCount
                && ingredient.test(input.getItem(0));
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
        return ClayiumRecipes.CLAY_WORK_TABLE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ClayiumRecipes.CLAY_WORK_TABLE_RECIPE_TYPE.get();
    }
}
