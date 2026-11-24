package net.claustra01.clayium.compat;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.ProgressionRateRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/** NEIMetalSeparator exposes each weighted product as a separate, searchable recipe page. */
public record RecipeViewerPage(RecipeHolder<ClayiumMachineRecipe> holder, RecipeViewerLayout layout, int outcome) {
    public static Stream<RecipeViewerPage> forRecipe(RecipeHolder<ClayiumMachineRecipe> holder) {
        var recipe = ProgressionRateRules.adjustedRecipeForDisplay(holder.id(), holder.value());
        var displayedHolder = new RecipeHolder<>(holder.id(), recipe);
        var layout = RecipeViewerLayout.machine(recipe);
        if (recipe.processingMethod() == ClayiumRecipeMethod.CHEMICAL_METAL_SEPARATOR && !recipe.randomOutputs().isEmpty()) {
            return IntStream.range(0, recipe.randomOutputs().size())
                    .mapToObj(index -> new RecipeViewerPage(displayedHolder, layout, index));
        }
        return Stream.of(new RecipeViewerPage(displayedHolder, layout, -1));
    }

    public ResourceLocation id() {
        return outcome < 0 ? holder.id() : holder.id().withSuffix("/outcome_" + outcome);
    }

    public List<ClayiumMachineRecipe.RandomOutput> randomOutputs() {
        return outcome < 0 ? holder.value().randomOutputs() : List.of(holder.value().randomOutputs().get(outcome));
    }
}
