/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.MachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/** Shared screen for the first CE-consuming processing machines. */
public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final ResourceLocation PLAYER_INVENTORY =
            Clayium.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = Clayium.id("textures/gui/slot.png");
    private static final ResourceLocation PROGRESS =
            Clayium.id("textures/gui/progressbarfurnace.png");

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        addRenderableWidget(Button.builder(
                        Component.literal("+5 CE"),
                        button -> {
                            if (minecraft != null && minecraft.gameMode != null) {
                                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                            }
                        })
                .bounds(leftPos + 74, topPos + 52, 42, 16)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.blit(PLAYER_INVENTORY, leftPos + 7, topPos + 83, 0, 0, 162, 72);
        graphics.blit(SLOT, leftPos + 34, topPos + 30, 0, 0, 18, 18);
        graphics.blit(SLOT, leftPos + 124, topPos + 30, 0, 0, 18, 18);
        int width = menu.totalProgress() <= 0
                ? 0
                : Mth.clamp(menu.progress() * 24 / menu.totalProgress(), 0, 24);
        graphics.blit(PROGRESS, leftPos + 76, topPos + 32, 0, 17, width, 17);
        graphics.drawString(font, "CE " + menu.energy() + " / " + menu.capacity(), leftPos + 8, topPos + 60, 0x404040, false);
        graphics.drawString(
                font,
                Component.translatable("gui.clayium_neoforged.stop_reason." + menu.stopReason().name().toLowerCase()),
                leftPos + 8,
                topPos + 72,
                0x404040,
                false);
    }
}
