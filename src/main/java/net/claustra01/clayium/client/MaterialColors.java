package net.claustra01.clayium.client;

import net.claustra01.clayium.material.ClayMaterials;
import net.claustra01.clayium.material.MaterialAppearance;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = net.claustra01.clayium.ClayiumMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class MaterialColors {
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (var variant : ClayMaterials.VARIANTS) {
            if (MaterialAppearance.usesTint(variant)) {
                event.register((stack, tintIndex) -> variant.material().color(tintIndex),
                        MaterialRegistration.MATERIAL_ITEMS.get(variant.id()).get());
            }
        }
        event.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> 0xFF464696;
            case 1 -> 0xFF78783C;
            case 2 -> 0xFFDCDCDC;
            default -> 0xFFFFFFFF;
        }, MaterialRegistration.MATERIAL_ITEMS.get("impure_osmium_ingot").get());
    }

    private MaterialColors() {
    }
}
