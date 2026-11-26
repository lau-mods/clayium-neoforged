package net.claustra01.clayium.client.screen.clayproduction;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class ClayProductionScreenRegistration {
    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(ClayProductionRegistration.MENU.get(), ClayProductionScreen::new);
    }

    private ClayProductionScreenRegistration() {
    }
}
