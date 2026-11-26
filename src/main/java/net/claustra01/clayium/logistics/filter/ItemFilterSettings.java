package net.claustra01.clayium.logistics.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

/**
 * Original ItemFilterWhitelist / ItemFilterBlacklist: ten remembered stacks (5x2, GUI 20) and a
 * whitelist/blacklist mode that replaces the two separate original items.
 */
public record ItemFilterSettings(Mode mode, List<ItemStack> items) {
    public static final int SIZE = 10;
    public static final ItemFilterSettings EMPTY = new ItemFilterSettings(Mode.WHITELIST, List.of());
    public static final Codec<ItemFilterSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Mode.CODEC.optionalFieldOf("mode", Mode.WHITELIST).forGetter(ItemFilterSettings::mode),
            ItemStack.OPTIONAL_CODEC.listOf(0, SIZE).optionalFieldOf("items", List.of())
                    .forGetter(ItemFilterSettings::items))
            .apply(instance, ItemFilterSettings::new));

    public ItemFilterSettings {
        List<ItemStack> normalized = new ArrayList<>(SIZE);
        for (int index = 0; index < SIZE; index++) {
            ItemStack stack = index < items.size() ? items.get(index) : ItemStack.EMPTY;
            normalized.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        }
        items = List.copyOf(normalized);
    }

    public ItemStack item(int index) {
        return items.get(index);
    }

    public boolean accepts(ItemStack candidate) {
        return ItemFilter.accepts(this, candidate);
    }

    public ItemFilterSettings withMode(Mode newMode) {
        return new ItemFilterSettings(newMode, items);
    }

    public ItemFilterSettings withItem(int index, ItemStack stack) {
        List<ItemStack> changed = new ArrayList<>(items);
        changed.set(index, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        return new ItemFilterSettings(mode, changed);
    }

    public int entryCount() {
        return (int)items.stream().filter(stack -> !stack.isEmpty()).count();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ItemFilterSettings settings) || mode != settings.mode) {
            return false;
        }
        for (int index = 0; index < SIZE; index++) {
            if (!ItemStack.matches(items.get(index), settings.items.get(index))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = mode.hashCode();
        for (ItemStack stack : items) {
            hash = hash * 31 + ItemStack.hashItemAndComponents(stack);
        }
        return hash;
    }

    public enum Mode implements StringRepresentable {
        WHITELIST,
        BLACKLIST;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public Mode next() {
            return this == WHITELIST ? BLACKLIST : WHITELIST;
        }
    }
}
