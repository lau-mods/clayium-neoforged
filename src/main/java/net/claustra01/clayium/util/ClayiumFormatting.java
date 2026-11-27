package net.claustra01.clayium.util;

/**
 * Formatting helpers matching 1.7.10 UtilLocale (CE SI numerals, stack counts, resonance, FE).
 * Note: 1 internal CE unit = 10 uCE, so 100,000 internal units = 1 CE.
 */
public final class ClayiumFormatting {
    private static final String[] CE_NUMERALS = {"u", "m", "", "k", "M", "G", "T", "P", "E", "Z", "Y"};
    private static final String[] STACK_NUMERALS = {"", "k", "M", "G", "T", "P", "E", "Z", "Y"};

    private ClayiumFormatting() {}

    public static String formatClayEnergy(double ce) {
        return formatClayEnergy(ce, true);
    }

    public static String formatClayEnergy(double ce, boolean trimTrailingZeros) {
        double n = ce * 10.0;
        if (n == 0.0) {
            return "0.0";
        }
        String sign = "";
        if (n < 0.0) {
            n = -n;
            sign = "-";
        }
        int k = (int) Math.floor(Math.log10(n));
        int p = Math.min(Math.max(0, k / 3), CE_NUMERALS.length - 1);
        int d = (int) (n * 1000.0 / Math.pow(10.0, p * 3));
        return sign + formatClayEnergyDigits(d, p, p == 0 || trimTrailingZeros);
    }

    public static String formatClayEnergy(long ce) {
        return formatClayEnergy(ce, true);
    }

    public static String formatClayEnergy(long ce, boolean trimTrailingZeros) {
        if (ce == 0L) {
            return "0";
        }
        String sign = "";
        double mag;
        if (ce < 0L) {
            sign = "-";
            mag = (-(double) ce) * 10.0;
        } else {
            mag = ((double) ce) * 10.0;
        }
        int k = (int) Math.floor(Math.log10(mag));
        int p = Math.min(Math.max(0, k / 3), CE_NUMERALS.length - 1);
        int d = (int) (mag * 1000.0 / Math.pow(10.0, p * 3));
        return sign + formatClayEnergyDigits(d, p, p == 0 || trimTrailingZeros);
    }

    private static String formatClayEnergyDigits(int d, int p, boolean trimTrailingZeros) {
        if (d % 10 != 0 || !trimTrailingZeros) {
            return (d / 1000) + "." + (d / 100 % 10) + (d / 10 % 10) + (d % 10) + CE_NUMERALS[p];
        }
        if (d % 100 != 0) {
            return (d / 1000) + "." + (d / 100 % 10) + (d / 10 % 10) + CE_NUMERALS[p];
        }
        if (d % 1000 != 0) {
            return (d / 1000) + "." + (d / 100 % 10) + CE_NUMERALS[p];
        }
        return (d / 1000) + CE_NUMERALS[p];
    }

    public static String formatStackSize(long stackSize) {
        return formatStackSize(stackSize, false);
    }

    public static String formatStackSize(long stackSize, boolean trimTrailingZeros) {
        if (stackSize == 0L) {
            return "0";
        }
        String sign = "";
        double mag = stackSize;
        if (stackSize < 0L) {
            mag = -(double) stackSize;
            sign = "-";
        }
        int k = (int) Math.floor(Math.log10(mag));
        if (k < 5) {
            return sign + (stackSize < 0L ? String.valueOf(-stackSize) : String.valueOf(stackSize));
        }
        int p = Math.min(k / 3, STACK_NUMERALS.length - 1);
        int d = (int) (mag / Math.pow(10.0, k - 2));
        boolean flag1 = trimTrailingZeros && k % 3 <= 1 && d % 10 == 0;
        boolean flag2 = flag1 && k % 3 == 0 && (d / 10 % 10) == 0;
        return sign
                + (d / 100)
                + (flag2 ? "" : (k % 3 == 0 ? "." : "") + (d / 10 % 10))
                + (flag1 ? "" : (k % 3 == 1 ? "." : "") + (d % 10))
                + STACK_NUMERALS[p];
    }

    public static String formatCAResonance(double resonance) {
        if (resonance < 1.0) {
            return String.format("%.4f", resonance);
        }
        if (resonance < 10.0) {
            return String.format("%.3f", resonance);
        }
        if (resonance < 100.0) {
            return String.format("%.2f", resonance);
        }
        if (resonance < 1000.0) {
            return String.format("%.1f", resonance);
        }
        return formatStackSize((long) resonance);
    }

    public static String formatLaser(long laser) {
        return formatStackSize(laser);
    }

    public static String formatCraftTime(long craftTime) {
        return formatStackSize(craftTime, true);
    }

    public static String formatFE(long fe) {
        return String.format("%,d", fe);
    }
}
