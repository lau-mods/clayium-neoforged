/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.registry;

import com.mojang.serialization.Codec;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.data.MachineTierData;
import net.claustra01.clayium.data.FilterSettings;
import net.claustra01.clayium.data.IoMemory;
import net.claustra01.clayium.data.ClaySteelToolSettings;
import net.claustra01.clayium.data.StorageContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Clayium.MODID);

    /**
     * Immutable named tier data for tiered ItemStacks. Different tiers are
     * intentionally different stack states. A missing value maps to
     * {@link MachineTierData#DEFAULT} at the call site.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MachineTierData>> MACHINE_TIER =
            DATA_COMPONENTS.register("machine_tier", () -> DataComponentType.<MachineTierData>builder()
                    .persistent(MachineTierData.CODEC)
                    .networkSynchronized(MachineTierData.STREAM_CODEC)
                    .cacheEncoding()
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FilterSettings>> FILTER_SETTINGS =
            DATA_COMPONENTS.register("filter_settings", () -> DataComponentType.<FilterSettings>builder()
                    .persistent(FilterSettings.CODEC)
                    .networkSynchronized(FilterSettings.STREAM_CODEC)
                    .cacheEncoding()
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<IoMemory>> IO_MEMORY =
            DATA_COMPONENTS.register("io_memory", () -> DataComponentType.<IoMemory>builder()
                    .persistent(IoMemory.CODEC)
                    .networkSynchronized(IoMemory.STREAM_CODEC)
                    .cacheEncoding()
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ClaySteelToolSettings>> CLAY_STEEL_TOOL_SETTINGS =
            DATA_COMPONENTS.register("clay_steel_tool_settings", () -> DataComponentType.<ClaySteelToolSettings>builder()
                    .persistent(ClaySteelToolSettings.CODEC)
                    .networkSynchronized(ClaySteelToolSettings.STREAM_CODEC)
                    .cacheEncoding()
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> STORAGE_CAPACITY =
            DATA_COMPONENTS.register("storage_capacity", () -> DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .cacheEncoding()
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StorageContents>> STORAGE_CONTENTS =
            DATA_COMPONENTS.register("storage_contents", () -> DataComponentType.<StorageContents>builder()
                    .persistent(StorageContents.CODEC)
                    .networkSynchronized(StorageContents.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    private ClayiumDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
