/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.laser;

/** Immutable three-colour Clay Laser packet. */
public record ClayLaser(int age, int blue, int green, int red) {
    private static final double[] BASE = {2.5D, 1.8D, 1.5D};
    private static final double[] MAX = {1000D, 300D, 100D};
    private static final double DAMPING = 0.1D;

    public ClayLaser {
        if (age < 0 || blue < 0 || green < 0 || red < 0) throw new IllegalArgumentException("Negative laser value");
    }

    public double energy() {
        return energy(blue, green, red);
    }

    public static double energy(int blue, int green, int red) {
        int[] values = {blue, green, red};
        double result = 1D;
        for (int index = 0; index < values.length; index++) {
            result *= perColour(values[index], BASE[index], MAX[index]);
        }
        return result - 1D;
    }

    private static double perColour(int number, double base, double maximum) {
        if (number <= 0) return 1D;
        double c = Math.log(maximum) / (Math.log((1D + DAMPING) / DAMPING) * Math.log(base));
        double a = Math.exp((1D + DAMPING) / c);
        double e = Math.pow(maximum,
                Math.log((1D + DAMPING) / (Math.pow(a, -number) + DAMPING))
                        / Math.log((1D + DAMPING) / DAMPING));
        double factor = 1D / (1D + DAMPING * Math.pow(a, number))
                + DAMPING * Math.pow(a, number) / (1D + DAMPING * Math.pow(a, number)) * number;
        return Math.max(e * factor, 1D);
    }

    public static ClayLaser merge(java.util.Collection<ClayLaser> lasers) {
        int age = lasers.stream().mapToInt(ClayLaser::age).max().orElse(0);
        int blue = age >= 10 ? lasers.stream().mapToInt(ClayLaser::blue).max().orElse(0)
                : lasers.stream().mapToInt(ClayLaser::blue).sum();
        int green = age >= 10 ? lasers.stream().mapToInt(ClayLaser::green).max().orElse(0)
                : lasers.stream().mapToInt(ClayLaser::green).sum();
        int red = age >= 10 ? lasers.stream().mapToInt(ClayLaser::red).max().orElse(0)
                : lasers.stream().mapToInt(ClayLaser::red).sum();
        return new ClayLaser(age + 1, blue, green, red);
    }
}
