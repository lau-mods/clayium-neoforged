package net.claustra01.clayium.client.renderer.entity;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.WeaponRegistration;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class WeaponEntityRenderers {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WeaponRegistration.CLAY_BALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(WeaponRegistration.TELEPORT_BALL.get(),
                context -> new ThrownItemRenderer<>(context, 1.0F, true));
    }

    private WeaponEntityRenderers() {
    }
}
