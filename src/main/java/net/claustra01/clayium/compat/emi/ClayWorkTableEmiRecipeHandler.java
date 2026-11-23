package net.claustra01.clayium.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import java.util.List;
import net.claustra01.clayium.workbench.ClayWorkTableMenu;
import net.minecraft.world.inventory.Slot;

final class ClayWorkTableEmiRecipeHandler implements StandardRecipeHandler<ClayWorkTableMenu> {
    private final EmiRecipeCategory category;

    ClayWorkTableEmiRecipeHandler(EmiRecipeCategory category) {
        this.category = category;
    }

    @Override
    public List<Slot> getInputSources(ClayWorkTableMenu menu) {
        return List.copyOf(menu.slots.subList(4, menu.slots.size()));
    }

    @Override
    public List<Slot> getCraftingSlots(ClayWorkTableMenu menu) {
        return List.copyOf(menu.slots.subList(0, 2));
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == category;
    }
}
