package net.claustra01.clayium.client.screen.logistics;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.client.screen.legacy.LegacyNumerals;
import net.claustra01.clayium.logistics.NormalInventoryLayout;
import net.claustra01.clayium.logistics.NormalInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original GuiMultipageContainer / GuiClayContainerTemp over ContainerNormalInventory. */
public final class NormalInventoryScreen extends AbstractContainerScreen<NormalInventoryMenu> {
    public NormalInventoryScreen(NormalInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        NormalInventoryLayout layout = menu.layout();
        imageWidth = layout.machineWidth();
        imageHeight = menu.machineHeight() + 94;
        inventoryLabelX = layout.playerX() + 8;
        inventoryLabelY = menu.machineHeight();
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
                topPos + menu.machineHeight(), 0, 0, 176, 94, 256, 256);
        for (int row = 0; row < layout.rows(); row++) {
            for (int column = 0; column < layout.columns(); column++) {
                LegacyGuiGraphics.slot(graphics, leftPos + layout.slotX(column), topPos + layout.slotY(row));
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.layout().drawNames()) {
            graphics.drawString(font, title, 6, 6, LegacyGuiGraphics.TEXT_COLOR, false);
            graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                    LegacyGuiGraphics.TEXT_COLOR, false);
        }
        double resonance = menu.resonance();
        if (!Double.isNaN(resonance)) {
            graphics.drawString(font, Component.translatable("gui.clayium.machine.resonance",
                    LegacyNumerals.resonance(resonance)), 80, menu.machineHeight() - 12,
                    LegacyGuiGraphics.TEXT_COLOR, false);
        }
    }
}
