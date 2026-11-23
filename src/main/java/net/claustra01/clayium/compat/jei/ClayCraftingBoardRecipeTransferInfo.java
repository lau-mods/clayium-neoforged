package net.claustra01.clayium.compat.jei;

import java.util.List;
import java.util.Optional;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.claustra01.clayium.workbench.ClayCraftingBoardMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

final class ClayCraftingBoardRecipeTransferInfo
        implements IRecipeTransferInfo<ClayCraftingBoardMenu, RecipeHolder<CraftingRecipe>> {
    @Override
    public Class<? extends ClayCraftingBoardMenu> getContainerClass() {
        return ClayCraftingBoardMenu.class;
    }

    @Override
    public Optional<MenuType<ClayCraftingBoardMenu>> getMenuType() {
        return Optional.of(WorkbenchRegistration.CLAY_CRAFTING_BOARD_MENU.get());
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public boolean canHandle(ClayCraftingBoardMenu menu, RecipeHolder<CraftingRecipe> recipe) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(ClayCraftingBoardMenu menu, RecipeHolder<CraftingRecipe> recipe) {
        return List.copyOf(menu.slots.subList(1, 10));
    }

    @Override
    public List<Slot> getInventorySlots(ClayCraftingBoardMenu menu, RecipeHolder<CraftingRecipe> recipe) {
        return List.copyOf(menu.slots.subList(10, menu.slots.size()));
    }
}
