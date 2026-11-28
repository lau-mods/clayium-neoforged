package net.claustra01.clayium.client.screen;

import net.claustra01.clayium.menu.StorageContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for Storage Container and Vacuum Container showing stored / maximum capacity.
 */
public class StorageContainerScreen extends ClayiumBaseScreen<StorageContainerMenu> {
    public StorageContainerScreen(StorageContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        int size = this.menu.getStoredCount();
        int max = this.menu.getMaxStorageSize();
        int sizeWidth = this.font.width(String.valueOf(size));
        guiGraphics.drawString(
                this.font,
                size + " / " + max,
                this.menu.machineGuiSizeX / 2 - 8 - sizeWidth,
                this.menu.machineGuiSizeY - 12,
                0x404040,
                false
        );
    }
}
