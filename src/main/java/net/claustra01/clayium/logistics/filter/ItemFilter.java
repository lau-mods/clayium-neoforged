package net.claustra01.clayium.logistics.filter;

import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public final class ItemFilter {
    private static final int MAX_NESTING = 16;

    public static boolean accepts(ItemStack filterStack, ItemStack candidate) {
        if (filterStack.isEmpty() || !filterStack.has(LogisticsRegistration.ITEM_FILTER_SETTINGS.get())) {
            return true;
        }
        return filterStack.getOrDefault(
                LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.ALLOW_ALL).accepts(candidate);
    }

    static boolean matches(ItemFilterRule rule, ItemStack candidate, int depth) {
        if (candidate.isEmpty()) {
            return false;
        }
        return switch (rule.type()) {
            case FUZZY -> !rule.reference().isEmpty() && ItemStack.isSameItem(rule.reference(), candidate);
            case TAG -> matchesTag(rule.value(), candidate);
            case DISPLAY_NAME -> candidate.getHoverName().getString().equals(rule.value());
            case INTERNAL_ID -> BuiltInRegistries.ITEM.getKey(candidate.getItem()).toString().equals(rule.value());
            case MOD_ID -> BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace().equals(rule.value());
            case DURABILITY -> candidate.isDamageableItem()
                    && Integer.toString(candidate.getDamageValue()).equals(rule.value());
            case COMPONENTS -> !rule.reference().isEmpty()
                    && ItemStack.isSameItemSameComponents(rule.reference(), candidate);
            case NESTED -> depth < MAX_NESTING && matchesNested(rule.reference(), candidate, depth + 1);
        };
    }

    private static boolean matchesTag(String value, ItemStack candidate) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        return id != null && candidate.is(TagKey.create(Registries.ITEM, id));
    }

    private static boolean matchesNested(ItemStack nested, ItemStack candidate, int depth) {
        ItemFilterSettings settings = nested.get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get());
        if (settings == null) {
            return false;
        }
        boolean matched = settings.rules().stream().anyMatch(rule -> matches(rule, candidate, depth));
        return settings.mode() == ItemFilterSettings.Mode.WHITELIST ? matched : !matched;
    }

    private ItemFilter() {
    }
}
