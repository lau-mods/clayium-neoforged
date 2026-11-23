package net.claustra01.clayium.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;

public final class RecipeViewerRenderer {
    public static void draw(GuiGraphics graphics, RecipeViewerLayout layout) {
        RecipeViewerLayout.Background background = layout.background();
        graphics.blit(
                background.texture(),
                0,
                0,
                background.u(),
                background.v(),
                background.width(),
                background.height(),
                background.textureWidth(),
                background.textureHeight());

        RecipeViewerLayout.Progress progress = layout.progress();
        int tick = (int)(net.minecraft.Util.getMillis() / 50 % 40);
        if (layout.operation() == 0) {
            LegacyGuiGraphics.progress(graphics, progress.x(), progress.y(), tick, 40);
        } else {
            graphics.blit(background.texture(), progress.x(), progress.y(), 176, 0,
                    progress.width() * tick / 40, progress.height(), 256, 256);
            for (int i = 0; i < 6; i++) {
                boolean selected = layout.operation() == i + 1;
                graphics.blit(background.texture(), 35 + 16 * i, 41, 176 + 16 * (i % 5),
                        32 + 48 * (i / 5) + (selected ? 16 : 0), 16, 16, 256, 256);
            }
        }

        var font = Minecraft.getInstance().font;
        for (RecipeViewerLayout.TextLine line : layout.text()) {
            float scale = Math.min(1F, (float)(layout.width() - line.x() - 1) / Math.max(1, font.width(line.value())));
            graphics.pose().pushPose();
            graphics.pose().translate(line.centered() ? line.x() - font.width(line.value()) / 2 : line.x(), line.y(), 0);
            graphics.pose().scale(scale, scale, 1F);
            graphics.drawString(font, line.value(), 0, 0, line.color(), false);
            graphics.pose().popPose();
        }
    }

    public static void drawPage(GuiGraphics graphics, RecipeViewerPage page) {
        draw(graphics, page.layout());
        if (page.outcome() >= 0) {
            var recipe = page.holder().value();
            double percent = RecipeViewerLayout.randomOutputPercent(recipe, recipe.randomOutputs().get(page.outcome()));
            graphics.drawString(Minecraft.getInstance().font,
                    String.format(java.util.Locale.ROOT, "%.2f%%", percent), 69, 10, 0xFF000000, false);
        }
    }

    private RecipeViewerRenderer() {
    }
}
