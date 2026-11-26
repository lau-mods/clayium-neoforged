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
import net.claustra01.clayium.compat.RecipeViewerLayout;
import net.claustra01.clayium.compat.RecipeViewerRenderer;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.claustra01.clayium.compat.RecipeViewerPage;



final class MachineRecipeCategory implements IRecipeCategory<RecipeViewerPage> {
    private final ClayiumRecipeMethod method;
    private final RecipeType<RecipeViewerPage> recipeType;
    private final IDrawable icon;

    MachineRecipeCategory(
            ClayiumRecipeMethod method,
            RecipeType<RecipeViewerPage> recipeType,
            IGuiHelper gui) {
        this.method = method;
        this.recipeType = recipeType;
        ItemStack iconStack = RecipeViewerCatalysts.forMethod(method).stream().findFirst().orElse(ItemStack.EMPTY);
        this.icon = iconStack.isEmpty() ? null : gui.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<RecipeViewerPage> getRecipeType() {
        return recipeType;
    }

    @Override
    public net.minecraft.resources.ResourceLocation getRegistryName(RecipeViewerPage page) {
        return page.id();
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.clayium_neoforged.recipe." + method.id());
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
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            RecipeViewerPage page,
            IFocusGroup focuses) {
        ClayiumMachineRecipe recipe = page.holder().value();
        RecipeViewerData data = RecipeViewerData.from(recipe);
        RecipeViewerLayout layout = page.layout();
        for (RecipeViewerLayout.Slot slot : layout.slots()) {
            switch (slot.kind()) {
                case ITEM_INPUT -> {
                    var input = recipe.itemInputs().get(slot.sourceIndex());
                    builder.addInputSlot(slot.x(), slot.y())
                            .addItemStacks(java.util.Arrays.stream(input.ingredient().getItems())
                                    .map(stack -> stack.copyWithCount(input.count())).toList());
                }
                case FLUID_INPUT -> addFluid(builder.addInputSlot(slot.x(), slot.y()),
                        recipe.fluidInputs().get(slot.sourceIndex()));
                case TOOL -> builder.addInputSlot(slot.x(), slot.y())
                        .addItemStacks(data.workToolItems());
                case ITEM_OUTPUT -> builder.addOutputSlot(slot.x(), slot.y())
                        .addItemStack(recipe.itemOutputs().get(slot.sourceIndex()));
                case FLUID_OUTPUT -> addFluid(builder.addOutputSlot(slot.x(), slot.y()),
                        recipe.fluidOutputs().get(slot.sourceIndex()));
                case RANDOM_OUTPUT -> addRandomOutput(
                        builder.addOutputSlot(slot.x(), slot.y()),
                        recipe, recipe.randomOutputs().get(slot.sourceIndex()));
                case WEIGHTED_OUTPUTS -> addRandomOutput(builder.addOutputSlot(slot.x(), slot.y()),
                        recipe, recipe.randomOutputs().get(page.outcome()));
            }
        }
    }

    private static void addFluid(
            mezz.jei.api.gui.builder.IRecipeSlotBuilder slot,
            net.neoforged.neoforge.fluids.FluidStack fluid) {
        slot.setFluidRenderer(Math.max(1_000, fluid.getAmount()), false, 16, 16)
                .addFluidStack(fluid.getFluid(), fluid.getAmount(), fluid.getComponentsPatch());
    }

    private static void addRandomOutput(
            mezz.jei.api.gui.builder.IRecipeSlotBuilder slot,
            ClayiumMachineRecipe recipe,
            ClayiumMachineRecipe.RandomOutput output) {
        slot.addItemStack(output.stack())
                .addRichTooltipCallback((view, tooltip) -> addProbabilityTooltip(tooltip, recipe, output));
    }

    private static void addProbabilityTooltip(
            mezz.jei.api.gui.builder.ITooltipBuilder tooltip,
            ClayiumMachineRecipe recipe,
            ClayiumMachineRecipe.RandomOutput output) {
        tooltip.add(Component.translatable(
                "gui.clayium_neoforged.recipe.probability_weight",
                RecipeViewerLayout.formattedRandomOutputPercent(recipe, output),
                output.weight(),
                RecipeViewerLayout.totalRandomWeight(recipe)));
    }

    @Override
    public void draw(
            RecipeViewerPage page,
            IRecipeSlotsView slots,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        RecipeViewerRenderer.drawPage(graphics, page);
    }
}
