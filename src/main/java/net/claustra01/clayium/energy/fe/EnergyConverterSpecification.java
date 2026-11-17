package net.claustra01.clayium.energy.fe;

import java.util.List;
import net.claustra01.clayium.energy.ClayEnergy;

public record EnergyConverterSpecification(
        int tier,
        String id,
        long clayEnergyPerTick,
        int generatedFePerTick,
        int outputLimitPerTick,
        int capacity) {
    public static final double OVERCLOCK_ENERGY_EXPONENT = 1.0D;
    public static final List<EnergyConverterSpecification> VALUES = List.of(
            converter(4, "ce_rf_converter", 100L, 10, 10, 10_000),
            converter(5, "advanced_ce_rf_converter", 1_000L, 30, 30, 30_000),
            converter(6, "precision_ce_rf_converter", 10_000L, 90, 90, 90_000),
            converter(7, "clay_steel_ce_rf_converter", 100_000L, 270, 270, 270_000),
            converter(8, "clayium_ce_rf_converter", 1_000_000L, 810, 810, 810_000),
            converter(9, "ultimate_ce_rf_converter", 10_000_000L, 2_430, 2_430, 2_430_000),
            converter(10, "antimatter_ce_rf_converter", 100_000_000L, 7_290, 7_290, 7_290_000),
            converter(11, "pure_antimatter_ce_rf_converter", 1_000_000_000L, 21_870, 21_870, 21_870_000),
            converter(12, "oec_ce_rf_converter", 10_000_000_000L, 65_610, 65_610, 65_610_000),
            converter(13, "opa_ce_rf_converter", 100_000_000_000L, 196_830, 196_830, 196_830_000));

    public ClayEnergy clayEnergyCost(double overclockFactor) {
        if (overclockFactor <= 1.0D) {
            return ClayEnergy.ofInternal(clayEnergyPerTick);
        }
        double scaled = clayEnergyPerTick * Math.pow(overclockFactor, OVERCLOCK_ENERGY_EXPONENT);
        return ClayEnergy.ofInternal(saturatedCeil(scaled));
    }

    public int generatedFe(double overclockFactor) {
        return scaledFe(generatedFePerTick, overclockFactor);
    }

    public int outputLimit(double overclockFactor) {
        return scaledFe(outputLimitPerTick, overclockFactor);
    }

    private static EnergyConverterSpecification converter(
            int tier, String id, long clayEnergyPerTick, int generatedFePerTick, int outputLimitPerTick, int capacity) {
        return new EnergyConverterSpecification(
                tier, id, clayEnergyPerTick, generatedFePerTick, outputLimitPerTick, capacity);
    }

    private static int scaledFe(int base, double factor) {
        if (factor <= 1.0D) {
            return base;
        }
        double scaled = base * factor;
        return scaled >= Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, (int)Math.floor(scaled));
    }

    private static long saturatedCeil(double value) {
        if (!Double.isFinite(value) || value >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.max(1L, (long)Math.ceil(value));
    }
}
