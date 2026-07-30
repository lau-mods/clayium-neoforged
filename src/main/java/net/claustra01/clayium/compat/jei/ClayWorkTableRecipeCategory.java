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
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ClayWorkTableRecipeCategory implements IRecipeCategory<ClayWorkTableRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 92;
    private static final int TEXT_COLOR = 0xFF555555;

    private final IDrawable icon;
    private final IDrawableStatic arrow;

    public ClayWorkTableRecipeCategory(IGuiHelper guiHelper, IDrawable icon) {
        this.icon = icon;
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<ClayWorkTableRecipe> getRecipeType() {
        return ClayiumJeiRecipeTypes.CLAY_WORK_TABLE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.clayium_neoforged.category.clay_work_table");
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
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            ClayWorkTableRecipe recipe,
            IFocusGroup focuses) {
        builder.addInputSlot(34, 26)
                .setStandardSlotBackground()
                .addIngredients(recipe.ingredient());
        builder.addOutputSlot(124, 26)
                .setOutputSlotBackground()
                .addItemStack(recipe.result());
    }

    @Override
    public void draw(
            ClayWorkTableRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        arrow.draw(graphics, 78, 30);
        Font font = Minecraft.getInstance().font;
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.required_actions",
                        recipe.requiredActions()),
                8,
                4,
                TEXT_COLOR,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.operation",
                        Component.translatable(recipe.operation().translationKey())),
                8,
                57,
                TEXT_COLOR,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.clay_energy_per_tick",
                        0),
                8,
                69,
                TEXT_COLOR,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.total_clay_energy",
                        0),
                8,
                81,
                TEXT_COLOR,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.minimum_tier",
                        recipe.minimumTier().displayName()),
                100,
                69,
                TEXT_COLOR,
                false);
    }
}
