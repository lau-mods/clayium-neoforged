package net.claustra01.clayium.client.screen;

import net.claustra01.clayium.menu.PANAdapterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client screen for PAN Adapter: displays 3x3 pattern/preview slots and page navigation for tiers 11..13.
 */
public class PANAdapterScreen extends ClayiumBaseScreen<PANAdapterMenu> {
    public PANAdapterScreen(PANAdapterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        if (this.menu.isMultipage()) {
            addRenderableWidget(Button.builder(
                    Component.literal("<"),
                    b -> sendGuiButton(PANAdapterMenu.BUTTON_PREVIOUS_PAGE))
                    .bounds(this.leftPos + 4, this.topPos + this.menu.playerSlotOffsetY - 60, 16, 16)
                    .build());
            addRenderableWidget(Button.builder(
                    Component.literal(">"),
                    b -> sendGuiButton(PANAdapterMenu.BUTTON_NEXT_PAGE))
                    .bounds(this.leftPos + this.menu.machineGuiSizeX - 20, this.topPos + this.menu.playerSlotOffsetY - 60, 16, 16)
                    .build());
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (this.menu.isMultipage()) {
            guiGraphics.drawString(
                    this.font,
                    this.menu.getPresentPageNum() + "/" + this.menu.getMaxPageNum(),
                    this.menu.machineGuiSizeX - 20,
                    this.menu.playerSlotOffsetY - 32,
                    0x404040,
                    false
            );
        }
    }
}
