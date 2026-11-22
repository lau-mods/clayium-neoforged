package net.claustra01.clayium.datagen.progression;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.FluidRegistration;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.ItemLike;

public final class TransportProgressionRecipes {
    public static void buildRecipes(RecipeOutput output) {
        for (var definition : LogisticsRegistration.BUFFERS.values()) {
            int tier = definition.tier();
            machine(output, "logistics/" + definition.id(), definition.block().get(),
                    4, tier, 40, 16,
                    ProgressionRecipeSupport.material(tierMaterial(tier) + "_plate"), 1,
                    circuit(tier), 1);
        }
        for (var definition : MultitrackRegistration.MULTITRACK_BUFFERS.values()) {
            int tier = definition.tier();
            machine(output, "logistics/" + definition.id(), definition.block().get(),
                    4, tier, 40, 1, item("buffer_tier_" + tier), 6,
                    ProgressionRecipeSupport.material("large_" + tierMaterial(tier) + "_plate"), 1);
        }
        for (var definition : MultitrackRegistration.DISTRIBUTORS.values()) {
            int tier = definition.tier();
            machine(output, "logistics/" + definition.id(), definition.block().get(),
                    6, tier, 120, 1, item("buffer_tier_" + tier), 1,
                    AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get(), 1);
        }
        for (var definition : InterfaceRegistration.INTERFACES.values()) {
            int tier = definition.tier();
            String id = definition.id();
            if (id.startsWith("clay_laser_interface")) {
                machine(output, "interfaces/" + id, definition.block().get(),
                        6, tier, 120, 1, item("buffer_tier_" + tier), 1,
                        ProgressionRecipeSupport.component("laser_parts"), 1);
            } else if (id.startsWith("redstone_interface")) {
                machine(output, "interfaces/" + id, definition.block().get(),
                        4, tier, 40, 1, item("clay_interface_tier_" + tier), 1,
                        ProgressionRecipeSupport.material("energized_clay_dust"), 16);
            } else {
                machine(output, "interfaces/" + id, definition.block().get(),
                        4, tier, 40, 1, AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get(), 1,
                        item("buffer_tier_6"), 1);
            }
        }
        for (var definition : FluidRegistration.MACHINES.values()) {
            int tier = definition.specification().tier();
            String id = definition.specification().id();
            if (id.startsWith("fluid_transfer_machine")) {
                machine(output, "fluid/" + id, definition.block().get(),
                        4, tier, 40, 1, item("fluid_translator_tier_5"), 2,
                        AssemblyMachineRegistration.MACHINE_HULLS.get(5).get(), 1);
            } else {
                machine(output, "fluid/" + id, definition.block().get(),
                        4, tier, 40, 1, item("buffer_tier_" + tier), 1, Items.BUCKET, 4);
            }
        }
    }

    private static void machine(RecipeOutput output, String path, ItemLike result,
            int requiredTier, int energyTier, long ticks, int resultCount,
            ItemLike first, int firstCount, ItemLike second, int secondCount) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, requiredTier)
                .requires(first, firstCount).requires(second, secondCount)
                .output(result, resultCount).clayEnergy(ClayiumRecipeEnergy.e(1.0D, energyTier))
                .processingTicks(ticks).save(output, ClayiumMod.id("progression/" + path));
    }

    private static ItemLike circuit(int tier) {
        return ProgressionRecipeSupport.component(switch (tier) {
            case 4 -> "basic_circuit";
            case 5 -> "advanced_circuit";
            case 6 -> "precision_circuit";
            case 7 -> "integrated_circuit";
            case 8 -> "clay_core";
            case 9 -> "clay_brain";
            case 10 -> "clay_spirit";
            case 11 -> "clay_soul";
            case 12 -> "clay_anima";
            case 13 -> "clay_psyche";
            default -> throw new IllegalArgumentException("Unexpected transport tier " + tier);
        });
    }

    private static String tierMaterial(int tier) {
        return switch (tier) {
            case 4 -> "advanced_industrial_clay";
            case 5 -> "impure_silicon";
            case 6 -> "aluminium";
            case 7 -> "clay_steel";
            case 8 -> "clayium";
            case 9 -> "ultimate_alloy";
            case 10 -> "antimatter";
            case 11 -> "pure_antimatter";
            case 12 -> "octuple_energetic_clay";
            case 13 -> "octuple_pure_antimatter";
            default -> throw new IllegalArgumentException("Unexpected transport tier " + tier);
        };
    }

    private static ItemLike item(String id) {
        return BuiltInRegistries.ITEM.get(ClayiumMod.id(id));
    }

    private TransportProgressionRecipes() {
    }
}
