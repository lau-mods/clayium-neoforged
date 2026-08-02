/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.world.inventory.AutoCrafterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AutoCrafterScreen extends AbstractDedicatedMachineScreen<AutoCrafterMenu> {
    public AutoCrafterScreen(AutoCrafterMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Override protected void drawDevice(GuiGraphics graphics) {
        for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++) {
            smallSlot(graphics, 62 + column * 18, 18 + row * 18, 0, 0);
            smallSlot(graphics, 5 + column * 18, 18 + row * 18, 96, 32);
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 2; column++) {
            smallSlot(graphics, 135 + column * 18, 18 + row * 18, 0, 0);
        }
    }
    @Override protected int tier() { return menu.tier(); }
    @Override protected long energyPerTick() { return menu.energyPerTick(); }
}
