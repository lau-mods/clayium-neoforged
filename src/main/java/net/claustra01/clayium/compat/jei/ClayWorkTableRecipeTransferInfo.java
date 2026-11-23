package net.claustra01.clayium.compat.jei;

import java.util.List;
import java.util.Optional;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.workbench.ClayWorkTableMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.claustra01.clayium.compat.RecipeViewerPage;

final class ClayWorkTableRecipeTransferInfo
        implements IRecipeTransferInfo<ClayWorkTableMenu, RecipeViewerPage> {
    @Override
    public Class<? extends ClayWorkTableMenu> getContainerClass() {
        return ClayWorkTableMenu.class;
    }

    @Override
    public Optional<MenuType<ClayWorkTableMenu>> getMenuType() {
        return Optional.of(net.claustra01.clayium.registry.WorkbenchRegistration.CLAY_WORK_TABLE_MENU.get());
    }

    @Override
    public RecipeType<RecipeViewerPage> getRecipeType() {
        return ClayiumJeiPlugin.machineType(net.claustra01.clayium.recipe.ClayiumRecipeMethod.CLAY_WORK_TABLE);
    }

    @Override
    public boolean canHandle(ClayWorkTableMenu menu, RecipeViewerPage recipe) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(ClayWorkTableMenu menu, RecipeViewerPage recipe) {
        return List.copyOf(menu.slots.subList(0, 2));
    }

    @Override
    public List<Slot> getInventorySlots(ClayWorkTableMenu menu, RecipeViewerPage recipe) {
        return List.copyOf(menu.slots.subList(4, menu.slots.size()));
    }
}
