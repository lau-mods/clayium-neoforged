package net.claustra01.clayium.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.clayium.compat.RecipeViewerCatalysts;
import net.claustra01.clayium.compat.RecipeViewerData;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

final class MachineRecipeCategory implements IRecipeCategory<RecipeHolder<ClayiumMachineRecipe>> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 103;
    private final ClayiumRecipeMethod method;
    private final RecipeType<RecipeHolder<ClayiumMachineRecipe>> recipeType;
    private final IDrawable icon;

    MachineRecipeCategory(
            ClayiumRecipeMethod method,
            RecipeType<RecipeHolder<ClayiumMachineRecipe>> recipeType,
            IGuiHelper gui) {
        this.method = method;
        this.recipeType = recipeType;
        ItemStack iconStack = RecipeViewerCatalysts.forMethod(method).stream().findFirst().orElse(ItemStack.EMPTY);
        this.icon = iconStack.isEmpty() ? null : gui.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<RecipeHolder<ClayiumMachineRecipe>> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.clayium.recipe." + method.id());
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
            RecipeHolder<ClayiumMachineRecipe> holder,
            IFocusGroup focuses) {
        ClayiumMachineRecipe recipe = holder.value();
        for (int index = 0; index < recipe.itemInputs().size(); index++) {
            var input = recipe.itemInputs().get(index);
            builder.addInputSlot(2 + index * 20, 2)
                    .setStandardSlotBackground()
                    .addItemStacks(java.util.Arrays.stream(input.ingredient().getItems())
                            .map(stack -> stack.copyWithCount(input.count())).toList());
        }
        for (int index = 0; index < recipe.fluidInputs().size(); index++) {
            var fluid = recipe.fluidInputs().get(index);
            builder.addInputSlot(2 + index * 20, 22)
                    .setStandardSlotBackground()
                    .setFluidRenderer(Math.max(1_000, fluid.getAmount()), true, 16, 16)
                    .addFluidStack(fluid.getFluid(), fluid.getAmount(), fluid.getComponentsPatch())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.literal(fluid.getAmount() + " mB")));
        }
        for (int index = 0; index < recipe.itemOutputs().size(); index++) {
            builder.addOutputSlot(158 - index * 20, 2)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.itemOutputs().get(index));
        }
        for (int index = 0; index < recipe.randomOutputs().size(); index++) {
            var output = recipe.randomOutputs().get(index);
            builder.addOutputSlot(158 - (recipe.itemOutputs().size() + index) * 20, 2)
                    .setStandardSlotBackground()
                    .addItemStack(output.stack())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.literal(
                            Math.round(output.chance() * 100.0F) + "% / weight " + output.weight())));
        }
        for (int index = 0; index < recipe.fluidOutputs().size(); index++) {
            var fluid = recipe.fluidOutputs().get(index);
            builder.addOutputSlot(158 - index * 20, 22)
                    .setStandardSlotBackground()
                    .setFluidRenderer(Math.max(1_000, fluid.getAmount()), true, 16, 16)
                    .addFluidStack(fluid.getFluid(), fluid.getAmount(), fluid.getComponentsPatch())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.literal(fluid.getAmount() + " mB")));
        }
    }

    @Override
    public void draw(
            RecipeHolder<ClayiumMachineRecipe> holder,
            IRecipeSlotsView slots,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        ClayiumMachineRecipe recipe = holder.value();
        RecipeViewerData data = RecipeViewerData.from(recipe);
        var font = Minecraft.getInstance().font;
        if (method == ClayiumRecipeMethod.CLAY_WORK_TABLE) {
            graphics.drawString(font, Component.translatable("gui.clayium.recipe.work_operation", data.requiredTier()),
                    2, 45, 0x404040, false);
            graphics.drawString(font, Component.translatable("gui.clayium.recipe.work_tool." + data.workTool()),
                    2, 56, 0x404040, false);
            graphics.drawString(font, Component.translatable("gui.clayium.recipe.work_clicks", data.processingTicks()),
                    2, 67, 0x404040, false);
        } else {
            graphics.drawString(font, Component.translatable("gui.clayium.recipe.tier", data.requiredTier()),
                    2, 45, 0x404040, false);
            graphics.drawString(font, Component.translatable("gui.clayium.recipe.time",
                    data.requiredTier(), data.processingTicks()), 2, 56, 0x404040, false);
            graphics.drawString(font, Component.translatable("gui.clayium.recipe.energy",
                    data.requiredTier(), data.formattedClayEnergy()), 2, 67, 0x404040, false);
        }
        int fluidLine = 78;
        if (!recipe.fluidInputs().isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.clayium.recipe.fluid_input",
                    RecipeViewerData.formattedFluidAmounts(recipe.fluidInputs())),
                    2, fluidLine, 0x404040, false);
            fluidLine += 11;
        }
        if (!recipe.fluidOutputs().isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.clayium.recipe.fluid_output",
                    RecipeViewerData.formattedFluidAmounts(recipe.fluidOutputs())),
                    2, fluidLine, 0x404040, false);
        } else if (recipe.fluidInputs().isEmpty() && data.structureDependent()) {
            graphics.drawString(font, Component.translatable("gui.clayium.recipe.structure"),
                    2, 78, 0x404040, false);
        }
    }
}
