package net.claustra01.clayium.machine.series;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum ElementalMillingSeries implements MachineSeries {
    INSTANCE;

    @Override
    public ClayiumRecipeMethod recipeMethod() {
        return ClayiumRecipeMethod.MILLING_MACHINE;
    }

    @Override
    public int[] tiers() {
        return new int[] {1};
    }

    @Override
    public TierManager.MachineParameters parameters(int tier) {
        return TierManager.general(tier);
    }

    @Override
    public String frontTexture() {
        return "claymillingmachine";
    }

    @Override
    public String registrationId(int tier) {
        return "elemental_milling_machine";
    }
}
