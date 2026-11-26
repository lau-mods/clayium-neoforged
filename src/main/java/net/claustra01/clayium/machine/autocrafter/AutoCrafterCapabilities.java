package net.claustra01.clayium.machine.autocrafter;

import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class AutoCrafterCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                AutoCrafterRegistration.BLOCK_ENTITY.get(),
                AutoCrafterBlockEntity::itemHandler);
    }

    private AutoCrafterCapabilities() {
    }
}
