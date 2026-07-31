/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.compat.jei;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.client.gui.screens.inventory.ClayWorkTableScreen;
import net.claustra01.clayium.client.gui.screens.inventory.MachineScreen;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.machine.MachineLayout;
import net.claustra01.clayium.machine.Phase4MachineCatalog;
import net.claustra01.clayium.machine.Phase6MachineCatalog;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.recipe.SmelterRecipeAdapter;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.ClayWorkTableMenu;
import net.claustra01.clayium.world.inventory.MachineMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;

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
                guiHelper.createDrawableItemStack(
                        ClayiumRegistries.CLAY_WORK_TABLE_ITEM.get().getDefaultInstance())));
        ClayiumJeiRecipeTypes.MACHINES.forEach((machineId, recipeType) ->
                registration.addRecipeCategories(new MachineRecipeCategory(
                        guiHelper,
                        recipeType,
                        Component.translatable(
                                "jei." + Clayium.MODID + ".category." + machineId.getPath()),
                        guiHelper.createDrawableItemStack(iconFor(machineId)),
                        MachineLayout.forMachine(machineId))));
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
        for (Phase4MachineCatalog.Entry entry : Phase4MachineCatalog.ENTRIES) {
            registration.addRecipeCatalyst(
                    ClayiumRegistries.PHASE4_MACHINE_ITEMS.get(entry.blockId()).get(),
                    ClayiumJeiRecipeTypes.MACHINES.get(entry.machineId()));
        }
        for (Phase6MachineCatalog.Entry entry : Phase6MachineCatalog.ENTRIES) {
            registration.addRecipeCatalyst(
                    ClayiumRegistries.PHASE6_MACHINE_ITEMS.get(entry.blockId()).get(),
                    ClayiumJeiRecipeTypes.MACHINES.get(entry.machineId()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                ClayWorkTableScreen.class,
                48,
                29,
                80,
                16,
                ClayiumJeiRecipeTypes.CLAY_WORK_TABLE);
        registration.addGuiContainerHandler(MachineScreen.class, new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
            @Override
            public java.util.Collection<IGuiClickableArea> getGuiClickableAreas(
                    MachineScreen screen,
                    double guiMouseX,
                    double guiMouseY) {
                RecipeType<MachineRecipe> recipeType =
                        ClayiumJeiRecipeTypes.MACHINES.get(screen.machineId());
                return recipeType == null
                        ? java.util.List.of()
                        : java.util.List.of(IGuiClickableArea.createBasic(76, 35, 24, 17, recipeType));
            }
        });
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
        ClayiumJeiRecipeTypes.MACHINES.forEach((machineId, recipeType) -> {
            MachineLayout layout = MachineLayout.forMachine(machineId);
            if (layout == MachineLayout.ASSEMBLER) {
                registerTransfer(
                        registration,
                        ClayiumRegistries.ASSEMBLER_MACHINE_MENU.get(),
                        recipeType,
                        2,
                        3);
            } else if (layout == MachineLayout.CHEMICAL) {
                registerTransfer(registration, ClayiumRegistries.CHEMICAL_MACHINE_MENU.get(), recipeType, 2, 4);
            } else if (layout == MachineLayout.CENTRIFUGE) {
                registerTransfer(registration, ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_1.get(), recipeType, 1, 2);
                registerTransfer(registration, ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_2.get(), recipeType, 1, 3);
                registerTransfer(registration, ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_3.get(), recipeType, 1, 4);
                registerTransfer(registration, ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_4.get(), recipeType, 1, 5);
            } else {
                registerTransfer(registration, ClayiumRegistries.MACHINE_MENU.get(), recipeType, 1, 2);
            }
        });
    }

    private static void registerTransfer(
            IRecipeTransferRegistration registration,
            MenuType<MachineMenu> menuType,
            RecipeType<MachineRecipe> recipeType,
            int inputCount,
            int playerInventoryStart) {
        registration.addRecipeTransferHandler(
                MachineMenu.class,
                menuType,
                recipeType,
                0,
                inputCount,
                playerInventoryStart,
                36);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<ClayWorkTableRecipe> workTableRecipes = level.getRecipeManager()
                .getAllRecipesFor(ClayiumRecipes.CLAY_WORK_TABLE_RECIPE_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .toList();
        registration.addRecipes(ClayiumJeiRecipeTypes.CLAY_WORK_TABLE, workTableRecipes);
        List<MachineRecipe> machineRecipes = level.getRecipeManager()
                .getAllRecipesFor(ClayiumRecipes.MACHINE_RECIPE_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .toList();
        ClayiumJeiRecipeTypes.MACHINES.forEach((machineId, recipeType) -> {
            List<MachineRecipe> recipes = machineId.equals(ClayiumMachineIds.SMELTER)
                    ? SmelterRecipeAdapter.all(level)
                    : machineRecipes.stream()
                            .filter(recipe -> recipe.machine().equals(machineId))
                            .toList();
            registration.addRecipes(recipeType, recipes);
        });
    }

    private static ItemStack iconFor(ResourceLocation machineId) {
        if (ClayiumMachineIds.CLAY_BENDING_MACHINE.equals(machineId)) {
            return ClayiumRegistries.CLAY_BENDING_MACHINE_ITEM.get().getDefaultInstance();
        }
        if (ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE.equals(machineId)) {
            return ClayiumRegistries.ELEMENTAL_MILLING_MACHINE_ITEM.get().getDefaultInstance();
        }
        return Phase4MachineCatalog.ENTRIES.stream()
                .filter(entry -> entry.machineId().equals(machineId))
                .findFirst()
                .map(entry -> ClayiumRegistries.PHASE4_MACHINE_ITEMS
                        .get(entry.blockId())
                        .get()
                        .getDefaultInstance())
                .orElseGet(() -> Phase6MachineCatalog.ENTRIES.stream()
                        .filter(entry -> entry.machineId().equals(machineId))
                        .findFirst()
                        .map(entry -> ClayiumRegistries.PHASE6_MACHINE_ITEMS.get(entry.blockId()).get().getDefaultInstance())
                        .orElse(ItemStack.EMPTY));
    }
}
