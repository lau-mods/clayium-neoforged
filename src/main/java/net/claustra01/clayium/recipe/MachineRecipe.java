/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.tier.ClayTier;
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

/**
 * Shared data-driven item recipe used by Clayium processing machines.
 *
 * <p>The two-input/four-output bounds cover the original simple machines,
 * Assembler, Inscriber, and Centrifuge without introducing separate execution
 * engines solely for slot counts.</p>
 */
public record MachineRecipe(
        ResourceLocation machine,
        List<MachineIngredient> ingredients,
        List<ItemStack> results,
        long processingTimeTicks,
        long clayEnergyPerTick,
        ClayTier minimumTier
) implements Recipe<MachineRecipeInput> {
    public static final int MAX_INPUTS = 2;
    public static final int MAX_OUTPUTS = 4;

    private static final Codec<Long> NON_NEGATIVE_LONG = Codec.LONG.comapFlatMap(
            value -> value < 0
                    ? com.mojang.serialization.DataResult.error(() -> "Value must not be negative")
                    : com.mojang.serialization.DataResult.success(value),
            value -> value);

    public static final com.mojang.serialization.MapCodec<MachineRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("machine").forGetter(MachineRecipe::machine),
                    MachineIngredient.CODEC.listOf(1, MAX_INPUTS)
                            .fieldOf("ingredients")
                            .forGetter(MachineRecipe::ingredients),
                    ItemStack.CODEC.listOf(1, MAX_OUTPUTS)
                            .fieldOf("results")
                            .forGetter(MachineRecipe::results),
                    NON_NEGATIVE_LONG.validate(value -> value > 0
                            ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "Value must be positive"))
                            .fieldOf("processing_time_ticks")
                            .forGetter(MachineRecipe::processingTimeTicks),
                    NON_NEGATIVE_LONG
                            .fieldOf("clay_energy_per_tick")
                            .forGetter(MachineRecipe::clayEnergyPerTick),
                    ClayTier.CODEC.fieldOf("minimum_tier").forGetter(MachineRecipe::minimumTier)
            ).apply(instance, MachineRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public MachineRecipe decode(RegistryFriendlyByteBuf buffer) {
                    ResourceLocation machine = ResourceLocation.STREAM_CODEC.decode(buffer);
                    int ingredientCount = ByteBufCodecs.VAR_INT.decode(buffer);
                    if (ingredientCount < 1 || ingredientCount > MAX_INPUTS) {
                        throw new IllegalArgumentException("Invalid machine ingredient count: " + ingredientCount);
                    }
                    List<MachineIngredient> ingredients = new ArrayList<>(ingredientCount);
                    for (int index = 0; index < ingredientCount; index++) {
                        ingredients.add(MachineIngredient.STREAM_CODEC.decode(buffer));
                    }
                    int resultCount = ByteBufCodecs.VAR_INT.decode(buffer);
                    if (resultCount < 1 || resultCount > MAX_OUTPUTS) {
                        throw new IllegalArgumentException("Invalid machine result count: " + resultCount);
                    }
                    List<ItemStack> results = new ArrayList<>(resultCount);
                    for (int index = 0; index < resultCount; index++) {
                        results.add(ItemStack.STREAM_CODEC.decode(buffer));
                    }
                    return new MachineRecipe(
                            machine,
                            ingredients,
                            results,
                            ByteBufCodecs.VAR_LONG.decode(buffer),
                            ByteBufCodecs.VAR_LONG.decode(buffer),
                            ClayTier.STREAM_CODEC.decode(buffer));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, MachineRecipe recipe) {
                    ResourceLocation.STREAM_CODEC.encode(buffer, recipe.machine());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.ingredients.size());
                    recipe.ingredients.forEach(ingredient ->
                            MachineIngredient.STREAM_CODEC.encode(buffer, ingredient));
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.results.size());
                    recipe.results.forEach(result -> ItemStack.STREAM_CODEC.encode(buffer, result));
                    ByteBufCodecs.VAR_LONG.encode(buffer, recipe.processingTimeTicks);
                    ByteBufCodecs.VAR_LONG.encode(buffer, recipe.clayEnergyPerTick);
                    ClayTier.STREAM_CODEC.encode(buffer, recipe.minimumTier);
                }
            };

    public MachineRecipe {
        if (machine == null) {
            throw new IllegalArgumentException("Machine recipe machine ID must not be null");
        }
        if (ingredients == null || ingredients.isEmpty() || ingredients.size() > MAX_INPUTS) {
            throw new IllegalArgumentException("Machine recipe must have 1 to " + MAX_INPUTS + " ingredients");
        }
        ingredients = List.copyOf(ingredients);
        if (results == null || results.isEmpty() || results.size() > MAX_OUTPUTS) {
            throw new IllegalArgumentException("Machine recipe must have 1 to " + MAX_OUTPUTS + " results");
        }
        List<ItemStack> copiedResults = new ArrayList<>(results.size());
        for (ItemStack result : results) {
            if (result == null || result.isEmpty() || result.getCount() > result.getMaxStackSize()) {
                throw new IllegalArgumentException("Machine recipe result must be a valid stack");
            }
            copiedResults.add(result.copy());
        }
        results = List.copyOf(copiedResults);
        if (processingTimeTicks < 1) {
            throw new IllegalArgumentException("processingTimeTicks must be positive");
        }
        if (clayEnergyPerTick < 0) {
            throw new IllegalArgumentException("clayEnergyPerTick must not be negative");
        }
        if (minimumTier == null) {
            throw new IllegalArgumentException("minimumTier must not be null");
        }
        // Several original end-game recipes intentionally exceed Long.MAX_VALUE in total CE.
        // Time and CE/t remain exact longs; UI totals and policy calculations saturate separately.
    }

    public MachineRecipe(
            ResourceLocation machine,
            Ingredient ingredient,
            ItemStack result,
            long processingTimeTicks,
            long clayEnergyPerTick,
            ClayTier minimumTier) {
        this(
                machine,
                List.of(new MachineIngredient(ingredient, 1)),
                List.of(result),
                processingTimeTicks,
                clayEnergyPerTick,
                minimumTier);
    }

    @Override
    public List<ItemStack> results() {
        return results.stream().map(ItemStack::copy).toList();
    }

    /** Compatibility accessor for one-input recipes. */
    public Ingredient ingredient() {
        return ingredients.getFirst().ingredient();
    }

    /** Compatibility accessor for one-output recipes. */
    public ItemStack result() {
        return results.getFirst().copy();
    }

    public Optional<int[]> matchInputSlots(MachineRecipeInput input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        int[] matchedSlots = new int[ingredients.size()];
        Arrays.fill(matchedSlots, -1);
        return matchIngredient(input, 0, new boolean[input.size()], matchedSlots)
                ? Optional.of(matchedSlots)
                : Optional.empty();
    }

    private boolean matchIngredient(
            MachineRecipeInput input,
            int ingredientIndex,
            boolean[] usedSlots,
            int[] matchedSlots) {
        if (ingredientIndex >= ingredients.size()) {
            return true;
        }
        MachineIngredient ingredient = ingredients.get(ingredientIndex);
        for (int slot = 0; slot < input.size(); slot++) {
            if (!usedSlots[slot] && ingredient.test(input.getItem(slot))) {
                usedSlots[slot] = true;
                matchedSlots[ingredientIndex] = slot;
                if (matchIngredient(input, ingredientIndex + 1, usedSlots, matchedSlots)) {
                    return true;
                }
                usedSlots[slot] = false;
                matchedSlots[ingredientIndex] = -1;
            }
        }
        return false;
    }

    @Override
    public boolean matches(MachineRecipeInput input, Level level) {
        return matchInputSlots(input).isPresent();
    }

    @Override
    public ItemStack assemble(MachineRecipeInput input, HolderLookup.Provider registries) {
        return result();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= ingredients.size();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        ingredients.forEach(ingredient -> result.add(ingredient.ingredient()));
        return result;
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
