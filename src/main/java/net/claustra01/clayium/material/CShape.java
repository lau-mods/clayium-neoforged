package net.claustra01.clayium.material;

/**
 * All 25 material shapes defined in 1.7.10 CMaterials.
 */
public enum CShape {
    PLATE("Plate", "plate", "plate", "plates", 0),
    STICK("Stick", "stick", "stick", "rods", 1),
    SHORT_STICK("ShortStick", "short_stick", "shortstick", "short_sticks", 2),
    RING("Ring", "ring", "ring", "rings", 3),
    SMALL_RING("SmallRing", "small_ring", "smallring", "small_rings", 4),
    GEAR("Gear", "gear", "gear", "gears", 5),
    BLADE("Blade", "blade", "blade", "blades", 6),
    NEEDLE("Needle", "needle", "needle", "needles", 7),
    DISC("Disc", "disc", "disc", "discs", 16),
    SMALL_DISC("SmallDisc", "small_disc", "smalldisc", "small_discs", 17),
    CYLINDER("Cylinder", "cylinder", "cylinder", "cylinders", 18),
    PIPE("Pipe", "pipe", "pipe", "pipes", 19),
    LARGE_BALL("LargeBall", "large_ball", "largeball", "large_balls", 32),
    LARGE_PLATE("LargePlate", "large_plate", "largeplate", "large_plates", 33),
    GRINDING_HEAD("GrindingHead", "grinding_head", "grindinghead", "grinding_heads", 64),
    BEARING("Bearing", "bearing", "bearing", "bearings", 65),
    SPINDLE("Spindle", "spindle", "spindle", "spindles", 66),
    CUTTING_HEAD("CuttingHead", "cutting_head", "cuttinghead", "cutting_heads", 67),
    WATER_WHEEL("WaterWheel", "wheel", "waterwheel", "wheels", 68),
    BLOCK("Block", "block", "block", "storage_blocks", 1024),
    BALL("Ball", "ball", "ball", "balls", 8),
    DUST("Dust", "dust", "dust", "dusts", 128),
    INGOT("Ingot", "ingot", "ingot", "ingots", 129),
    GEM("Gem", "gem", "gem", "gems", 130),
    CRYSTAL("Crystal", "crystal", "crystal", "crystals", 131);

    private final String shapeName;
    private final String idSuffix;
    private final String iconName;
    private final String tagFolder;
    private final int legacyMeta;

    CShape(String shapeName, String idSuffix, String iconName, String tagFolder, int legacyMeta) {
        this.shapeName = shapeName;
        this.idSuffix = idSuffix;
        this.iconName = iconName;
        this.tagFolder = tagFolder;
        this.legacyMeta = legacyMeta;
    }

    public String shapeName() {
        return this.shapeName;
    }

    public String idSuffix() {
        return this.idSuffix;
    }

    public String iconName() {
        return this.iconName;
    }

    public String tagFolder() {
        return this.tagFolder;
    }

    public int legacyMeta() {
        return this.legacyMeta;
    }
}
