/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.energy.ClayEnergyFormatter;
import net.claustra01.clayium.world.inventory.AbstractDedicatedMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

abstract class AbstractDedicatedMachineScreen<M extends AbstractDedicatedMachineMenu>
        extends AbstractContainerScreen<M> {
    protected static final ResourceLocation SLOT = Clayium.id("textures/gui/slot.png");
    protected static final ResourceLocation PROGRESS = Clayium.id("textures/gui/progressbarfurnace.png");
    private static final ResourceLocation BACK = Clayium.id("textures/gui/gui_back.png");
    private static final ResourceLocation TOP = Clayium.id("textures/gui/gui_t.png");
    private static final ResourceLocation BOTTOM = Clayium.id("textures/gui/gui_b.png");
    private static final ResourceLocation LEFT = Clayium.id("textures/gui/gui_l.png");
    private static final ResourceLocation RIGHT = Clayium.id("textures/gui/gui_r.png");
    private static final ResourceLocation TL = Clayium.id("textures/gui/gui_tl.png");
    private static final ResourceLocation TR = Clayium.id("textures/gui/gui_tr.png");
    private static final ResourceLocation BL = Clayium.id("textures/gui/gui_bl.png");
    private static final ResourceLocation BR = Clayium.id("textures/gui/gui_br.png");
    private static final ResourceLocation PLAYER = Clayium.id("textures/gui/gui_playerinventory.png");

    protected AbstractDedicatedMachineScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = menu.machineHeight() + 94;
        titleLabelX = 6;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = menu.machineHeight();
    }

    @Override
    protected final void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        tile(graphics, BACK, leftPos + 4, topPos + 4, 168, imageHeight - 8, 8, 8);
        tile(graphics, TOP, leftPos + 4, topPos, 168, 4, 1, 4);
        tile(graphics, BOTTOM, leftPos + 4, topPos + imageHeight - 4, 168, 4, 1, 4);
        tile(graphics, LEFT, leftPos, topPos + 4, 4, imageHeight - 8, 4, 1);
        tile(graphics, RIGHT, leftPos + 172, topPos + 4, 4, imageHeight - 8, 4, 1);
        whole(graphics, TL, leftPos, topPos);
        whole(graphics, TR, leftPos + 172, topPos);
        whole(graphics, BL, leftPos, topPos + imageHeight - 4);
        whole(graphics, BR, leftPos + 172, topPos + imageHeight - 4);
        drawDevice(graphics);
        graphics.blit(PLAYER, leftPos, topPos + menu.machineHeight(), 0, 0, 176, 94);
    }

    protected abstract void drawDevice(GuiGraphics graphics);
    protected abstract int tier();
    protected abstract long energyPerTick();

    protected final void smallSlot(GuiGraphics graphics, int x, int y, int u, int v) {
        graphics.blit(SLOT, leftPos + x - 1, topPos + y - 1, u, v, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        if (energyPerTick() > 0) {
            graphics.drawString(font, ClayEnergyFormatter.formatPerTick(energyPerTick()),
                    4, menu.machineHeight() - 24, 0x404040, false);
        }
        graphics.drawString(font, Component.translatable("gui.clayium_neoforged.energy",
                ClayEnergyFormatter.format(menu.energy())), 4, menu.machineHeight() - 12, 0x404040, false);
        Component tierText = Component.translatable("gui.clayium_neoforged.tier", tier());
        graphics.drawString(font, tierText, imageWidth - 6 - font.width(tierText),
                menu.machineHeight() - 12, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private static void tile(GuiGraphics graphics, ResourceLocation texture,
                             int x, int y, int width, int height, int tileWidth, int tileHeight) {
        for (int yy = 0; yy < height; yy += tileHeight) for (int xx = 0; xx < width; xx += tileWidth) {
            graphics.blit(texture, x + xx, y + yy, 0, 0,
                    Math.min(tileWidth, width - xx), Math.min(tileHeight, height - yy), tileWidth, tileHeight);
        }
    }

    private static void whole(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        graphics.blit(texture, x, y, 0, 0, 4, 4, 4, 4);
    }
}
