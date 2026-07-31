/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.machine;

import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.resources.ResourceLocation;

/** Original Clayium inventory layouts represented by the shared machine runtime. */
public enum MachineLayout {
    SIMPLE(new int[]{0}, new int[]{1}),
    ASSEMBLER(new int[]{0, 1}, new int[]{2}),
    CENTRIFUGE(new int[]{0}, new int[]{1, 2, 3, 4});

    public static final int ENERGY_SLOT = 5;
    public static final int STORAGE_SLOT_COUNT = 6;

    private final int[] inputSlots;
    private final int[] outputSlots;

    MachineLayout(int[] inputSlots, int[] outputSlots) {
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
    }

    public int[] inputSlots() {
        return inputSlots.clone();
    }

    public int[] outputSlots(ClayTier tier) {
        if (this != CENTRIFUGE) {
            return outputSlots.clone();
        }
        int count = Math.max(1, Math.min(4, tier.progressionIndex() - 2));
        return java.util.Arrays.copyOf(outputSlots, count);
    }

    public boolean isInputSlot(int slot) {
        for (int inputSlot : inputSlots) {
            if (inputSlot == slot) {
                return true;
            }
        }
        return false;
    }

    public boolean isOutputSlot(int slot, ClayTier tier) {
        for (int outputSlot : outputSlots(tier)) {
            if (outputSlot == slot) {
                return true;
            }
        }
        return false;
    }

    public static MachineLayout forMachine(ResourceLocation machineId) {
        if (ClayiumMachineIds.ASSEMBLER.equals(machineId)
                || ClayiumMachineIds.INSCRIBER.equals(machineId)) {
            return ASSEMBLER;
        }
        if (ClayiumMachineIds.CENTRIFUGE.equals(machineId)) {
            return CENTRIFUGE;
        }
        return SIMPLE;
    }
}
