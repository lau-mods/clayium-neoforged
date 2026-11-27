package net.claustra01.clayium.util;

import net.claustra01.clayium.ClayiumConfig;

/**
 * ProgressionRate scaling helpers ported from 1.7.10 ClayiumCore.
 */
public final class ProgressionRate {
    private ProgressionRate() {}

    public static double rate() {
        return ClayiumConfig.PROGRESSION_RATE.get();
    }

    public static double multiplyD(double value) {
        return multiplyD(value, rate());
    }

    public static double multiplyD(double value, double rate) {
        return value * rate;
    }

    public static float multiplyF(float value) {
        return multiplyF(value, rate());
    }

    public static float multiplyF(float value, double rate) {
        return (float) ((double) value * rate);
    }

    public static int multiplyI(int value) {
        return multiplyI(value, rate());
    }

    public static int multiplyI(int value, double rate) {
        int r = (int) ((double) value * rate);
        return r != 0 ? r : (value < 0 ? -1 : (value > 0 ? 1 : 0));
    }

    public static long multiplyL(long value) {
        return multiplyL(value, rate());
    }

    public static long multiplyL(long value, double rate) {
        long r = (long) ((double) value * rate);
        return r != 0L ? r : (value < 0L ? -1L : (value > 0L ? 1L : 0L));
    }

    public static int multiplyStackSize(int value) {
        return multiplyStackSize(value, rate());
    }

    public static int multiplyStackSize(int value, double rate) {
        return Math.min(64, multiplyI(value, rate));
    }

    public static double divideD(double value) {
        return divideD(value, rate());
    }

    public static double divideD(double value, double rate) {
        return value / rate;
    }

    public static float divideF(float value) {
        return divideF(value, rate());
    }

    public static float divideF(float value, double rate) {
        return (float) ((double) value / rate);
    }

    public static int divideI(int value) {
        return divideI(value, rate());
    }

    public static int divideI(int value, double rate) {
        int r = (int) ((double) value / rate);
        return r != 0 ? r : (value < 0 ? -1 : (value > 0 ? 1 : 0));
    }

    public static long divideL(long value) {
        return divideL(value, rate());
    }

    public static long divideL(long value, double rate) {
        long r = (long) ((double) value / rate);
        return r != 0L ? r : (value < 0L ? -1L : (value > 0L ? 1L : 0L));
    }

    public static int divideStackSize(int value) {
        return divideStackSize(value, rate());
    }

    public static int divideStackSize(int value, double rate) {
        return Math.min(64, divideI(value, rate));
    }
}
