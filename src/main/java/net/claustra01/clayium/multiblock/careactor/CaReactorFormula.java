package net.claustra01.clayium.multiblock.careactor;

import java.util.Optional;

/**
 * Original hotfix2 {@code TileCAReactor} rules. {@code averageRank} is the mean hull metadata
 * (rank 1 hull = 0). Efficiency speeds the craft; each craft yields one item.
 */
public record CaReactorFormula(
        int hullCount,
        double averageRank,
        double efficiency,
        double energyMultiplier,
        int compressionRank) {
    public static Optional<CaReactorFormula> calculate(int hullCount, double averageRank) {
        if (hullCount < 0 || !Double.isFinite(averageRank) || averageRank < 0.0D) {
            return Optional.empty();
        }
        double efficiency = 0.2D * Math.pow(1.02D, hullCount) * Math.pow(7.5D, averageRank);
        double energy = Math.pow(1.01D, hullCount * averageRank);
        if (!positiveFinite(efficiency) || !positiveFinite(energy)) {
            return Optional.empty();
        }
        return Optional.of(new CaReactorFormula(
                hullCount, averageRank, efficiency, energy, Math.min((int)averageRank, 8)));
    }

    /**
     * {@code machineTimeToCraft = (long)(1000 * ticks * 9^rank)} advanced by
     * {@code 1000 * efficiency} per tick (truncated because the counter is a long).
     */
    public Optional<ScaledProcess> scale(long recipeTicks, long recipeEnergy) {
        double total = 1000.0D * recipeTicks * Math.pow(9.0D, compressionRank);
        double perTick = Math.floor(1000.0D * efficiency);
        long energy = recipeEnergy == 0L ? 0L : checkedFloor(recipeEnergy * energyMultiplier);
        if (!Double.isFinite(total) || total > Long.MAX_VALUE || perTick < 1.0D || energy < 0L) {
            return Optional.empty();
        }
        long ticks = Math.max(1L, (long)Math.ceil((double)(long)total / perTick));
        if (energy != 0L && ticks > Long.MAX_VALUE / energy) {
            return Optional.empty();
        }
        return Optional.of(new ScaledProcess(ticks, energy));
    }

    private static long checkedFloor(double value) {
        return !Double.isFinite(value) || value < 0.0D || value > Long.MAX_VALUE
                ? -1L : (long)Math.floor(value);
    }

    private static boolean positiveFinite(double value) {
        return Double.isFinite(value) && value > 0.0D;
    }

    public record ScaledProcess(long requiredProgress, long energyPerProgress) {
    }
}
