/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.recipe.ClayWorkTableOperation;
import net.claustra01.clayium.world.inventory.ClayWorkTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/** Client-only screen for the dedicated Clay Work Table menu. */
public final class ClayWorkTableScreen extends AbstractContainerScreen<ClayWorkTableMenu> {
    private static final ResourceLocation BACKGROUND =
            Clayium.id("textures/gui/clay_work_table.png");

    public ClayWorkTableScreen(ClayWorkTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        for (ClayWorkTableOperation operation : ClayWorkTableOperation.values()) {
            int buttonId = operation.buttonId();
            addRenderableWidget(Button.builder(
                            Component.literal(Integer.toString(buttonId)),
                            button -> {
                                if (minecraft != null && minecraft.gameMode != null) {
                                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
                                }
                            })
                    .bounds(leftPos + 40 + (buttonId - 1) * 16, topPos + 52, 16, 16)
                    .build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int total = menu.totalProgress();
        if (total > 0) {
            int width = Mth.clamp(menu.progress() * 80 / total, 0, 80);
            graphics.fill(leftPos + 48, topPos + 29, leftPos + 48 + width, topPos + 45, 0x806B5540);
        }
    }
}
