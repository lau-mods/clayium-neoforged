package net.claustra01.clayium.logistics;

import net.claustra01.clayium.registry.LogisticsRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class LogisticsCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                LogisticsRegistration.BUFFER_BLOCK_ENTITY.get(),
                BufferBlockEntity::itemHandler);
    }

    private LogisticsCapabilities() {
    }
}
