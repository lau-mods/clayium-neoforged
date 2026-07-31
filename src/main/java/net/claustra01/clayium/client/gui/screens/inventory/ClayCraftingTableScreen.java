/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.ClayCraftingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original Clayium frame, slot positions, and crafting-arrow overlay. */
public final class ClayCraftingTableScreen extends AbstractContainerScreen<ClayCraftingTableMenu> {
    private static final ResourceLocation PLAYER = Clayium.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation OVERLAY = Clayium.id("textures/gui/clay_crafting_table.png");
    private static final ResourceLocation SLOT = Clayium.id("textures/gui/slot.png");
    private static final ResourceLocation BACK = Clayium.id("textures/gui/gui_back.png");
    private static final ResourceLocation TOP = Clayium.id("textures/gui/gui_t.png");
    private static final ResourceLocation BOTTOM = Clayium.id("textures/gui/gui_b.png");
    private static final ResourceLocation LEFT = Clayium.id("textures/gui/gui_l.png");
    private static final ResourceLocation RIGHT = Clayium.id("textures/gui/gui_r.png");
    private static final ResourceLocation TOP_LEFT = Clayium.id("textures/gui/gui_tl.png");
    private static final ResourceLocation TOP_RIGHT = Clayium.id("textures/gui/gui_tr.png");
    private static final ResourceLocation BOTTOM_LEFT = Clayium.id("textures/gui/gui_bl.png");
    private static final ResourceLocation BOTTOM_RIGHT = Clayium.id("textures/gui/gui_br.png");

    public ClayCraftingTableScreen(ClayCraftingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        for (int y = 4; y < 68; y += 8) for (int x = 4; x < 172; x += 8) {
            graphics.blit(BACK, leftPos + x, topPos + y, 0, 0,
                    Math.min(8, 172 - x), Math.min(8, 68 - y), 8, 8);
        }
        tile(graphics, TOP, leftPos + 4, topPos, 168, 4, 1, 4);
        tile(graphics, BOTTOM, leftPos + 4, topPos + 68, 168, 4, 1, 4);
        tile(graphics, LEFT, leftPos, topPos + 4, 4, 64, 4, 1);
        tile(graphics, RIGHT, leftPos + 172, topPos + 4, 4, 64, 4, 1);
        whole(graphics, TOP_LEFT, leftPos, topPos);
        whole(graphics, TOP_RIGHT, leftPos + 172, topPos);
        whole(graphics, BOTTOM_LEFT, leftPos, topPos + 68);
        whole(graphics, BOTTOM_RIGHT, leftPos + 172, topPos + 68);
        graphics.blit(PLAYER, leftPos, topPos + 72, 0, 0, 176, 94);
        for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++) {
            graphics.blit(SLOT, leftPos + 29 + column * 18, topPos + 16 + row * 18, 0, 0, 18, 18);
        }
        graphics.blit(SLOT, leftPos + 123, topPos + 34, 0, 0, 18, 18);
        graphics.blit(OVERLAY, leftPos, topPos, 0, 0, 176, 72, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static void tile(GuiGraphics g, ResourceLocation texture, int x, int y, int width, int height, int tw, int th) {
        for (int dy = 0; dy < height; dy += th) for (int dx = 0; dx < width; dx += tw) {
            g.blit(texture, x + dx, y + dy, 0, 0, Math.min(tw, width - dx), Math.min(th, height - dy), tw, th);
        }
    }

    private static void whole(GuiGraphics g, ResourceLocation texture, int x, int y) {
        g.blit(texture, x, y, 0, 0, 4, 4, 4, 4);
    }
}
