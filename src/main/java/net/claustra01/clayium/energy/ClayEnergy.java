package net.claustra01.clayium.energy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ClayEnergy(long internalValue) implements Comparable<ClayEnergy> {
    private static final String[] NUMERAL_PREFIXES = {"u", "m", "", "k", "M", "G", "T", "P", "E", "Z", "Y"};
    public static final long INTERNAL_UNITS_PER_CE = 100_000L;
    public static final ClayEnergy ZERO = new ClayEnergy(0L);
    public static final ClayEnergy MAX = new ClayEnergy(Long.MAX_VALUE);

    public ClayEnergy {
        if (internalValue < 0L) {
            throw new IllegalArgumentException("Clay Energy must not be negative: " + internalValue);
        }
    }

    public static ClayEnergy ofInternal(long internalValue) {
        return internalValue == 0L ? ZERO : new ClayEnergy(internalValue);
    }

    public static ClayEnergy ofCe(long clayEnergy) {
        if (clayEnergy < 0L) {
            throw new IllegalArgumentException("Clay Energy must not be negative: " + clayEnergy + " CE");
        }
        if (clayEnergy > Long.MAX_VALUE / INTERNAL_UNITS_PER_CE) {
            return MAX;
        }
        return ofInternal(clayEnergy * INTERNAL_UNITS_PER_CE);
    }

    public ClayEnergy plus(ClayEnergy other) {
        if (Long.MAX_VALUE - internalValue < other.internalValue) {
            return MAX;
        }
        return ofInternal(internalValue + other.internalValue);
    }

    public ClayEnergy minus(ClayEnergy other) {
        if (other.internalValue >= internalValue) {
            return ZERO;
        }
        return ofInternal(internalValue - other.internalValue);
    }

    public boolean canAfford(ClayEnergy required) {
        return internalValue >= required.internalValue;
    }

    public String format() {
        return formatValue() + " CE";
    }

    /**
     * Stored-energy text of the original GuiClayEnergyTemp:
     * {@code UtilLocale.ClayEnergyNumeral(clayEnergy, false)} followed by {@code CE}.
     */
    public String formatGui() {
        return numeral(internalValue, false) + "CE";
    }

    static String numeral(long ce, boolean trimZeros) {
        if (ce == 0L) {
            return "0";
        }
        double n = ce * 10.0D;
        int k = (int)Math.floor(Math.log10(n));
        int p = Math.min(k / 3, NUMERAL_PREFIXES.length - 1);
        int d = (int)(n * 1000.0D / Math.pow(10.0D, p * 3));
        boolean flag = p == 0 || trimZeros;
        String whole = String.valueOf(d / 1000) + ".";
        if (d % 10 != 0 || !flag) {
            return whole + d / 100 % 10 + d / 10 % 10 + d % 10 + NUMERAL_PREFIXES[p];
        }
        if (d % 100 != 0) {
            return whole + d / 100 % 10 + d / 10 % 10 + NUMERAL_PREFIXES[p];
        }
        if (d % 1000 != 0) {
            return whole + d / 100 % 10 + NUMERAL_PREFIXES[p];
        }
        return d / 1000 + NUMERAL_PREFIXES[p];
    }

    public String formatValue() {
        if (internalValue == 0L) {
            return "0";
        }
        int prefix = 0;
        long nextPrefixThreshold = 100L;
        while (prefix + 1 < NUMERAL_PREFIXES.length && internalValue >= nextPrefixThreshold) {
            prefix++;
            if (nextPrefixThreshold > Long.MAX_VALUE / 1_000L) {
                break;
            }
            nextPrefixThreshold *= 1_000L;
        }
        BigDecimal displayed = BigDecimal.valueOf(internalValue)
                .movePointLeft(prefix * 3 - 1)
                .setScale(3, RoundingMode.DOWN)
                .stripTrailingZeros();
        return displayed.toPlainString() + NUMERAL_PREFIXES[prefix];
    }

    @Override
    public int compareTo(ClayEnergy other) {
        return Long.compare(internalValue, other.internalValue);
    }
}
