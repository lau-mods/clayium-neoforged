package net.claustra01.clayium.client.screen;

import net.claustra01.clayium.menu.WaterWheelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for Clay Water Wheel and Dense Clay Water Wheel displaying progress, durability, and surrounding water count.
 */
public class WaterWheelScreen extends ClayiumBaseScreen<WaterWheelMenu> {
    public WaterWheelScreen(WaterWheelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(
                this.font,
                Component.translatable("gui.WaterWheel.progress", this.menu.getProgress()),
                6,
                this.menu.machineGuiSizeY - 12,
                0x404040,
                false
        );
        guiGraphics.drawString(
                this.font,
                Component.translatable("gui.WaterWheel.durability", this.menu.getEfficiency()),
                90,
                this.menu.machineGuiSizeY - 12,
                0x404040,
                false
        );
        guiGraphics.drawString(
                this.font,
                Component.translatable("gui.WaterWheel.surroundingWater", this.menu.getSurroundingWaterCount()),
                90,
                this.menu.machineGuiSizeY,
                0x404040,
                false
        );
    }
}
