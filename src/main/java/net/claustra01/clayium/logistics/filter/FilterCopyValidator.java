package net.claustra01.clayium.logistics.filter;

import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.world.item.ItemStack;

/** Copies filter settings for the filter duplicator within the original filter-size limits. */
public final class FilterCopyValidator {
    public static final int MAX_ENTRIES = 100;
    public static final int MAX_NESTING = ItemFilter.MAX_NESTING;

    public static boolean copy(ItemStack source, ItemStack destination) {
        ItemFilterSettings settings = source.get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get());
        if (settings == null || !valid(settings, 0, new int[] {0})) {
            return false;
        }
        destination.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), settings);
        if (destination.is(LogisticsRegistration.FILTER_DUPLICATOR.get())) {
            destination.set(LogisticsRegistration.FILTER_COPY.get(), true);
        }
        return true;
    }

    /** Original IItemWithFilterSize: every remembered stack and every nested entry counts. */
    public static boolean valid(ItemFilterSettings settings, int depth, int[] count) {
        if (depth > MAX_NESTING) {
            return false;
        }
        for (ItemStack remembered : settings.items()) {
            if (remembered.isEmpty()) {
                continue;
            }
            if (++count[0] > MAX_ENTRIES) {
                return false;
            }
            ItemFilterSettings nested = remembered.is(LogisticsRegistration.ITEM_FILTER.get())
                    ? remembered.get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get()) : null;
            if (nested != null && !valid(nested, depth + 1, count)) {
                return false;
            }
        }
        return true;
    }

    private FilterCopyValidator() {
    }
}
