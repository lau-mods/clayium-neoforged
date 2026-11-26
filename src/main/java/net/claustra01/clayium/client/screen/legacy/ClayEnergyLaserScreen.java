package net.claustra01.clayium.client.screen.legacy;

import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.laser.ClayEnergyLaserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original GuiClayEnergyTemp over ContainerNothing(32): title, stored CE at (4, 20), inventory. */
public final class ClayEnergyLaserScreen extends AbstractContainerScreen<ClayEnergyLaserMenu> {
    public ClayEnergyLaserScreen(ClayEnergyLaserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = ClayEnergyLaserMenu.MACHINE_HEIGHT + 94;
        inventoryLabelX = 8;
        inventoryLabelY = ClayEnergyLaserMenu.MACHINE_HEIGHT;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + ClayEnergyLaserMenu.MACHINE_HEIGHT,
                0, 0, 176, 94, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 6, 6, LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.clayium.machine.energy",
                ClayEnergy.ofInternal(menu.storedEnergy()).formatGui()), 4, ClayEnergyLaserMenu.MACHINE_HEIGHT - 12,
                LegacyGuiGraphics.TEXT_COLOR, false);
    }
}
