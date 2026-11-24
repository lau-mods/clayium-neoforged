package net.claustra01.clayium.energy;

import net.claustra01.clayium.ClayiumConfig;

public final class TierManager {
    public static final int MIN_TIER = 0;
    public static final int MAX_TIER = 13;

    private static final double[] OVERCLOCKER_FACTORS = {1.5D, 2.3D, 3.5D, 5.0D};

    public static MachineParameters general(int tier) {
        checkTier(tier);
        return new MachineParameters(
                ClayiumConfig.GENERAL_MACHINE.craftTime(tier),
                ClayiumConfig.GENERAL_MACHINE.energy(tier));
    }

    public static MachineParameters smelter(int tier) {
        checkTier(tier);
        return new MachineParameters(
                ClayiumConfig.SMELTER_MACHINE.craftTime(tier),
                ClayiumConfig.SMELTER_MACHINE.energy(tier));
    }

    public static MachineParameters caCondenser(int tier) {
        checkTier(tier);
        return new MachineParameters(
                ClayiumConfig.CA_CONDENSER_MACHINE.craftTime(tier),
                ClayiumConfig.CA_CONDENSER_MACHINE.energy(tier));
    }

    public static TransportParameters machineTransport(int tier) {
        checkTier(tier);
        return transport(ClayiumConfig.MACHINE_TRANSPORT, tier);
    }

    public static TransportParameters bufferTransport(int tier) {
        checkTier(tier);
        return transport(ClayiumConfig.BUFFER_TRANSPORT, tier);
    }

    public static long recipeEnergy(double value, int tier) {
        double internalEnergy = 100.0D * value * Math.pow(10.0D, tier - 4.0D);
        if (internalEnergy >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return value > 0.0D ? Math.max(1L, (long) Math.floor(internalEnergy)) : 0L;
    }

    public static double overclockerFactor(int rank) {
        if (rank < 1 || rank > OVERCLOCKER_FACTORS.length) {
            throw new IllegalArgumentException("Overclocker rank must be from 1 through 4");
        }
        return OVERCLOCKER_FACTORS[rank - 1];
    }

    public static double totalOverclockFactor(int... ranks) {
        double factor = 1.0D;
        for (int rank : ranks) {
            factor *= overclockerFactor(rank);
        }
        return factor;
    }

    public static ProcessingParameters calculate(
            MachineParameters tierParameters,
            long recipeTicks,
            long recipeEnergy,
            double totalOverclockFactor) {
        double overclockFactor = totalOverclockFactor > 10.0D ? totalOverclockFactor / 10.0D : 1.0D;
        double timeMultiplier = tierParameters.craftTimeMultiplier / overclockFactor;
        double energyMultiplier = tierParameters.energyMultiplier * Math.pow(overclockFactor, 1.5D);
        double progressPerTick = totalOverclockFactor / overclockFactor;
        long requiredProgress = floorWithMinimum(recipeTicks * timeMultiplier);
        long energyPerProgress = recipeEnergy == 0L ? 0L : floorWithMinimum(recipeEnergy * energyMultiplier);
        long totalEnergy = saturatedMultiply(requiredProgress, energyPerProgress);
        return new ProcessingParameters(
                requiredProgress,
                ClayEnergy.ofInternal(energyPerProgress),
                ClayEnergy.ofInternal(totalEnergy),
                timeMultiplier,
                energyMultiplier,
                progressPerTick);
    }

    private static TransportParameters transport(ClayiumConfig.TransportBalance balance, int tier) {
        return new TransportParameters(
                balance.insertInterval(tier),
                balance.extractInterval(tier),
                balance.maximumInsert(tier),
                balance.maximumExtract(tier));
    }

    private static void checkTier(int tier) {
        if (tier < MIN_TIER || tier > MAX_TIER) {
            throw new IllegalArgumentException("Tier must be from 0 through 13: " + tier);
        }
    }

    private static long floorWithMinimum(double value) {
        if (!Double.isFinite(value) || value >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.max(1L, (long) Math.floor(value));
    }

    private static long saturatedMultiply(long left, long right) {
        if (left != 0L && right > Long.MAX_VALUE / left) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    public record MachineParameters(double craftTimeMultiplier, double energyMultiplier) {
    }

    public record TransportParameters(
            int insertIntervalTicks,
            int extractIntervalTicks,
            int maximumInsertItems,
            int maximumExtractItems) {
        public TransportParameters(int intervalTicks, int maximumItems) {
            this(intervalTicks, intervalTicks, maximumItems, maximumItems);
        }

        public int intervalTicks() {
            return Math.min(insertIntervalTicks, extractIntervalTicks);
        }

        public int maximumItems() {
            return Math.max(maximumInsertItems, maximumExtractItems);
        }
    }

    public record ProcessingParameters(
            long requiredProgress,
            ClayEnergy energyPerProgress,
            ClayEnergy totalEnergy,
            double timeMultiplier,
            double energyMultiplier,
            double progressPerTick) {
    }

    private TierManager() {
    }
}
