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
import net.claustra01.clayium.energy.ClayEnergyFormatter;
import net.claustra01.clayium.machine.MachineLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** JEI presentation shared by data-driven Clayium machine recipes. */
public final class MachineRecipeCategory implements IRecipeCategory<MachineRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 104;
    private static final int TEXT_COLOR = 0xFF555555;

    private final RecipeType<MachineRecipe> recipeType;
    private final Component title;
    private final IDrawable icon;
    private final IDrawableStatic arrow;
    private final MachineLayout layout;

    public MachineRecipeCategory(
            IGuiHelper guiHelper,
            RecipeType<MachineRecipe> recipeType,
            Component title,
            IDrawable icon,
            MachineLayout layout) {
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;
        this.layout = layout;
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
        for (int index = 0; index < recipe.ingredients().size(); index++) {
            var ingredient = recipe.ingredients().get(index);
            int x = layout == MachineLayout.ASSEMBLER || layout == MachineLayout.CHEMICAL
                    ? recipe.ingredients().size() == 1 ? 44 : 32 + index * 18
                    : 44;
            var slot = builder.addInputSlot(x, 35);
            if (layout == MachineLayout.SIMPLE || layout == MachineLayout.CENTRIFUGE) {
                slot.setOutputSlotBackground();
            } else {
                slot.setStandardSlotBackground();
            }
            slot.addItemStacks(java.util.Arrays.stream(ingredient.ingredient().getItems())
                            .map(stack -> stack.copyWithCount(ingredient.count()))
                            .toList());
        }
        for (int index = 0; index < recipe.results().size(); index++) {
            int x = layout == MachineLayout.CHEMICAL && recipe.results().size() > 1
                    ? 110 + 18 * index : 116;
            int y = layout == MachineLayout.CENTRIFUGE
                            ? 35 + 18 * index - 9 * (recipe.results().size() - 1)
                            : 35;
            var slot = builder.addOutputSlot(x, y);
            if (layout == MachineLayout.SIMPLE || layout == MachineLayout.ASSEMBLER) {
                slot.setOutputSlotBackground();
            } else {
                slot.setStandardSlotBackground();
            }
            slot.addItemStack(recipe.results().get(index));
        }
    }

    @Override
    public void draw(
            MachineRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        arrow.draw(graphics, 76, 35);
        Font font = Minecraft.getInstance().font;
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.processing_time",
                        recipe.processingTimeTicks()),
                8,
                80,
                TEXT_COLOR,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.clay_energy_per_tick",
                        ClayEnergyFormatter.format(recipe.clayEnergyPerTick())),
                8,
                92,
                TEXT_COLOR,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.total_clay_energy",
                        ClayEnergyFormatter.format(totalClayEnergy(recipe))),
                88,
                80,
                TEXT_COLOR,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.clayium_neoforged.minimum_tier",
                        recipe.minimumTier().progressionIndex()),
                88,
                92,
                TEXT_COLOR,
                false);
    }

    private static long totalClayEnergy(MachineRecipe recipe) {
        try {
            return Math.multiplyExact(recipe.clayEnergyPerTick(), (long) recipe.processingTimeTicks());
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }
}
