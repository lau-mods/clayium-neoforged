package net.claustra01.clayium.datagen.recipe;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

public final class ClayiumRecipeBuilder {
    private final ClayiumRecipeMethod processingMethod;
    private final int requiredTier;
    private final List<SizedIngredient> itemInputs = new ArrayList<>();
    private final List<FluidStack> fluidInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private final List<FluidStack> fluidOutputs = new ArrayList<>();
    private final List<ClayiumMachineRecipe.RandomOutput> randomOutputs = new ArrayList<>();
    private final List<ICondition> conditions = new ArrayList<>();
    private long clayEnergy;
    private long processingTicks = 1L;
    private ClayiumMachineRecipe.EnvironmentCondition environment = ClayiumMachineRecipe.EnvironmentCondition.ANY;
    private ClayiumMachineRecipe.ProgressionRate progressionRate = ClayiumMachineRecipe.ProgressionRate.NONE;

    private ClayiumRecipeBuilder(ClayiumRecipeMethod processingMethod, int requiredTier) {
        this.processingMethod = processingMethod;
        this.requiredTier = requiredTier;
    }

    public static ClayiumRecipeBuilder machine(ClayiumRecipeMethod processingMethod, int requiredTier) {
        return new ClayiumRecipeBuilder(processingMethod, requiredTier);
    }

    public ClayiumRecipeBuilder requires(ItemLike item, int count) {
        return requires(Ingredient.of(item), count);
    }

    public ClayiumRecipeBuilder requires(ItemLike item) {
        return requires(item, 1);
    }

    public ClayiumRecipeBuilder requires(Ingredient ingredient, int count) {
        itemInputs.add(new SizedIngredient(ingredient, count));
        return this;
    }

    public ClayiumRecipeBuilder requiresFluid(FluidStack fluid) {
        fluidInputs.add(fluid.copy());
        return this;
    }

    public ClayiumRecipeBuilder output(ItemLike item, int count) {
        itemOutputs.add(new ItemStack(item, count));
        return this;
    }

    public ClayiumRecipeBuilder outputFluid(FluidStack fluid) {
        fluidOutputs.add(fluid.copy());
        return this;
    }

    public ClayiumRecipeBuilder randomOutput(ItemLike item, int count, int weight, float chance) {
        randomOutputs.add(new ClayiumMachineRecipe.RandomOutput(new ItemStack(item, count), weight, chance));
        return this;
    }

    public ClayiumRecipeBuilder clayEnergy(long clayEnergy) {
        this.clayEnergy = clayEnergy;
        return this;
    }

    public ClayiumRecipeBuilder processingTicks(long processingTicks) {
        this.processingTicks = processingTicks;
        return this;
    }

    public ClayiumRecipeBuilder environment(ClayiumMachineRecipe.EnvironmentCondition environment) {
        this.environment = environment;
        return this;
    }

    public ClayiumRecipeBuilder progressionRate(ClayiumMachineRecipe.ProgressionRate progressionRate) {
        this.progressionRate = progressionRate;
        return this;
    }

    public ClayiumRecipeBuilder when(ICondition condition) {
        conditions.add(condition);
        return this;
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        ClayiumMachineRecipe recipe = new ClayiumMachineRecipe(
                processingMethod,
                requiredTier,
                itemInputs,
                fluidInputs,
                itemOutputs,
                fluidOutputs,
                clayEnergy,
                processingTicks,
                environment,
                randomOutputs,
                progressionRate);
        output.accept(id, recipe, null, conditions.toArray(ICondition[]::new));
    }
}
