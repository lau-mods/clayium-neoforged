package net.claustra01.clayium.util;

/**
 * Clayium machine and material tier definitions (0..13), matching original CBlocks.tierPrefix and UtilTier rules.
 */
public enum ClayTier {
    NONE(0, "", "none"),
    CLAY(1, "Clay", "clay"),
    DENSE_CLAY(2, "DenseClay", "dense_clay"),
    SIMPLE(3, "Simple", "simple"),
    BASIC(4, "Basic", "basic"),
    ADVANCED(5, "Advanced", "advanced"),
    PRECISION(6, "Precision", "precision"),
    CLAY_STEEL(7, "ClaySteel", "clay_steel"),
    CLAYIUM(8, "Clayium", "clayium"),
    ULTIMATE(9, "Ultimate", "ultimate"),
    ANTIMATTER(10, "Antimatter", "antimatter"),
    PURE_ANTIMATTER(11, "PureAntimatter", "pure_antimatter"),
    OEC(12, "OEC", "oec"),
    OPA(13, "OPA", "opa");

    private static final ClayTier[] BY_INDEX = values();

    private final int index;
    private final String prefix;
    private final String idPrefix;

    ClayTier(int index, String prefix, String idPrefix) {
        this.index = index;
        this.prefix = prefix;
        this.idPrefix = idPrefix;
    }

    public int index() {
        return this.index;
    }

    public String prefix() {
        return this.prefix;
    }

    public String idPrefix() {
        return this.idPrefix;
    }

    public static ClayTier of(int tier) {
        if (tier < 0) {
            return NONE;
        }
        if (tier >= BY_INDEX.length) {
            return OPA;
        }
        return BY_INDEX[tier];
    }
}
