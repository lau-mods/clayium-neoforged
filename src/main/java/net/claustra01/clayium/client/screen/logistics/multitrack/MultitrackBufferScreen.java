package net.claustra01.clayium.client.screen.logistics.multitrack;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.logistics.multitrack.MultitrackBufferMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original GuiClayContainerTemp over ContainerMultitrackBuffer (GUI 31). */
public final class MultitrackBufferScreen extends AbstractContainerScreen<MultitrackBufferMenu> {
    public MultitrackBufferScreen(MultitrackBufferMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = menu.machineWidth();
        imageHeight = menu.machineHeight() + 94;
        titleLabelX = 6;
        titleLabelY = 6;
        inventoryLabelX = (imageWidth - 176) / 2 + 8;
        inventoryLabelY = menu.machineHeight();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos + (imageWidth - 176) / 2,
                topPos + menu.machineHeight(), 0, 0, 176, 94, 256, 256);
        int offsetX = menu.trackOffsetX();
        for (int track = 0; track < menu.trackCount(); track++) {
            int y = topPos + track * 18 + 18;
            for (int slot = 0; slot < menu.slotsPerTrack(); slot++) {
                LegacyGuiGraphics.multitrackSlot(graphics, leftPos + slot * 18 + offsetX, y, track, false);
            }
            LegacyGuiGraphics.multitrackSlot(graphics,
                    leftPos + menu.slotsPerTrack() * 18 + 4 + offsetX, y, track, true);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                LegacyGuiGraphics.TEXT_COLOR, false);
    }
}
