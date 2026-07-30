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

    private ClayiumMachineIds() {
    }
}
