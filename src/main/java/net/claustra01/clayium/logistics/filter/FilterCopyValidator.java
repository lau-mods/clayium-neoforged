package net.claustra01.clayium.logistics.filter;

import java.util.HashSet;
import java.util.Set;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class FilterCopyValidator {
    public static final int MAX_RULES = 100;
    public static final int MAX_NESTING = 16;

    public static boolean copy(ItemStack source, ItemStack destination) {
        ItemFilterSettings settings = source.get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get());
        if (settings == null || !valid(settings, 0, new HashSet<>(), new int[] {0})) {
            return false;
        }
        destination.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(),
                new ItemFilterSettings(settings.mode(), settings.rules()));
        if (destination.is(LogisticsRegistration.FILTER_DUPLICATOR.get())) {
            destination.set(LogisticsRegistration.FILTER_COPY.get(), true);
        }
        return true;
    }

    public static boolean canAdd(ItemFilterSettings settings, ItemFilterRule rule) {
        if (rule.type() != ItemFilterRule.MatchType.NESTED) {
            return true;
        }
        ItemFilterSettings nested = rule.reference().get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get());
        if (nested == null || directFingerprint(settings).equals(directFingerprint(nested))) {
            return false;
        }
        return valid(nested, 1, new HashSet<>(Set.of(directFingerprint(settings))),
                new int[] {settings.rules().size() + 1});
    }

    private static boolean valid(ItemFilterSettings settings, int depth, Set<String> ancestors, int[] count) {
        if (depth > MAX_NESTING || settings.rules().size() > MAX_RULES) {
            return false;
        }
        String fingerprint = directFingerprint(settings);
        if (!ancestors.add(fingerprint)) {
            return false;
        }
        count[0] += settings.rules().size();
        if (count[0] > MAX_RULES) {
            return false;
        }
        for (ItemFilterRule rule : settings.rules()) {
            if (rule.type() != ItemFilterRule.MatchType.NESTED) {
                continue;
            }
            ItemFilterSettings nested = rule.reference().get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get());
            if (nested == null || !valid(nested, depth + 1, new HashSet<>(ancestors), count)) {
                return false;
            }
        }
        return true;
    }

    private static String directFingerprint(ItemFilterSettings settings) {
        return fingerprint(settings, 0);
    }

    private static String fingerprint(ItemFilterSettings settings, int depth) {
        StringBuilder result = new StringBuilder(settings.mode().name());
        for (ItemFilterRule rule : settings.rules()) {
            result.append('|').append(rule.type()).append(':').append(rule.value());
            if (!rule.reference().isEmpty()) {
                result.append('@').append(BuiltInRegistries.ITEM.getKey(rule.reference().getItem()));
                ItemFilterSettings nested = rule.reference().get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get());
                if (nested != null) {
                    result.append('(').append(depth < MAX_NESTING
                            ? fingerprint(nested, depth + 1) : nested.mode()).append(')');
                }
            }
        }
        return result.toString();
    }

    private FilterCopyValidator() {
    }
}
