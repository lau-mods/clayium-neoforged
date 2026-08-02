/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.world.inventory.ClayEnergyConverterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ClayEnergyConverterScreen extends AbstractDedicatedMachineScreen<ClayEnergyConverterMenu> {
    public ClayEnergyConverterScreen(ClayEnergyConverterMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Override protected void drawDevice(GuiGraphics graphics) { smallSlot(graphics, 80, 25, 96, 0); }
    @Override protected int tier() { return menu.tier(); }
    @Override protected long energyPerTick() { return menu.energyPerTick(); }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, "FE: " + menu.fe() + " / " + menu.capacity(), 12, 48, 0x404040, false);
        graphics.drawString(font, "Convert: " + net.claustra01.clayium.energy.ClayEnergyFormatter.formatPerTick(menu.energyPerTick())
                + " -> " + menu.production() + " FE/t", 12, 60, 0x404040, false);
        graphics.drawString(font, "Output: " + menu.output() + " FE/t", 12, 72, 0x404040, false);
    }
}
