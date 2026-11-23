package net.claustra01.clayium.client.screen.storage;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.storage.VoidContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class VoidContainerScreen extends AbstractContainerScreen<VoidContainerMenu> {

    public VoidContainerScreen(VoidContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 168;
        inventoryLabelX = 8;
        inventoryLabelY = 75;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.SLOT, leftPos + 79, topPos + 34, 0, 0, 18, 18);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + 76, 0, 0, 176, 86);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        Component notice = Component.translatable("gui.clayium.storage.void_hint");
        graphics.drawString(font, notice, (imageWidth - font.width(notice)) / 2, 58, 0x704040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
