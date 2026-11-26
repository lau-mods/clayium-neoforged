package net.claustra01.clayium.client.screen.storage;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.storage.StorageContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class StorageContainerScreen extends AbstractContainerScreen<StorageContainerMenu> {

    public StorageContainerScreen(StorageContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        // ContainerStorageContainer: LargeSlotTexture at (44,35)/(116,35), SlotMemory at (142,18).
        LegacyGuiGraphics.largeSlot(graphics, leftPos + 44, topPos + 35);
        LegacyGuiGraphics.largeSlot(graphics, leftPos + 116, topPos + 35);
        LegacyGuiGraphics.slot(graphics, leftPos + 142, topPos + 18);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + 72, 0, 0, 176, 94);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 6, 6, 0x404040, false);
        // GuiStorageContainer: "size / max" with " / " anchored at machineGuiSizeX/2 - 8.
        String size = Long.toString(menu.amount());
        graphics.drawString(font, size + " / " + menu.capacity(),
                imageWidth / 2 - 8 - font.width(size), 72 - 12, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
