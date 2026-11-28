package net.claustra01.clayium.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumRecipeTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/**
 * Recipe matching helpers preserving 1.7.10 Clayium priority ordering and 2-slot {@code getCraftPermutation} semantics.
 */
public final class ClayMachineRecipeLookup {
    public record PermutationMatch(int[] permutation, ClayMachineRecipe recipe) {
        public int consumedCount(int permIndex) {
            return this.recipe.inputs().get(permIndex).count();
        }
    }

    private ClayMachineRecipeLookup() {}

    public static List<ClayMachineRecipe> recipesForCategory(@Nullable Level level, String categoryId) {
        if (level == null || categoryId == null || categoryId.isEmpty()) {
            return List.of();
        }
        var type = ClayiumRecipeTypes.byCategory(categoryId).get();
        List<RecipeHolder<ClayMachineRecipe>> holders = level.getRecipeManager().getAllRecipesFor(type);
        List<ClayMachineRecipe> list = new ArrayList<>(holders.size());
        for (RecipeHolder<ClayMachineRecipe> holder : holders) {
            list.add(holder.value());
        }
        return list;
    }

    @Nullable
    public static ClayMachineRecipe findBestSingle(
            Collection<ClayMachineRecipe> recipes,
            ItemStack input,
            int machineTier) {
        if (input.isEmpty() || recipes.isEmpty()) {
            return null;
        }
        ClayMachineRecipe best = null;
        int bestPriority = Integer.MIN_VALUE;
        for (ClayMachineRecipe recipe : recipes) {
            if (recipe.tier() > machineTier || recipe.inputs().size() != 1) {
                continue;
            }
            if (!recipe.inputs().get(0).test(input)) {
                continue;
            }
            if (best == null || recipe.priority() > bestPriority) {
                best = recipe;
                bestPriority = recipe.priority();
            }
        }
        return best;
    }

    @Nullable
    public static PermutationMatch findBestTwoInput(
            Collection<ClayMachineRecipe> recipes,
            ItemStack slot0,
            ItemStack slot1,
            int machineTier,
            Predicate<ClayMachineRecipe> canFitOutputs) {
        if (recipes.isEmpty()) {
            return null;
        }
        int[][] candidatePerms = {
                {0, 1},
                {1, 0},
                {0},
                {1}
        };
        for (int[] perm : candidatePerms) {
            ItemStack first = perm[0] == 0 ? slot0 : slot1;
            ItemStack second = perm.length == 2 ? (perm[1] == 0 ? slot0 : slot1) : ItemStack.EMPTY;
            if (perm.length == 2 && (first.isEmpty() || second.isEmpty())) {
                continue;
            }
            if (perm.length == 1 && first.isEmpty()) {
                continue;
            }
            ClayMachineRecipe best = null;
            int bestPriority = Integer.MIN_VALUE;
            for (ClayMachineRecipe recipe : recipes) {
                if (recipe.tier() > machineTier || recipe.inputs().size() != perm.length) {
                    continue;
                }
                if (!recipe.inputs().get(0).test(first)) {
                    continue;
                }
                if (perm.length == 2 && !recipe.inputs().get(1).test(second)) {
                    continue;
                }
                if (!canFitOutputs.test(recipe)) {
                    continue;
                }
                if (best == null || recipe.priority() > bestPriority) {
                    best = recipe;
                    bestPriority = recipe.priority();
                }
            }
            if (best != null) {
                return new PermutationMatch(perm, best);
            }
        }
        return null;
    }

    public static boolean isCraftableAnyInput(
            Collection<ClayMachineRecipe> recipes,
            ItemStack candidate,
            int machineTier) {
        if (candidate.isEmpty() || recipes.isEmpty()) {
            return false;
        }
        for (ClayMachineRecipe recipe : recipes) {
            if (recipe.tier() > machineTier) {
                continue;
            }
            for (CountedIngredient input : recipe.inputs()) {
                if (input.ingredient().test(candidate)) {
                    return true;
                }
            }
        }
        return false;
    }
}
