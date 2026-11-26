package net.claustra01.clayium.material;

import java.util.Map;

public final class MaterialAppearance {
    private static final Map<String, String> SPECIAL_TEXTURES = Map.ofEntries(
            Map.entry("energized_clay_dust", "engclaydust"),
            Map.entry("excited_clay_dust", "excclaydust"),
            Map.entry("salt", "saltdust"),
            Map.entry("calcareous_clay_dust", "calclaydust"),
            Map.entry("calcium_chloride_dust", "calciumchloridedust"),
            Map.entry("sodium_carbonate_dust", "sodiumcarbonatedust"),
            Map.entry("quartz_dust", "quartzdust"));

    public static String dedicatedTexture(MaterialVariant variant) {
        String material = variant.material().id();
        String shape = variant.shape().name().toLowerCase();
        if (material.equals("clay")) {
            return clayTexture(shape);
        }
        if (material.equals("dense_clay")) {
            return denseClayTexture(shape);
        }
        if (material.equals("ind_clay")) {
            return compactTexture("indclay", shape);
        }
        if (material.equals("advind_clay")) {
            return compactTexture("advindclay", shape);
        }
        if (material.equals("impure_silicon")) {
            return shape.equals("dust") ? null : compactTexture("impuresilicon", shape);
        }
        return SPECIAL_TEXTURES.get(variant.id());
    }

    public static String genericTexture(MaterialVariant variant) {
        return switch (variant.shape()) {
            case DUST -> "dust";
            case INGOT -> "ingot";
            case PLATE -> "plate";
            case LARGE_PLATE -> "largeplate";
            case GEM -> gemTexture(variant.material().id());
            default -> throw new IllegalArgumentException("No generic texture for " + variant.id());
        };
    }

    public static boolean usesTint(MaterialVariant variant) {
        return dedicatedTexture(variant) == null && variant.shape() != MaterialShape.BLOCK;
    }

    private static String clayTexture(String shape) {
        return switch (shape) {
            case "ball" -> "minecraft:item/clay_ball";
            case "short_stick" -> "shortclaystick";
            case "small_disc" -> "smallclaydisc";
            case "small_ring" -> "smallclayring";
            case "large_ball" -> "largeclayball";
            case "large_plate" -> "largeclayplate";
            case "water_wheel" -> "claywaterwheel";
            default -> "clay" + shape.replace("_", "");
        };
    }

    private static String denseClayTexture(String shape) {
        return switch (shape) {
            case "ball" -> null;
            case "short_stick" -> "shortdenseclaystick";
            case "small_disc" -> "smalldenseclaydisc";
            case "small_ring" -> "smalldenseclayring";
            case "large_plate" -> "largedenseclayplate";
            case "water_wheel" -> "denseclaywaterwheel";
            default -> "denseclay" + shape.replace("_", "");
        };
    }

    private static String compactTexture(String prefix, String shape) {
        return prefix + shape.replace("large_", "large").replace("_", "");
    }

    private static String gemTexture(String material) {
        if (material.startsWith("compressed_pure_antimatter_")) {
            int compression = Integer.parseInt(material.substring(material.lastIndexOf('_') + 1));
            return "matter" + Math.min(compression + 1, 5);
        }
        return material.equals("octuple_pure_antimatter") ? "matter5" : "matter";
    }

    private MaterialAppearance() {
    }
}
