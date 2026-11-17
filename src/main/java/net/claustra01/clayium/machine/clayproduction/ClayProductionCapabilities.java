package net.claustra01.clayium.machine.clayproduction;

import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ClayProductionCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayProductionRegistration.BLOCK_ENTITY.get(),
                ClayProductionBlockEntity::itemHandler);
    }

    private ClayProductionCapabilities() {
    }
}
