package net.claustra01.clayium.logistics;

import net.claustra01.clayium.logistics.creative.CreativeEnergySourceBlockEntity;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.claustra01.clayium.registry.CreativeSourceRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class LogisticsCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                LogisticsRegistration.BUFFER_BLOCK_ENTITY.get(),
                BufferBlockEntity::itemHandler);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE_BLOCK_ENTITY.get(),
                CreativeEnergySourceBlockEntity::itemHandler);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                MultitrackRegistration.MULTITRACK_BUFFER_BLOCK_ENTITY.get(),
                net.claustra01.clayium.logistics.multitrack.MultitrackBufferBlockEntity::itemHandler);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                MultitrackRegistration.DISTRIBUTOR_BLOCK_ENTITY.get(),
                net.claustra01.clayium.logistics.distributor.DistributorBlockEntity::itemHandler);
    }

    private LogisticsCapabilities() {
    }
}
