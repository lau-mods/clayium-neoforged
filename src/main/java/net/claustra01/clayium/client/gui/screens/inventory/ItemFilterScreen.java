/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.network.SetFilterPatternPayload;
import net.claustra01.clayium.world.inventory.ItemFilterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** Original 5x2 list editor and one-line regexp editor. */
public final class ItemFilterScreen extends AbstractContainerScreen<ItemFilterMenu> {
    private static final ResourceLocation BACK = Clayium.id("textures/gui/gui_back.png");
    private static final ResourceLocation TOP = Clayium.id("textures/gui/gui_t.png");
    private static final ResourceLocation BOTTOM = Clayium.id("textures/gui/gui_b.png");
    private static final ResourceLocation LEFT = Clayium.id("textures/gui/gui_l.png");
    private static final ResourceLocation RIGHT = Clayium.id("textures/gui/gui_r.png");
    private static final ResourceLocation TOP_LEFT = Clayium.id("textures/gui/gui_tl.png");
    private static final ResourceLocation TOP_RIGHT = Clayium.id("textures/gui/gui_tr.png");
    private static final ResourceLocation BOTTOM_LEFT = Clayium.id("textures/gui/gui_bl.png");
    private static final ResourceLocation BOTTOM_RIGHT = Clayium.id("textures/gui/gui_br.png");
    private static final ResourceLocation PLAYER = Clayium.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = Clayium.id("textures/gui/slot.png");
    private EditBox pattern;

    public ItemFilterScreen(ItemFilterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = menu.isListEditor() ? 148 : 130;
        inventoryLabelY = machineHeight();
    }

    @Override
    protected void init() {
        super.init();
        if (menu.isStringEditor()) {
            pattern = new EditBox(font, leftPos + 12, topPos + 18, imageWidth - 24, 14, title);
            pattern.setMaxLength(128);
            pattern.setValue(menu.pattern());
            pattern.setResponder(value -> PacketDistributor.sendToServer(new SetFilterPatternPayload(value)));
            addRenderableWidget(pattern);
            setInitialFocus(pattern);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        tile(graphics, BACK, leftPos + 4, topPos + 4, 168, imageHeight - 8, 8, 8);
        tile(graphics, TOP, leftPos + 4, topPos, 168, 4, 1, 4);
        tile(graphics, BOTTOM, leftPos + 4, topPos + imageHeight - 4, 168, 4, 1, 4);
        tile(graphics, LEFT, leftPos, topPos + 4, 4, imageHeight - 8, 4, 1);
        tile(graphics, RIGHT, leftPos + 172, topPos + 4, 4, imageHeight - 8, 4, 1);
        whole(graphics, TOP_LEFT, leftPos, topPos);
        whole(graphics, TOP_RIGHT, leftPos + 172, topPos);
        whole(graphics, BOTTOM_LEFT, leftPos, topPos + imageHeight - 4);
        whole(graphics, BOTTOM_RIGHT, leftPos + 172, topPos + imageHeight - 4);
        if (menu.isListEditor()) {
            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 5; column++) {
                    graphics.blit(SLOT, leftPos + 43 + column * 18, topPos + 17 + row * 18,
                            0, 0, 18, 18);
                }
            }
        }
        graphics.blit(PLAYER, leftPos, topPos + machineHeight(), 0, 0, 176, 94);
    }

    private int machineHeight() {
        return menu.isListEditor() ? 54 : 36;
    }

    private static void tile(
            GuiGraphics graphics, ResourceLocation texture, int x, int y,
            int width, int height, int textureWidth, int textureHeight) {
        for (int dy = 0; dy < height; dy += textureHeight) {
            for (int dx = 0; dx < width; dx += textureWidth) {
                graphics.blit(
                        texture,
                        x + dx,
                        y + dy,
                        0,
                        0,
                        Math.min(textureWidth, width - dx),
                        Math.min(textureHeight, height - dy),
                        textureWidth,
                        textureHeight);
            }
        }
    }

    private static void whole(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        graphics.blit(texture, x, y, 0, 0, 4, 4, 4, 4);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
