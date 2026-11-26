package net.claustra01.clayium.compat;

import java.util.Comparator;
import java.util.List;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.clayproduction.ClayProductionType;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.CaReactorRegistration;
import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.claustra01.clayium.registry.FluidRegistration;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class RecipeViewerCatalysts {
    public static List<ItemStack> forMethod(ClayiumRecipeMethod method) {
        if (method == ClayiumRecipeMethod.CLAY_WORK_TABLE) {
            return List.of(
                    new ItemStack(WorkbenchRegistration.CLAY_WORK_TABLE_ITEM.get()));
        }
        if (method == ClayiumRecipeMethod.BLAST_FURNACE || method == ClayiumRecipeMethod.REACTOR) {
            return MultiblockRegistration.CONTROLLERS.entrySet().stream()
                    .filter(entry -> entry.getKey().recipeMethod() == method)
                    .map(entry -> new ItemStack(entry.getValue().get())).toList();
        }
        if (method == ClayiumRecipeMethod.CA_REACTOR) {
            return CaReactorRegistration.CORES.values().stream()
                    .sorted(Comparator.comparingInt(CaReactorRegistration.CoreDefinition::tier))
                    .map(core -> new ItemStack(core.item().get())).toList();
        }
        if (method == ClayiumRecipeMethod.EC_CONDENSER) {
            return ClayProductionRegistration.BLOCKS.entrySet().stream()
                    .filter(entry -> entry.getKey().kind() == ClayProductionType.Kind.ENERGETIC_CONDENSER)
                    .map(entry -> new ItemStack(entry.getValue().get().asItem())).toList();
        }
        return BuiltInRegistries.BLOCK.stream()
                .filter(ClayMachineBlock.class::isInstance)
                .map(ClayMachineBlock.class::cast)
                .filter(block -> block.series().recipeMethod() == method)
                .sorted(Comparator.comparingInt(ClayMachineBlock::tier))
                .map(block -> new ItemStack(block.asItem())).toList();
    }

    private RecipeViewerCatalysts() {
    }
}
