package net.claustra01.clayium.material;

import net.minecraft.util.FastColor;

/**
 * Immutable descriptor for a Clayium material (name, texture stem, tag stem, legacy meta, 3-layer RGB tint, hardness).
 */
public final class CMaterial {
    private final String idPrefix;
    private final String name;
    private final String iconName;
    private final String odName;
    private final int meta;
    private final int baseRgb;
    private final int darkRgb;
    private final int lightRgb;
    private final float hardness;

    public CMaterial(
            String idPrefix,
            String name,
            String iconName,
            String odName,
            int meta,
            int[] base,
            int[] dark,
            int[] light,
            float hardness) {
        this.idPrefix = idPrefix;
        this.name = name;
        this.iconName = iconName;
        this.odName = odName;
        this.meta = meta;
        this.baseRgb = packRgb(base[0], base[1], base[2]);
        this.darkRgb = packRgb(dark[0], dark[1], dark[2]);
        this.lightRgb = packRgb(light[0], light[1], light[2]);
        this.hardness = hardness;
    }

    public static int packRgb(int r, int g, int b) {
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public String idPrefix() {
        return this.idPrefix;
    }

    public String name() {
        return this.name;
    }

    public String iconName() {
        return this.iconName;
    }

    public String odName() {
        return this.odName;
    }

    public int meta() {
        return this.meta;
    }

    public int baseRgb() {
        return this.baseRgb;
    }

    public int darkRgb() {
        return this.darkRgb;
    }

    public int lightRgb() {
        return this.lightRgb;
    }

    public int colorRgbForLayer(int tintIndex) {
        return switch (tintIndex) {
            case 0 -> this.baseRgb;
            case 1 -> this.darkRgb;
            case 2 -> this.lightRgb;
            default -> 0xFFFFFF;
        };
    }

    public int opaqueArgbForLayer(int tintIndex) {
        return FastColor.ARGB32.opaque(colorRgbForLayer(tintIndex));
    }

    public float hardness() {
        return this.hardness;
    }
}
