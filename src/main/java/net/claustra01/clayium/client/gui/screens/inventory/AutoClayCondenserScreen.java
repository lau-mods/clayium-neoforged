/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.world.inventory.AutoClayCondenserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AutoClayCondenserScreen extends AbstractDedicatedMachineScreen<AutoClayCondenserMenu> {
    public AutoClayCondenserScreen(AutoClayCondenserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
    @Override protected void drawDevice(GuiGraphics graphics) {
        for (int row = 0; row < 4; row++) for (int column = 0; column < 5; column++) {
            smallSlot(graphics, 43 + column * 18, 18 + row * 18, 0, 0);
        }
        smallSlot(graphics, 151, 18, 96, 0);
    }
    @Override protected int tier() { return 5; }
    @Override protected long energyPerTick() { return 0; }
}
