package net.claustra01.clayium.multiblock;

import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class MultiblockCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                MultiblockRegistration.CONTROLLER_BLOCK_ENTITY.get(),
                MultiblockControllerBlockEntity::itemHandler);
        event.registerBlockEntity(
                InterfaceCapabilities.REDSTONE_RECEIVER,
                MultiblockRegistration.CONTROLLER_BLOCK_ENTITY.get(),
                (controller, direction) -> controller);
        event.registerBlockEntity(
                InterfaceCapabilities.LASER_RECEIVER,
                MultiblockRegistration.CONTROLLER_BLOCK_ENTITY.get(),
                (controller, direction) -> controller.machineType().requiresLaserInterface() ? controller : null);
    }

    private MultiblockCapabilities() {
    }
}
