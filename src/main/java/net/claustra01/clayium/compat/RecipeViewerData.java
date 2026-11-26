package net.claustra01.clayium.compat;

import java.util.List;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record RecipeViewerData(
        int requiredTier,
        long processingTicks,
        long clayEnergyPerTick,
        long clayEnergy,
        boolean structureDependent,
        List<SizedIngredient> itemInputs,
        List<ItemStack> itemOutputs) {
    public static RecipeViewerData from(ClayiumMachineRecipe recipe) {
        List<ItemStack> outputs = recipe.itemOutputs().stream().map(ItemStack::copy).toList();
        if (recipe.processingMethod() == ClayiumRecipeMethod.CLAY_WORK_TABLE) {
            return new RecipeViewerData(recipe.requiredTier(), recipe.processingTicks(), 0L, 0L, false,
                    List.copyOf(recipe.itemInputs()), outputs);
        }
        TierManager.ProcessingParameters parameters = TierManager.calculate(
                machineParameters(recipe), recipe.processingTicks(), recipe.clayEnergy(), 1.0D);
        return new RecipeViewerData(
                recipe.requiredTier(),
                parameters.requiredProgress(),
                parameters.energyPerProgress().internalValue(),
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
        return formatEnergy(clayEnergy);
    }

    public String formattedClayEnergyPerTick() {
        return formatEnergy(clayEnergyPerTick);
    }

    private static String formatEnergy(long internalEnergy) {
        return ClayEnergy.ofInternal(internalEnergy).formatValue();
    }

    public static long panEnergyPerTick(int tier) {
        return Math.max(1L, (long)(100_000L * TierManager.general(tier).energyMultiplier()));
    }

    public List<ItemStack> workToolItems() {
        return switch (requiredTier) {
            case 1, 2 -> List.of();
            case 3 -> List.of(new ItemStack(WorkbenchRegistration.CLAY_ROLLING_PIN.get()));
            case 4, 6 -> List.of(
                    new ItemStack(WorkbenchRegistration.CLAY_SLICER.get()),
                    new ItemStack(WorkbenchRegistration.CLAY_SPATULA.get()));
            case 5 -> List.of(new ItemStack(WorkbenchRegistration.CLAY_SPATULA.get()));
            default -> throw new IllegalArgumentException("Unknown work operation: " + requiredTier);
        };
    }

}
