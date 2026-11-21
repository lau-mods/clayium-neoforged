package net.claustra01.clayium.client.screen.storage;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.client.screen.logistics.LogisticsBackground;
import net.claustra01.clayium.storage.StorageContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class StorageContainerScreen extends AbstractContainerScreen<StorageContainerMenu> {
    private static final ResourceLocation SLOT = ClayiumMod.id("textures/gui/slot.png");
    private static final ResourceLocation PLAYER_INVENTORY = ClayiumMod.id("textures/gui/gui_playerinventory.png");

    public StorageContainerScreen(StorageContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 168;
        inventoryLabelX = 8;
        inventoryLabelY = 75;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LogisticsBackground.draw(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(SLOT, leftPos + 79, topPos + 34, 0, 0, 18, 18);
        graphics.blit(PLAYER_INVENTORY, leftPos, topPos + 76, 0, 0, 176, 86);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        Component amount = Component.literal(String.format("%,d / %,d", menu.amount(), menu.capacity()));
        graphics.drawString(font, amount, (imageWidth - font.width(amount)) / 2, 58, 0x404040, false);
        if (menu.expanded()) {
            graphics.drawString(font, Component.translatable("gui.clayium.storage.expanded"), 8, 19, 0x405060, false);
        }
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
