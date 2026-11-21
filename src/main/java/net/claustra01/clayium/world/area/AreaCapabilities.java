package net.claustra01.clayium.world.area;

import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class AreaCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                AreaMachineRegistration.MACHINE_BLOCK_ENTITY.get(),
                AreaMachineBlockEntity::itemHandler);
        event.registerBlockEntity(
                InterfaceCapabilities.LASER_RECEIVER,
                AreaMachineRegistration.MACHINE_BLOCK_ENTITY.get(),
                (machine, direction) -> machine);
    }

    private AreaCapabilities() {
    }
}
