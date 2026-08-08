/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.pan;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record PanConversion(List<ItemStack> ingredients, List<ItemStack> results, double energy) {
    public PanConversion {
        ingredients = ingredients.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
        results = results.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
        if (results.isEmpty() || energy < 0.0D || !Double.isFinite(energy)) {
            throw new IllegalArgumentException("Invalid PAN conversion");
        }
    }
}
