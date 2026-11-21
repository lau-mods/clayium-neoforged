package net.claustra01.clayium.client.screen.area;

import net.claustra01.clayium.client.screen.logistics.LogisticsBackground;
import net.claustra01.clayium.world.area.AreaMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AreaMachineScreen extends AbstractContainerScreen<AreaMachineMenu> {
    public AreaMachineScreen(AreaMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 182;
        inventoryLabelY = 88;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LogisticsBackground.draw(graphics, leftPos, topPos, imageWidth, imageHeight);
        for (int index = 0; index < 12; index++) {
            int x = index == 0 ? 8 : index == 1 ? 8 : index == 11 ? 35 : 62 + (index - 2) % 3 * 18;
            int y = index == 0 ? 35 : index == 1 || index == 11 ? 58 : 26 + (index - 2) / 3 * 18;
            graphics.fill(leftPos + x - 1, topPos + y - 1, leftPos + x + 17, topPos + y + 17, 0xFF555555);
            graphics.fill(leftPos + x, topPos + y, leftPos + x + 16, topPos + y + 16, 0xFFAAAAAA);
        }
    }
}
