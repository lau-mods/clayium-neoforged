/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.logistics.LogisticsKind;
import net.claustra01.clayium.world.inventory.LogisticsMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original Clayium component-composed logistics screens. */
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
        imageHeight = menu.machineHeight() + 94;
        inventoryLabelY = menu.machineHeight();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        tile(graphics, BACK, leftPos + 4, topPos + 4, 168, imageHeight - 8, 8, 8);
        tile(graphics, TOP, leftPos + 4, topPos, 168, 4, 1, 4);
        tile(graphics, BOTTOM, leftPos + 4, topPos + imageHeight - 4, 168, 4, 1, 4);
        tile(graphics, LEFT, leftPos, topPos + 4, 4, imageHeight - 8, 4, 1);
        tile(graphics, RIGHT, leftPos + 172, topPos + 4, 4, imageHeight - 8, 4, 1);
        whole(graphics, TOP_LEFT, leftPos, topPos);
        whole(graphics, TOP_RIGHT, leftPos + 172, topPos);
        whole(graphics, BOTTOM_LEFT, leftPos, topPos + imageHeight - 4);
        whole(graphics, BOTTOM_RIGHT, leftPos + 172, topPos + imageHeight - 4);
        drawDeviceSlots(graphics);
        graphics.blit(PLAYER, leftPos, topPos + menu.machineHeight(), 0, 0, 176, 94);
    }

    private void drawDeviceSlots(GuiGraphics graphics) {
        switch (menu.kind()) {
            case BUFFER -> drawBufferSlots(graphics);
            case MULTITRACK_BUFFER -> drawMultitrackSlots(graphics);
            case DISTRIBUTOR -> drawDistributorSlots(graphics);
            case STORAGE_CONTAINER -> {
                largeSlot(graphics, 44, 35);
                largeSlot(graphics, 116, 35);
                normalSlot(graphics, 142, 18, 96, 32);
            }
            case VOID_CONTAINER -> {
                largeSlot(graphics, 80, 35);
                normalSlot(graphics, 142, 18, 96, 32);
            }
        }
    }

    private void drawBufferSlots(GuiGraphics graphics) {
        int offsetX = (176 - menu.columns() * 18) / 2 + 1;
        for (int slot = 0; slot < menu.kind().slots(menu.tier()); slot++) {
            normalSlot(graphics, offsetX + slot % menu.columns() * 18, 18 + slot / menu.columns() * 18, 0, 0);
        }
    }

    private void drawMultitrackSlots(GuiGraphics graphics) {
        int offsetX = (176 - (menu.columns() * 18 + 22)) / 2 + 1;
        for (int track = 0; track < menu.tracks(); track++) {
            for (int column = 0; column < menu.columns(); column++) {
                normalSlot(
                        graphics,
                        offsetX + column * 18,
                        18 + track * 18,
                        32 + track * 32,
                        96);
            }
            normalSlot(
                    graphics,
                    offsetX + menu.columns() * 18 + 4,
                    18 + track * 18,
                    32 + track * 32,
                    128);
        }
    }

    private void drawDistributorSlots(GuiGraphics graphics) {
        int colonyX = menu.tier() == 7 ? 2 : menu.tier() == 8 ? 3 : 4;
        int colonyY = menu.tier() == 7 || menu.tier() == 8 ? 2 : 3;
        int offsetX = (176 - (2 * colonyX * 18 + (colonyX - 1) * 2)) / 2 + 1;
        for (int cy = 0; cy < colonyY; cy++) {
            for (int cx = 0; cx < colonyX; cx++) {
                for (int row = 0; row < 2; row++) {
                    for (int column = 0; column < 2; column++) {
                        normalSlot(
                                graphics,
                                offsetX + cx * 38 + column * 18,
                                18 + cy * 38 + row * 18,
                                0,
                                0);
                    }
                }
            }
        }
    }

    private void normalSlot(GuiGraphics graphics, int itemX, int itemY, int u, int v) {
        graphics.blit(SLOT, leftPos + itemX - 1, topPos + itemY - 1, u, v, 18, 18);
    }

    private void largeSlot(GuiGraphics graphics, int itemX, int itemY) {
        graphics.blit(SLOT, leftPos + itemX - 5, topPos + itemY - 5, 0, 32, 26, 26);
    }

    private static void tile(
            GuiGraphics graphics, ResourceLocation texture, int x, int y,
            int width, int height, int textureWidth, int textureHeight) {
        for (int dy = 0; dy < height; dy += textureHeight) {
            for (int dx = 0; dx < width; dx += textureWidth) {
                graphics.blit(
                        texture,
                        x + dx,
                        y + dy,
                        0,
                        0,
                        Math.min(textureWidth, width - dx),
                        Math.min(textureHeight, height - dy),
                        textureWidth,
                        textureHeight);
            }
        }
    }

    private static void whole(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        graphics.blit(texture, x, y, 0, 0, 4, 4, 4, 4);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        if (menu.kind() == LogisticsKind.STORAGE_CONTAINER) {
            Component count = Component.literal(menu.storedCount() + " / " + menu.storageCapacity());
            graphics.drawString(
                    font,
                    count,
                    imageWidth - 6 - font.width(count),
                    menu.machineHeight() - 12,
                    0x404040,
                    false);
        }
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }
}
