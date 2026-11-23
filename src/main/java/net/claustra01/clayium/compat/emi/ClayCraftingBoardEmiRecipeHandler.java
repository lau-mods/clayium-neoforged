package net.claustra01.clayium.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import java.util.List;
import net.claustra01.clayium.workbench.ClayCraftingBoardMenu;
import net.minecraft.world.inventory.Slot;

final class ClayCraftingBoardEmiRecipeHandler implements StandardRecipeHandler<ClayCraftingBoardMenu> {
    @Override
    public List<Slot> getInputSources(ClayCraftingBoardMenu menu) {
        return List.copyOf(menu.slots.subList(10, menu.slots.size()));
    }

    @Override
    public List<Slot> getCraftingSlots(ClayCraftingBoardMenu menu) {
        return List.copyOf(menu.slots.subList(1, 10));
    }

    @Override
    public Slot getOutputSlot(ClayCraftingBoardMenu menu) {
        return menu.slots.getFirst();
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING;
    }
}
