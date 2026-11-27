package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(Registries.MENU, ClayiumMod.MOD_ID);

    private ClayiumMenus() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
