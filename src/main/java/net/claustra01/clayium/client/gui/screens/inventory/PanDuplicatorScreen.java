/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.world.inventory.PanDuplicatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class PanDuplicatorScreen extends AbstractDedicatedMachineScreen<PanDuplicatorMenu> {
    public PanDuplicatorScreen(PanDuplicatorMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Override protected void drawDevice(GuiGraphics graphics) {
        smallSlot(graphics, 26, 30, 0, 0);
        smallSlot(graphics, 62, 30, 0, 0);
        smallSlot(graphics, 134, 30, 0, 0);
    }
    @Override protected int tier() { return menu.tier(); }
    @Override protected long energyPerTick() { return menu.energyPerTick(); }
}
