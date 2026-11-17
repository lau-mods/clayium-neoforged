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

    default int outputSlots() {
        return 1;
    }

    default String registrationId(int tier) {
        return recipeMethod().id() + "_tier_" + tier;
    }
}
