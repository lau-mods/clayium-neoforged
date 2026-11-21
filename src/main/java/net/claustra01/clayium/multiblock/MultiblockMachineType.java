package net.claustra01.clayium.multiblock;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.series.MachineSeries;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum MultiblockMachineType implements MachineSeries {
    BLAST_FURNACE("clay_blast_furnace", ClayiumRecipeMethod.BLAST_FURNACE, 6, 2, "blastfurnace"),
    REACTOR("clay_reactor", ClayiumRecipeMethod.REACTOR, 7, 2, "reactor");

    private final String id;
    private final ClayiumRecipeMethod recipeMethod;
    private final int minimumTier;
    private final int inputSlots;
    private final String frontTexture;

    MultiblockMachineType(
            String id,
            ClayiumRecipeMethod recipeMethod,
            int minimumTier,
            int inputSlots,
            String frontTexture) {
        this.id = id;
        this.recipeMethod = recipeMethod;
        this.minimumTier = minimumTier;
        this.inputSlots = inputSlots;
        this.frontTexture = frontTexture;
    }

    public String id() {
        return id;
    }

    public int minimumTier() {
        return minimumTier;
    }

    public boolean requiresLaserInterface() {
        return this == REACTOR;
    }

    @Override
    public ClayiumRecipeMethod recipeMethod() {
        return recipeMethod;
    }

    @Override
    public int[] tiers() {
        return new int[] {minimumTier};
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
    public String frontTexture() {
        return frontTexture;
    }

    @Override
    public String registrationId(int tier) {
        return id;
    }
}
