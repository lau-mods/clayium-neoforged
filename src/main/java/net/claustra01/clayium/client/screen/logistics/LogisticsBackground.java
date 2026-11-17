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
        graphics.blit(TEXTURE, x, y + height - 4, 4, 4, 0, 176, 4, 4, 256, 256);
        graphics.blit(TEXTURE, x + 4, y + height - 4, width - 8, 4, 4, 176, 168, 4, 256, 256);
        graphics.blit(TEXTURE, x + width - 4, y + height - 4, 4, 4, 172, 176, 4, 4, 256, 256);
    }

    private LogisticsBackground() {
    }
}
