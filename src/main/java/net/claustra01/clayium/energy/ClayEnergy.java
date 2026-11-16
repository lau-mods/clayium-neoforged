package net.claustra01.clayium.energy;

import java.math.BigDecimal;

public record ClayEnergy(long internalValue) implements Comparable<ClayEnergy> {
    public static final long INTERNAL_UNITS_PER_CE = 100_000L;
    public static final ClayEnergy ZERO = new ClayEnergy(0L);
    public static final ClayEnergy MAX = new ClayEnergy(Long.MAX_VALUE);

    public static ClayEnergy ofInternal(long internalValue) {
        return internalValue == 0L ? ZERO : new ClayEnergy(internalValue);
    }

    public static ClayEnergy ofCe(long clayEnergy) {
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
        return BigDecimal.valueOf(internalValue, 5).stripTrailingZeros().toPlainString() + " CE";
    }

    @Override
    public int compareTo(ClayEnergy other) {
        return Long.compare(internalValue, other.internalValue);
    }
}
