/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.MachineRecipe;

public final class ClayiumJeiRecipeTypes {
    public static final RecipeType<ClayWorkTableRecipe> CLAY_WORK_TABLE =
            RecipeType.create(Clayium.MODID, "clay_work_table", ClayWorkTableRecipe.class);
    public static final RecipeType<MachineRecipe> CLAY_BENDING_MACHINE =
            RecipeType.create(Clayium.MODID, "clay_bending_machine", MachineRecipe.class);
    public static final RecipeType<MachineRecipe> ELEMENTAL_MILLING_MACHINE =
            RecipeType.create(Clayium.MODID, "elemental_milling_machine", MachineRecipe.class);

    private ClayiumJeiRecipeTypes() {
    }
}
