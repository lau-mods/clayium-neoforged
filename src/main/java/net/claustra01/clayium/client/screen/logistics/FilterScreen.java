package net.claustra01.clayium.client.screen.logistics;

import net.claustra01.clayium.client.screen.legacy.LegacyButton;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.logistics.filter.FilterMenu;
import net.claustra01.clayium.logistics.filter.ItemFilterSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original GuiTemp over ContainerItemFilterWhiteList, plus the whitelist/blacklist toggle. */
public final class FilterScreen extends AbstractContainerScreen<FilterMenu> {
    private LegacyButton mode;

    public FilterScreen(FilterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = FilterMenu.MACHINE_HEIGHT + 94;
        titleLabelX = 6;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = FilterMenu.MACHINE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        mode = addRenderableWidget(new LegacyButton(leftPos + 136, topPos + 28, 36, 14, modeText(), () -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, FilterMenu.BUTTON_TOGGLE_MODE);
            }
        }));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        mode.setMessage(modeText());
    }

    private Component modeText() {
        return Component.translatable(menu.modeSelected() == ItemFilterSettings.Mode.WHITELIST
                ? "gui.clayium_neoforged.item_filter.whitelist" : "gui.clayium_neoforged.item_filter.blacklist");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + FilterMenu.MACHINE_HEIGHT,
                0, 0, 176, 94, 256, 256);
        for (int index = 0; index < ItemFilterSettings.SIZE; index++) {
            LegacyGuiGraphics.slot(graphics, leftPos + menu.slots.get(index).x, topPos + menu.slots.get(index).y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                LegacyGuiGraphics.TEXT_COLOR, false);
    }
}
