package net.claustra01.clayium.datagen.energyconverter;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.energy.fe.EnergyConverterSpecification;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.ItemLike;

public final class EnergyConverterRecipes {
    public static void buildRecipes(RecipeOutput output) {
        for (EnergyConverterSpecification specification : EnergyConverterSpecification.VALUES) {
            ClayiumRecipeBuilder builder = ClayiumRecipeBuilder.machine(
                            ClayiumRecipeMethod.ASSEMBLER, requiredAssemblerTier(specification.tier()))
                    .clayEnergy(ClayiumRecipeEnergy.e(1.0D, specification.tier()))
                    .processingTicks(40);
            if (specification.tier() == 4) {
                builder.requires(AssemblyMachineRegistration.MACHINE_HULLS.get(4).get())
                        .requires(MaterialRegistration.COMPONENTS.get("clay_energy_excitor").get());
            } else {
                builder.requires(EnergyConverterRegistration.CONVERTERS
                                .get(specification.tier() - 1).block().get())
                        .requires(upgradeMaterial(specification.tier()));
            }
            builder.output(EnergyConverterRegistration.CONVERTERS.get(specification.tier()).block().get(), 1)
                    .save(output, ClayiumMod.id("energy_converter/" + specification.id()));
        }
    }

    private static int requiredAssemblerTier(int converterTier) {
        if (converterTier >= 10) {
            return 10;
        }
        return converterTier >= 6 ? 6 : 4;
    }

    private static ItemLike upgradeMaterial(int tier) {
        return switch (tier) {
            case 5 -> MaterialRegistration.COMPONENTS.get("advanced_circuit").get();
            case 6 -> MaterialRegistration.COMPONENTS.get("precision_circuit").get();
            case 7 -> MaterialRegistration.COMPONENTS.get("integrated_circuit").get();
            case 8 -> MaterialRegistration.MATERIAL_ITEMS.get("clayium_plate").get();
            case 9 -> MaterialRegistration.MATERIAL_ITEMS.get("ultimate_alloy_plate").get();
            case 10 -> MaterialRegistration.MATERIAL_ITEMS.get("antimatter_plate").get();
            case 11 -> MaterialRegistration.MATERIAL_ITEMS.get("pure_antimatter_plate").get();
            case 12 -> MaterialRegistration.MATERIAL_ITEMS.get("octuple_energetic_clay_plate").get();
            case 13 -> MaterialRegistration.MATERIAL_ITEMS.get("octuple_pure_antimatter_plate").get();
            default -> throw new IllegalArgumentException("Unknown energy converter tier: " + tier);
        };
    }

    private EnergyConverterRecipes() {
    }
}
