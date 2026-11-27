package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(ClayiumMod.MOD_ID);

    private ClayiumItems() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
