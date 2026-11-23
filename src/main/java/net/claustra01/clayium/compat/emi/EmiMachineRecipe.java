package net.claustra01.clayium.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.compat.RecipeViewerData;
import net.claustra01.clayium.compat.RecipeViewerLayout;
import net.claustra01.clayium.compat.RecipeViewerRenderer;
import net.claustra01.clayium.compat.RecipeViewerPage;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

final class EmiMachineRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final RecipeViewerPage page;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    EmiMachineRecipe(EmiRecipeCategory category, RecipeViewerPage page) {
        this.category = category;
        this.page = page;
        ClayiumMachineRecipe recipe = page.holder().value();
        List<EmiIngredient> recipeInputs = new ArrayList<>();
        recipe.itemInputs().forEach(input -> recipeInputs.add(EmiIngredient.of(input.ingredient(), input.count())));
        recipe.fluidInputs().forEach(fluid -> recipeInputs.add(EmiStack.of(
                fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount())));
        if (recipe.processingMethod() == ClayiumRecipeMethod.CLAY_WORK_TABLE) {
            List<EmiStack> tools = RecipeViewerData.from(recipe).workToolItems().stream()
                    .map(EmiStack::of)
                    .toList();
            if (!tools.isEmpty()) {
                recipeInputs.add(EmiIngredient.of(tools));
            }
        }
        inputs = List.copyOf(recipeInputs);

        List<EmiStack> recipeOutputs = new ArrayList<>();
        recipe.itemOutputs().forEach(stack -> recipeOutputs.add(EmiStack.of(stack, stack.getCount())));
        recipe.fluidOutputs().forEach(fluid -> recipeOutputs.add(EmiStack.of(
                fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount())));
        page.randomOutputs().forEach(output -> recipeOutputs.add(
                EmiStack.of(output.stack(), output.stack().getCount())
                        .setChance((float)(RecipeViewerLayout.randomOutputPercent(recipe, output) / 100.0D))));
        outputs = List.copyOf(recipeOutputs);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public ResourceLocation getId() {
        return page.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return RecipeViewerLayout.WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return RecipeViewerLayout.HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ClayiumMachineRecipe recipe = page.holder().value();
        RecipeViewerLayout layout = page.layout();
        RecipeViewerData data = RecipeViewerData.from(recipe);
        widgets.addDrawable(0, 0, layout.width(), layout.height(),
                (graphics, mouseX, mouseY, delta) ->
                        RecipeViewerRenderer.drawPage(graphics, page));
        for (RecipeViewerLayout.Slot slot : layout.slots()) {
            switch (slot.kind()) {
                case ITEM_INPUT -> {
                    var input = recipe.itemInputs().get(slot.sourceIndex());
                    widgets.addSlot(EmiIngredient.of(input.ingredient(), input.count()), slot.x() - 1, slot.y() - 1)
                            .drawBack(false);
                }
                case FLUID_INPUT -> addFluidTank(widgets, recipe.fluidInputs().get(slot.sourceIndex()),
                        slot.x() - 1, slot.y() - 1, false);
                case TOOL -> widgets.addSlot(
                        EmiIngredient.of(data.workToolItems().stream().map(EmiStack::of).toList()),
                        slot.x() - 1, slot.y() - 1).drawBack(false);
                case ITEM_OUTPUT -> widgets.addSlot(
                                EmiStack.of(recipe.itemOutputs().get(slot.sourceIndex())), slot.x() - 1, slot.y() - 1)
                        .drawBack(false)
                        .recipeContext(this);
                case FLUID_OUTPUT -> addFluidTank(widgets, recipe.fluidOutputs().get(slot.sourceIndex()),
                        slot.x() - 1, slot.y() - 1, true);
                case RANDOM_OUTPUT -> addRandomOutput(
                        widgets, recipe, recipe.randomOutputs().get(slot.sourceIndex()), slot.x() - 1, slot.y() - 1);
                case WEIGHTED_OUTPUTS -> addRandomOutput(widgets, recipe, recipe.randomOutputs().get(page.outcome()),
                        slot.x() - 1, slot.y() - 1);
            }
        }
    }

    private void addFluidTank(
            WidgetHolder widgets,
            net.neoforged.neoforge.fluids.FluidStack fluid,
            int x,
            int y,
            boolean output) {
        var tank = widgets.addTank(
                        EmiStack.of(fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount()),
                        x, y, 18, 18, Math.max(1_000, fluid.getAmount()))
                .drawBack(false);
        if (output) {
            tank.recipeContext(this);
        }
    }

    private void addRandomOutput(
            WidgetHolder widgets,
            ClayiumMachineRecipe recipe,
            ClayiumMachineRecipe.RandomOutput output,
            int x,
            int y) {
        widgets.addSlot(EmiStack.of(output.stack()).setChance(
                        (float)(RecipeViewerLayout.randomOutputPercent(recipe, output) / 100.0D)), x, y)
                .drawBack(false)
                .recipeContext(this)
                .appendTooltip(Component.translatable(
                        "gui.clayium.recipe.weight",
                        output.weight(),
                        RecipeViewerLayout.totalRandomWeight(recipe)));
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return page.holder();
    }
}
