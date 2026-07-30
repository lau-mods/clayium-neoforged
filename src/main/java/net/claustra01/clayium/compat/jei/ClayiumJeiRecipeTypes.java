/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.recipe.MachineRecipe;

public final class ClayiumJeiRecipeTypes {
    public static final RecipeType<MachineRecipe> CLAY_WORK_TABLE =
            RecipeType.create(Clayium.MODID, "clay_work_table", MachineRecipe.class);

    private ClayiumJeiRecipeTypes() {
    }
}
