package net.claustra01.clayium.world.area;

public enum AreaMarkerType {
    NORMAL("clay_marker", 0.2F, 0.8F, 1.0F),
    OPEN_PIT("clay_marker_open_pit", 1.0F, 0.55F, 0.1F),
    GROUND_LEVELING("clay_marker_ground_leveling", 0.3F, 1.0F, 0.3F),
    PRISM("clay_marker_prism", 0.9F, 0.2F, 1.0F);

    private final String id;
    private final float red;
    private final float green;
    private final float blue;

    AreaMarkerType(String id, float red, float green, float blue) {
        this.id = id;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public String id() {
        return id;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }
}
