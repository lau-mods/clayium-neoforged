package net.claustra01.clayium.client.screen.legacy;

import java.util.Locale;

/** Number formats of the original {@code mods.clayium.util.UtilLocale}. */
public final class LegacyNumerals {
    private static final String[] STACK_PREFIXES = {"", "k", "M", "G", "T", "P", "E", "Z", "Y"};

    private LegacyNumerals() {
    }

    /** {@code UtilLocale.CAResonanceNumeral}. */
    public static String resonance(double value) {
        if (value < 1.0D) {
            return String.format(Locale.ROOT, "%.4f", value);
        }
        if (value < 10.0D) {
            return String.format(Locale.ROOT, "%.3f", value);
        }
        if (value < 100.0D) {
            return String.format(Locale.ROOT, "%.2f", value);
        }
        if (value < 1000.0D) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        return stackSize((long)value);
    }

    /** {@code UtilLocale.StackSizeNumeral(n, false)}. */
    public static String stackSize(long n) {
        if (n == 0L) {
            return "0";
        }
        String sign = "";
        if (n < 0L) {
            n--;
            sign = "-";
        }
        int k = (int)Math.floor(Math.log10(n));
        if (k < 5) {
            return sign + n;
        }
        int p = Math.min(k / 3, STACK_PREFIXES.length - 1);
        int d = (int)(n / Math.pow(10.0D, k - 2));
        return sign + d / 100 + (k % 3 == 0 ? "." : "") + d / 10 % 10
                + (k % 3 == 1 ? "." : "") + d % 10 + STACK_PREFIXES[p];
    }
}
