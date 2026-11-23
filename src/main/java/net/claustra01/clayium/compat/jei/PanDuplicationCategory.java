package net.claustra01.clayium.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.clayium.compat.RecipeViewerData;
import net.claustra01.clayium.compat.RecipeViewerLayout;
import net.claustra01.clayium.compat.RecipeViewerRenderer;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class PanDuplicationCategory implements IRecipeCategory<PanDuplicationRecipe> {
    private final IDrawable icon;

    PanDuplicationCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemLike(PanRegistration.DEFINITIONS.get("pan_duplicator_tier_4").block().get());
    }

    @Override
    public RecipeType<PanDuplicationRecipe> getRecipeType() {
        return ClayiumJeiPlugin.PAN_DUPLICATION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.clayium.recipe.pan_duplication");
    }

    @Override
    public int getWidth() {
        return RecipeViewerLayout.WIDTH;
    }

    @Override
    public int getHeight() {
        return RecipeViewerLayout.HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PanDuplicationRecipe recipe, IFocusGroup focuses) {
        RecipeViewerLayout layout = RecipeViewerLayout.pan(recipe.tier());
        builder.addInputSlot(layout.slots().get(0).x(), layout.slots().get(0).y())
                .addItemStack(new ItemStack(Items.CLAY));
        builder.addInputSlot(layout.slots().get(1).x(), layout.slots().get(1).y())
                .addItemStack(new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get()));
        builder.addOutputSlot(layout.slots().get(2).x(), layout.slots().get(2).y())
                .addItemStack(new ItemStack(Items.CLAY));
    }

    @Override
    public void draw(PanDuplicationRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
            double mouseX, double mouseY) {
        RecipeViewerRenderer.draw(graphics, RecipeViewerLayout.pan(recipe.tier()));
    }
}
