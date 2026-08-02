/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Formats ordinary quantities with the same three-decimal metric style as Clay Energy. */
public final class MetricFormatter {
    private static final String[] PREFIXES = {"", "k", "M", "G", "T", "P", "E"};
    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1_000);

    private MetricFormatter() {}

    public static String format(long quantity) {
        BigDecimal value = BigDecimal.valueOf(quantity);
        int prefix = 0;
        while (value.abs().compareTo(THOUSAND) >= 0 && prefix < PREFIXES.length - 1) {
            value = value.divide(THOUSAND, 6, RoundingMode.DOWN);
            prefix++;
        }
        return value.setScale(3, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
                + PREFIXES[prefix];
    }
}
