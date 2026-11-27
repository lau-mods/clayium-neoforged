package net.claustra01.clayium.recipe;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.claustra01.clayium.recipe.ClayMachineRecipe.MachineInput;
import net.claustra01.clayium.registry.ClayiumRecipeTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

/**
 * Smelter & generic machine recipe lookup helper preserving vanilla furnace fallback and priority sorting.
 */
public final class SmelterRecipeHelper {
    public static final long VANILLA_SMELTING_ENERGY = 4L;
    public static final long VANILLA_SMELTING_TIME = 200L;

    private SmelterRecipeHelper() {}

    public static Optional<ClayMachineRecipe> findSmelterMatch(Level level, ItemStack input, int machineTier) {
        if (input.isEmpty() || machineTier < 0) {
            return Optional.empty();
        }
        Optional<RecipeHolder<SmeltingRecipe>> vanilla = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
        if (vanilla.isPresent()) {
            ItemStack out = vanilla.get().value().getResultItem(level.registryAccess()).copy();
            if (!out.isEmpty()) {
                return Optional.of(new ClayMachineRecipe(
                        "smelter",
                        List.of(new CountedIngredient(net.minecraft.world.item.crafting.Ingredient.of(input.getItem()), 1)),
                        List.of(out),
                        0,
                        VANILLA_SMELTING_ENERGY,
                        VANILLA_SMELTING_TIME,
                        Integer.MAX_VALUE
                ));
            }
        }
        return findMachineMatch(level, "smelter", new MachineInput(List.of(input), machineTier));
    }

    public static Optional<ClayMachineRecipe> findMachineMatch(Level level, String categoryId, MachineInput input) {
        RecipeType<ClayMachineRecipe> type = ClayiumRecipeTypes.byCategory(categoryId).get();
        return level.getRecipeManager().getRecipesFor(type, input, level).stream()
                .map(RecipeHolder::value)
                .min(Comparator.comparingInt(ClayMachineRecipe::priority));
    }
}
