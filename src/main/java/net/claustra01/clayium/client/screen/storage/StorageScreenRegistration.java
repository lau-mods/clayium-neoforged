package net.claustra01.clayium.client.screen.storage;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.StorageRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class StorageScreenRegistration {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(StorageRegistration.STORAGE_CONTAINER_MENU.get(), StorageContainerScreen::new);
        event.register(StorageRegistration.VOID_CONTAINER_MENU.get(), VoidContainerScreen::new);
        event.register(StorageRegistration.METAL_CHEST_MENU.get(), MetalChestScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                StorageRegistration.METAL_CHEST_BLOCK_ENTITY.get(), MetalChestRenderer::new);
    }

    private StorageScreenRegistration() {
    }
}
