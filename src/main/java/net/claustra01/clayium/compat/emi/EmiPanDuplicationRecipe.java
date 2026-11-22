package net.claustra01.clayium.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.compat.RecipeViewerData;
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
        return 176;
    }

    @Override
    public int getDisplayHeight() {
        return 82;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 2, 2);
        widgets.addSlot(inputs.get(1), 22, 2);
        widgets.addSlot(outputs.getFirst(), 158, 2).recipeContext(this);
        widgets.addText(Component.translatable("gui.clayium.recipe.pan_rate", tier,
                RecipeViewerData.panEnergyPerTick(tier) / 100_000D), 2, 27, 0x404040, false);
        widgets.addText(Component.translatable("gui.clayium.recipe.pan_sample"), 2, 39, 0x404040, false);
        widgets.addText(Component.translatable("gui.clayium.recipe.pan_energy"), 2, 51, 0x404040, false);
        widgets.addText(Component.translatable("gui.clayium.recipe.pan_ticks"), 2, 63, 0x404040, false);
    }
}
