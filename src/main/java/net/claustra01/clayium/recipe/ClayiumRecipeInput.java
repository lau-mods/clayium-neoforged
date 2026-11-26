package net.claustra01.clayium.recipe;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record ClayiumRecipeInput(
        List<ItemStack> items,
        List<FluidStack> fluids,
        int machineTier,
        long availableClayEnergy,
        BlockPos machinePosition) implements RecipeInput {
    public ClayiumRecipeInput {
        items = List.copyOf(items);
        fluids = List.copyOf(fluids);
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
