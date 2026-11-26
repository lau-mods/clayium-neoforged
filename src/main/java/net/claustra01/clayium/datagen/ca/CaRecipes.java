package net.claustra01.clayium.datagen.ca;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe.ProgressionRate;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.CaRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.ItemLike;

public final class CaRecipes {
    public static void buildRecipes(RecipeOutput output) {
        ItemLike antimatter = MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get();
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CA_CONDENSER, 9)
                .requires(MaterialRegistration.COMPONENTS.get("antimatter_seed").get())
                .output(antimatter, 1)
                .clayEnergy(ClayiumRecipeEnergy.e(2.5D, 9))
                .processingTicks(2_000)
                .progressionRate(ProgressionRate.TICKS)
                .save(output, ClayiumMod.id("ca_condenser/antimatter"));

        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CA_INJECTOR, 10)
                .requires(AssemblyMachineRegistration.MACHINE_HULLS.get(10).get())
                .requires(antimatter, 8)
                .output(CaRegistration.RESONATING_COLLECTOR.get(), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(2.0D, 10))
                .processingTicks(4_000)
                .save(output, ClayiumMod.id("ca_injector/resonating_collector"));

        augmentChains(output, antimatter, "resonator");
        storageChain(output, antimatter);
        overclockerRecipes(output);

    }

    private static void augmentChains(RecipeOutput output, ItemLike antimatter, String suffix) {
        ItemLike[] values = {
                CaRegistration.AUGMENTS.get("antimatter_" + suffix).block().get(),
                CaRegistration.AUGMENTS.get("pure_antimatter_" + suffix).block().get(),
                CaRegistration.AUGMENTS.get("oec_" + suffix).block().get(),
                CaRegistration.AUGMENTS.get("opa_" + suffix).block().get()
        };
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CA_INJECTOR, 9)
                .requires(AssemblyMachineRegistration.MACHINE_HULLS.get(9).get())
                .requires(antimatter, 8)
                .output(values[0], 1)
                .clayEnergy(ClayiumRecipeEnergy.e(2.0D, 9))
                .processingTicks(4_000)
                .save(output, ClayiumMod.id("ca_injector/antimatter_" + suffix));
        for (int rank = 1; rank < values.length; rank++) {
            int tier = rank + 10;
            ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CA_INJECTOR, tier)
                    .requires(values[rank - 1], 16)
                    .requires(antimatter, 64)
                    .output(values[rank], 1)
                    .clayEnergy(ClayiumRecipeEnergy.e(2.0D, tier))
                    .processingTicks(4_000)
                    .save(output, ClayiumMod.id("ca_injector/" + switch (rank) {
                        case 1 -> "pure_antimatter_";
                        case 2 -> "oec_";
                        default -> "opa_";
                    } + suffix));
        }
    }

    private static void storageChain(RecipeOutput output, ItemLike antimatter) {
        ItemLike firstOverclocker = CaRegistration.AUGMENTS.get("antimatter_overclocker").block().get();
        ItemLike firstStorage = CaRegistration.AUGMENTS.get("antimatter_energy_storage_upgrade").block().get();
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CA_INJECTOR, 9)
                .requires(firstOverclocker)
                .requires(antimatter, 8)
                .output(firstStorage, 1)
                .clayEnergy(ClayiumRecipeEnergy.e(2.0D, 9))
                .processingTicks(4_000)
                .save(output, ClayiumMod.id("ca_injector/antimatter_energy_storage_upgrade"));
        ItemLike[] values = {
                firstStorage,
                CaRegistration.AUGMENTS.get("pure_antimatter_energy_storage_upgrade").block().get(),
                CaRegistration.AUGMENTS.get("oec_energy_storage_upgrade").block().get(),
                CaRegistration.AUGMENTS.get("opa_energy_storage_upgrade").block().get()
        };
        for (int rank = 1; rank < values.length; rank++) {
            int tier = rank + 10;
            ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CA_INJECTOR, tier)
                    .requires(values[rank - 1], 16)
                    .requires(antimatter, 64)
                    .output(values[rank], 1)
                    .clayEnergy(ClayiumRecipeEnergy.e(2.0D, tier))
                    .processingTicks(4_000)
                    .save(output, ClayiumMod.id("ca_injector/" + switch (rank) {
                        case 1 -> "pure_antimatter_";
                        case 2 -> "oec_";
                        default -> "opa_";
                    } + "energy_storage_upgrade"));
        }
    }

    private static void overclockerRecipes(RecipeOutput output) {
        String[] materials = {"antimatter", "pure_antimatter", "oec", "opa"};
        int[] hullCounts = {1, 4, 16, 64};
        int[] resonatorCounts = {8, 16, 32, 64};
        long[] reactorTicks = {10_000_000_000_000L, 100_000_000_000_000L,
                1_000_000_000_000_000L, 1_000_000_000_000_000L};
        for (int index = 0; index < materials.length; index++) {
            int tier = index + 10;
            ItemLike hull = tier == 10
                    ? AssemblyMachineRegistration.MACHINE_HULLS.get(10).get()
                    : tier == 11 ? MaterialRegistration.MATERIAL_BLOCKS.get("pure_antimatter_block").get()
                    : tier == 12
                            ? MaterialRegistration.COMPRESSION_BLOCKS.get("octuple_compressed_energetic_clay").get()
                            : MaterialRegistration.MATERIAL_BLOCKS.get("opa_block").get();
            ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, tier)
                    .requires(hull, hullCounts[index])
                    .requires(CaRegistration.AUGMENTS.get(materials[index] + "_resonator").block().get(),
                            resonatorCounts[index])
                    .output(CaRegistration.AUGMENTS.get(materials[index] + "_overclocker").block().get(), 1)
                    .clayEnergy(ClayiumRecipeEnergy.e(5.0D, tier))
                    .processingTicks(reactorTicks[index])
                    .save(output, ClayiumMod.id("reactor/" + materials[index] + "_overclocker"));
        }
    }

    private CaRecipes() {
    }
}
