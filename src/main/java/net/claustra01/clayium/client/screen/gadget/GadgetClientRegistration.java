package net.claustra01.clayium.client.screen.gadget;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.GadgetRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class GadgetClientRegistration {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(GadgetRegistration.GADGET_HOLDER_MENU.get(), GadgetHolderScreen::new);
    }

    private GadgetClientRegistration() {
    }
}
