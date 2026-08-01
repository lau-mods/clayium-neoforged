/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.machine;

import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.resources.ResourceLocation;

/** Per-tier processing multipliers used by manufacturing machines. */
public final class MachinePerformance {
    private MachinePerformance() {
    }

    public static int processingTime(MachineRecipe recipe, ResourceLocation machineId, ClayTier tier) {
        double multiplier = timeMultiplier(machineId, tier.progressionIndex());
        return Math.max(1, (int) (recipe.processingTimeTicks() * multiplier));
    }

    public static long clayEnergyPerTick(MachineRecipe recipe, ResourceLocation machineId, ClayTier tier) {
        double multiplier = energyMultiplier(machineId, tier.progressionIndex());
        return Math.max(0, (long) (recipe.clayEnergyPerTick() * multiplier));
    }

    private static double timeMultiplier(ResourceLocation machineId, int tier) {
        if (machineId.equals(ClayiumMachineIds.SMELTER)) {
            return switch (tier) {
                case 4 -> 2.0;
                case 5 -> 0.5;
                case 6 -> 0.125;
                default -> 1.0;
            };
        }
        if (usesGenericTierManager(machineId)) {
            return switch (tier) {
                case 5 -> 0.25;
                case 6 -> 0.0625;
                default -> 1.0;
            };
        }
        return 1.0;
    }

    private static double energyMultiplier(ResourceLocation machineId, int tier) {
        if (machineId.equals(ClayiumMachineIds.SMELTER)) {
            return switch (tier) {
                case 5 -> 14.0;
                case 6 -> 200.0;
                default -> 1.0;
            };
        }
        if (usesGenericTierManager(machineId)) {
            return switch (tier) {
                case 5 -> 5.0;
                case 6 -> 25.0;
                default -> 1.0;
            };
        }
        return 1.0;
    }

    private static boolean usesGenericTierManager(ResourceLocation machineId) {
        return machineId.equals(ClayiumMachineIds.GRINDER)
                || machineId.equals(ClayiumMachineIds.CONDENSER)
                || machineId.equals(ClayiumMachineIds.CENTRIFUGE);
    }
}
