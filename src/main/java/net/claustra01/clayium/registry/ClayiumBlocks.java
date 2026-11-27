package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumBlocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(ClayiumMod.MOD_ID);

    private ClayiumBlocks() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
