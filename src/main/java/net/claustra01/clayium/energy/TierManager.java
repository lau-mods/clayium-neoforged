package net.claustra01.clayium.energy;

public final class TierManager {
    public static final int MIN_TIER = 0;
    public static final int MAX_TIER = 13;

    private static final MachineParameters DEFAULT_MACHINE = new MachineParameters(1.0D, 1.0D);
    private static final MachineParameters[] GENERAL = machineTable();
    private static final MachineParameters[] SMELTER = machineTable();
    private static final MachineParameters[] CA_CONDENSER = machineTable();
    private static final TransportParameters[] MACHINE_TRANSPORT = new TransportParameters[MAX_TIER + 1];
    private static final TransportParameters[] BUFFER_TRANSPORT = new TransportParameters[MAX_TIER + 1];
    private static final double[] OVERCLOCKER_FACTORS = {1.5D, 2.3D, 3.5D, 5.0D};

    static {
        GENERAL[5] = new MachineParameters(0.25D, 5.0D);
        GENERAL[6] = new MachineParameters(0.0625D, 25.0D);
        GENERAL[10] = new MachineParameters(0.01D, 250.0D);

        SMELTER[4] = new MachineParameters(2.0D, 1.0D);
        SMELTER[5] = new MachineParameters(0.5D, 14.0D);
        SMELTER[6] = new MachineParameters(0.125D, 200.0D);
        SMELTER[7] = new MachineParameters(0.03D, 2_800.0D);
        SMELTER[8] = new MachineParameters(0.01D, 40_000.0D);
        SMELTER[9] = new MachineParameters(0.0025D, 560_000.0D);

        CA_CONDENSER[10] = new MachineParameters(0.1D, 10.0D);
        CA_CONDENSER[11] = new MachineParameters(0.01D, 100.0D);

        for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
            MACHINE_TRANSPORT[tier] = tier <= 4
                    ? new TransportParameters(20, 8)
                    : tier == 5 ? new TransportParameters(2, 16) : new TransportParameters(1, 64);
        }

        int[] bufferIntervals = {8, 8, 8, 8, 8, 4, 2, 1, 1, 1, 1, 1, 1, 1};
        int[] bufferAmounts = {1, 1, 1, 1, 1, 4, 16, 64, 128, 192, 256, 512, 1_024, 6_400};
        for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
            BUFFER_TRANSPORT[tier] = new TransportParameters(bufferIntervals[tier], bufferAmounts[tier]);
        }
    }

    public static MachineParameters general(int tier) {
        return GENERAL[tier];
    }

    public static MachineParameters smelter(int tier) {
        return SMELTER[tier];
    }

    public static MachineParameters caCondenser(int tier) {
        return CA_CONDENSER[tier];
    }

    public static TransportParameters machineTransport(int tier) {
        return MACHINE_TRANSPORT[tier];
    }

    public static TransportParameters bufferTransport(int tier) {
        return BUFFER_TRANSPORT[tier];
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

    private static MachineParameters[] machineTable() {
        MachineParameters[] table = new MachineParameters[MAX_TIER + 1];
        java.util.Arrays.fill(table, DEFAULT_MACHINE);
        return table;
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

    public record TransportParameters(int intervalTicks, int maximumItems) {
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
