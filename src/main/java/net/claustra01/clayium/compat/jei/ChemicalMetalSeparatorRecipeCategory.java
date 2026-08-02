/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.compat.jei;

import net.claustra01.clayium.util.MetricFormatter;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.clayium.machine.ChemicalMetalSeparatorProcess;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ChemicalMetalSeparatorRecipeCategory implements IRecipeCategory<ChemicalMetalSeparatorJeiRecipe> {
    private final IDrawable icon;
    public ChemicalMetalSeparatorRecipeCategory(IGuiHelper helper, ItemStack icon) {
        this.icon = helper.createDrawableItemStack(icon);
    }
    @Override public RecipeType<ChemicalMetalSeparatorJeiRecipe> getRecipeType() {
        return ClayiumJeiRecipeTypes.CHEMICAL_METAL_SEPARATOR;
    }
    @Override public Component getTitle() {
        return Component.translatable("jei.clayium_neoforged.category.chemical_metal_separator");
    }
    @Override public int getWidth() { return 176; }
    @Override public int getHeight() { return 112; }
    @Override public IDrawable getIcon() { return icon; }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, ChemicalMetalSeparatorJeiRecipe recipe,
                                    IFocusGroup focuses) {
        builder.addInputSlot(8, 18).setStandardSlotBackground().addItemStack(
                new ItemStack(ClayiumRegistries.COMPONENT_ITEMS.get("industrial_clay_dust").get()));
        for (int index = 0; index < ChemicalMetalSeparatorProcess.PRODUCTS.size(); index++) {
            var product = ChemicalMetalSeparatorProcess.PRODUCTS.get(index);
            builder.addOutputSlot(80 + index % 5 * 18, 8 + index / 5 * 18)
                    .setStandardSlotBackground().addItemStack(product.stack())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                            "jei.clayium_neoforged.chance", String.format(Locale.ROOT, "%.2f",
                                    100.0D * product.weight() / ChemicalMetalSeparatorProcess.TOTAL_WEIGHT))));
        }
    }
    @Override public void draw(ChemicalMetalSeparatorJeiRecipe recipe, IRecipeSlotsView slots,
                               GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("jei.clayium_neoforged.processing_time",
                        MetricFormatter.format(40)),
                8, 86, 0xff555555, false);
        graphics.drawString(font, Component.translatable("jei.clayium_neoforged.clay_energy_per_tick", "5 mCE"),
                8, 98, 0xff555555, false);
    }
}
