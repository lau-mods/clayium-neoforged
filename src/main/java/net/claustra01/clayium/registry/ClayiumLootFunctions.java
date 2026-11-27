package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.loot.ClayOreBonusLootFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumLootFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> REGISTRY =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, ClayiumMod.MOD_ID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<ClayOreBonusLootFunction>> CLAY_ORE_BONUS =
            REGISTRY.register("clay_ore_bonus", () -> new LootItemFunctionType<>(ClayOreBonusLootFunction.CODEC));

    private ClayiumLootFunctions() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
