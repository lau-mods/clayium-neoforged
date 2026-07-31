/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Persistent contents of a legacy Clayium filter.
 *
 * <p>The filter kind belongs to the Item ID, just as it did in 1.7.10. This
 * component only replaces the old per-stack NBT payload.</p>
 */
public record FilterSettings(String pattern, List<ItemStack> entries, boolean copy) {
    public static final int ENTRY_SLOTS = 10;
    public static final int MAX_NESTED_FILTER_SIZE = 100;
    public static final FilterSettings DEFAULT = new FilterSettings("", List.of(), false);
    public static final Codec<FilterSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("pattern", "").forGetter(FilterSettings::pattern),
                    ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("entries", List.of())
                            .forGetter(FilterSettings::entries),
                    Codec.BOOL.optionalFieldOf("copy", false).forGetter(FilterSettings::copy))
            .apply(instance, FilterSettings::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FilterSettings> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    FilterSettings::pattern,
                    ItemStack.OPTIONAL_LIST_STREAM_CODEC,
                    FilterSettings::entries,
                    ByteBufCodecs.BOOL,
                    FilterSettings::copy,
                    FilterSettings::new);

    public FilterSettings {
        pattern = pattern == null ? "" : pattern.substring(0, Math.min(pattern.length(), 128));
        ArrayList<ItemStack> normalized = new ArrayList<>(ENTRY_SLOTS);
        for (int index = 0; index < Math.min(entries.size(), ENTRY_SLOTS); index++) {
            ItemStack entry = entries.get(index);
            normalized.add(entry.isEmpty() ? ItemStack.EMPTY : entry.copyWithCount(1));
        }
        entries = List.copyOf(normalized);
    }

    public ItemStack entry(int slot) {
        return slot >= 0 && slot < entries.size() ? entries.get(slot).copy() : ItemStack.EMPTY;
    }

    public FilterSettings withEntry(int slot, ItemStack stack) {
        if (slot < 0 || slot >= ENTRY_SLOTS) {
            return this;
        }
        ArrayList<ItemStack> changed = new ArrayList<>(entries);
        while (changed.size() <= slot) {
            changed.add(ItemStack.EMPTY);
        }
        changed.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        return new FilterSettings(pattern, changed, copy);
    }

    public FilterSettings withPattern(String value) {
        return new FilterSettings(value, entries, copy);
    }

    public FilterSettings withCopy(boolean value) {
        return new FilterSettings(pattern, entries, value);
    }
}
