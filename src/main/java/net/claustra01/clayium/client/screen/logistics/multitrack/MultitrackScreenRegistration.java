package net.claustra01.clayium.client.screen.logistics.multitrack;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class MultitrackScreenRegistration {
    @SubscribeEvent public static void register(RegisterMenuScreensEvent event) {
        event.register(MultitrackRegistration.MULTITRACK_BUFFER_MENU.get(), MultitrackBufferScreen::new);
        event.register(MultitrackRegistration.DISTRIBUTOR_MENU.get(), DistributorScreen::new);
    }
    @SubscribeEvent public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MultitrackRegistration.MULTITRACK_BUFFER_BLOCK_ENTITY.get(),
                MultitrackFaceRenderer::new);
    }
    private MultitrackScreenRegistration() { }
}
