/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** JEI presentation shared by the first common machine recipes. */
public final class MachineRecipeCategory implements IRecipeCategory<MachineRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 88;
    private static final int TEXT_COLOR = 0xFF555555;

    private final RecipeType<MachineRecipe> recipeType;
    private final Component title;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    public MachineRecipeCategory(
            IGuiHelper guiHelper,
            RecipeType<MachineRecipe> recipeType,
            Component title,
            IDrawable icon) {
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<MachineRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MachineRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(34, 24)
                .setStandardSlotBackground()
                .addIngredients(recipe.ingredient());
        builder.addOutputSlot(124, 24)
                .setOutputSlotBackground()
                .addItemStack(recipe.result());
    }

    @Override
    public void draw(
            MachineRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        arrow.draw(graphics, 78, 28);
        Font font = Minecraft.getInstance().font;
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.processing_time",
                        recipe.processingTimeTicks()),
                8,
                52,
                TEXT_COLOR,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.clay_energy_per_tick",
                        recipe.clayEnergyPerTick()),
                8,
                64,
                TEXT_COLOR,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.total_clay_energy",
                        recipe.clayEnergyPerTick() * recipe.processingTimeTicks()),
                88,
                52,
                TEXT_COLOR,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.minimum_tier",
                        recipe.minimumTier().displayName()),
                88,
                64,
                TEXT_COLOR,
                false);
    }
}
