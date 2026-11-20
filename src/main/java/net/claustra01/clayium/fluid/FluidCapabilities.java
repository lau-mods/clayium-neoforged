package net.claustra01.clayium.fluid;

import net.claustra01.clayium.registry.FluidRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class FluidCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                FluidRegistration.BLOCK_ENTITY.get(),
                FluidTransportBlockEntity::fluidHandler);
    }

    private FluidCapabilities() {
    }
}
