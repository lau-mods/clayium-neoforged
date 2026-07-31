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
    private static final ResourceLocation PLAYER = Clayium.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = Clayium.id("textures/gui/slot.png");

    public LogisticsScreen(LogisticsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        int filterHeight = menu.filterSlots() > 0 ? 24 : 0;
        imageHeight = 94 + 32 + menu.rows() * 18 + filterHeight;
        inventoryLabelY = 22 + menu.rows() * 18 + filterHeight;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int machineHeight = 32 + menu.rows() * 18 + (menu.filterSlots() > 0 ? 24 : 0);
        for (int y = 0; y < machineHeight; y += 8) {
            for (int x = 0; x < 176; x += 8) {
                graphics.blit(BACK, leftPos + x, topPos + y, 0, 0, Math.min(8, 176 - x), Math.min(8, machineHeight - y), 8, 8);
            }
        }
        for (int row = 0; row < menu.rows(); row++) {
            for (int column = 0; column < 9; column++) {
                graphics.blit(SLOT, leftPos + 8 + column * 18, topPos + 18 + row * 18, 0, 0, 18, 18);
            }
        }
        for (int filter = 0; filter < menu.filterSlots(); filter++) {
            graphics.blit(SLOT, leftPos + 35 + filter * 18, topPos + 24 + menu.rows() * 18, 0, 0, 18, 18);
        }
        graphics.blit(PLAYER, leftPos, topPos + machineHeight, 0, 0, 176, 94);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }
}
