package net.claustra01.clayium.machine.series;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public interface MachineSeries {
    ClayiumRecipeMethod recipeMethod();

    int[] tiers();

    TierManager.MachineParameters parameters(int tier);

    default String frontTexture() {
        return recipeMethod().id();
    }

    default int inputSlots() {
        return 1;
    }
}
