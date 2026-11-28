package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.world.ClayCompoundOreFeature;
import net.claustra01.clayium.world.LargeDenseClayOreFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumFeatures {
    public static final DeferredRegister<Feature<?>> REGISTRY =
            DeferredRegister.create(Registries.FEATURE, ClayiumMod.MOD_ID);

    public static final DeferredHolder<Feature<?>, ClayCompoundOreFeature> CLAY_COMPOUND_ORE =
            REGISTRY.register("clay_compound_ore", () -> new ClayCompoundOreFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, LargeDenseClayOreFeature> LARGE_DENSE_CLAY_ORE_FEATURE =
            REGISTRY.register("large_dense_clay_ore", () -> new LargeDenseClayOreFeature(NoneFeatureConfiguration.CODEC));

    private ClayiumFeatures() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
