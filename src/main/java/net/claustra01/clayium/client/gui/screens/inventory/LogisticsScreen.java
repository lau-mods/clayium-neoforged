/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.LogisticsMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original Clayium component-texture style applied to the six-row logistics inventory. */
public final class LogisticsScreen extends AbstractContainerScreen<LogisticsMenu> {
    private static final ResourceLocation BACK = Clayium.id("textures/gui/gui_back.png");
    private static final ResourceLocation TOP = Clayium.id("textures/gui/gui_t.png");
    private static final ResourceLocation BOTTOM = Clayium.id("textures/gui/gui_b.png");
    private static final ResourceLocation LEFT = Clayium.id("textures/gui/gui_l.png");
    private static final ResourceLocation RIGHT = Clayium.id("textures/gui/gui_r.png");
    private static final ResourceLocation TOP_LEFT = Clayium.id("textures/gui/gui_tl.png");
    private static final ResourceLocation TOP_RIGHT = Clayium.id("textures/gui/gui_tr.png");
    private static final ResourceLocation BOTTOM_LEFT = Clayium.id("textures/gui/gui_bl.png");
    private static final ResourceLocation BOTTOM_RIGHT = Clayium.id("textures/gui/gui_br.png");
    private static final ResourceLocation PLAYER = Clayium.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = Clayium.id("textures/gui/slot.png");

    public LogisticsScreen(LogisticsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        int filterHeight = menu.filterSlots() > 0 && !menu.multitrack() ? 24 : 0;
        imageHeight = 94 + 32 + menu.rows() * 18 + filterHeight;
        inventoryLabelY = 22 + menu.rows() * 18 + filterHeight;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int machineHeight = 32 + menu.rows() * 18 + (menu.filterSlots() > 0 && !menu.multitrack() ? 24 : 0);
        tile(graphics, BACK, leftPos + 4, topPos + 4, 168, machineHeight - 8, 8, 8);
        tile(graphics, TOP, leftPos + 4, topPos, 168, 4, 1, 4);
        tile(graphics, BOTTOM, leftPos + 4, topPos + machineHeight - 4, 168, 4, 1, 4);
        tile(graphics, LEFT, leftPos, topPos + 4, 4, machineHeight - 8, 4, 1);
        tile(graphics, RIGHT, leftPos + 172, topPos + 4, 4, machineHeight - 8, 4, 1);
        whole(graphics, TOP_LEFT, leftPos, topPos);
        whole(graphics, TOP_RIGHT, leftPos + 172, topPos);
        whole(graphics, BOTTOM_LEFT, leftPos, topPos + machineHeight - 4);
        whole(graphics, BOTTOM_RIGHT, leftPos + 172, topPos + machineHeight - 4);
        int inventoryWidth = menu.columns() * 18 + (menu.multitrack() ? 22 : 0);
        int inventoryX = (176 - inventoryWidth) / 2 + 1;
        for (int row = 0; row < menu.rows(); row++) {
            for (int column = 0; column < menu.columns(); column++) {
                int uvY = menu.multitrack() ? 96 : 0;
                int uvX = menu.multitrack() ? 32 + row * 32 : 0;
                graphics.blit(SLOT, leftPos + inventoryX + column * 18, topPos + 18 + row * 18, uvX, uvY, 18, 18);
            }
        }
        for (int filter = 0; filter < menu.filterSlots(); filter++) {
            int x = menu.multitrack() ? inventoryX + menu.columns() * 18 + 4 : 35 + filter * 18;
            int y = menu.multitrack() ? 18 + filter * 18 : 24 + menu.rows() * 18;
            int uvX = menu.multitrack() ? 32 + filter * 32 : 96;
            int uvY = menu.multitrack() ? 128 : 32;
            graphics.blit(SLOT, leftPos + x, topPos + y, uvX, uvY, 18, 18);
        }
        graphics.blit(PLAYER, leftPos, topPos + machineHeight, 0, 0, 176, 94);
    }

    private static void tile(
            GuiGraphics graphics, ResourceLocation texture, int x, int y,
            int width, int height, int textureWidth, int textureHeight) {
        for (int dy = 0; dy < height; dy += textureHeight) {
            for (int dx = 0; dx < width; dx += textureWidth) {
                graphics.blit(texture, x + dx, y + dy, 0, 0,
                        Math.min(textureWidth, width - dx),
                        Math.min(textureHeight, height - dy),
                        textureWidth, textureHeight);
            }
        }
    }

    private static void whole(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        graphics.blit(texture, x, y, 0, 0, 4, 4, 4, 4);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }
}
