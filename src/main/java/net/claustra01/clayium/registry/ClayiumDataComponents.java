package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumDataComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTRY =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ClayiumMod.MOD_ID);

    private ClayiumDataComponents() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
