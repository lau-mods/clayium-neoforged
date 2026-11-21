package net.claustra01.clayium.client.renderer.area;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class AreaRendererRegistration {
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                AreaMachineRegistration.MARKER_BLOCK_ENTITY.get(), AreaMarkerRenderer::new);
        event.registerBlockEntityRenderer(
                AreaMachineRegistration.MACHINE_BLOCK_ENTITY.get(), AreaMachineRenderer::new);
    }

    private AreaRendererRegistration() {
    }
}
