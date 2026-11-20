package net.claustra01.clayium.machine.environment;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.series.MachineSeries;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum EnvironmentSeries implements MachineSeries {
    SALT_EXTRACTOR;

    private static final int[] TIERS = {4, 5, 6, 7};

    @Override
    public ClayiumRecipeMethod recipeMethod() {
        return ClayiumRecipeMethod.SALT_EXTRACTOR;
    }

    @Override
    public int[] tiers() {
        return TIERS.clone();
    }

    @Override
    public TierManager.MachineParameters parameters(int tier) {
        return TierManager.general(tier);
    }

    @Override
    public boolean canStartWithoutItemInput() {
        return true;
    }

    @Override
    public String frontTexture() {
        return "saltextractor";
    }
}
