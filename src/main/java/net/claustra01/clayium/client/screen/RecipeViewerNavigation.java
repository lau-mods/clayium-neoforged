package net.claustra01.clayium.client.screen;

import java.util.function.Consumer;
import net.claustra01.clayium.client.screen.legacy.LegacyButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** A running viewer supplies the category opener. Container screens also work without a viewer. */
public final class RecipeViewerNavigation {
    public static final ResourceLocation CRAFTING = ResourceLocation.withDefaultNamespace("crafting");
    private static Consumer<ResourceLocation> jei;
    private static Consumer<ResourceLocation> emi;

    public static void setJei(Consumer<ResourceLocation> opener) {
        jei = opener;
    }

    public static void setEmi(Consumer<ResourceLocation> opener) {
        emi = opener;
    }

    public static boolean open(ResourceLocation category) {
        var opener = emi != null ? emi : jei;
        if (opener == null) return false;
        opener.accept(category);
        return true;
    }

    public static LegacyButton button(int x, int y, ResourceLocation category) {
        var button = new LegacyButton(x + 151, y + 4, 18, 16, Component.literal("?"), () -> open(category));
        button.visible = emi != null || jei != null;
        button.setTooltip(Tooltip.create(Component.translatable("gui.clayium.recipe.show")));
        return button;
    }

    private RecipeViewerNavigation() {}
}
