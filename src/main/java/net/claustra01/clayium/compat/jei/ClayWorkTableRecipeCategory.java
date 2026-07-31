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
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.registry.ClayiumRegistries;
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
        switch (recipe.operation()) {
            case ROLL -> builder.addSlot(RecipeIngredientRole.CATALYST, 79, 26)
                    .setStandardSlotBackground()
                    .addItemStack(ClayiumRegistries.CLAY_ROLLING_PIN.get().getDefaultInstance());
            case SLICE, DIVIDE -> builder.addSlot(RecipeIngredientRole.CATALYST, 79, 26)
                    .setStandardSlotBackground()
                    .addItemStacks(java.util.List.of(
                            ClayiumRegistries.CLAY_SLICER.get().getDefaultInstance(),
                            ClayiumRegistries.CLAY_SPATULA.get().getDefaultInstance()));
            case PUNCH -> builder.addSlot(RecipeIngredientRole.CATALYST, 79, 26)
                    .setStandardSlotBackground()
                    .addItemStack(ClayiumRegistries.CLAY_SPATULA.get().getDefaultInstance());
            default -> {
            }
        }
    }

    @Override
    public void draw(
            ClayWorkTableRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        if (recipe.operation().buttonId() < 3) {
            arrow.draw(graphics, 78, 30);
        }
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
        if (recipe.operation().buttonId() >= 3) {
            graphics.drawString(
                    font,
                    Component.translatable(
                            "jei.clayium_neoforged.required_tool",
                            Component.translatable(
                                    "jei.clayium_neoforged.tool." + recipe.operation().id())),
                    8,
                    45,
                    TEXT_COLOR,
                    false);
        }
        if (recipe.inputCount() > 1) {
            graphics.drawString(
                    font,
                    Component.translatable(
                            "jei.clayium_neoforged.input_count",
                            recipe.inputCount()),
                    8,
                    16,
                    TEXT_COLOR,
                    false);
        }
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
                        "jei.clayium_neoforged.minimum_tier",
                        recipe.minimumTier().progressionIndex()),
                8,
                69,
                TEXT_COLOR,
                false);
    }
}
