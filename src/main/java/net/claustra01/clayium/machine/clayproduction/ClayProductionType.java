package net.claustra01.clayium.machine.clayproduction;

public enum ClayProductionType {
    ENERGETIC_CLAY_CONDENSER_MK_I("energetic_clay_condenser_mk_i", 3, Kind.ENERGETIC_CONDENSER, "eccondenser", 0),
    ENERGETIC_CLAY_CONDENSER_MK_II("energetic_clay_condenser_mk_ii", 4, Kind.ENERGETIC_CONDENSER, "eccondenser", 0),
    AUTOMATIC_CLAY_CONDENSER_MK_I("automatic_clay_condenser_mk_i", 5, Kind.AUTOMATIC_CONDENSER, "autoclaycondenser", 0),
    AUTOMATIC_CLAY_CONDENSER_MK_II("automatic_clay_condenser_mk_ii", 7, Kind.AUTOMATIC_CONDENSER, "autoclaycondenser", 0),
    SOLAR_CLAY_FABRICATOR_MK_I("solar_clay_fabricator_mk_i", 5, Kind.SOLAR_FABRICATOR, "solar", 4),
    SOLAR_CLAY_FABRICATOR_MK_II("solar_clay_fabricator_mk_ii", 6, Kind.SOLAR_FABRICATOR, "solar", 6),
    LITHIUM_SOLAR_CLAY_FABRICATOR("lithium_solar_clay_fabricator", 7, Kind.SOLAR_FABRICATOR, "solar", 9),
    CLAY_FABRICATOR_MK_I("clay_fabricator_mk_i", 8, Kind.CLAY_FABRICATOR, "clayfabricator", 11),
    CLAY_FABRICATOR_MK_II("clay_fabricator_mk_ii", 9, Kind.CLAY_FABRICATOR, "clayfabricator", 13),
    CLAY_FABRICATOR_MK_III("clay_fabricator_mk_iii", 13, Kind.CLAY_FABRICATOR, "clayfabricator", 13);

    private final String id;
    private final int tier;
    private final Kind kind;
    private final String texture;
    private final int maximumProductionLevel;

    ClayProductionType(String id, int tier, Kind kind, String texture, int maximumProductionLevel) {
        this.id = id;
        this.tier = tier;
        this.kind = kind;
        this.texture = texture;
        this.maximumProductionLevel = maximumProductionLevel;
    }

    public String id() {
        return id;
    }

    public int tier() {
        return tier;
    }

    public Kind kind() {
        return kind;
    }

    public String texture() {
        return texture;
    }

    public int maximumProductionLevel() {
        return maximumProductionLevel;
    }

    public enum Kind {
        ENERGETIC_CONDENSER,
        AUTOMATIC_CONDENSER,
        SOLAR_FABRICATOR,
        CLAY_FABRICATOR
    }
}
