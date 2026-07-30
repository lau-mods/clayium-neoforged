/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Immutable one-item input used by the first common machine recipe model. */
public final class MachineRecipeInput implements RecipeInput {
    private final ItemStack input;

    public MachineRecipeInput(ItemStack input) {
        this.input = Objects.requireNonNull(input, "input").copy();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot != 0) {
            throw new IndexOutOfBoundsException("Machine recipe input only has slot 0");
        }
        return input;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return input.isEmpty();
    }
}
