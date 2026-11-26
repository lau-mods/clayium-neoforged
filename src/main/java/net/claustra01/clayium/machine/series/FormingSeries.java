package net.claustra01.clayium.machine.series;

import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public enum FormingSeries implements MachineSeries {
    BENDING_MACHINE("BendingMachine", ClayiumRecipeMethod.BENDING_MACHINE, new int[] {1, 2, 3, 4, 5, 6, 7, 9}),
    WIRE_DRAWING_MACHINE("WireDrawingMachine", ClayiumRecipeMethod.WIRE_DRAWING_MACHINE, new int[] {1, 2, 3, 4}),
    PIPE_DRAWING_MACHINE("PipeDrawingMachine", ClayiumRecipeMethod.PIPE_DRAWING_MACHINE, new int[] {1, 2, 3, 4}),
    CUTTING_MACHINE("CuttingMachine", ClayiumRecipeMethod.CUTTING_MACHINE, new int[] {1, 2, 3, 4}),
    LATHE("Lathe", ClayiumRecipeMethod.LATHE, new int[] {1, 2, 3, 4}),
    MILLING_MACHINE("MillingMachine", ClayiumRecipeMethod.MILLING_MACHINE, new int[] {3, 4});

    private final String legacyName;
    private final ClayiumRecipeMethod recipeMethod;
    private final int[] tiers;

    FormingSeries(String legacyName, ClayiumRecipeMethod recipeMethod, int[] tiers) {
        this.legacyName = legacyName;
        this.recipeMethod = recipeMethod;
        this.tiers = tiers;
    }

    public String legacyName() {
        return legacyName;
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
    public String frontTexture() {
        return switch (this) {
            case BENDING_MACHINE -> "bendingmachine";
            case WIRE_DRAWING_MACHINE -> "wiredrawingmachine";
            case PIPE_DRAWING_MACHINE -> "pipedrawingmachine";
            case CUTTING_MACHINE -> "cuttingmachine";
            case LATHE -> "lathe";
            case MILLING_MACHINE -> "millingmachine";
        };
    }
}
