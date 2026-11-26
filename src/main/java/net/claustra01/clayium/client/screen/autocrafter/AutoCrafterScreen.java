package net.claustra01.clayium.client.screen.autocrafter;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AutoCrafterScreen extends AbstractContainerScreen<AutoCrafterMenu> {

    public AutoCrafterScreen(AutoCrafterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 178;
        inventoryLabelX = 8;
        inventoryLabelY = 84;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + 84, 0, 0, 176, 94);
        // ContainerAutoCrafter: SlotMemory pattern (filter texture), materials, results.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                LegacyGuiGraphics.slot(graphics, leftPos + 5 + column * 18, topPos + 18 + row * 18,
                        LegacyGuiGraphics.SlotTexture.FILTER);
            }
        }
        slots(graphics, 61, 17, 3, 3);
        slots(graphics, 134, 17, 2, 3);
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
        graphics.drawString(font, title, 6, 6, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.machine.energy",
                ClayEnergy.ofInternal(menu.storedEnergy()).formatGui()), 4, 84 - 12, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
