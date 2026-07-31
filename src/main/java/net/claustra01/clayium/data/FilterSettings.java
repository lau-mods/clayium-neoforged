/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Immutable Smart Filter state. Empty entries deliberately match every item. */
public record FilterSettings(boolean blacklist, List<ResourceLocation> itemIds) {
    public static final FilterSettings DEFAULT = new FilterSettings(false, List.of());
    public static final Codec<FilterSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("blacklist", false).forGetter(FilterSettings::blacklist),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(FilterSettings::itemIds))
            .apply(instance, FilterSettings::new));
    public static final StreamCodec<io.netty.buffer.ByteBuf, FilterSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            FilterSettings::blacklist,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
            FilterSettings::itemIds,
            FilterSettings::new);

    public FilterSettings {
        itemIds = List.copyOf(itemIds);
    }

    public boolean matches(ItemStack stack) {
        if (itemIds.isEmpty()) {
            return true;
        }
        boolean listed = itemIds.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        return blacklist != listed;
    }

    public FilterSettings add(ResourceLocation itemId) {
        if (itemIds.contains(itemId)) {
            return this;
        }
        var copy = new java.util.ArrayList<>(itemIds);
        copy.add(itemId);
        return new FilterSettings(blacklist, copy);
    }

    public FilterSettings toggleListType() {
        return new FilterSettings(!blacklist, itemIds);
    }
}
