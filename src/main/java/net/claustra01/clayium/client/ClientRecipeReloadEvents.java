package net.claustra01.clayium.client;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.recipe.ClayiumRecipeCache;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class ClientRecipeReloadEvents {
    @SubscribeEvent
    public static void recipesUpdated(RecipesUpdatedEvent event) {
        ClayiumRecipeCache.recipesReloaded(event.getRecipeManager());
    }

    private ClientRecipeReloadEvents() {
    }
}
