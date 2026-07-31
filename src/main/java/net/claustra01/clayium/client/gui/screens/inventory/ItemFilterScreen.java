/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.client.gui.screens.inventory;

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
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private EditBox pattern;

    public ItemFilterScreen(ItemFilterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = menu.isListEditor() ? 148 : 130;
        inventoryLabelY = menu.isListEditor() ? 55 : 37;
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
        int rows = menu.isListEditor() ? 2 : 1;
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, rows * 18 + 17);
        graphics.blit(
                BACKGROUND,
                leftPos,
                topPos + (menu.isListEditor() ? 54 : 36),
                0,
                126,
                imageWidth,
                96);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
