/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.energy.ClayEnergyFormatter;
import net.claustra01.clayium.world.inventory.MachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/** Original-style component-composed GUI shared by the first Clayium machines. */
public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final ResourceLocation BACK = Clayium.id("textures/gui/gui_back.png");
    private static final ResourceLocation TOP = Clayium.id("textures/gui/gui_t.png");
    private static final ResourceLocation BOTTOM = Clayium.id("textures/gui/gui_b.png");
    private static final ResourceLocation LEFT = Clayium.id("textures/gui/gui_l.png");
    private static final ResourceLocation RIGHT = Clayium.id("textures/gui/gui_r.png");
    private static final ResourceLocation TOP_LEFT = Clayium.id("textures/gui/gui_tl.png");
    private static final ResourceLocation TOP_RIGHT = Clayium.id("textures/gui/gui_tr.png");
    private static final ResourceLocation BOTTOM_LEFT = Clayium.id("textures/gui/gui_bl.png");
    private static final ResourceLocation BOTTOM_RIGHT = Clayium.id("textures/gui/gui_br.png");
    private static final ResourceLocation PLAYER_INVENTORY =
            Clayium.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = Clayium.id("textures/gui/slot.png");
    private static final ResourceLocation PROGRESS =
            Clayium.id("textures/gui/progressbarfurnace.png");
    private static final ResourceLocation BUTTON = Clayium.id("textures/gui/button.png");
    private ManualCraftButton manualCraftButton;

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelX = 6;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        manualCraftButton = addRenderableWidget(new ManualCraftButton(
                leftPos + 80,
                topPos + 56,
                button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                    }
                }));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (manualCraftButton != null) {
            manualCraftButton.active =
                    menu.stopReason() == net.claustra01.clayium.world.level.block.entity.MachineBlockEntity.StopReason.RUNNING
                            || menu.stopReason()
                                    == net.claustra01.clayium.world.level.block.entity.MachineBlockEntity.StopReason.INSUFFICIENT_ENERGY;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos + 4
                && mouseX < leftPos + 76
                && mouseY >= topPos + 58
                && mouseY < topPos + 70) {
            graphics.renderTooltip(
                    font,
                    Component.translatable(
                            "gui.clayium_neoforged.stop_reason."
                                    + menu.stopReason().name().toLowerCase()),
                    mouseX,
                    mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        tile(graphics, BACK, leftPos + 4, topPos + 4, 168, 158, 8, 8);
        tile(graphics, TOP, leftPos + 4, topPos, 168, 4, 1, 4);
        tile(graphics, BOTTOM, leftPos + 4, topPos + 162, 168, 4, 1, 4);
        tile(graphics, LEFT, leftPos, topPos + 4, 4, 158, 4, 1);
        tile(graphics, RIGHT, leftPos + 172, topPos + 4, 4, 158, 4, 1);
        blitWhole(graphics, TOP_LEFT, leftPos, topPos, 4, 4);
        blitWhole(graphics, TOP_RIGHT, leftPos + 172, topPos, 4, 4);
        blitWhole(graphics, BOTTOM_LEFT, leftPos, topPos + 162, 4, 4);
        blitWhole(graphics, BOTTOM_RIGHT, leftPos + 172, topPos + 162, 4, 4);
        graphics.blit(PLAYER_INVENTORY, leftPos, topPos + 72, 0, 0, 176, 94);
        graphics.blit(SLOT, leftPos + 39, topPos + 30, 0, 32, 26, 26);
        graphics.blit(SLOT, leftPos + 111, topPos + 30, 0, 32, 26, 26);
        graphics.blit(PROGRESS, leftPos + 76, topPos + 35, 0, 0, 24, 17);
        int width = menu.totalProgress() <= 0
                ? 0
                : Mth.clamp(menu.progress() * 24 / menu.totalProgress(), 0, 24);
        graphics.blit(PROGRESS, leftPos + 76, topPos + 35, 0, 17, width, 17);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(
                font,
                Component.translatable(
                        "gui.clayium_neoforged.energy",
                        ClayEnergyFormatter.format(menu.energy())),
                4,
                60,
                0x404040,
                false);
        Component tier = Component.translatable("gui.clayium_neoforged.tier", menu.tier());
        graphics.drawString(font, tier, imageWidth - 6 - font.width(tier), 60, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private static void tile(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height,
            int textureWidth,
            int textureHeight) {
        for (int drawY = 0; drawY < height; drawY += textureHeight) {
            for (int drawX = 0; drawX < width; drawX += textureWidth) {
                int drawWidth = Math.min(textureWidth, width - drawX);
                int drawHeight = Math.min(textureHeight, height - drawY);
                graphics.blit(
                        texture,
                        x + drawX,
                        y + drawY,
                        0.0F,
                        0.0F,
                        drawWidth,
                        drawHeight,
                        textureWidth,
                        textureHeight);
            }
        }
    }

    private static void blitWhole(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int textureWidth,
            int textureHeight) {
        graphics.blit(
                texture,
                x,
                y,
                0.0F,
                0.0F,
                textureWidth,
                textureHeight,
                textureWidth,
                textureHeight);
    }

    private static final class ManualCraftButton extends Button {
        private ManualCraftButton(int x, int y, OnPress onPress) {
            super(x, y, 16, 16, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                float partialTick) {
            int state = active ? (isHoveredOrFocused() ? 2 : 1) : 0;
            graphics.blit(BUTTON, getX(), getY(), 0, state * 16, 16, 16);
        }
    }
}
