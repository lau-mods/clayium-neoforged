package net.claustra01.clayium.datagen.recipe;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.material.MaterialRecipeProvider;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.datagen.workbench.WorkbenchRecipeProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.ItemExistsCondition;
import net.neoforged.neoforge.fluids.FluidStack;

public final class MachineRecipeProvider extends RecipeProvider {
    public MachineRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        MaterialRecipeProvider.buildRecipes(recipeOutput);
        WorkbenchRecipeProvider.buildWorkbenchRecipes(recipeOutput);
        CondenserGrinderDecomposerRecipes.buildRecipes(recipeOutput);
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.BENDING_MACHINE, 4)
                .requires(Items.CLAY, 2)
                .requiresFluid(new FluidStack(Fluids.WATER, 250))
                .output(MaterialRegistration.COMPRESSION_BLOCKS.get("dense_clay").get(), 1)
                .outputFluid(new FluidStack(Fluids.LAVA, 125))
                .clayEnergy(750_000L)
                .processingTicks(40)
                .environment(new ClayiumMachineRecipe.EnvironmentCondition(
                        Optional.of(Level.OVERWORLD.location()), Optional.of(-64), Optional.of(319), false))
                .randomOutput(MaterialRegistration.MATERIAL_ITEMS.get("industrial_clay_dust").get(), 2, 7, 0.25F)
                .save(recipeOutput, ClayiumMod.id("gametest/machine_recipe"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, 4)
                .requires(Items.CLAY, 1)
                .output(Items.CLAY, 1)
                .clayEnergy(100_000L)
                .processingTicks(20)
                .when(new ItemExistsCondition("absent_mod:missing_component"))
                .save(recipeOutput, ClayiumMod.id("gametest/conditional_missing_external_item"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, 4)
                .requires(Items.CLAY, 1).output(Items.DIAMOND, 1)
                .clayEnergy(100_000L).processingTicks(20)
                .save(recipeOutput, ClayiumMod.id("gametest/reload_changed"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, 4)
                .requires(Items.BRICK, 1).output(Items.DIAMOND, 1)
                .clayEnergy(200_000L).processingTicks(30)
                .save(recipeOutput, ClayiumMod.id("gametest/reload_removed"));
    }
}
