package net.claustra01.clayium.client.screen.workbench;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.workbench.ClayCraftingBoardMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class ClayCraftingBoardScreen extends AbstractContainerScreen<ClayCraftingBoardMenu> {
    private static final ResourceLocation BACKGROUND = ClayiumMod.id("textures/gui/back.png");
    private static final ResourceLocation CRAFTING = ClayiumMod.id("textures/gui/claycraftingtable.png");
    private static final ResourceLocation PLAYER_INVENTORY = ClayiumMod.id("textures/gui/playerinventory.png");
    private static final ResourceLocation SLOT = ClayiumMod.id("textures/gui/slot.png");

    public ClayCraftingBoardScreen(ClayCraftingBoardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 234;
        inventoryLabelY = 141;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        if (menu.adjacentSlotCount() > 0) {
            graphics.drawString(font, Component.translatable("container.clayium.nearby_inventory"),
                    8, 74, 0x404040, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, 176, 181, 256, 256);
        graphics.blit(CRAFTING, leftPos, topPos, 0, 0, 176, 80, 256, 256);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                drawSlot(graphics, 29 + column * 18, 16 + row * 18);
            }
        }
        for (int slot = 0; slot < menu.adjacentSlotCount(); slot++) {
            drawSlot(graphics, 7 + slot % 9 * 18, 83 + slot / 9 * 18);
        }
        graphics.blit(PLAYER_INVENTORY, leftPos, topPos + 140, 0, 0, 176, 94, 256, 256);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.blit(SLOT, leftPos + x, topPos + y, 0, 0, 18, 18, 256, 256);
    }
}
