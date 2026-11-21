package net.claustra01.clayium.client.screen.pan;

import java.math.BigDecimal;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.pan.PanBlock;
import net.claustra01.clayium.pan.PanMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class PanScreen extends AbstractContainerScreen<PanMenu> {
    private static final ResourceLocation BACKGROUND = ClayiumMod.id("textures/gui/back.png");
    private static final ResourceLocation PLAYER_INVENTORY = ClayiumMod.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = ClayiumMod.id("textures/gui/slot.png");
    private static final ResourceLocation PROGRESS = ClayiumMod.id("textures/gui/progressbarfurnace.png");

    public PanScreen(PanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 185;
        inventoryLabelY = 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, 180);
        graphics.blit(BACKGROUND, leftPos, topPos + 180, 0, 175, imageWidth, 4);
        graphics.blit(BACKGROUND, leftPos, topPos + 184, 0, 180, imageWidth, 1);
        graphics.blit(PLAYER_INVENTORY, leftPos, topPos + 93, 0, 0, 176, 90);
        if (menu.kind() == PanBlock.Kind.DUPLICATOR) {
            for (int[] slot : new int[][] {{34, 34}, {34, 56}, {124, 34}, {34, 78}}) {
                graphics.blit(SLOT, leftPos + slot[0], topPos + slot[1], 0, 0, 18, 18);
            }
            int width = menu.requiredProgress() == 0L ? 0
                    : (int)Math.min(24L, 24L * menu.progress() / menu.requiredProgress());
            graphics.blit(PROGRESS, leftPos + 84, topPos + 36, 0, 0, 24, 16);
            if (width > 0) {
                graphics.blit(PROGRESS, leftPos + 84, topPos + 36, 0, 16, width, 16);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 7, 6, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.pan.tier", menu.tier()), 7, 18,
                0x404040, false);
        drawFitted(graphics, Component.translatable("gui.clayium.pan.network",
                menu.nodes(), menu.adapters()), 70, 27, 99, 0x405060);
        if (menu.kind() == PanBlock.Kind.DUPLICATOR) {
            drawFitted(graphics, Component.translatable(menu.sampleReachable()
                    ? "gui.clayium.pan.reachable" : "gui.clayium.pan.unreachable"), 70, 56, 99, 0x404040);
        }
        drawFitted(graphics, Component.translatable("gui.clayium.pan.candidates", menu.candidates()),
                70, 66, 99, 0x404040);
        drawFitted(graphics, Component.translatable("gui.clayium.pan.energy", format(menu.requiredEnergy())),
                70, 76, 99, 0x404040);
        drawFitted(graphics, Component.translatable("gui.clayium.pan.progress",
                menu.progress(), menu.requiredProgress()), 70, 86, 99, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private static String format(long internal) {
        return BigDecimal.valueOf(internal, 5).stripTrailingZeros().toPlainString() + " CE";
    }

    private void drawFitted(GuiGraphics graphics, Component label, int x, int y, int width, int color) {
        float scale = Math.min(1.0F, (float)width / font.width(label));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, label, 0, 0, color, false);
        graphics.pose().popPose();
    }
}
