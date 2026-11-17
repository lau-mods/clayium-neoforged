package net.claustra01.clayium.logistics.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.StringRepresentable;

public record ItemFilterSettings(Mode mode, List<ItemFilterRule> rules) {
    public static final ItemFilterSettings ALLOW_ALL = new ItemFilterSettings(Mode.BLACKLIST, List.of());
    public static final Codec<ItemFilterSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Mode.CODEC
                    .optionalFieldOf("mode", Mode.BLACKLIST).forGetter(ItemFilterSettings::mode),
            ItemFilterRule.CODEC.listOf(0, 32)
                    .optionalFieldOf("rules", List.of()).forGetter(ItemFilterSettings::rules))
            .apply(instance, ItemFilterSettings::new));

    public ItemFilterSettings {
        rules = List.copyOf(rules);
    }

    public boolean accepts(ItemStack stack) {
        boolean matched = rules.stream().anyMatch(rule -> ItemFilter.matches(rule, stack, 0));
        return mode == Mode.WHITELIST ? matched : !matched;
    }

    public ItemFilterSettings withMode(Mode newMode) {
        return new ItemFilterSettings(newMode, rules);
    }

    public ItemFilterSettings withRule(ItemFilterRule rule) {
        if (rules.size() >= 32) {
            return this;
        }
        java.util.ArrayList<ItemFilterRule> changed = new java.util.ArrayList<>(rules);
        changed.add(rule);
        return new ItemFilterSettings(mode, changed);
    }

    public ItemFilterSettings withoutLastRule() {
        return rules.isEmpty() ? this : new ItemFilterSettings(mode, rules.subList(0, rules.size() - 1));
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
