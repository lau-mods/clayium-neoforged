package net.claustra01.clayium.client.screen.logistics;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class LogisticsBackground {
    private static final ResourceLocation TEXTURE = ClayiumMod.id("textures/gui/back.png");

    public static void draw(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.blit(TEXTURE, x, y, 4, 4, 0, 0, 4, 4, 256, 256);
        graphics.blit(TEXTURE, x + 4, y, width - 8, 4, 4, 0, 168, 4, 256, 256);
        graphics.blit(TEXTURE, x + width - 4, y, 4, 4, 172, 0, 4, 4, 256, 256);
        graphics.blit(TEXTURE, x, y + 4, 4, height - 8, 0, 4, 4, 172, 256, 256);
        graphics.blit(TEXTURE, x + 4, y + 4, width - 8, height - 8, 4, 4, 168, 172, 256, 256);
        graphics.blit(TEXTURE, x + width - 4, y + 4, 4, height - 8, 172, 4, 4, 172, 256, 256);
        // The original image has no bottom edge, so draw its top rows in reverse order.
        for (int row = 0; row < 4; row++) {
            int bottomY = y + height - 4 + row;
            int sourceY = 3 - row;
            graphics.blit(TEXTURE, x, bottomY, 4, 1, 0, sourceY, 4, 1, 256, 256);
            graphics.blit(TEXTURE, x + 4, bottomY, width - 8, 1, 4, sourceY, 168, 1, 256, 256);
            graphics.blit(TEXTURE, x + width - 4, bottomY, 4, 1, 172, sourceY, 4, 1, 256, 256);
        }
    }

    private LogisticsBackground() {
    }
}
