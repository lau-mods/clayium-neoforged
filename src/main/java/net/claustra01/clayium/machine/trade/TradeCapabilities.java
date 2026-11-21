package net.claustra01.clayium.machine.trade;

import net.claustra01.clayium.registry.TradeRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class TradeCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TradeRegistration.BLOCK_ENTITY.get(),
                (autoTrader, direction) -> autoTrader.itemHandler(direction));
    }

    private TradeCapabilities() {
    }
}
