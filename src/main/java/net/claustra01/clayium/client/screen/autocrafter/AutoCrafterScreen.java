package net.claustra01.clayium.client.screen.autocrafter;

import java.util.Locale;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AutoCrafterScreen extends AbstractContainerScreen<AutoCrafterMenu> {

    public AutoCrafterScreen(AutoCrafterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 226;
        inventoryLabelX = 34;
        inventoryLabelY = 128;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos + 26, topPos + 130, 0, 0, 176, 86);
        slots(graphics, 7, 33, 3, 3);
        slots(graphics, 106, 33, 3, 3);
        slots(graphics, 173, 33, 2, 3);
        slot(graphics, 74, 51);
        slot(graphics, 74, 75);
        graphics.drawString(font, "→", leftPos + 64, topPos + 57, 0x404040, false);
        graphics.drawString(font, "→", leftPos + 163, topPos + 57, 0x404040, false);
    }

    private void slots(GuiGraphics graphics, int x, int y, int columns, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                slot(graphics, x + column * 18, y + row * 18);
            }
        }
    }

    private void slot(GuiGraphics graphics, int x, int y) {
        graphics.blit(LegacyGuiGraphics.SLOT, leftPos + x, topPos + y, 0, 0, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String titleText = title.getString();
        if (font.width(titleText) > imageWidth - 70) {
            titleText = font.plainSubstrByWidth(titleText, imageWidth - 80) + "…";
        }
        graphics.drawString(font, titleText, 7, 7, 0x404040, false);
        Component tier = Component.literal("Tier " + menu.tier());
        graphics.drawString(font, tier, imageWidth - 7 - font.width(tier), 7, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.auto_crafter.pattern"), 8, 22, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.auto_crafter.materials"), 107, 22, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.auto_crafter.outputs"), 174, 22, 0x404040, false);
        String cycle = Component.translatable(
                "gui.clayium.auto_crafter.cycle", menu.intervalTicks(), menu.craftsPerCycle()).getString();
        graphics.drawString(font, cycle, 8, 94, 0x404040, false);
        graphics.drawString(font, Component.translatable(
                "gui.clayium.auto_crafter.stored_ce",
                String.format(Locale.ROOT, "%.5f", menu.storedEnergy() / 100_000.0D)), 8, 106, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
