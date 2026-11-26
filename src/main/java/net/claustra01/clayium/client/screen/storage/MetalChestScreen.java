package net.claustra01.clayium.client.screen.storage;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.storage.MetalChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class MetalChestScreen extends AbstractContainerScreen<MetalChestMenu> {
    private Button previous;
    private Button next;

    public MetalChestScreen(MetalChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = menu.imageWidth();
        imageHeight = menu.imageHeight();
        inventoryLabelX = (imageWidth - 176) / 2 + 8;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        if (menu.type().pages() == 1) return;
        previous = addRenderableWidget(Button.builder(Component.literal("◀"), button -> changePage(menu.page() - 1))
                .bounds(leftPos + inventoryLabelX + 162, topPos + inventoryLabelY + 12, 16, 16).build());
        next = addRenderableWidget(Button.builder(Component.literal("▶"), button -> changePage(menu.page() + 1))
                .bounds(leftPos + inventoryLabelX + 180, topPos + inventoryLabelY + 12, 16, 16).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (menu.type().pages() == 1) return;
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
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        int storageX = leftPos + (imageWidth - menu.type().columns() * 18) / 2 + 1;
        int storageY = menu.storageY();
        for (int row = 0; row < menu.type().rows(); row++) {
            for (int column = 0; column < menu.type().columns(); column++) {
                graphics.blit(LegacyGuiGraphics.SLOT, storageX + column * 18 - 1, topPos + storageY - 1 + row * 18, 0, 0, 18, 18);
            }
        }
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos + inventoryLabelX - 8,
                topPos + inventoryLabelY, 0, 0, 176, 94, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.type().rows() <= 6) {
            graphics.drawString(font, title, 6, 6, 0x404040, false);
            graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        }
        if (menu.type().pages() > 1) {
            String page = (menu.page() + 1) + "/" + menu.type().pages();
            graphics.drawString(font, page, inventoryLabelX + 162, inventoryLabelY + 32, 0x404040, false);
        }
    }
}
