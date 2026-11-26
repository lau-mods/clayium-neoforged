package net.claustra01.clayium.machine.series;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum EnergeticClayDecomposerSeries implements MachineSeries {
    INSTANCE;

    @Override
    public ClayiumRecipeMethod recipeMethod() {
        return ClayiumRecipeMethod.EC_DECOMPOSER;
    }

    @Override
    public int[] tiers() {
        return new int[] {13};
    }

    @Override
    public TierManager.MachineParameters parameters(int tier) {
        return TierManager.general(tier);
    }

    @Override
    public String frontTexture() {
        return "ecdecomposer";
    }

    @Override
    public String registrationId(int tier) {
        return "energetic_clay_decomposer";
    }
}
