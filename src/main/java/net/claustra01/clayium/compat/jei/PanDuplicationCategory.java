package net.claustra01.clayium.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.clayium.compat.RecipeViewerData;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.minecraft.client.Minecraft;
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
        return 176;
    }

    @Override
    public int getHeight() {
        return 82;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PanDuplicationRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(2, 2).setStandardSlotBackground().addItemStack(new ItemStack(Items.CLAY));
        builder.addInputSlot(22, 2).setStandardSlotBackground()
                .addItemStack(new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get()));
        builder.addOutputSlot(158, 2).setStandardSlotBackground().addItemStack(new ItemStack(Items.CLAY));
    }

    @Override
    public void draw(PanDuplicationRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
            double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("gui.clayium.recipe.pan_rate", recipe.tier(),
                RecipeViewerData.panEnergyPerTick(recipe.tier()) / 100_000D),
                2, 27, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.recipe.pan_sample"),
                2, 39, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.recipe.pan_energy"),
                2, 51, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.recipe.pan_ticks"),
                2, 63, 0x404040, false);
    }
}
