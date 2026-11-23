package net.claustra01.clayium.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import net.claustra01.clayium.client.screen.RecipeViewerNavigation;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import java.util.EnumMap;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.compat.RecipeViewerCatalysts;
import net.claustra01.clayium.compat.RecipeViewerDescriptions;
import net.claustra01.clayium.pan.PanBlock;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;

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
                    .flatMap(net.claustra01.clayium.compat.RecipeViewerPage::forRecipe)
                    .map(page -> new EmiMachineRecipe(category, page))
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

        for (RecipeViewerDescriptions.Page page : RecipeViewerDescriptions.pages()) {
            registry.addRecipe(new EmiInfoRecipe(
                    page.stacks().stream().map(EmiStack::of).map(stack -> (dev.emi.emi.api.stack.EmiIngredient)stack)
                            .toList(),
                    java.util.List.of(page.title(), page.text()),
                    page.location()));
        }

        registry.addWorkstation(dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(WorkbenchRegistration.CLAY_CRAFTING_BOARD_ITEM.get()));
        registry.addRecipeHandler(WorkbenchRegistration.CLAY_WORK_TABLE_MENU.get(),
                new ClayWorkTableEmiRecipeHandler(categories.get(ClayiumRecipeMethod.CLAY_WORK_TABLE)));
        registry.addRecipeHandler(WorkbenchRegistration.CLAY_CRAFTING_BOARD_MENU.get(),
                new ClayCraftingBoardEmiRecipeHandler());
        RecipeViewerNavigation.setEmi(id -> EmiApi.displayRecipeCategory(id.equals(RecipeViewerNavigation.CRAFTING)
                ? VanillaEmiRecipeCategories.CRAFTING : categories.get(ClayiumRecipeMethod.byId(id))));
    }
}
