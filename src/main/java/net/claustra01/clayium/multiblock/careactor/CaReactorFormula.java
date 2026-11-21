package net.claustra01.clayium.multiblock.careactor;

import java.util.Optional;

public record CaReactorFormula(
        int hullCount,
        double averageRank,
        double outputMultiplier,
        double energyMultiplier,
        int compressionRank,
        double timeMultiplier) {
    public static Optional<CaReactorFormula> calculate(int hullCount, double averageRank) {
        if (hullCount < 0 || !Double.isFinite(averageRank) || averageRank < 0.0D) {
            return Optional.empty();
        }
        double output = 0.2D * Math.pow(1.02D, hullCount) * Math.pow(7.5D, averageRank);
        double energy = Math.pow(1.01D, hullCount * averageRank);
        double time = Math.pow(9.0D, averageRank);
        if (!positiveFinite(output) || !positiveFinite(energy) || !positiveFinite(time)) {
            return Optional.empty();
        }
        return Optional.of(new CaReactorFormula(
                hullCount,
                averageRank,
                output,
                energy,
                Math.min((int)averageRank, 8),
                time));
    }

    public Optional<ScaledProcess> scale(long recipeTicks, long recipeEnergy) {
        long ticks = checkedFloor(recipeTicks * timeMultiplier);
        long energy = recipeEnergy == 0L ? 0L : checkedFloor(recipeEnergy * energyMultiplier);
        if (ticks < 1L || energy < 0L || energy != 0L && ticks > Long.MAX_VALUE / energy) {
            return Optional.empty();
        }
        return Optional.of(new ScaledProcess(ticks, energy));
    }

    public long outputCount(long baseCount, boolean compress) {
        long count = checkedFloor(baseCount * outputMultiplier
                / (compress ? Math.pow(9.0D, compressionRank) : 1.0D));
        return count < 0L ? -1L : Math.max(1L, count);
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
