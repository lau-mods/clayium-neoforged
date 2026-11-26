package net.claustra01.clayium.client.screen.autocrafter;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class AutoCrafterScreenRegistration {
    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(AutoCrafterRegistration.MENU.get(), AutoCrafterScreen::new);
    }

    private AutoCrafterScreenRegistration() {
    }
}
