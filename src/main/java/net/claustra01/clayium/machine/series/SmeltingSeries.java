package net.claustra01.clayium.machine.series;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum SmeltingSeries implements MachineSeries {
    SMELTER(ClayiumRecipeMethod.SMELTER, new int[] {4, 5, 6, 7, 8, 9}, 1, true),
    ALLOY_SMELTER(ClayiumRecipeMethod.ALLOY_SMELTER, new int[] {6}, 2, false),
    ELECTROLYSIS_REACTOR(ClayiumRecipeMethod.ELECTROLYSIS_REACTOR, new int[] {6, 7, 8, 9}, 1, false);

    private final ClayiumRecipeMethod recipeMethod;
    private final int[] tiers;
    private final int inputSlots;
    private final boolean smelterTierParameters;

    SmeltingSeries(
            ClayiumRecipeMethod recipeMethod,
            int[] tiers,
            int inputSlots,
            boolean smelterTierParameters) {
        this.recipeMethod = recipeMethod;
        this.tiers = tiers;
        this.inputSlots = inputSlots;
        this.smelterTierParameters = smelterTierParameters;
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
        return smelterTierParameters ? TierManager.smelter(tier) : TierManager.general(tier);
    }

    @Override
    public String frontTexture() {
        return switch (this) {
            case SMELTER -> "smelter";
            case ALLOY_SMELTER -> "alloysmelter";
            case ELECTROLYSIS_REACTOR -> "electrolysisreactor";
        };
    }

    @Override
    public int inputSlots() {
        return inputSlots;
    }
}
