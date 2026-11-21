package net.claustra01.clayium.machine.miner;

import net.claustra01.clayium.registry.MinerRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class MinerCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                MinerRegistration.BLOCK_ENTITY.get(),
                BasicOperationBlockEntity::itemHandler);
    }

    private MinerCapabilities() {
    }
}
