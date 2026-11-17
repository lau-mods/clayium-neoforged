package net.claustra01.clayium.gadget;

import net.claustra01.clayium.ClayiumMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class GadgetEvents {
    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            GadgetEffects.update(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        GadgetEffects.update(event.getEntity());
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        GadgetEffects.update(event.getEntity());
    }

    @SubscribeEvent
    public static void changeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        GadgetEffects.update(event.getEntity());
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        event.getEntity().getData(net.claustra01.clayium.registry.GadgetRegistration.PLAYER_STATE)
                .supersonicSpeed(false);
        GadgetEffects.update(event.getEntity());
    }

    private GadgetEvents() {
    }
}
