package net.claustra01.clayium.block;

import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;

/**
 * 16 dye-colored silicone blocks (tier 5), reproducing original Clayium color softening formula.
 */
public class SiliconeColoredBlock extends TieredBlock {
    private final DyeColor dyeColor;
    private final int tintRgb;

    public SiliconeColoredBlock(Properties properties, DyeColor dyeColor) {
        super(properties, 5, "siliconecolored");
        this.dyeColor = dyeColor;
        this.tintRgb = computeTintRgb(dyeColor);
    }

    public static int computeTintRgb(DyeColor dyeColor) {
        int mapRgb = dyeColor.getMapColor().col;
        int r = (((mapRgb >> 16) & 0xFF) * 3 + 256) / 4;
        int g = (((mapRgb >> 8) & 0xFF) * 3 + 256) / 4;
        int b = ((mapRgb & 0xFF) * 3 + 256) / 4;
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public DyeColor getDyeColor() {
        return this.dyeColor;
    }

    public int getTintRgb() {
        return this.tintRgb;
    }

    public int getOpaqueTintArgb() {
        return FastColor.ARGB32.opaque(this.tintRgb);
    }
}
