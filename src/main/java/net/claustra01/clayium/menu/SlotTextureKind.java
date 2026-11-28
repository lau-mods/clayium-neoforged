package net.claustra01.clayium.menu;

/**
 * Slot background texture regions inside {@code textures/gui/slot.png} matching 1.7.10 {@code RectangleTexture}.
 */
public enum SlotTextureKind {
    SMALL(18, 18, 0, 0),
    LARGE(26, 26, 0, 32),
    IMPORT_1(18, 18, 32, 0),
    IMPORT_2(18, 18, 32, 32),
    EXPORT_1(18, 18, 64, 0),
    EXPORT_2(18, 18, 64, 32),
    ENERGY(18, 18, 96, 0),
    FILTER(18, 18, 96, 32),
    MULTITRACK_0(18, 18, 32, 96),
    MULTITRACK_1(18, 18, 64, 96),
    MULTITRACK_2(18, 18, 96, 96),
    MULTITRACK_3(18, 18, 128, 96),
    MULTITRACK_4(18, 18, 160, 96),
    MULTITRACK_5(18, 18, 192, 96),
    MULTITRACK_FILTER_0(18, 18, 32, 128),
    MULTITRACK_FILTER_1(18, 18, 64, 128),
    MULTITRACK_FILTER_2(18, 18, 96, 128),
    MULTITRACK_FILTER_3(18, 18, 128, 128),
    MULTITRACK_FILTER_4(18, 18, 160, 128),
    MULTITRACK_FILTER_5(18, 18, 192, 128);

    public final int sizeX;
    public final int sizeY;
    public final int textureX;
    public final int textureY;

    SlotTextureKind(int sizeX, int sizeY, int textureX, int textureY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.textureX = textureX;
        this.textureY = textureY;
    }

    public static SlotTextureKind multitrack(int track) {
        return switch (Math.max(0, Math.min(5, track))) {
            case 0 -> MULTITRACK_0;
            case 1 -> MULTITRACK_1;
            case 2 -> MULTITRACK_2;
            case 3 -> MULTITRACK_3;
            case 4 -> MULTITRACK_4;
            default -> MULTITRACK_5;
        };
    }

    public static SlotTextureKind multitrackFilter(int track) {
        return switch (Math.max(0, Math.min(5, track))) {
            case 0 -> MULTITRACK_FILTER_0;
            case 1 -> MULTITRACK_FILTER_1;
            case 2 -> MULTITRACK_FILTER_2;
            case 3 -> MULTITRACK_FILTER_3;
            case 4 -> MULTITRACK_FILTER_4;
            default -> MULTITRACK_FILTER_5;
        };
    }
}
