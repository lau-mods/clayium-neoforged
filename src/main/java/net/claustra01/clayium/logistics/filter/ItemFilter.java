package net.claustra01.clayium.logistics.filter;

import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.world.item.ItemStack;

/** Matching of the original ItemFilterWhitelist / ItemFilterTemp. */
public final class ItemFilter {
    public static final int MAX_NESTING = 16;

    /** An empty filter slot accepts everything; a filter item applies its remembered stacks. */
    public static boolean accepts(ItemStack filterStack, ItemStack candidate) {
        ItemFilterSettings settings = settings(filterStack);
        return settings == null || accepts(settings, candidate);
    }

    /**
     * Original memory-slot filter (TileMultitrackBuffer.isItemValidForTrack,
     * TileVacuumContainer.checkFilterSlot): empty accepts all; otherwise the remembered filter
     * must match, or the candidate must be the same item type.
     */
    public static boolean matchesMemory(ItemStack memory, ItemStack candidate) {
        if (memory.isEmpty()) {
            return true;
        }
        ItemFilterSettings settings = settings(memory);
        return settings != null && accepts(settings, candidate)
                || !candidate.isEmpty() && ItemStack.isSameItem(memory, candidate);
    }

    /** True when {@code stack} is a configured filter item (original ItemFilterTemp.isFilter). */
    public static boolean isFilter(ItemStack stack) {
        return settings(stack) != null;
    }

    static boolean accepts(ItemFilterSettings settings, ItemStack candidate) {
        return accepts(settings, candidate, 0, new int[] {0});
    }

    private static boolean accepts(ItemFilterSettings settings, ItemStack candidate, int depth, int[] visited) {
        boolean matched = false;
        for (ItemStack remembered : settings.items()) {
            if (remembered.isEmpty()) {
                continue;
            }
            if (++visited[0] > FilterCopyValidator.MAX_ENTRIES) {
                return false;
            }
            ItemFilterSettings nested = settings(remembered);
            if (nested != null && depth < MAX_NESTING && accepts(nested, candidate, depth + 1, visited)
                    || !candidate.isEmpty() && ItemStack.isSameItem(remembered, candidate)) {
                matched = true;
                break;
            }
        }
        return settings.mode() == ItemFilterSettings.Mode.WHITELIST ? matched : !matched;
    }

    private static ItemFilterSettings settings(ItemStack stack) {
        return stack.is(LogisticsRegistration.ITEM_FILTER.get())
                ? stack.getOrDefault(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.EMPTY)
                : null;
    }

    private ItemFilter() {
    }
}
