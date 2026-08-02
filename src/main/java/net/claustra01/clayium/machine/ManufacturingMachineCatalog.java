/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.machine;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.resources.ResourceLocation;

/** Manufacturing machine variants available through Precision tier. */
public final class ManufacturingMachineCatalog {
    public record Entry(
            String blockId,
            String typeId,
            String displayTypeName,
            ResourceLocation machineId,
            ClayTier tier,
            String originalOverlay) {
    }

    public static final List<Entry> ENTRIES = createEntries();

    private ManufacturingMachineCatalog() {
    }

    private static List<Entry> createEntries() {
        List<Entry> entries = new ArrayList<>();
        add(entries, "bending_machine", "Bending Machine",
                ClayiumMachineIds.CLAY_BENDING_MACHINE, "bendingmachine", 2, 3, 4, 5, 6, 7);
        add(entries, "milling_machine", "Milling Machine",
                ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE, "millingmachine", 3, 4);
        add(entries, "wire_drawing_machine", "Wire Drawing Machine",
                ClayiumMachineIds.WIRE_DRAWING_MACHINE, "wiredrawingmachine", 1, 2, 3, 4);
        add(entries, "pipe_drawing_machine", "Pipe Drawing Machine",
                ClayiumMachineIds.PIPE_DRAWING_MACHINE, "pipedrawingmachine", 1, 2, 3, 4);
        add(entries, "cutting_machine", "Cutting Machine",
                ClayiumMachineIds.CUTTING_MACHINE, "cuttingmachine", 1, 2, 3, 4);
        add(entries, "lathe", "Lathe",
                ClayiumMachineIds.LATHE, "lathe", 1, 2, 3, 4);
        add(entries, "condenser", "Condenser",
                ClayiumMachineIds.CONDENSER, "condenser", 2, 3, 4, 5);
        add(entries, "grinder", "Grinder",
                ClayiumMachineIds.GRINDER, "grinder", 2, 3, 4, 5, 6);
        add(entries, "decomposer", "Decomposer",
                ClayiumMachineIds.DECOMPOSER, "decomposer", 2, 3, 4);
        add(entries, "assembler", "Assembler",
                ClayiumMachineIds.ASSEMBLER, "assembler", 3, 4, 6);
        add(entries, "inscriber", "Inscriber",
                ClayiumMachineIds.INSCRIBER, "inscriber", 3, 4);
        add(entries, "centrifuge", "Centrifuge",
                ClayiumMachineIds.CENTRIFUGE, "centrifuge", 3, 4, 5, 6);
        add(entries, "smelter", "Smelter",
                ClayiumMachineIds.SMELTER, "smelter", 4, 5, 6, 7);
        add(entries, "energetic_clay_condenser", "Energetic Clay Condenser",
                ClayiumMachineIds.ENERGETIC_CLAY_CONDENSER, "eccondenser", 3, 4);
        return List.copyOf(entries);
    }

    private static void add(
            List<Entry> entries,
            String typeId,
            String displayTypeName,
            ResourceLocation machineId,
            String originalOverlay,
            int... tiers) {
        for (int tierIndex : tiers) {
            ClayTier tier = ClayTier.byLegacyIndex(tierIndex);
            entries.add(new Entry(
                    tier.id() + "_" + typeId,
                    typeId,
                    displayTypeName,
                    machineId,
                    tier,
                    originalOverlay));
        }
    }
}
