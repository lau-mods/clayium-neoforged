package net.claustra01.clayium.energy;

import net.claustra01.clayium.block.UpgradeBlock;
import net.claustra01.clayium.block.UpgradeBlock.UpgradeKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

/**
 * Exact overclock, energy storage upgrade, resonator, and CA reactor math ported from 1.7.10 Clayium.
 */
public final class ClayEnergyUpgradeMath {
    public static final int RESONATOR_SCAN_RANGE = 2;

    private ClayEnergyUpgradeMath() {}

    public static double effectiveOverclockFactor(double overclockTotalFactor) {
        return overclockTotalFactor > 10.0 ? overclockTotalFactor / 10.0 : 1.0;
    }

    public static double tickIncrement(double overclockTotalFactor, double effectiveFactor) {
        return effectiveFactor <= 0.0 ? 1.0 : overclockTotalFactor / effectiveFactor;
    }

    public static float craftTimeMultiplier(float initCraftTime, double effectiveFactor) {
        return (float) ((double) initCraftTime / effectiveFactor);
    }

    public static float energyConsumptionMultiplier(float initConsumingEnergy, double effectiveFactor) {
        return (float) ((double) initConsumingEnergy * Math.pow(effectiveFactor, 1.5));
    }

    public static int clampEnergySlotLimit(int sumAdditionalStorage) {
        return Math.max(1, Math.min(ClayEnergyStorage.MAX_ENERGY_SLOT_LIMIT, 1 + sumAdditionalStorage));
    }

    public static double caInjectorExponent(int tier) {
        return switch (tier) {
            case 9 -> 0.2;
            case 10 -> 0.9;
            case 11 -> 3.0;
            default -> 1.0;
        };
    }

    public static double caInjectorCraftTimeMultiplier(double resonance, int tier) {
        return Math.pow(Math.max(1.0, resonance), -caInjectorExponent(tier));
    }

    public static double caReactorEfficiency(int hullNum, double averageRank) {
        return 0.2 * Math.pow(1.02, hullNum) * Math.pow(7.5, averageRank);
    }

    public static double caReactorConsumingEnergyMultiplier(int hullNum, double averageRank) {
        return Math.pow(1.01, (double) hullNum * averageRank);
    }

    public static double caReactorPureAntimatterTimeMultiplier(int rank) {
        return Math.pow(9.0, Math.min(Math.max(0, rank), 8));
    }

    public static AdjacentUpgradeSummary scanAdjacentUpgrades(BlockGetter level, BlockPos pos) {
        double overclockTotal = 1.0;
        int additionalStorage = 0;
        for (Direction dir : Direction.values()) {
            Block block = level.getBlockState(pos.relative(dir)).getBlock();
            if (block instanceof UpgradeBlock upgrade) {
                if (upgrade.getKind() == UpgradeKind.OVERCLOCKER) {
                    overclockTotal *= upgrade.getFactor();
                } else if (upgrade.getKind() == UpgradeKind.ENERGY_STORAGE) {
                    additionalStorage += upgrade.getAdditionalStorage();
                }
            }
        }
        return new AdjacentUpgradeSummary(overclockTotal, clampEnergySlotLimit(additionalStorage));
    }

    public static double scanResonance(BlockGetter level, BlockPos center) {
        double resonance = 1.0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -RESONATOR_SCAN_RANGE; dx <= RESONATOR_SCAN_RANGE; dx++) {
            for (int dy = -RESONATOR_SCAN_RANGE; dy <= RESONATOR_SCAN_RANGE; dy++) {
                for (int dz = -RESONATOR_SCAN_RANGE; dz <= RESONATOR_SCAN_RANGE; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    Block block = level.getBlockState(cursor).getBlock();
                    if (block instanceof UpgradeBlock upgrade && upgrade.getKind() == UpgradeKind.RESONATOR) {
                        resonance *= upgrade.getFactor();
                    }
                }
            }
        }
        return resonance;
    }

    public record AdjacentUpgradeSummary(double overclockTotalFactor, int energySlotLimit) {}
}
