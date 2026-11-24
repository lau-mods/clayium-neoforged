package net.claustra01.clayium.recipe;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

public final class ClayiumRecipeCache {
    private static final Map<RecipeManager, Map<RecipeType<?>, TypeIndex>> CACHES = new WeakHashMap<>();

    public static synchronized Optional<RecipeHolder<ClayiumMachineRecipe>> find(
            RecipeManager manager,
            RecipeType<ClayiumMachineRecipe> type,
            ClayiumRecipeInput input,
            Level level) {
        Map<RecipeType<?>, TypeIndex> managerCache = CACHES.computeIfAbsent(manager, ignored -> new IdentityHashMap<>());
        TypeIndex index = managerCache.computeIfAbsent(type, ignored -> build(manager, type));
        return index.candidates(input).stream()
                .filter(holder -> {
                    ClayiumMachineRecipe adjusted = ProgressionRateRules.adjustedRecipe(holder.id(), holder.value());
                    if (!adjusted.matches(input, level)) {
                        return false;
                    }
                    return !ProgressionRateRules.requiresExactItemCount(holder.id())
                            || input.items().stream().mapToInt(ItemStack::getCount).sum()
                                    == adjusted.itemInputs().stream().mapToInt(
                                            net.neoforged.neoforge.common.crafting.SizedIngredient::count).sum();
                })
                .findFirst();
    }

    public static void recipesReloaded(RecipeManager manager) {
        synchronized (ClayiumRecipeCache.class) {
            CACHES.clear();
        }
        NeoForge.EVENT_BUS.post(new ClayiumRecipesReloadedEvent(manager));
    }

    private static TypeIndex build(RecipeManager manager, RecipeType<ClayiumMachineRecipe> type) {
        Map<Item, Map<Integer, List<RecipeHolder<ClayiumMachineRecipe>>>> byItemAndTier = new IdentityHashMap<>();
        List<RecipeHolder<ClayiumMachineRecipe>> withoutIndexedItem = new ArrayList<>();
        for (RecipeHolder<ClayiumMachineRecipe> holder : manager.getAllRecipesFor(type)) {
            ClayiumMachineRecipe recipe = holder.value();
            LinkedHashSet<Item> indexedItems = new LinkedHashSet<>();
            recipe.itemInputs().forEach(input -> {
                for (ItemStack stack : input.ingredient().getItems()) {
                    indexedItems.add(stack.getItem());
                }
            });
            if (indexedItems.isEmpty()) {
                withoutIndexedItem.add(holder);
            } else {
                for (Item item : indexedItems) {
                    byItemAndTier.computeIfAbsent(item, ignored -> new java.util.TreeMap<>())
                            .computeIfAbsent(recipe.requiredTier(), ignored -> new ArrayList<>())
                            .add(holder);
                }
            }
        }
        return new TypeIndex(byItemAndTier, List.copyOf(withoutIndexedItem));
    }

    private record TypeIndex(
            Map<Item, Map<Integer, List<RecipeHolder<ClayiumMachineRecipe>>>> byItemAndTier,
            List<RecipeHolder<ClayiumMachineRecipe>> withoutIndexedItem) {
        private List<RecipeHolder<ClayiumMachineRecipe>> candidates(ClayiumRecipeInput input) {
            LinkedHashSet<RecipeHolder<ClayiumMachineRecipe>> candidates = new LinkedHashSet<>(withoutIndexedItem);
            for (ItemStack stack : input.items()) {
                Map<Integer, List<RecipeHolder<ClayiumMachineRecipe>>> byTier = byItemAndTier.get(stack.getItem());
                if (byTier == null) {
                    continue;
                }
                byTier.forEach((tier, recipes) -> {
                    if (tier <= input.machineTier()) {
                        candidates.addAll(recipes);
                    }
                });
            }
            return List.copyOf(candidates);
        }
    }

    private ClayiumRecipeCache() {
    }
}
