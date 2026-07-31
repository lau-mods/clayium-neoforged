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
import net.claustra01.clayium.machine.ChemicalMetalSeparatorProcess;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SpecialProcessRecipeCategory implements IRecipeCategory<SpecialProcessRecipe> {
    private final RecipeType<SpecialProcessRecipe> type;
    private final SpecialProcessRecipe.Kind kind;
    private final IDrawable icon;
    private final IDrawableStatic arrow;
    public SpecialProcessRecipeCategory(IGuiHelper helper, RecipeType<SpecialProcessRecipe> type,
                                        SpecialProcessRecipe.Kind kind, ItemStack icon) {
        this.type=type;this.kind=kind;this.icon=helper.createDrawableItemStack(icon);this.arrow=helper.getRecipeArrow();
    }
    @Override public RecipeType<SpecialProcessRecipe> getRecipeType(){return type;}
    @Override public Component getTitle(){return Component.translatable("jei.clayium_neoforged.category."+(kind==SpecialProcessRecipe.Kind.QUARTZ_CRUCIBLE?"quartz_crucible":"chemical_metal_separator"));}
    @Override public int getWidth(){return 176;} @Override public int getHeight(){return kind==SpecialProcessRecipe.Kind.QUARTZ_CRUCIBLE?78:112;} @Override public IDrawable getIcon(){return icon;}
    @Override public void setRecipe(IRecipeLayoutBuilder b,SpecialProcessRecipe recipe,IFocusGroup focuses){
        if(kind==SpecialProcessRecipe.Kind.QUARTZ_CRUCIBLE){
            b.addInputSlot(20,22).setStandardSlotBackground().addItemStack(new ItemStack(ClayiumRegistries.PHASE6_ITEMS.get("impure_silicon_ingot").get(),recipe.amount()));
            b.addInputSlot(44,22).setStandardSlotBackground().addItemStack(new ItemStack(Items.STRING));
            b.addOutputSlot(116,22).setOutputSlotBackground().addItemStack(new ItemStack(ClayiumRegistries.PHASE6_ITEMS.get("silicon_ingot").get(),recipe.amount()));
        } else {
            b.addInputSlot(8,18).setStandardSlotBackground().addItemStack(new ItemStack(ClayiumRegistries.PHASE4_ITEMS.get("industrial_clay_dust").get()));
            for(int i=0;i<ChemicalMetalSeparatorProcess.PRODUCTS.size();i++){
                var product=ChemicalMetalSeparatorProcess.PRODUCTS.get(i);
                b.addOutputSlot(80+(i%5)*18,8+(i/5)*18).setStandardSlotBackground().addItemStack(product.stack())
                        .addRichTooltipCallback((view,tooltip)->tooltip.add(Component.translatable("jei.clayium_neoforged.chance",100.0D*product.weight()/ChemicalMetalSeparatorProcess.TOTAL_WEIGHT)));
            }
        }
    }
    @Override public void draw(SpecialProcessRecipe recipe,IRecipeSlotsView slots,GuiGraphics g,double mx,double my){
        var font=Minecraft.getInstance().font;
        if(kind==SpecialProcessRecipe.Kind.QUARTZ_CRUCIBLE){arrow.draw(g,76,22);g.drawString(font,Component.translatable("jei.clayium_neoforged.processing_time",600*recipe.amount()),8,56,0xff555555,false);}
        else {g.drawString(font,Component.translatable("jei.clayium_neoforged.processing_time",40),8,86,0xff555555,false);g.drawString(font,Component.translatable("jei.clayium_neoforged.clay_energy_per_tick","5 mCE"),8,98,0xff555555,false);}
    }
}
