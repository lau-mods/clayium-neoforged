package net.claustra01.clayium.client.screen.logistics;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.logistics.BufferMenu;
import net.claustra01.clayium.logistics.NormalInventoryLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original GuiMultipageContainer over ContainerNormalInventory (GUI 11). */
public final class BufferScreen extends AbstractContainerScreen<BufferMenu> {
    public BufferScreen(BufferMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        NormalInventoryLayout layout = menu.layout();
        imageWidth = layout.machineWidth();
        imageHeight = layout.imageHeight();
        titleLabelX = 6;
        titleLabelY = 6;
        inventoryLabelX = layout.playerX() + 8;
        inventoryLabelY = layout.machineHeight();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        NormalInventoryLayout layout = menu.layout();
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos + layout.playerX(),
                topPos + layout.machineHeight(), 0, 0, 176, 94, 256, 256);
        for (int row = 0; row < layout.rows(); row++) {
            for (int column = 0; column < layout.columns(); column++) {
                LegacyGuiGraphics.slot(graphics, leftPos + layout.slotX(column), topPos + layout.slotY(row));
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.layout().drawNames()) {
            return;
        }
        graphics.drawString(font, title, titleLabelX, titleLabelY, LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                LegacyGuiGraphics.TEXT_COLOR, false);
    }
}
