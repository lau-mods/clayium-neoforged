package net.claustra01.clayium.client.screen;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.menu.AreaMinerMenu;
import net.claustra01.clayium.util.ClayiumFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for Area Miner / Area Collector / Advanced Area Miner / Area Replacer.
 */
public class AreaMinerScreen extends ClayiumBaseScreen<AreaMinerMenu> {
    public AreaMinerScreen(AreaMinerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int[] texXs = {16, 32, 64, 48};
        boolean vertical = this.menu.isReplaceMode();
        int shiftX = (vertical || this.menu.getTier() >= 9) ? -10 : 0;

        for (int i = 0; i < 4; i++) {
            int bx;
            int by;
            if (vertical) {
                bx = this.leftPos + shiftX + 16;
                by = this.topPos + 16 + i * 18;
            } else {
                bx = this.leftPos + shiftX + 16 + (i % 2) * 18;
                by = this.topPos + 16 + (i / 2) * 18;
            }
            addRenderableWidget(new ClayPictureButton(
                    i,
                    bx,
                    by,
                    16,
                    16,
                    BUTTON_TEXTURE,
                    texXs[i],
                    0,
                    this::sendGuiButton
            ));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        drawClayEnergyText(guiGraphics, this.menu.getClayEnergy());
        guiGraphics.drawString(
                this.font,
                Component.translatable("gui.Common.clayLaser", ClayiumFormatting.formatLaser(this.menu.getLaserEnergy())),
                64,
                this.menu.machineGuiSizeY - 12,
                0x404040,
                false
        );
        if (this.menu.isReplaceMode() || this.menu.getTier() >= 9) {
            guiGraphics.drawString(this.font, Component.translatable("gui.AreaMiner.harvest"), 128, 21, 0x404040, false);
            guiGraphics.drawString(this.font, Component.translatable("gui.AreaMiner.fortune"), 128, 39, 0x404040, false);
            guiGraphics.drawString(this.font, Component.translatable("gui.AreaMiner.silktouch"), 128, 57, 0x404040, false);
        }
    }

    @Nullable
    @Override
    protected ClayContainerBlockEntity getEnergyPreviewSource() {
        return this.menu.getMiner();
    }
}
