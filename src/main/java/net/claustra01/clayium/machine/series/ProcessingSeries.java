package net.claustra01.clayium.machine.series;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum ProcessingSeries implements MachineSeries {
    CONDENSER(ClayiumRecipeMethod.CONDENSER, new int[] {2, 3, 4, 5, 10}),
    GRINDER(ClayiumRecipeMethod.GRINDER, new int[] {2, 3, 4, 5, 6, 10}),
    DECOMPOSER(ClayiumRecipeMethod.DECOMPOSER, new int[] {2, 3, 4});

    private final ClayiumRecipeMethod recipeMethod;
    private final int[] tiers;

    ProcessingSeries(ClayiumRecipeMethod recipeMethod, int[] tiers) {
        this.recipeMethod = recipeMethod;
        this.tiers = tiers;
    }

    @Override
    public ClayiumRecipeMethod recipeMethod() {
        return recipeMethod;
    }

    @Override
    public int[] tiers() {
        return tiers.clone();
    }

    @Override
    public TierManager.MachineParameters parameters(int tier) {
        return TierManager.general(tier);
    }
}
