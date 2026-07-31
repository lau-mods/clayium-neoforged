/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.logistics;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.tier.ClayTier;

/** Original Phase 5 logistics variants and stable modern IDs. */
public final class Phase5LogisticsCatalog {
    public record Entry(String blockId, String displayTypeName, LogisticsKind kind, ClayTier tier) {
    }

    public static final List<Entry> ENTRIES = create();

    private Phase5LogisticsCatalog() {
    }

    private static List<Entry> create() {
        List<Entry> entries = new ArrayList<>();
        add(entries, "buffer", "Buffer", LogisticsKind.BUFFER, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
        add(entries, "multitrack_buffer", "Multi-track Buffer", LogisticsKind.MULTITRACK_BUFFER,
                4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
        add(entries, "distributor", "Distributor", LogisticsKind.DISTRIBUTOR, 7, 8, 9);
        entries.add(new Entry("storage_container", "Storage Container", LogisticsKind.STORAGE_CONTAINER, ClayTier.PRECISION));
        entries.add(new Entry("void_container", "Void Container", LogisticsKind.VOID_CONTAINER, ClayTier.PRECISION));
        return List.copyOf(entries);
    }

    private static void add(
            List<Entry> entries, String suffix, String name, LogisticsKind kind, int... tiers) {
        for (int tier : tiers) {
            ClayTier clayTier = ClayTier.byLegacyIndex(tier);
            entries.add(new Entry(clayTier.id() + "_" + suffix, name, kind, clayTier));
        }
    }
}
