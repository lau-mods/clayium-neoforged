/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.MetalChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class MetalChestScreen extends AbstractContainerScreen<MetalChestMenu> {
    private static final ResourceLocation PLAYER = Clayium.id("textures/gui/gui_playerinventory.png");

    public MetalChestScreen(MetalChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = menu.imageWidth();
        imageHeight = menu.machineHeight() + 94;
        inventoryLabelY = menu.machineHeight();
    }

    @Override protected void init() {
        super.init();
        if (menu.pages() > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(0))
                    .bounds(leftPos + imageWidth / 2 - 32, topPos + menu.rows() * 18 + 20, 20, 16).build());
            addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
                    .bounds(leftPos + imageWidth / 2 + 12, topPos + menu.rows() * 18 + 20, 20, 16).build());
        }
    }

    private void changePage(int button) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xffc6c6c6);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + menu.machineHeight() - 4, 0xff8b8b8b);
        for (int row = 0; row < menu.rows(); row++) for (int column = 0; column < menu.columns(); column++) {
            int x = leftPos + (imageWidth - menu.columns() * 18) / 2 + column * 18;
            int y = topPos + 18 + row * 18;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xff373737);
            graphics.fill(x, y, x + 16, y + 16, 0xff8b8b8b);
        }
        int playerX = leftPos + (imageWidth - 176) / 2;
        graphics.blit(PLAYER, playerX, topPos + menu.machineHeight(), 0, 0, 176, 94);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        if (menu.pages() > 1) {
            Component page = Component.translatable("gui.clayium_neoforged.metal_chest.page", menu.page() + 1, menu.pages());
            graphics.drawCenteredString(font, page, imageWidth / 2, menu.rows() * 18 + 24, 0x404040);
        }
        graphics.drawString(font, playerInventoryTitle, (imageWidth - 160) / 2, menu.machineHeight(), 0x404040, false);
    }
}
