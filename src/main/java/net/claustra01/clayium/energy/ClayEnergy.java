package net.claustra01.clayium.energy;

import net.minecraft.world.item.ItemStack;

/**
 * Constants and safe long-arithmetic helpers for Clay Energy (CE).
 * 1 internal unit = 10 uCE; 100,000 internal units = 1 CE.
 */
public final class ClayEnergy {
    public static final long UNIT_TO_UCE = 10L;
    public static final long UNITS_PER_CE = 100_000L;
    public static final long MANUAL_BUTTON_PRESS_CE = 5L;

    private ClayEnergy() {}

    public static long fromCe(double ce) {
        if (ce <= 0.0 || Double.isNaN(ce)) {
            return 0L;
        }
        double scaled = ce * UNITS_PER_CE;
        if (scaled >= (double) Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.round(scaled);
    }

    public static double toCe(long internalUnits) {
        return (double) internalUnits / (double) UNITS_PER_CE;
    }

    public static long safeAdd(long a, long b) {
        if (b <= 0L) {
            return Math.max(0L, a + Math.max(0L, b));
        }
        if (a > Long.MAX_VALUE - b) {
            return Long.MAX_VALUE;
        }
        return a + b;
    }

    public static long getItemEnergy(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0L;
        }
        if (stack.getItem() instanceof IClayEnergy ceItem) {
            return Math.max(0L, ceItem.getClayEnergy(stack));
        }
        return 0L;
    }

    public static boolean hasItemEnergy(ItemStack stack) {
        return getItemEnergy(stack) > 0L;
    }
}
