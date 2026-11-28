package net.claustra01.clayium.pan;

import java.util.List;
import net.claustra01.clayium.recipe.CountedIngredient;
import net.minecraft.world.item.ItemStack;

/**
 * Single directed conversion rule registered into a PAN network by a PAN Adapter.
 */
public record PANConversion(
        List<CountedIngredient> inputs,
        List<ItemStack> results,
        double energy) {

    public PANConversion {
        inputs = List.copyOf(inputs);
        results = results.stream().map(ItemStack::copy).toList();
    }
}
