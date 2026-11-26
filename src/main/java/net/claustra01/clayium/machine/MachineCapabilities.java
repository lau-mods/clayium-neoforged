package net.claustra01.clayium.machine;

import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class MachineCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(InterfaceCapabilities.REDSTONE_RECEIVER,
                MachineRegistration.MACHINE_BLOCK_ENTITY.get(), (machine, direction) -> machine);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                MachineRegistration.MACHINE_BLOCK_ENTITY.get(),
                ClayMachineBlockEntity::itemHandler);
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                MachineRegistration.MACHINE_BLOCK_ENTITY.get(),
                ClayMachineBlockEntity::fluidHandler);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                EnvironmentMachineRegistration.COBBLESTONE_GENERATOR_BLOCK_ENTITY.get(),
                net.claustra01.clayium.machine.environment.CobblestoneGeneratorBlockEntity::itemHandler);
    }

    private MachineCapabilities() {
    }
}
