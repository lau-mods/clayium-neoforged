/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import net.minecraft.resources.ResourceLocation;
import net.claustra01.clayium.tier.ClayTier;

/** Small behavioural policies that remain independent from inventory layout and recipe data. */
public final class MachineProcessPolicy {
    private MachineProcessPolicy() {}
    public static boolean consumesInputs(ResourceLocation machineId) {
        return !ClayiumMachineIds.CLAY_FABRICATOR.equals(machineId);
    }
    public static boolean acceptsEnergeticClay(ResourceLocation machineId) {
        return !ClayiumMachineIds.SOLAR_CLAY_FABRICATOR.equals(machineId)
                && !ClayiumMachineIds.CLAY_FABRICATOR.equals(machineId);
    }

    public static int batchSize(ResourceLocation machineId, int inputCount) {
        return ClayiumMachineIds.CLAY_FABRICATOR.equals(machineId)
                ? Math.max(1, Math.min(64, inputCount)) : 1;
    }

    public static long processingTime(long baseTicks, ResourceLocation machineId, ClayTier machineTier, int batchSize) {
        if (!ClayiumMachineIds.CLAY_FABRICATOR.equals(machineId) || batchSize <= 1) return baseTicks;
        double exponent = machineTier.progressionIndex() >= ClayTier.ULTIMATE.progressionIndex() ? 0.3D : 0.85D;
        double scaled = baseTicks * Math.pow(batchSize, exponent);
        return scaled >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(1L, Math.round(scaled));
    }

    public static int outputCount(ResourceLocation machineId, int recipeCount, int batchSize) {
        if (!ClayiumMachineIds.CLAY_FABRICATOR.equals(machineId)) return recipeCount;
        return Math.min(64, Math.multiplyExact(recipeCount, Math.max(1, batchSize)));
    }
}
