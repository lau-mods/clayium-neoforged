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
import net.claustra01.clayium.client.gui.screens.inventory.MachineScreen;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.ClayWorkTableMenu;
import net.claustra01.clayium.world.inventory.MachineMenu;
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
        registration.addRecipeCategories(
                new MachineRecipeCategory(
                        guiHelper,
                        ClayiumJeiRecipeTypes.CLAY_BENDING_MACHINE,
                        net.minecraft.network.chat.Component.translatable(
                                "jei.clayium_neoforged.category.clay_bending_machine"),
                        guiHelper.createDrawableItemStack(
                                ClayiumRegistries.CLAY_BENDING_MACHINE_ITEM.get().getDefaultInstance())),
                new MachineRecipeCategory(
                        guiHelper,
                        ClayiumJeiRecipeTypes.ELEMENTAL_MILLING_MACHINE,
                        net.minecraft.network.chat.Component.translatable(
                                "jei.clayium_neoforged.category.elemental_milling_machine"),
                        guiHelper.createDrawableItemStack(
                                ClayiumRegistries.ELEMENTAL_MILLING_MACHINE_ITEM.get().getDefaultInstance())));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                ClayiumRegistries.CLAY_WORK_TABLE_ITEM.get(),
                ClayiumJeiRecipeTypes.CLAY_WORK_TABLE);
        registration.addRecipeCatalyst(
                ClayiumRegistries.CLAY_BENDING_MACHINE_ITEM.get(),
                ClayiumJeiRecipeTypes.CLAY_BENDING_MACHINE);
        registration.addRecipeCatalyst(
                ClayiumRegistries.ELEMENTAL_MILLING_MACHINE_ITEM.get(),
                ClayiumJeiRecipeTypes.ELEMENTAL_MILLING_MACHINE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                ClayWorkTableScreen.class,
                48,
                29,
                80,
                16,
                ClayiumJeiRecipeTypes.CLAY_WORK_TABLE);
        registration.addRecipeClickArea(
                MachineScreen.class,
                76,
                35,
                24,
                17,
                ClayiumJeiRecipeTypes.CLAY_BENDING_MACHINE,
                ClayiumJeiRecipeTypes.ELEMENTAL_MILLING_MACHINE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                ClayWorkTableMenu.class,
                ClayiumRegistries.CLAY_WORK_TABLE_MENU.get(),
                ClayiumJeiRecipeTypes.CLAY_WORK_TABLE,
                0,
                1,
                3,
                36);
        registration.addRecipeTransferHandler(
                MachineMenu.class,
                ClayiumRegistries.MACHINE_MENU.get(),
                ClayiumJeiRecipeTypes.CLAY_BENDING_MACHINE,
                0,
                1,
                2,
                36);
        registration.addRecipeTransferHandler(
                MachineMenu.class,
                ClayiumRegistries.MACHINE_MENU.get(),
                ClayiumJeiRecipeTypes.ELEMENTAL_MILLING_MACHINE,
                0,
                1,
                2,
                36);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<ClayWorkTableRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(ClayiumRecipes.CLAY_WORK_TABLE_RECIPE_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .toList();
        registration.addRecipes(ClayiumJeiRecipeTypes.CLAY_WORK_TABLE, recipes);
        List<MachineRecipe> machineRecipes = level.getRecipeManager()
                .getAllRecipesFor(ClayiumRecipes.MACHINE_RECIPE_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .toList();
        registration.addRecipes(
                ClayiumJeiRecipeTypes.CLAY_BENDING_MACHINE,
                machineRecipes.stream()
                        .filter(recipe -> recipe.machine().equals(ClayiumMachineIds.CLAY_BENDING_MACHINE))
                        .toList());
        registration.addRecipes(
                ClayiumJeiRecipeTypes.ELEMENTAL_MILLING_MACHINE,
                machineRecipes.stream()
                        .filter(recipe -> recipe.machine().equals(ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE))
                        .toList());
    }
}
