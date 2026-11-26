package net.claustra01.clayium.multiblock.careactor;

import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.registry.CaReactorRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class CaReactorCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CaReactorRegistration.CORE_BLOCK_ENTITY.get(),
                CaReactorCoreBlockEntity::itemHandler);
        event.registerBlockEntity(
                InterfaceCapabilities.REDSTONE_RECEIVER,
                CaReactorRegistration.CORE_BLOCK_ENTITY.get(),
                (core, direction) -> core);
        event.registerBlockEntity(
                InterfaceCapabilities.LASER_RECEIVER,
                CaReactorRegistration.CORE_BLOCK_ENTITY.get(),
                (core, direction) -> core);
    }

    private CaReactorCapabilities() {
    }
}
