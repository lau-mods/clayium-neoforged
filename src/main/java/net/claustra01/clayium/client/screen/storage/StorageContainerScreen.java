package net.claustra01.clayium.client.screen.storage;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.storage.StorageContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class StorageContainerScreen extends AbstractContainerScreen<StorageContainerMenu> {

    public StorageContainerScreen(StorageContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.SLOT, leftPos + 43, topPos + 34, 0, 0, 18, 18);
        graphics.blit(LegacyGuiGraphics.SLOT, leftPos + 115, topPos + 34, 0, 0, 18, 18);
        graphics.blit(LegacyGuiGraphics.SLOT, leftPos + 141, topPos + 17, 0, 0, 18, 18);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + 72, 0, 0, 176, 94);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        Component amount = Component.literal(menu.amount() + " / " + menu.capacity());
        graphics.drawString(font, amount, (imageWidth - font.width(amount)) / 2, 58, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
