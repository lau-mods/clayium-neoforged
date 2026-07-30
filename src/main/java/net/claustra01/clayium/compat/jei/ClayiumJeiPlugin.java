/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.compat.jei;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.client.gui.screens.inventory.ClayWorkTableScreen;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.ClayWorkTableMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class ClayiumJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = Clayium.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new ClayWorkTableRecipeCategory(
                guiHelper,
                guiHelper.createDrawableItemStack(ClayiumRegistries.CLAY_WORK_TABLE_ITEM.get().getDefaultInstance())));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<MachineRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(ClayiumRecipes.MACHINE_RECIPE_TYPE.get())
                .stream()
                .filter(holder -> holder.value().machine().equals(ClayiumMachineIds.CLAY_WORK_TABLE))
                .map(holder -> holder.value())
                .toList();
        registration.addRecipes(ClayiumJeiRecipeTypes.CLAY_WORK_TABLE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                ClayiumRegistries.CLAY_WORK_TABLE_ITEM.get(),
                ClayiumJeiRecipeTypes.CLAY_WORK_TABLE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                ClayWorkTableScreen.class,
                63,
                50,
                58,
                20,
                ClayiumJeiRecipeTypes.CLAY_WORK_TABLE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                ClayWorkTableMenu.class,
                ClayiumRegistries.CLAY_WORK_TABLE_MENU.get(),
                ClayiumJeiRecipeTypes.CLAY_WORK_TABLE,
                0,
                1,
                2,
                36);
    }
}
