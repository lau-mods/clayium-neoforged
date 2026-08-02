/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.compat.jei;

import net.claustra01.clayium.util.MetricFormatter;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class QuartzCrucibleRecipeCategory implements IRecipeCategory<QuartzCrucibleJeiRecipe> {
    private final IDrawable icon;
    private final IDrawableStatic arrow;
    public QuartzCrucibleRecipeCategory(IGuiHelper helper, ItemStack icon) {
        this.icon = helper.createDrawableItemStack(icon);
        arrow = helper.getRecipeArrow();
    }
    @Override public RecipeType<QuartzCrucibleJeiRecipe> getRecipeType() { return ClayiumJeiRecipeTypes.QUARTZ_CRUCIBLE; }
    @Override public Component getTitle() { return Component.translatable("jei.clayium_neoforged.category.quartz_crucible"); }
    @Override public int getWidth() { return 176; }
    @Override public int getHeight() { return 78; }
    @Override public IDrawable getIcon() { return icon; }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, QuartzCrucibleJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(20, 22).setStandardSlotBackground().addItemStack(
                new ItemStack(ClayiumRegistries.MATERIAL_ITEMS.get("impure_silicon_ingot").get(), recipe.amount()));
        builder.addInputSlot(44, 22).setStandardSlotBackground().addItemStack(new ItemStack(Items.STRING));
        builder.addOutputSlot(116, 22).setOutputSlotBackground().addItemStack(
                new ItemStack(ClayiumRegistries.MATERIAL_ITEMS.get("silicon_ingot").get(), recipe.amount()));
    }
    @Override public void draw(QuartzCrucibleJeiRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                               double mouseX, double mouseY) {
        arrow.draw(graphics, 76, 22);
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.clayium_neoforged.processing_time",
                        MetricFormatter.format(600L * recipe.amount())),
                8, 56, 0xff555555, false);
    }
}
