package net.claustra01.clayium.machine.ca;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.series.MachineSeries;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum CaSeries implements MachineSeries {
    CONDENSER(ClayiumRecipeMethod.CA_CONDENSER, new int[] {9, 10, 11}, 1),
    INJECTOR(ClayiumRecipeMethod.CA_INJECTOR, new int[] {9, 10, 11, 12, 13}, 2);

    private final ClayiumRecipeMethod recipeMethod;
    private final int[] tiers;
    private final int inputSlots;

    CaSeries(ClayiumRecipeMethod recipeMethod, int[] tiers, int inputSlots) {
        this.recipeMethod = recipeMethod;
        this.tiers = tiers;
        this.inputSlots = inputSlots;
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
        return this == CONDENSER ? TierManager.caCondenser(tier) : TierManager.general(tier);
    }

    @Override
    public int inputSlots() {
        return inputSlots;
    }

    @Override
    public String frontTexture() {
        return this == CONDENSER ? "cacondenser" : "cainjector";
    }
}
