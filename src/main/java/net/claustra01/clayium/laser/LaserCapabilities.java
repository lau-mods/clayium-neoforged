package net.claustra01.clayium.laser;

import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.registry.LaserRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class LaserCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                LaserRegistration.LASER_BLOCK_ENTITY.get(),
                ClayEnergyLaserBlockEntity::fuelHandler);
        event.registerBlockEntity(
                InterfaceCapabilities.LASER_RECEIVER,
                LaserRegistration.REFLECTOR_BLOCK_ENTITY.get(),
                (reflector, side) -> reflector);
    }

    private LaserCapabilities() {
    }
}
