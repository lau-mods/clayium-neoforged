package net.claustra01.clayium.machine.series;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum ChemicalSeries implements MachineSeries {
    CHEMICAL_REACTOR(ClayiumRecipeMethod.CHEMICAL_REACTOR, new int[] {4, 5, 8}, 2, 2, "chemicalreactor"),
    CHEMICAL_METAL_SEPARATOR(
            ClayiumRecipeMethod.CHEMICAL_METAL_SEPARATOR,
            new int[] {6},
            1,
            1,
            "chemicalmetalseparator"),
    MATTER_TRANSFORMER(ClayiumRecipeMethod.MATTER_TRANSFORMER, new int[] {7, 8, 9, 10, 11, 12}, 1, 1, "transformer");

    private final ClayiumRecipeMethod recipeMethod;
    private final int[] tiers;
    private final int inputSlots;
    private final int outputSlots;
    private final String frontTexture;

    ChemicalSeries(
            ClayiumRecipeMethod recipeMethod,
            int[] tiers,
            int inputSlots,
            int outputSlots,
            String frontTexture) {
        this.recipeMethod = recipeMethod;
        this.tiers = tiers;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.frontTexture = frontTexture;
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
        return this == CHEMICAL_METAL_SEPARATOR
                ? new TierManager.MachineParameters(1.0D, 1.0D)
                : TierManager.general(tier);
    }

    @Override
    public int inputSlots() {
        return inputSlots;
    }

    @Override
    public int outputSlots() {
        return outputSlots;
    }

    @Override
    public String frontTexture() {
        return frontTexture;
    }

    @Override
    public String registrationId(int tier) {
        return this == CHEMICAL_METAL_SEPARATOR
                ? "chemical_metal_separator"
                : this == MATTER_TRANSFORMER ? "transformer_tier_" + tier : MachineSeries.super.registrationId(tier);
    }
}
