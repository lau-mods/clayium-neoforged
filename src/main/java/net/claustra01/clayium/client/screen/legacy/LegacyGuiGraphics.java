package net.claustra01.clayium.client.screen.legacy;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Draws the nine original GuiTemp tiles without resampling their pixels. */
public final class LegacyGuiGraphics {
    public static final int TEXT_COLOR = 0x404040;

    private static final ResourceLocation BACK = texture("gui_back.png");
    private static final ResourceLocation TOP = texture("gui_t.png");
    private static final ResourceLocation BOTTOM = texture("gui_b.png");
    private static final ResourceLocation LEFT = texture("gui_l.png");
    private static final ResourceLocation RIGHT = texture("gui_r.png");
    private static final ResourceLocation TOP_LEFT = texture("gui_tl.png");
    private static final ResourceLocation TOP_RIGHT = texture("gui_tr.png");
    private static final ResourceLocation BOTTOM_LEFT = texture("gui_bl.png");
    private static final ResourceLocation BOTTOM_RIGHT = texture("gui_br.png");
    public static final ResourceLocation PLAYER_INVENTORY = texture("gui_playerinventory.png");
    public static final ResourceLocation SLOT = texture("slot.png");
    public static final ResourceLocation PROGRESS = texture("progressbarfurnace.png");

    public static void background(GuiGraphics graphics, int x, int y, int width, int height) {
        int innerWidth = Math.max(0, width - 8);
        int innerHeight = Math.max(0, height - 8);
        for (int row = 0; row < innerHeight; row += 8) {
            for (int column = 0; column < innerWidth; column += 8) {
                int tileWidth = Math.min(8, innerWidth - column);
                int tileHeight = Math.min(8, innerHeight - row);
                graphics.blit(BACK, x + 4 + column, y + 4 + row,
                        tileWidth, tileHeight, 0, 0, tileWidth, tileHeight, 8, 8);
            }
        }
        for (int column = 0; column < innerWidth; column++) {
            graphics.blit(TOP, x + 4 + column, y, 1, 4, 0, 0, 1, 4, 1, 4);
            graphics.blit(BOTTOM, x + 4 + column, y + height - 4,
                    1, 4, 0, 0, 1, 4, 1, 4);
        }
        for (int row = 0; row < innerHeight; row++) {
            graphics.blit(LEFT, x, y + 4 + row, 4, 1, 0, 0, 4, 1, 4, 1);
            graphics.blit(RIGHT, x + width - 4, y + 4 + row,
                    4, 1, 0, 0, 4, 1, 4, 1);
        }
        graphics.blit(TOP_LEFT, x, y, 0, 0, 4, 4, 4, 4);
        graphics.blit(TOP_RIGHT, x + width - 4, y, 0, 0, 4, 4, 4, 4);
        graphics.blit(BOTTOM_LEFT, x, y + height - 4, 0, 0, 4, 4, 4, 4);
        graphics.blit(BOTTOM_RIGHT, x + width - 4, y + height - 4, 0, 0, 4, 4, 4, 4);
    }

    public static void slot(GuiGraphics graphics, int x, int y) {
        graphics.blit(SLOT, x - 1, y - 1, 0, 0, 18, 18, 256, 256);
    }

    public static void progress(GuiGraphics graphics, int x, int y, long value, long maximum) {
        graphics.blit(PROGRESS, x, y, 0, 0, 24, 17, 256, 256);
        if (value > 0L && maximum > 0L) {
            int width = Math.max(1, (int)Math.min(24L, (long)(24.0 * value / maximum)));
            graphics.blit(PROGRESS, x, y, 0, 17, width, 17, 256, 256);
        }
    }

    public static void slider(GuiGraphics graphics, int x, int y, int thumbY) {
        for (int row = 0; row < 96; row++) {
            graphics.blit(LEFT, x + 1, y + row, 4, 1, 0, 0, 4, 1, 4, 1);
        }
        graphics.blit(SLOT, x, thumbY, 0, 0, 6, 6, 256, 256);
    }

    private static ResourceLocation texture(String name) {
        return ClayiumMod.id("textures/gui/" + name);
    }

    private LegacyGuiGraphics() {
    }
}
