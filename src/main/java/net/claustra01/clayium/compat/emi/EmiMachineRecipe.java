package net.claustra01.clayium.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.compat.RecipeViewerData;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

final class EmiMachineRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final RecipeHolder<ClayiumMachineRecipe> holder;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    EmiMachineRecipe(EmiRecipeCategory category, RecipeHolder<ClayiumMachineRecipe> holder) {
        this.category = category;
        this.holder = holder;
        ClayiumMachineRecipe recipe = holder.value();
        List<EmiIngredient> recipeInputs = new ArrayList<>();
        recipe.itemInputs().forEach(input -> recipeInputs.add(EmiIngredient.of(input.ingredient(), input.count())));
        recipe.fluidInputs().forEach(fluid -> recipeInputs.add(EmiStack.of(
                fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount())));
        inputs = List.copyOf(recipeInputs);

        List<EmiStack> recipeOutputs = new ArrayList<>();
        recipe.itemOutputs().forEach(stack -> recipeOutputs.add(EmiStack.of(stack, stack.getCount())));
        recipe.fluidOutputs().forEach(fluid -> recipeOutputs.add(EmiStack.of(
                fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount())));
        recipe.randomOutputs().forEach(output -> recipeOutputs.add(
                EmiStack.of(output.stack(), output.stack().getCount()).setChance(output.chance())));
        outputs = List.copyOf(recipeOutputs);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public ResourceLocation getId() {
        return holder.id();
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
        return 176;
    }

    @Override
    public int getDisplayHeight() {
        return 103;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ClayiumMachineRecipe recipe = holder.value();
        int inputIndex = 0;
        for (var input : recipe.itemInputs()) {
            widgets.addSlot(EmiIngredient.of(input.ingredient(), input.count()), 2 + inputIndex++ * 20, 2);
        }
        for (var fluid : recipe.fluidInputs()) {
            widgets.addTank(EmiStack.of(fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount()),
                    2 + (inputIndex++ - recipe.itemInputs().size()) * 20, 22, 18, 18,
                    Math.max(1_000, fluid.getAmount()));
        }
        int outputIndex = 0;
        for (var output : recipe.itemOutputs()) {
            widgets.addSlot(EmiStack.of(output, output.getCount()), 158 - outputIndex++ * 20, 2).recipeContext(this);
        }
        for (var output : recipe.randomOutputs()) {
            widgets.addSlot(EmiStack.of(output.stack(), output.stack().getCount()).setChance(output.chance()),
                    158 - outputIndex++ * 20, 2).recipeContext(this)
                    .appendTooltip(Component.literal("Weight " + output.weight()));
        }
        int fluidOutputIndex = 0;
        for (var fluid : recipe.fluidOutputs()) {
            widgets.addTank(EmiStack.of(fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount()),
                    158 - fluidOutputIndex++ * 20, 22, 18, 18, Math.max(1_000, fluid.getAmount()))
                    .recipeContext(this);
        }

        RecipeViewerData data = RecipeViewerData.from(recipe);
        if (recipe.processingMethod() == ClayiumRecipeMethod.CLAY_WORK_TABLE) {
            widgets.addText(Component.translatable("gui.clayium.recipe.work_operation", data.requiredTier()),
                    2, 45, 0x404040, false);
            widgets.addText(Component.translatable("gui.clayium.recipe.work_tool." + data.workTool()),
                    2, 56, 0x404040, false);
            widgets.addText(Component.translatable("gui.clayium.recipe.work_clicks", data.processingTicks()),
                    2, 67, 0x404040, false);
        } else {
            widgets.addText(Component.translatable("gui.clayium.recipe.tier", data.requiredTier()),
                    2, 45, 0x404040, false);
            widgets.addText(Component.translatable("gui.clayium.recipe.time",
                    data.requiredTier(), data.processingTicks()), 2, 56, 0x404040, false);
            widgets.addText(Component.translatable("gui.clayium.recipe.energy",
                    data.requiredTier(), data.formattedClayEnergy()), 2, 67, 0x404040, false);
        }
        int fluidLine = 78;
        if (!recipe.fluidInputs().isEmpty()) {
            widgets.addText(Component.translatable("gui.clayium.recipe.fluid_input",
                    RecipeViewerData.formattedFluidAmounts(recipe.fluidInputs())),
                    2, fluidLine, 0x404040, false);
            fluidLine += 11;
        }
        if (!recipe.fluidOutputs().isEmpty()) {
            widgets.addText(Component.translatable("gui.clayium.recipe.fluid_output",
                    RecipeViewerData.formattedFluidAmounts(recipe.fluidOutputs())),
                    2, fluidLine, 0x404040, false);
        } else if (recipe.fluidInputs().isEmpty() && data.structureDependent()) {
            widgets.addText(Component.translatable("gui.clayium.recipe.structure"), 2, 78, 0x404040, false);
        }
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }
}
