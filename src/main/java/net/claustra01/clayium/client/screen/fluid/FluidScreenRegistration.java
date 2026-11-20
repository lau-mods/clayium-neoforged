package net.claustra01.clayium.client.screen.fluid;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.FluidRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class FluidScreenRegistration {
    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(FluidRegistration.MENU.get(), FluidTransportScreen::new);
    }

    private FluidScreenRegistration() {
    }
}
