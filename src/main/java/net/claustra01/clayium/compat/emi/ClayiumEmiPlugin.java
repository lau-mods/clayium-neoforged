package net.claustra01.clayium.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import java.util.EnumMap;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.compat.RecipeViewerCatalysts;
import net.claustra01.clayium.pan.PanBlock;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;

@EmiEntrypoint
public final class ClayiumEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        Map<ClayiumRecipeMethod, EmiRecipeCategory> categories = new EnumMap<>(ClayiumRecipeMethod.class);
        for (ClayiumRecipeMethod method : ClayiumRecipeMethod.values()) {
            var catalysts = RecipeViewerCatalysts.forMethod(method);
            EmiRecipeCategory category = new EmiRecipeCategory(
                    method.location(), catalysts.isEmpty() ? EmiStack.EMPTY : EmiStack.of(catalysts.getFirst()));
            categories.put(method, category);
            registry.addCategory(category);
            catalysts.forEach(stack -> registry.addWorkstation(category, EmiStack.of(stack)));
            registry.getRecipeManager().getAllRecipesFor(RecipeRegistration.type(method)).stream()
                    .map(holder -> new EmiMachineRecipe(category, holder))
                    .forEach(registry::addRecipe);
        }

        var duplicator = PanRegistration.DEFINITIONS.get("pan_duplicator_tier_4").block().get();
        EmiRecipeCategory panCategory = new EmiRecipeCategory(
                ClayiumMod.id("pan_duplication"), EmiStack.of(duplicator));
        registry.addCategory(panCategory);
        PanRegistration.DEFINITIONS.values().stream()
                .filter(definition -> definition.kind() == PanBlock.Kind.DUPLICATOR)
                .forEach(definition -> registry.addWorkstation(panCategory, EmiStack.of(definition.block().get())));
        java.util.stream.IntStream.rangeClosed(4, 13)
                .forEach(tier -> registry.addRecipe(new EmiPanDuplicationRecipe(panCategory, tier)));
    }
}
