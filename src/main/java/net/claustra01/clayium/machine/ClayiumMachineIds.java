/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.machine;

import net.claustra01.clayium.Clayium;
import net.minecraft.resources.ResourceLocation;

/** Stable machine IDs shared by recipes, processors, and integrations. */
public final class ClayiumMachineIds {
    public static final ResourceLocation CLAY_WORK_TABLE = Clayium.id("clay_work_table");
    public static final ResourceLocation CLAY_BENDING_MACHINE = Clayium.id("clay_bending_machine");
    public static final ResourceLocation ELEMENTAL_MILLING_MACHINE = Clayium.id("elemental_milling_machine");
    public static final ResourceLocation GRINDER = Clayium.id("grinder");
    public static final ResourceLocation CONDENSER = Clayium.id("condenser");
    public static final ResourceLocation DECOMPOSER = Clayium.id("decomposer");
    public static final ResourceLocation SMELTER = Clayium.id("smelter");
    public static final ResourceLocation LATHE = Clayium.id("lathe");
    public static final ResourceLocation CUTTING_MACHINE = Clayium.id("cutting_machine");
    public static final ResourceLocation WIRE_DRAWING_MACHINE = Clayium.id("wire_drawing_machine");
    public static final ResourceLocation PIPE_DRAWING_MACHINE = Clayium.id("pipe_drawing_machine");
    public static final ResourceLocation ASSEMBLER = Clayium.id("assembler");
    public static final ResourceLocation INSCRIBER = Clayium.id("inscriber");
    public static final ResourceLocation CENTRIFUGE = Clayium.id("centrifuge");
    public static final ResourceLocation CHEMICAL_REACTOR = Clayium.id("chemical_reactor");
    public static final ResourceLocation ELECTROLYSIS_REACTOR = Clayium.id("electrolysis_reactor");
    public static final ResourceLocation ALLOY_SMELTER = Clayium.id("alloy_smelter");
    public static final ResourceLocation SOLAR_CLAY_FABRICATOR = Clayium.id("solar_clay_fabricator");
    public static final ResourceLocation CLAY_BLAST_FURNACE = Clayium.id("clay_blast_furnace");
    public static final ResourceLocation ENERGETIC_CLAY_CONDENSER = Clayium.id("energetic_clay_condenser");

    private ClayiumMachineIds() {
    }
}
