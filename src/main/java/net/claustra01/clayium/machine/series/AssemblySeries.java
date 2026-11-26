package net.claustra01.clayium.machine.series;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum AssemblySeries implements MachineSeries {
    ASSEMBLER(ClayiumRecipeMethod.ASSEMBLER, new int[] {3, 4, 6, 10}, 2, 1),
    INSCRIBER(ClayiumRecipeMethod.INSCRIBER, new int[] {3, 4}, 2, 1),
    CENTRIFUGE(ClayiumRecipeMethod.CENTRIFUGE, new int[] {3, 4, 5, 6}, 1, 4);

    private final ClayiumRecipeMethod recipeMethod;
    private final int[] tiers;
    private final int inputSlots;
    private final int outputSlots;

    AssemblySeries(
            ClayiumRecipeMethod recipeMethod,
            int[] tiers,
            int inputSlots,
            int outputSlots) {
        this.recipeMethod = recipeMethod;
        this.tiers = tiers;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
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

    @Override
    public int inputSlots() {
        return inputSlots;
    }

    @Override
    public int outputSlots() {
        return outputSlots;
    }
}
