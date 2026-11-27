package net.claustra01.clayium.laser;

/**
 * Color-channel laser payload ported from 1.7.10 {@code ClayLaser}.
 */
public class ClayLaser {
    public static final int COLOR_NUM = 3;
    public static final int LIFESPAN = 10;
    public static final double[] BASES = {2.5, 1.8, 1.5};
    public static final double[] MAX_ENERGIES = {1000.0, 300.0, 100.0};
    public static final double[] DAMPING_RATES = {0.1, 0.1, 0.1};

    public final int[] numbers = new int[COLOR_NUM];
    public int age;

    public ClayLaser(int age, int... n) {
        this.age = age;
        for (int i = 0; i < n.length && i < COLOR_NUM; i++) {
            this.numbers[i] = n[i];
        }
    }

    public static double calculateEnergyPerColor(int number, double base, double maxEnergy, double dampingRate) {
        if (number > 0 && !(dampingRate <= 0.0) && !(maxEnergy < 0.0) && !(base < 1.0)) {
            double c = Math.log(maxEnergy) / (Math.log((1.0 + dampingRate) / dampingRate) * Math.log(base));
            double a = Math.pow(Math.E, (1.0 + dampingRate) / c);
            double e = Math.pow(
                    maxEnergy,
                    Math.log((1.0 + dampingRate) / (Math.pow(a, -number) + dampingRate)) / Math.log((1.0 + dampingRate) / dampingRate)
            );
            double m = 1.0 / (1.0 + dampingRate * Math.pow(a, number))
                    + dampingRate * Math.pow(a, number) / (1.0 + dampingRate * Math.pow(a, number)) * number;
            return Math.max(e * m, 1.0);
        }
        return 1.0;
    }

    public static double getEnergy(int... n) {
        double res = 1.0;
        for (int i = 0; i < n.length && i < COLOR_NUM; i++) {
            res *= calculateEnergyPerColor(n[i], BASES[i], MAX_ENERGIES[i], DAMPING_RATES[i]);
        }
        return res - 1.0;
    }

    public double getEnergy() {
        return getEnergy(this.numbers);
    }

    public static ClayLaser mergeClayLasers(ClayLaser[] lasers) {
        int[] merged = new int[COLOR_NUM];
        int maxAge = 0;
        for (ClayLaser laser : lasers) {
            if (laser != null) {
                maxAge = Math.max(maxAge, laser.age);
            }
        }
        if (maxAge >= LIFESPAN) {
            for (ClayLaser laser : lasers) {
                if (laser != null) {
                    for (int i = 0; i < COLOR_NUM; i++) {
                        merged[i] = Math.max(merged[i], laser.numbers[i]);
                    }
                }
            }
        } else {
            for (ClayLaser laser : lasers) {
                if (laser != null) {
                    for (int i = 0; i < COLOR_NUM; i++) {
                        merged[i] += laser.numbers[i];
                    }
                }
            }
        }
        return new ClayLaser(maxAge, merged);
    }
}
