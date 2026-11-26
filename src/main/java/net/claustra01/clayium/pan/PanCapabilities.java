package net.claustra01.clayium.pan;

import net.claustra01.clayium.registry.PanRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class PanCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                PanRegistration.BLOCK_ENTITY.get(),
                PanBlockEntity::itemHandler);
    }

    private PanCapabilities() {
    }
}
