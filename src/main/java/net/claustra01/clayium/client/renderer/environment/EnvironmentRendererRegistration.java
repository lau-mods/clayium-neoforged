package net.claustra01.clayium.client.renderer.environment;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class EnvironmentRendererRegistration {
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                EnvironmentMachineRegistration.QUARTZ_CRUCIBLE_BLOCK_ENTITY.get(), QuartzCrucibleRenderer::new);
    }

    private EnvironmentRendererRegistration() {
    }
}
