package net.claustra01.clayium.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.compat.RecipeViewerData;
import net.claustra01.clayium.compat.RecipeViewerLayout;
import net.claustra01.clayium.compat.RecipeViewerRenderer;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

final class EmiPanDuplicationRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final int tier;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs = List.of(EmiStack.of(Items.CLAY));

    EmiPanDuplicationRecipe(EmiRecipeCategory category, int tier) {
        this.category = category;
        this.tier = tier;
        inputs = List.of(
                EmiStack.of(Items.CLAY),
                EmiStack.of(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get()));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public ResourceLocation getId() {
        return ClayiumMod.id("viewer/pan_duplication_tier_" + tier);
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
        RecipeViewerLayout layout = RecipeViewerLayout.pan(tier);
        widgets.addDrawable(0, 0, layout.width(), layout.height(),
                (graphics, mouseX, mouseY, delta) -> RecipeViewerRenderer.draw(graphics, layout));
        widgets.addSlot(inputs.get(0), layout.slots().get(0).x() - 1, layout.slots().get(0).y() - 1).drawBack(false);
        widgets.addSlot(inputs.get(1), layout.slots().get(1).x() - 1, layout.slots().get(1).y() - 1).drawBack(false);
        widgets.addSlot(outputs.getFirst(), layout.slots().get(2).x() - 1, layout.slots().get(2).y() - 1)
                .drawBack(false)
                .recipeContext(this);
    }
}
