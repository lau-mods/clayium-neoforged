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

    public static long resonanceProcessingTime(
            long baseTicks, ResourceLocation machineId, ClayTier machineTier, double resonance) {
        if (!ClayiumMachineIds.CA_INJECTOR.equals(machineId)) {
            return baseTicks;
        }
        double exponent = switch (machineTier.progressionIndex()) {
            case 9 -> 0.2D;
            case 10 -> 0.9D;
            case 11 -> 3.0D;
            default -> 1.0D;
        };
        double scaled = baseTicks * Math.pow(Math.max(1.0D, resonance), -exponent);
        return Math.max(1L, scaled >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.round(scaled));
    }

    public static int outputCount(ResourceLocation machineId, int recipeCount, int batchSize) {
        if (!ClayiumMachineIds.CLAY_FABRICATOR.equals(machineId)) return recipeCount;
        return Math.min(64, Math.multiplyExact(recipeCount, Math.max(1, batchSize)));
    }

    public static int resonanceOutputCount(ResourceLocation machineId, int recipeCount, double resonance) {
        if (!ClayiumMachineIds.CA_CONDENSER.equals(machineId)) {
            return recipeCount;
        }
        return Math.max(1, (int)Math.floor(recipeCount * (Math.log(Math.max(1.0D, resonance)) + 1.0D)));
    }
}
