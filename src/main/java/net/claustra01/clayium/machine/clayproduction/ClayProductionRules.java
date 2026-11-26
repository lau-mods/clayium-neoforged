package net.claustra01.clayium.machine.clayproduction;

import java.util.OptionalInt;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ClayProductionRules {
    public static OptionalInt productionLevel(ItemStack stack) {
        if (stack.is(Items.CLAY)) {
            return OptionalInt.of(0);
        }
        for (CompressionTier tier : CompressionTier.values()) {
            if (stack.is(MaterialRegistration.COMPRESSION_BLOCKS.get(tier.id()).get().asItem())) {
                return OptionalInt.of(tier.originalMetadata() + 1);
            }
        }
        return OptionalInt.empty();
    }

    public static Item itemForLevel(int level) {
        if (level == 0) {
            return Items.CLAY;
        }
        CompressionTier tier = CompressionTier.values()[level - 1];
        return MaterialRegistration.COMPRESSION_BLOCKS.get(tier.id()).get().asItem();
    }

    public static long clayEnergyValue(int level) {
        return level == 0 ? 0L : ClayEnergy.ofCe(CompressionTier.values()[level - 1].clayEnergy()).internalValue();
    }

    public static int solarInputLevel(ClayProductionType type, ItemStack stack) {
        OptionalInt level = productionLevel(stack);
        if (level.isPresent()) {
            return level.getAsInt();
        }
        if (type == ClayProductionType.LITHIUM_SOLAR_CLAY_FABRICATOR
                && stack.is(MaterialRegistration.MATERIAL_ITEMS.get("lithium_ingot").get())) {
            return 8;
        }
        return -1;
    }

    public static long solarTicks(ClayProductionType type, int inputLevel) {
        double base;
        double initialMultiplier;
        if (type == ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_I) {
            base = 4.0D;
            initialMultiplier = normalizedSolarMultiplier(4, base, 5_000.0D);
        } else if (type == ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_II) {
            base = 3.0D;
            initialMultiplier = normalizedSolarMultiplier(6, base, 50_000.0D);
        } else {
            base = 2.0D;
            initialMultiplier = normalizedSolarMultiplier(9, base, 3_000_000.0D);
        }
        return Math.max(1L, (long)(Math.pow(base, inputLevel) * initialMultiplier));
    }

    public static long fabricatorTicks(ClayProductionType type, int inputLevel, int count) {
        if (type == ClayProductionType.CLAY_FABRICATOR_MK_III) {
            return 1L;
        }
        double base = type == ClayProductionType.CLAY_FABRICATOR_MK_I ? 5.0D : 2.0D;
        double exponent = type == ClayProductionType.CLAY_FABRICATOR_MK_I ? 0.85D : 0.3D;
        int maximumLevel = type.maximumProductionLevel();
        double targetTicks = type == ClayProductionType.CLAY_FABRICATOR_MK_I ? 45_000_000.0D : 1_000_000_000.0D;
        double initialMultiplier = Math.pow(10.0D, maximumLevel) * 64.0D
                / (Math.pow(base, maximumLevel) * Math.pow(64.0D, exponent))
                / (targetTicks / 20.0D);
        return Math.max(1L, (long)(Math.pow(base, inputLevel) * Math.pow(count, exponent) * initialMultiplier));
    }

    private static double normalizedSolarMultiplier(int maximumLevel, double base, double targetTicks) {
        return Math.pow(10.0D, maximumLevel + 1) * (base - 1.0D)
                / (base * (Math.pow(base, maximumLevel) - 1.0D))
                / (targetTicks / 20.0D);
    }

    private ClayProductionRules() {
    }
}
