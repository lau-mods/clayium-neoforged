package net.claustra01.clayium.client.screen.gadget;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.gadget.GadgetHolderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class GadgetHolderScreen extends AbstractContainerScreen<GadgetHolderMenu> {
    private static final ResourceLocation PLAYER_INVENTORY = ClayiumMod.id("textures/gui/playerinventory.png");

    public GadgetHolderScreen(GadgetHolderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 173;
        inventoryLabelY = 79;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos + 4, topPos + 4, leftPos + 172, topPos + 169, 0xFFC6C6C6);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + 172, topPos + 69, 0xFF3C3C3C);
        graphics.blit(PLAYER_INVENTORY, leftPos + 4, topPos + 83, 4, 4, 168, 86, 256, 256);
        graphics.blit(ClayiumMod.id("textures/gui/gui_tl.png"), leftPos, topPos, 0, 0, 4, 4, 4, 4);
        graphics.blit(ClayiumMod.id("textures/gui/gui_tr.png"), leftPos + 172, topPos, 0, 0, 4, 4, 4, 4);
        graphics.blit(ClayiumMod.id("textures/gui/gui_bl.png"), leftPos, topPos + 169, 0, 0, 4, 4, 4, 4);
        graphics.blit(ClayiumMod.id("textures/gui/gui_br.png"), leftPos + 172, topPos + 169, 0, 0, 4, 4, 4, 4);
        graphics.blit(ClayiumMod.id("textures/gui/gui_t.png"), leftPos + 4, topPos, 168, 4, 0, 0, 1, 4, 1, 4);
        graphics.blit(ClayiumMod.id("textures/gui/gui_b.png"), leftPos + 4, topPos + 169, 168, 4, 0, 0, 1, 4, 1, 4);
        graphics.blit(ClayiumMod.id("textures/gui/gui_l.png"), leftPos, topPos + 4, 4, 165, 0, 0, 4, 1, 4, 1);
        graphics.blit(ClayiumMod.id("textures/gui/gui_r.png"), leftPos + 172, topPos + 4, 4, 165, 0, 0, 4, 1, 4, 1);
        for (int slot = 0; slot < 16; slot++) {
            int x = leftPos + 16 + slot % 8 * 18;
            int y = topPos + 28 + slot / 8 * 18;
            graphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF202020);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.clayium.gadget_holder.installed"), 17, 18, 0xD8D8D8, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
