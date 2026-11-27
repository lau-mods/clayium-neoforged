package net.claustra01.clayium.registry;

import com.mojang.serialization.MapCodec;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.recipe.ClayiumConfigCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ClayiumRecipeConditions {
    public static final DeferredRegister<MapCodec<? extends ICondition>> REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, ClayiumMod.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ClayiumConfigCondition>> CONFIG =
            REGISTRY.register("config", () -> ClayiumConfigCondition.CODEC);

    private ClayiumRecipeConditions() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
