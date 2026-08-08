/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.energy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Formats internal CE units with the prefixes used by the original Clayium UI. */
public final class ClayEnergyFormatter {
    private static final String[] PREFIXES = {"u", "m", "", "k", "M", "G", "T", "P", "E"};

    private ClayEnergyFormatter() {
    }

    public static String format(long internalEnergy) {
        return formatInternal(BigDecimal.valueOf(internalEnergy));
    }

    /** Formats fractional internal CE values used by the original PAN cost graph. */
    public static String format(double internalEnergy) {
        if (!Double.isFinite(internalEnergy)) {
            return internalEnergy > 0.0D ? "∞CE" : "0uCE";
        }
        return formatInternal(BigDecimal.valueOf(internalEnergy));
    }

    public static String formatRatio(long internalEnergyNumerator, long denominator) {
        if (denominator <= 0) {
            throw new IllegalArgumentException("Clay Energy ratio denominator must be positive");
        }
        return formatInternal(BigDecimal.valueOf(internalEnergyNumerator)
                .divide(BigDecimal.valueOf(denominator), 9, RoundingMode.DOWN));
    }

    private static String formatInternal(BigDecimal internalEnergy) {
        BigDecimal value = internalEnergy.multiply(BigDecimal.TEN);
        int prefix = 0;
        BigDecimal thousand = BigDecimal.valueOf(1_000);
        while (value.abs().compareTo(thousand) >= 0 && prefix < PREFIXES.length - 1) {
            value = value.divide(thousand, 6, RoundingMode.DOWN);
            prefix++;
        }
        String numeral = value.setScale(3, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
        return numeral + PREFIXES[prefix] + "CE";
    }

    public static String formatPerTick(long internalEnergy) {
        return format(internalEnergy) + "/t";
    }
}
