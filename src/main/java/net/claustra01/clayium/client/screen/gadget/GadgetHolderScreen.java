package net.claustra01.clayium.client.screen.gadget;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.gadget.GadgetHolderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class GadgetHolderScreen extends AbstractContainerScreen<GadgetHolderMenu> {
    public GadgetHolderScreen(GadgetHolderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 173;
        inventoryLabelY = 79;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + 79,
                0, 0, 176, 90, 256, 256);
        for (int slot = 0; slot < 16; slot++) {
            int x = leftPos + 16 + slot % 8 * 18;
            int y = topPos + 28 + slot / 8 * 18;
            LegacyGuiGraphics.slot(graphics, x + 1, y + 1);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                LegacyGuiGraphics.TEXT_COLOR, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
