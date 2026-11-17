package net.claustra01.clayium.recipe;

import net.claustra01.clayium.ClayiumMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class RecipeReloadEvents {
    @SubscribeEvent
    public static void datapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            ClayiumRecipeCache.recipesReloaded(event.getPlayerList().getServer().getRecipeManager());
        }
    }

    private RecipeReloadEvents() {
    }
}
