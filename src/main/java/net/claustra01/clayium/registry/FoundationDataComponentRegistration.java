package net.claustra01.clayium.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class FoundationDataComponentRegistration {
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TIER =
            ClayiumRegistries.DATA_COMPONENT_TYPES.registerComponentType(
                    "tier",
                    builder -> builder
                            .persistent(Codec.intRange(0, 13))
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    private FoundationDataComponentRegistration() {
    }
}
