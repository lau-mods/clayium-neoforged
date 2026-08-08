/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import java.util.List;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.resources.ResourceLocation;

/** Chemical and advanced processing machine variants. */
public final class SpecializedMachineCatalog {
    public record Entry(String blockId, String displayTypeName, ResourceLocation machineId,
                        ClayTier tier, String originalOverlay) {}

    public static final List<Entry> ENTRIES = List.of(
            entry(4, "chemical_reactor", "Chemical Reactor", ClayiumMachineIds.CHEMICAL_REACTOR, "chemicalreactor"),
            entry(5, "chemical_reactor", "Chemical Reactor", ClayiumMachineIds.CHEMICAL_REACTOR, "chemicalreactor"),
            entry(8, "chemical_reactor", "Chemical Reactor", ClayiumMachineIds.CHEMICAL_REACTOR, "chemicalreactor"),
            entry(6, "electrolysis_reactor", "Electrolysis Reactor", ClayiumMachineIds.ELECTROLYSIS_REACTOR, "electrolysisreactor"),
            entry(7, "electrolysis_reactor", "Electrolysis Reactor", ClayiumMachineIds.ELECTROLYSIS_REACTOR, "electrolysisreactor"),
            entry(8, "electrolysis_reactor", "Electrolysis Reactor", ClayiumMachineIds.ELECTROLYSIS_REACTOR, "electrolysisreactor"),
            entry(9, "electrolysis_reactor", "Electrolysis Reactor", ClayiumMachineIds.ELECTROLYSIS_REACTOR, "electrolysisreactor"),
            entry(6, "alloy_smelter", "Alloy Smelter", ClayiumMachineIds.ALLOY_SMELTER, "alloysmelter"),
            entry(7, "matter_transformer", "Matter Transformer", ClayiumMachineIds.MATTER_TRANSFORMER, "transformer"),
            entry(8, "matter_transformer", "Matter Transformer", ClayiumMachineIds.MATTER_TRANSFORMER, "transformer"),
            entry(9, "matter_transformer", "Matter Transformer", ClayiumMachineIds.MATTER_TRANSFORMER, "transformer"),
            entry(10, "matter_transformer", "Matter Transformer", ClayiumMachineIds.MATTER_TRANSFORMER, "transformer"),
            entry(11, "matter_transformer", "Matter Transformer", ClayiumMachineIds.MATTER_TRANSFORMER, "transformer"),
            entry(12, "matter_transformer", "Matter Transformer", ClayiumMachineIds.MATTER_TRANSFORMER, "transformer"),
            entry(9, "ca_condenser", "CA Condenser", ClayiumMachineIds.CA_CONDENSER, "cacondenser"),
            entry(10, "ca_condenser", "CA Condenser", ClayiumMachineIds.CA_CONDENSER, "cacondenser"),
            entry(11, "ca_condenser", "CA Condenser", ClayiumMachineIds.CA_CONDENSER, "cacondenser"),
            entry(9, "ca_injector", "CA Injector", ClayiumMachineIds.CA_INJECTOR, "cainjector"),
            entry(10, "ca_injector", "CA Injector", ClayiumMachineIds.CA_INJECTOR, "cainjector"),
            entry(11, "ca_injector", "CA Injector", ClayiumMachineIds.CA_INJECTOR, "cainjector"),
            entry(12, "ca_injector", "CA Injector", ClayiumMachineIds.CA_INJECTOR, "cainjector"),
            entry(13, "ca_injector", "CA Injector", ClayiumMachineIds.CA_INJECTOR, "cainjector"),
            entry(8, "clay_fabricator_mk1", "Clay Fabricator MK1", ClayiumMachineIds.CLAY_FABRICATOR, "clayfabricator"),
            entry(9, "clay_fabricator_mk2", "Clay Fabricator MK2", ClayiumMachineIds.CLAY_FABRICATOR, "clayfabricator"),
            entry(13, "clay_fabricator_mk3", "Clay Fabricator MK3", ClayiumMachineIds.CLAY_FABRICATOR, "clayfabricator"),
            entry(13, "energetic_clay_decomposer", "Energetic Clay Decomposer", ClayiumMachineIds.ENERGETIC_CLAY_DECOMPOSER, "ecdecomposer"),
            entry(5, "solar_clay_fabricator_mk1", "Solar Clay Fabricator MK1", ClayiumMachineIds.SOLAR_CLAY_FABRICATOR, "solar"),
            entry(6, "solar_clay_fabricator_mk2", "Solar Clay Fabricator MK2", ClayiumMachineIds.SOLAR_CLAY_FABRICATOR, "solar"),
            entry(7, "lithium_solar_clay_fabricator", "Lithium Solar Clay Fabricator", ClayiumMachineIds.SOLAR_CLAY_FABRICATOR, "solar"));

    private SpecializedMachineCatalog() {}

    private static Entry entry(int tier, String suffix, String name, ResourceLocation machineId, String overlay) {
        ClayTier clayTier = ClayTier.byLegacyIndex(tier);
        return new Entry(clayTier.id() + "_" + suffix, name, machineId, clayTier, overlay);
    }
}
