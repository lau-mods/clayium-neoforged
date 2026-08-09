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
    private static final ResourceLocation BACK = Clayium.id("textures/gui/gui_back.png");
    private static final ResourceLocation TOP = Clayium.id("textures/gui/gui_t.png");
    private static final ResourceLocation BOTTOM = Clayium.id("textures/gui/gui_b.png");
    private static final ResourceLocation LEFT = Clayium.id("textures/gui/gui_l.png");
    private static final ResourceLocation RIGHT = Clayium.id("textures/gui/gui_r.png");
    private static final ResourceLocation TOP_LEFT = Clayium.id("textures/gui/gui_tl.png");
    private static final ResourceLocation TOP_RIGHT = Clayium.id("textures/gui/gui_tr.png");
    private static final ResourceLocation BOTTOM_LEFT = Clayium.id("textures/gui/gui_bl.png");
    private static final ResourceLocation BOTTOM_RIGHT = Clayium.id("textures/gui/gui_br.png");
    private static final ResourceLocation SLOT = Clayium.id("textures/gui/slot.png");
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
                    .bounds(leftPos + playerOffsetX() + 170, topPos + menu.machineHeight() + 12, 16, 16).build());
            addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
                    .bounds(leftPos + playerOffsetX() + 188, topPos + menu.machineHeight() + 12, 16, 16).build());
        }
    }

    private void changePage(int button) {
        if (minecraft != null && minecraft.gameMode != null) {
            menu.changePageLocally(button);
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawOriginalFrame(graphics, leftPos, topPos, imageWidth, imageHeight);
        int playerX = leftPos + playerOffsetX();
        graphics.blit(PLAYER, playerX, topPos + menu.machineHeight(), 0, 0, 176, 94);
        for (int row = 0; row < menu.rows(); row++) for (int column = 0; column < menu.columns(); column++) {
            int x = leftPos + (imageWidth - menu.columns() * 18) / 2 + 1 + column * 18;
            int y = topPos + (menu.rows() > 6 ? 6 : 18) + row * 18;
            graphics.blit(SLOT, x - 1, y - 1, 0, 0, 18, 18, 256, 256);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.rows() <= 6) graphics.drawString(font, title, 6, 6, 0x404040, false);
        if (menu.pages() > 1) {
            Component page = Component.translatable("gui.clayium_neoforged.metal_chest.page", menu.page() + 1, menu.pages());
            graphics.drawString(font, page, playerOffsetX() + 170, menu.machineHeight() + 32, 0x404040, false);
        }
        if (menu.rows() <= 6)
            graphics.drawString(font, playerInventoryTitle, playerOffsetX() + 8, menu.machineHeight(), 0x404040, false);
    }

    private int playerOffsetX() { return (imageWidth - 176) / 2; }

    private static void drawOriginalFrame(GuiGraphics graphics, int x, int y, int width, int height) {
        tile(graphics, BACK, x + 4, y + 4, width - 8, height - 8, 8, 8);
        tile(graphics, TOP, x + 4, y, width - 8, 4, 1, 4);
        tile(graphics, BOTTOM, x + 4, y + height - 4, width - 8, 4, 1, 4);
        tile(graphics, LEFT, x, y + 4, 4, height - 8, 4, 1);
        tile(graphics, RIGHT, x + width - 4, y + 4, 4, height - 8, 4, 1);
        graphics.blit(TOP_LEFT, x, y, 0, 0, 4, 4, 4, 4);
        graphics.blit(TOP_RIGHT, x + width - 4, y, 0, 0, 4, 4, 4, 4);
        graphics.blit(BOTTOM_LEFT, x, y + height - 4, 0, 0, 4, 4, 4, 4);
        graphics.blit(BOTTOM_RIGHT, x + width - 4, y + height - 4, 0, 0, 4, 4, 4, 4);
    }

    private static void tile(GuiGraphics graphics, ResourceLocation texture, int x, int y,
                             int width, int height, int tileWidth, int tileHeight) {
        for (int dy = 0; dy < height; dy += tileHeight) {
            for (int dx = 0; dx < width; dx += tileWidth) {
                int drawWidth = Math.min(tileWidth, width - dx);
                int drawHeight = Math.min(tileHeight, height - dy);
                graphics.blit(texture, x + dx, y + dy, 0, 0,
                        drawWidth, drawHeight, tileWidth, tileHeight);
            }
        }
    }
}
