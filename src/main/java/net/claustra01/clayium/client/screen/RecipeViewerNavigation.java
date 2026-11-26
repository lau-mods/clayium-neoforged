package net.claustra01.clayium.client.screen;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

/** A running viewer supplies the category opener. Container screens also work without a viewer. */
public final class RecipeViewerNavigation {
    public static final ResourceLocation CRAFTING = ResourceLocation.withDefaultNamespace("crafting");
    private static Consumer<ResourceLocation> jei;

    public static void setJei(Consumer<ResourceLocation> opener) {
        jei = opener;
    }

    public static boolean open(ResourceLocation category) {
        if (jei == null) return false;
        jei.accept(category);
        return true;
    }

    private RecipeViewerNavigation() {}
}
