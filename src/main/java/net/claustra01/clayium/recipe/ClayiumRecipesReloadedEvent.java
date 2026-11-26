package net.claustra01.clayium.recipe;

import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.Event;

public final class ClayiumRecipesReloadedEvent extends Event {
    private final RecipeManager recipeManager;

    public ClayiumRecipesReloadedEvent(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    public RecipeManager recipeManager() {
        return recipeManager;
    }
}
