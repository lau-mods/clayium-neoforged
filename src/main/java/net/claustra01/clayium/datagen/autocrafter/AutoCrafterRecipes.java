package net.claustra01.clayium.datagen.autocrafter;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.machine.series.AssemblySeries;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.ItemLike;

public final class AutoCrafterRecipes {
    public static void buildRecipes(RecipeOutput output) {
        for (int tier = 5; tier <= 9; tier++) {
            ClayiumRecipeBuilder builder = ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, 4)
                    .clayEnergy(ClayiumRecipeEnergy.e(1.0D, tier))
                    .processingTicks(40);
            if (tier == 5) {
                builder.requires(machine(AssemblySeries.ASSEMBLER, 4))
                        .requires(AssemblyMachineRegistration.MACHINE_HULLS.get(5).get());
            } else {
                builder.requires(AutoCrafterRegistration.AUTO_CRAFTERS.get(tier - 1).block().get())
                        .requires(upgradePart(tier));
            }
            builder.output(AutoCrafterRegistration.AUTO_CRAFTERS.get(tier).block().get(), 1)
                    .save(output, ClayiumMod.id("auto_crafter/auto_crafter_tier_" + tier));
        }
    }

    private static ItemLike upgradePart(int tier) {
        return switch (tier) {
            case 6 -> AssemblyMachineRegistration.MACHINE_HULLS.get(6).get();
            case 7 -> MaterialRegistration.COMPONENTS.get("integrated_circuit").get();
            case 8 -> MaterialRegistration.MATERIAL_ITEMS.get("clayium_plate").get();
            case 9 -> MaterialRegistration.MATERIAL_ITEMS.get("ultimate_alloy_plate").get();
            default -> throw new IllegalArgumentException("Unknown auto crafter tier " + tier);
        };
    }

    private static ItemLike machine(AssemblySeries series, int tier) {
        return AssemblyMachineRegistration.MACHINES.values().stream()
                .filter(definition -> definition.series() == series && definition.tier() == tier)
                .findFirst().orElseThrow().block().get();
    }

    private AutoCrafterRecipes() {
    }
}
