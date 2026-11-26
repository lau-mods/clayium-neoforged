package net.claustra01.clayium.pan;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.recipe.ClayiumRecipesReloadedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class PanReloadEvents {
    @SubscribeEvent
    public static void recipesReloaded(ClayiumRecipesReloadedEvent event) {
        PanNetwork.recipesReloaded();
    }

    private PanReloadEvents() {
    }
}
