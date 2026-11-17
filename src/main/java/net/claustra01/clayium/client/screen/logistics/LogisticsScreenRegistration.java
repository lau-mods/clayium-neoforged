package net.claustra01.clayium.client.screen.logistics;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class LogisticsScreenRegistration {
    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(LogisticsRegistration.BUFFER_MENU.get(), BufferScreen::new);
        event.register(LogisticsRegistration.FILTER_MENU.get(), FilterScreen::new);
    }

    private LogisticsScreenRegistration() {
    }
}
