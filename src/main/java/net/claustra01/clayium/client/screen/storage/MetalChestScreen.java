package net.claustra01.clayium.client.screen.storage;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.client.screen.logistics.LogisticsBackground;
import net.claustra01.clayium.storage.MetalChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class MetalChestScreen extends AbstractContainerScreen<MetalChestMenu> {
    private static final ResourceLocation SLOT = ClayiumMod.id("textures/gui/slot.png");
    private Button previous;
    private Button next;

    public MetalChestScreen(MetalChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = menu.imageWidth();
        imageHeight = menu.imageHeight();
        inventoryLabelX = (imageWidth - 162) / 2;
        inventoryLabelY = 18 + menu.type().rows() * 18 + 3;
    }

    @Override
    protected void init() {
        super.init();
        previous = addRenderableWidget(Button.builder(Component.literal("◀"), button -> changePage(menu.page() - 1))
                .bounds(leftPos + imageWidth - 66, topPos + 4, 18, 12).build());
        next = addRenderableWidget(Button.builder(Component.literal("▶"), button -> changePage(menu.page() + 1))
                .bounds(leftPos + imageWidth - 24, topPos + 4, 18, 12).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        previous.active = menu.page() > 0;
        next.active = menu.page() + 1 < menu.type().pages();
    }

    private void changePage(int page) {
        if (menu.clickMenuButton(minecraft.player, page)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, page);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LogisticsBackground.draw(graphics, leftPos, topPos, imageWidth, imageHeight);
        int storageX = leftPos + (imageWidth - menu.type().columns() * 18) / 2;
        for (int row = 0; row < menu.type().rows(); row++) {
            for (int column = 0; column < menu.type().columns(); column++) {
                graphics.blit(SLOT, storageX + column * 18 - 1, topPos + 17 + row * 18, 0, 0, 18, 18);
            }
        }
        int playerX = leftPos + (imageWidth - 162) / 2;
        int playerY = topPos + 18 + menu.type().rows() * 18 + 14;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 9; column++) {
                int slotY = playerY + (row == 3 ? 58 : row * 18);
                graphics.blit(SLOT, playerX + column * 18 - 1, slotY - 1, 0, 0, 18, 18);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        int limit = imageWidth - 72;
        if (font.width(name) > limit) name = font.plainSubstrByWidth(name, limit - font.width("…")) + "…";
        graphics.drawString(font, name, 4, 5, 0x404040, false);
        String page = (menu.page() + 1) + "/" + menu.type().pages();
        graphics.drawString(font, page, imageWidth - 36 - font.width(page) / 2, 5, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
