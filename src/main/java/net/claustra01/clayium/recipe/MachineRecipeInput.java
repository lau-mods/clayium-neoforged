/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Immutable item inputs used by the common machine recipe model. */
public final class MachineRecipeInput implements RecipeInput {
    private final List<ItemStack> inputs;

    public MachineRecipeInput(ItemStack input) {
        this(List.of(input));
    }

    public MachineRecipeInput(List<ItemStack> inputs) {
        Objects.requireNonNull(inputs, "inputs");
        if (inputs.isEmpty() || inputs.size() > MachineRecipe.MAX_INPUTS) {
            throw new IllegalArgumentException("Machine recipe input must have 1 to "
                    + MachineRecipe.MAX_INPUTS + " slots");
        }
        this.inputs = inputs.stream()
                .map(stack -> Objects.requireNonNull(stack, "input stack").copy())
                .toList();
    }

    @Override
    public ItemStack getItem(int slot) {
        return inputs.get(slot);
    }

    @Override
    public int size() {
        return inputs.size();
    }

    @Override
    public boolean isEmpty() {
        return inputs.stream().allMatch(ItemStack::isEmpty);
    }
}
