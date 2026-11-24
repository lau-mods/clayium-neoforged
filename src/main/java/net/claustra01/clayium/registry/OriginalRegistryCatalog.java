package net.claustra01.clayium.registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class OriginalRegistryCatalog {
    public static final List<OriginalVariety> ORIGINAL_VARIETIES = readOriginalVarieties();

    public static final List<MachineFamily> MACHINE_FAMILIES = List.of(
            family("BendingMachine", "bending_machine", 1, 2, 3, 4, 5, 6, 7, 9),
            family("WireDrawingMachine", "wire_drawing_machine", 1, 2, 3, 4),
            family("PipeDrawingMachine", "pipe_drawing_machine", 1, 2, 3, 4),
            family("CuttingMachine", "cutting_machine", 1, 2, 3, 4),
            family("Lathe", "lathe", 1, 2, 3, 4),
            family("Condenser", "condenser", 2, 3, 4, 5, 10),
            family("Grinder", "grinder", 2, 3, 4, 5, 6, 10),
            family("Decomposer", "decomposer", 2, 3, 4),
            family("MillingMachine", "milling_machine", 3, 4),
            family("Assembler", "assembler", 3, 4, 6, 10),
            family("Inscriber", "inscriber", 3, 4),
            family("Centrifuge", "centrifuge", 3, 4, 5, 6),
            family("Smelter", "smelter", 4, 5, 6, 7, 8, 9),
            family("Buffer", "buffer", 4, 5, 6, 7, 8, 9, 10, 11, 12, 13),
            family("MultitrackBuffer", "multitrack_buffer", 4, 5, 6, 7, 8, 9, 10, 11, 12, 13),
            family("ChemicalReactor", "chemical_reactor", 4, 5, 8),
            family("SaltExtractor", "salt_extractor", 4, 5, 6, 7),
            family("AlloySmelter", "alloy_smelter", 6),
            family("ClayInterface", "clay_interface", 5, 6, 7, 8, 9, 10, 11, 12, 13),
            family("RedstoneInterface", "redstone_interface", 5, 6, 7, 8, 9, 10, 11, 12, 13),
            family("ElectrolysisReactor", "electrolysis_reactor", 6, 7, 8, 9),
            family("ClayLaserInterface", "clay_laser_interface", 7, 8, 9, 10, 11, 12, 13),
            family("Distributor", "distributor", 7, 8, 9),
            family("AutoCrafter", "auto_crafter", 5, 6, 7, 8, 9),
            family("Transformer", "transformer", 7, 8, 9, 10, 11, 12),
            family("CACondenser", "ca_condenser", 9, 10, 11),
            family("CAInjector", "ca_injector", 9, 10, 11, 12, 13),
            family("CAReactorCore", "ca_reactor_core", 10, 11, 12, 13),
            family("ClayEnergyLaser", "clay_energy_laser", 7, 8, 9, 10),
            family("PANAdapter", "pan_adapter", 10, 11, 12, 13),
            family("PANDuplicator", "pan_duplicator", 4, 5, 6, 7, 8, 9, 10, 11, 12, 13),
            family("FluidTranslator", "fluid_translator", 4, 5, 6, 7, 8, 9, 10, 11, 12, 13),
            family("FluidTransferMachine", "fluid_transfer_machine", 5));

    public static final List<RegisteredEntry> REGISTERED_ENTRIES = List.of(
            new RegisteredEntry("blockCompressedClay:0", "dense_clay", true),
            new RegisteredEntry("blockCompressedClay:1", "compressed_clay", true),
            new RegisteredEntry("blockClayOre:0", "clay_ore", true),
            new RegisteredEntry("blockClayium", "clayium_block", true),
            new RegisteredEntry("blocksBendingMachine:1", "clay_bending_machine", true),
            new RegisteredEntry("blockClayWaterWheel", "clay_water_wheel", true),
            new RegisteredEntry("blockDenseClayWaterWheel", "dense_clay_water_wheel", true),
            new RegisteredEntry("itemDusts:IndustrialClay", "industrial_clay_dust", false),
            new RegisteredEntry("itemClayWrench", "clay_wrench", false),
            new RegisteredEntry("itemClayPickaxe", "clay_pickaxe", false),
            new RegisteredEntry("itemGadgetHolder", "gadget_holder", false),
            new RegisteredEntry("tile.blockBasicRFGenerator.name", "ce_rf_converter", true),
            new RegisteredEntry("tile.blockAdvancedRFGenerator.name", "advanced_ce_rf_converter", true),
            new RegisteredEntry("tile.blockPrecisionRFGenerator.name", "precision_ce_rf_converter", true),
            new RegisteredEntry("tile.blockClaySteelRFGenerator.name", "clay_steel_ce_rf_converter", true),
            new RegisteredEntry("tile.blockClayiumRFGenerator.name", "clayium_ce_rf_converter", true),
            new RegisteredEntry("tile.blockUltimateRFGenerator.name", "ultimate_ce_rf_converter", true),
            new RegisteredEntry("tile.blockAntimatterRFGenerator.name", "antimatter_ce_rf_converter", true),
            new RegisteredEntry("tile.blockPureAntimatterRFGenerator.name", "pure_antimatter_ce_rf_converter", true),
            new RegisteredEntry("tile.blockOECRFGenerator.name", "oec_ce_rf_converter", true),
            new RegisteredEntry("tile.blockOPARFGenerator.name", "opa_ce_rf_converter", true),
            new RegisteredEntry("tile.blockPANCable.name", "pan_cable", true),
            new RegisteredEntry("tile.blockPANCore.name", "pan_core", true),
            new RegisteredEntry("tile.blockAntimatterPANAdapter.name", "pan_adapter_tier_10", true),
            new RegisteredEntry("tile.blockPureAntimatterPANAdapter.name", "pan_adapter_tier_11", true),
            new RegisteredEntry("tile.blockOECPANAdapter.name", "pan_adapter_tier_12", true),
            new RegisteredEntry("tile.blockOPAPANAdapter.name", "pan_adapter_tier_13", true),
            new RegisteredEntry("tile.blockBasicPANDuplicator.name", "pan_duplicator_tier_4", true),
            new RegisteredEntry("tile.blockAdvancedPANDuplicator.name", "pan_duplicator_tier_5", true),
            new RegisteredEntry("tile.blockPrecisionPANDuplicator.name", "pan_duplicator_tier_6", true),
            new RegisteredEntry("tile.blockClaySteelPANDuplicator.name", "pan_duplicator_tier_7", true),
            new RegisteredEntry("tile.blockClayiumPANDuplicator.name", "pan_duplicator_tier_8", true),
            new RegisteredEntry("tile.blockUltimatePANDuplicator.name", "pan_duplicator_tier_9", true),
            new RegisteredEntry("tile.blockAntimatterPANDuplicator.name", "pan_duplicator_tier_10", true),
            new RegisteredEntry("tile.blockPureAntimatterPANDuplicator.name", "pan_duplicator_tier_11", true),
            new RegisteredEntry("tile.blockOECPANDuplicator.name", "pan_duplicator_tier_12", true),
            new RegisteredEntry("tile.blockOPAPANDuplicator.name", "pan_duplicator_tier_13", true),
            new RegisteredEntry("item.itemFilterDuplicator.copy.name", "item_filter_duplicator", false),
            new RegisteredEntry("item.itemFilterWhitelist.name", "white_list_item_filter", false),
            new RegisteredEntry("item.itemDirectionMemory.name", "direction_memory_card", false),
            new RegisteredEntry("item.itemGadget.Blank.name", "clay_gadget_template", false),
            new RegisteredEntry("item.itemGadget.AntimatterOverclock.name", "clay_gadget_overclock_i", false),
            new RegisteredEntry("item.itemGadget.PureAntimatterOverclock.name", "clay_gadget_overclock_ii", false),
            new RegisteredEntry("item.itemGadget.OECOverclock.name", "clay_gadget_overclock_iii", false),
            new RegisteredEntry("item.itemGadget.OPAOverclock.name", "clay_gadget_overclock_iv", false),
            new RegisteredEntry("item.itemGadget.Flight0.name", "clay_gadget_flight", false),
            new RegisteredEntry("item.itemGadget.Flight1.name", "clay_gadget_free_flight", false),
            new RegisteredEntry("item.itemGadget.Flight2.name", "clay_gadget_supersonic_flight", false),
            new RegisteredEntry("item.itemGadget.Health0.name", "clay_gadget_extra_health_i", false),
            new RegisteredEntry("item.itemGadget.Health1.name", "clay_gadget_extra_health_ii", false),
            new RegisteredEntry("item.itemGadget.Health2.name", "clay_gadget_extra_health_iii", false),
            new RegisteredEntry("item.itemGadget.AutoEat0.name", "clay_gadget_economical_auto_eat", false),
            new RegisteredEntry("item.itemGadget.AutoEat1.name", "clay_gadget_auto_eat", false),
            new RegisteredEntry("item.itemGadget.RepeatedlyAttack.name", "clay_gadget_repeatedly_attack", false),
            new RegisteredEntry("item.itemGadget.LongArm0.name", "clay_gadget_long_arm", false),
            new RegisteredEntry("item.itemGadget.LongArm1.name", "clay_gadget_longer_arm", false),
            new RegisteredEntry("item.itemGadget.LongArm2.name", "clay_gadget_longest_arm", false));

    public static String machineId(MachineFamily family, int tier) {
        if (family.legacyName().equals("BendingMachine") && tier == 1) {
            return "clay_bending_machine";
        }
        if (family.legacyName().equals("CAReactorCore")) {
            return "ca_reactor_core_mk_" + switch (tier) {
                case 10 -> "i";
                case 11 -> "ii";
                case 12 -> "iii";
                case 13 -> "iv";
                default -> throw new IllegalArgumentException("Unsupported CA reactor core tier: " + tier);
            };
        }
        return family.namedIdStem() + "_tier_" + tier;
    }

    public static final List<ExcludedEntry> EXCLUDED_ENTRIES = List.of(
            new ExcludedEntry("capsule_1000mb", "fluid capsules are replaced by direct NeoForge fluid transfer"),
            new ExcludedEntry("capsule_125mb", "fluid capsules are replaced by direct NeoForge fluid transfer"),
            new ExcludedEntry("capsule_25mb", "fluid capsules are replaced by direct NeoForge fluid transfer"),
            new ExcludedEntry("capsule_5mb", "fluid capsules are replaced by direct NeoForge fluid transfer"),
            new ExcludedEntry("capsule_1mb", "fluid capsules are replaced by direct NeoForge fluid transfer"),
            new ExcludedEntry("legacy_numeric_ids", "Minecraft 1.21.1 uses namespaced registry IDs"),
            new ExcludedEntry("legacy_tile_entity_aliases", "old-world migration is outside the supported scope"));

    private static List<OriginalVariety> readOriginalVarieties() {
        try (var stream = OriginalRegistryCatalog.class.getClassLoader()
                        .getResourceAsStream("data/clayium/original_varieties.tsv")) {
            if (stream == null) {
                throw new IllegalStateException("Original variety ledger is missing");
            }
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines().map(line -> {
                    String[] columns = line.split("\t", -1);
                    if (columns.length != 3) {
                        throw new IllegalStateException("Invalid original variety ledger entry: " + line);
                    }
                    return new OriginalVariety(columns[0], columns[1], columns[2]);
                }).toList();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read original variety ledger", exception);
        }
    }

    private static MachineFamily family(String legacyName, String namedIdStem, int... tiers) {
        return new MachineFamily(legacyName, namedIdStem, tiers);
    }

    public record OriginalVariety(String kind, String legacyId, String namedId) {
    }

    public record RegisteredEntry(String legacyId, String namedId, boolean block) {
    }

    public record MachineFamily(String legacyName, String namedIdStem, int[] tiers) {
    }

    public record ExcludedEntry(String legacyId, String reason) {
    }

    private OriginalRegistryCatalog() {
    }
}
