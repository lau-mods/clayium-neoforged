package net.claustra01.clayium.logistics.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.StringRepresentable;
import java.util.Objects;

public record ItemFilterRule(MatchType type, String value, ItemStack reference) {
    public static final Codec<ItemFilterRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MatchType.CODEC
                    .fieldOf("type").forGetter(ItemFilterRule::type),
            Codec.STRING.optionalFieldOf("value", "").forGetter(ItemFilterRule::value),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("reference", ItemStack.EMPTY)
                    .forGetter(ItemFilterRule::reference))
            .apply(instance, ItemFilterRule::new));

    public ItemFilterRule {
        reference = reference.copyWithCount(reference.isEmpty() ? 0 : 1);
    }

    public static ItemFilterRule fromExample(MatchType type, ItemStack example) {
        String value = switch (type) {
            case TAG -> example.getTags().map(tag -> tag.location().toString()).sorted().findFirst().orElse("");
            case DISPLAY_NAME -> example.getHoverName().getString();
            case INTERNAL_ID -> BuiltInRegistries.ITEM.getKey(example.getItem()).toString();
            case MOD_ID -> BuiltInRegistries.ITEM.getKey(example.getItem()).getNamespace();
            case DURABILITY -> Integer.toString(example.getDamageValue());
            case FUZZY, COMPONENTS, NESTED -> "";
        };
        ItemStack reference = switch (type) {
            case FUZZY, COMPONENTS -> example;
            case NESTED -> example.has(LogisticsRegistration.ITEM_FILTER_SETTINGS.get()) ? example : ItemStack.EMPTY;
            default -> ItemStack.EMPTY;
        };
        return new ItemFilterRule(type, value, reference);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemFilterRule rule && type == rule.type
                && value.equals(rule.value) && ItemStack.matches(reference, rule.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, ItemStack.hashItemAndComponents(reference));
    }

    public enum MatchType implements StringRepresentable {
        FUZZY,
        TAG,
        DISPLAY_NAME,
        INTERNAL_ID,
        MOD_ID,
        DURABILITY,
        COMPONENTS,
        NESTED;

        public static final Codec<MatchType> CODEC = StringRepresentable.fromEnum(MatchType::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public MatchType next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
}
