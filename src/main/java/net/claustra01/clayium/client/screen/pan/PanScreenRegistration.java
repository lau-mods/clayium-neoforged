package net.claustra01.clayium.client.screen.pan;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.PanRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class PanScreenRegistration {
    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(PanRegistration.MENU.get(), PanScreen::new);
    }

    private PanScreenRegistration() {
    }
}
