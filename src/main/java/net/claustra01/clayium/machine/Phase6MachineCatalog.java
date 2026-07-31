/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import java.util.List;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.resources.ResourceLocation;

/** Original chemical-processing machine variants available through Precision tier. */
public final class Phase6MachineCatalog {
    public record Entry(String blockId, String displayTypeName, ResourceLocation machineId,
                        ClayTier tier, String originalOverlay) {}

    public static final List<Entry> ENTRIES = List.of(
            entry(4, "chemical_reactor", "Chemical Reactor", ClayiumMachineIds.CHEMICAL_REACTOR, "chemicalreactor"),
            entry(5, "chemical_reactor", "Chemical Reactor", ClayiumMachineIds.CHEMICAL_REACTOR, "chemicalreactor"),
            entry(6, "electrolysis_reactor", "Electrolysis Reactor", ClayiumMachineIds.ELECTROLYSIS_REACTOR, "electrolysisreactor"),
            entry(6, "alloy_smelter", "Alloy Smelter", ClayiumMachineIds.ALLOY_SMELTER, "alloysmelter"));

    private Phase6MachineCatalog() {}

    private static Entry entry(int tier, String suffix, String name, ResourceLocation machineId, String overlay) {
        ClayTier clayTier = ClayTier.byLegacyIndex(tier);
        return new Entry(clayTier.id() + "_" + suffix, name, machineId, clayTier, overlay);
    }
}
