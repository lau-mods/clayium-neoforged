package net.claustra01.clayium.laser;

public record LaserBeam(long blue, long green, long red, int age) {
    private static final double[] BASES = {2.5D, 1.8D, 1.5D};
    private static final double[] MAX_ENERGIES = {1_000.0D, 300.0D, 100.0D};
    private static final double DAMPING_RATE = 0.1D;

    public LaserBeam(long blue, long green, long red) {
        this(blue, green, red, 0);
    }

    public static LaserBeam merge(LaserBeam first, LaserBeam second) {
        int mergedAge = Math.max(first.age, second.age);
        if (mergedAge >= 10) {
            return new LaserBeam(
                    Math.max(first.blue, second.blue),
                    Math.max(first.green, second.green),
                    Math.max(first.red, second.red),
                    mergedAge);
        }
        return new LaserBeam(
                saturatedAdd(first.blue, second.blue),
                saturatedAdd(first.green, second.green),
                saturatedAdd(first.red, second.red),
                mergedAge);
    }

    public LaserBeam reflected() {
        return new LaserBeam(blue, green, red, age + 1);
    }

    public double energy() {
        double energy = 1.0D;
        long[] components = {blue, green, red};
        for (int color = 0; color < components.length; color++) {
            energy *= calculateEnergyPerColor(components[color], BASES[color], MAX_ENERGIES[color], DAMPING_RATE);
        }
        return energy - 1.0D;
    }

    public boolean isEmpty() {
        return blue == 0L && green == 0L && red == 0L;
    }

    private static double calculateEnergyPerColor(long count, double base, double maximum, double dampingRate) {
        if (count <= 0L || dampingRate <= 0.0D || maximum < 0.0D || base < 1.0D) {
            return 1.0D;
        }
        double exponent = Math.log(maximum)
                / (Math.log((1.0D + dampingRate) / dampingRate) * Math.log(base));
        double dampingBase = Math.pow(Math.E, (1.0D + dampingRate) / exponent);
        double damped = Math.pow(
                maximum,
                Math.log((1.0D + dampingRate) / (Math.pow(dampingBase, -count) + dampingRate))
                        / Math.log((1.0D + dampingRate) / dampingRate));
        double series = 1.0D / (1.0D + dampingRate * Math.pow(dampingBase, count))
                + count * dampingRate * Math.pow(dampingBase, count)
                / (1.0D + dampingRate * Math.pow(dampingBase, count));
        return Math.max(damped * series, 1.0D);
    }

    private static long saturatedAdd(long first, long second) {
        return Long.MAX_VALUE - first < second ? Long.MAX_VALUE : first + second;
    }
}
