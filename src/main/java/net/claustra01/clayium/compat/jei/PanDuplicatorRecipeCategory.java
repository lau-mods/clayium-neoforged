/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.compat.jei;

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

/** Documents operation that is intentionally dynamic and therefore has no datapack recipe instance. */
public final class PanDuplicatorRecipeCategory implements IRecipeCategory<PanDuplicatorJeiRecipe> {
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    public PanDuplicatorRecipeCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemStack(ClayiumRegistries.PAN_DUPLICATOR_BLOCKS
                .get("basic_pan_duplicator").get().asItem().getDefaultInstance());
        arrow = helper.getRecipeArrow();
    }

    @Override public RecipeType<PanDuplicatorJeiRecipe> getRecipeType() { return ClayiumJeiRecipeTypes.PAN_DUPLICATOR; }
    @Override public Component getTitle() { return Component.translatable("jei.clayium_neoforged.category.pan_duplicator"); }
    @Override public int getWidth() { return 176; }
    @Override public int getHeight() { return 82; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PanDuplicatorJeiRecipe recipe, IFocusGroup focuses) {
        ItemStack example = new ItemStack(Items.CHEST);
        builder.addInputSlot(20, 20).setStandardSlotBackground().addItemStack(example);
        builder.addInputSlot(48, 20).setStandardSlotBackground().addItemStack(
                ClayiumRegistries.MATERIAL_ITEMS.get("antimatter").get().getDefaultInstance());
        builder.addOutputSlot(122, 20).setOutputSlotBackground().addItemStack(example.copy());
    }

    @Override
    public void draw(PanDuplicatorJeiRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        arrow.draw(graphics, 82, 20);
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("jei.clayium_neoforged.pan.template_retained"),
                8, 51, 0xff555555, false);
        graphics.drawString(font, Component.translatable("jei.clayium_neoforged.pan.network_condition"),
                8, 63, 0xff555555, false);
    }
}
