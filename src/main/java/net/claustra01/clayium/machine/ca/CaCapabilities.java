package net.claustra01.clayium.machine.ca;

import net.claustra01.clayium.registry.CaRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class CaCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CaRegistration.COLLECTOR_BLOCK_ENTITY.get(),
                CaCollectorBlockEntity::itemHandler);
    }

    private CaCapabilities() {
    }
}
