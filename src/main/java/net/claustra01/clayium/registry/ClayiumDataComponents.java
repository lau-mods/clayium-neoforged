package net.claustra01.clayium.registry;

import com.mojang.serialization.Codec;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.DirectionMemoryData;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.FilterConfigData;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.MarkerPosData;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.StorageContentData;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumDataComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTRY =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ClayiumMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> CLAY_ENERGY =
            REGISTRY.register("clay_energy", () -> DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> SYNCHRONIZER_TARGET =
            REGISTRY.register("synchronizer_target", () -> DataComponentType.<GlobalPos>builder()
                    .persistent(GlobalPos.CODEC)
                    .networkSynchronized(GlobalPos.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DirectionMemoryData>> DIRECTION_MEMORY =
            REGISTRY.register("direction_memory", () -> DataComponentType.<DirectionMemoryData>builder()
                    .persistent(DirectionMemoryData.CODEC)
                    .networkSynchronized(DirectionMemoryData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FilterConfigData>> FILTER_CONFIG =
            REGISTRY.register("filter_config", () -> DataComponentType.<FilterConfigData>builder()
                    .persistent(FilterConfigData.CODEC)
                    .networkSynchronized(FilterConfigData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MarkerPosData>> MARKER_POS =
            REGISTRY.register("marker_pos", () -> DataComponentType.<MarkerPosData>builder()
                    .persistent(MarkerPosData.CODEC)
                    .networkSynchronized(MarkerPosData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> GADGET_INVENTORY =
            REGISTRY.register("gadget_inventory", () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> FLUID_CONTENT =
            REGISTRY.register("fluid_content", () -> DataComponentType.<SimpleFluidContent>builder()
                    .persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StorageContentData>> STORAGE_CONTENT =
            REGISTRY.register("storage_content", () -> DataComponentType.<StorageContentData>builder()
                    .persistent(StorageContentData.CODEC)
                    .networkSynchronized(StorageContentData.STREAM_CODEC)
                    .build());

    private ClayiumDataComponents() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
