package net.claustra01.clayium.client.screen;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.menu.NormalInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for Buffers, Metal Chests (with multi-page controls), CA Collector, Cobblestone Generator, and Salt Extractor.
 */
public class MultipageScreen extends ClayiumBaseScreen<NormalInventoryMenu> {
    public MultipageScreen(NormalInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        if (this.menu.isMultipage()) {
            int baseX = this.leftPos + this.menu.playerSlotOffsetX + 8 + 162;
            int baseY = this.topPos + this.menu.playerSlotOffsetY + 12;
            addRenderableWidget(Button.builder(
                    Component.literal("<"),
                    btn -> sendGuiButton(NormalInventoryMenu.BUTTON_PREVIOUS_PAGE)
            ).bounds(baseX, baseY, 16, 16).build());
            addRenderableWidget(Button.builder(
                    Component.literal(">"),
                    btn -> sendGuiButton(NormalInventoryMenu.BUTTON_NEXT_PAGE)
            ).bounds(baseX + 18, baseY, 16, 16).build());
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (this.menu.isMultipage()) {
            guiGraphics.drawString(
                    this.font,
                    this.menu.getPresentPageNum() + "/" + this.menu.getMaxPageNum(),
                    this.menu.playerSlotOffsetX + 8 + 162,
                    this.menu.playerSlotOffsetY + 32,
                    0x404040,
                    false
            );
        }
        if (this.menu.showsClayEnergy()) {
            drawClayEnergyText(guiGraphics, this.menu.getClayEnergy());
        }
    }

    @Nullable
    @Override
    protected ClayContainerBlockEntity getEnergyPreviewSource() {
        return this.menu.getContainerEntity();
    }
}
