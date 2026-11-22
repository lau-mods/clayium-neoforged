package net.claustra01.clayium.compat;

import java.math.BigDecimal;
import java.util.List;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record RecipeViewerData(
        int requiredTier,
        long processingTicks,
        long clayEnergy,
        boolean structureDependent,
        List<SizedIngredient> itemInputs,
        List<ItemStack> itemOutputs) {
    public static RecipeViewerData from(ClayiumMachineRecipe recipe) {
        List<ItemStack> outputs = recipe.itemOutputs().stream().map(ItemStack::copy).toList();
        if (recipe.processingMethod() == ClayiumRecipeMethod.CLAY_WORK_TABLE) {
            return new RecipeViewerData(recipe.requiredTier(), recipe.processingTicks(), 0L, false,
                    List.copyOf(recipe.itemInputs()), outputs);
        }
        TierManager.ProcessingParameters parameters = TierManager.calculate(
                machineParameters(recipe), recipe.processingTicks(), recipe.clayEnergy(), 1.0D);
        return new RecipeViewerData(
                recipe.requiredTier(),
                parameters.requiredProgress(),
                parameters.totalEnergy().internalValue(),
                recipe.processingMethod() == ClayiumRecipeMethod.CA_REACTOR,
                List.copyOf(recipe.itemInputs()), outputs);
    }

    private static TierManager.MachineParameters machineParameters(ClayiumMachineRecipe recipe) {
        return switch (recipe.processingMethod()) {
            case SMELTER -> TierManager.smelter(recipe.requiredTier());
            case CA_CONDENSER -> TierManager.caCondenser(recipe.requiredTier());
            case CLAY_WORK_TABLE, CHEMICAL_METAL_SEPARATOR, CA_REACTOR ->
                    new TierManager.MachineParameters(1.0D, 1.0D);
            default -> TierManager.general(recipe.requiredTier());
        };
    }

    public String formattedClayEnergy() {
        return BigDecimal.valueOf(clayEnergy, 5).stripTrailingZeros().toPlainString();
    }

    public static long panEnergyPerTick(int tier) {
        return Math.max(1L, (long)(100_000L * TierManager.general(tier).energyMultiplier()));
    }

    public String workTool() {
        return switch (requiredTier) {
            case 1, 2 -> "none";
            case 3 -> "rolling_pin";
            case 4, 6 -> "slicer_or_spatula";
            case 5 -> "spatula";
            default -> throw new IllegalArgumentException("Unknown work operation: " + requiredTier);
        };
    }

    public static String formattedFluidAmounts(List<FluidStack> fluids) {
        return fluids.stream()
                .map(fluid -> Integer.toString(fluid.getAmount()))
                .reduce((left, right) -> left + " + " + right)
                .orElse("0");
    }

}
