package net.claustra01.clayium.multiblock.careactor;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.series.MachineSeries;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum CaReactorSeries implements MachineSeries {
    CORE;

    @Override
    public ClayiumRecipeMethod recipeMethod() {
        return ClayiumRecipeMethod.CA_REACTOR;
    }

    @Override
    public int[] tiers() {
        return new int[] {10, 11, 12, 13};
    }

    @Override
    public TierManager.MachineParameters parameters(int tier) {
        return TierManager.general(tier);
    }

    @Override
    public String frontTexture() {
        return "careactorcore";
    }
}
