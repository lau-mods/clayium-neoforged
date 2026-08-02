/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.world.inventory.ChemicalMetalSeparatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class ChemicalMetalSeparatorScreen extends AbstractDedicatedMachineScreen<ChemicalMetalSeparatorMenu> {
    public ChemicalMetalSeparatorScreen(ChemicalMetalSeparatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
    @Override protected void drawDevice(GuiGraphics graphics) {
        graphics.blit(SLOT, leftPos + 20, topPos + 39, 0, 32, 26, 26);
        for (int row = 0; row < 4; row++) for (int column = 0; column < 4; column++) {
            smallSlot(graphics, 85 + column * 18, 17 + row * 18, 0, 0);
        }
        graphics.blit(PROGRESS, leftPos + 55, topPos + 44, 0, 0, 24, 17);
        int width = menu.totalProgress() <= 0 ? 0
                : Mth.clamp(menu.progress() * 24 / menu.totalProgress(), 0, 24);
        if (width > 0) graphics.blit(PROGRESS, leftPos + 55, topPos + 44, 0, 17, width, 17);
    }
    @Override protected int tier() { return 6; }
    @Override protected long energyPerTick() { return menu.energyPerTick(); }
}
