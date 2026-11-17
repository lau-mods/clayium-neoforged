package net.claustra01.clayium.machine;

import net.claustra01.clayium.registry.MachineRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class MachineCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                MachineRegistration.MACHINE_BLOCK_ENTITY.get(),
                ClayMachineBlockEntity::itemHandler);
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                MachineRegistration.MACHINE_BLOCK_ENTITY.get(),
                ClayMachineBlockEntity::fluidHandler);
    }

    private MachineCapabilities() {
    }
}
