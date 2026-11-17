package net.claustra01.clayium.recipe;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ClayiumRecipeEnergy {
    public static long e(double value, int tier) {
        return BigDecimal.valueOf(value)
                .movePointRight(tier - 2)
                .setScale(0, RoundingMode.FLOOR)
                .longValueExact();
    }

    private ClayiumRecipeEnergy() {
    }
}
