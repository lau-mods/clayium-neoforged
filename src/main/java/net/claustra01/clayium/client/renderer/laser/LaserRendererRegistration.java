package net.claustra01.clayium.client.renderer.laser;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.LaserRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class LaserRendererRegistration {
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                LaserRegistration.LASER_BLOCK_ENTITY.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(
                LaserRegistration.REFLECTOR_BLOCK_ENTITY.get(), LaserBlockEntityRenderer::new);
    }

    private LaserRendererRegistration() {
    }
}
