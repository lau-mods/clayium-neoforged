package net.claustra01.clayium.client.screen;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.menu.ClayWorkTableMenu;
import net.claustra01.clayium.network.GuiButtonPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Screen for Clay Work Table (Tier 0) featuring 6 action buttons and live progress bar.
 */
public class ClayWorkTableScreen extends AbstractContainerScreen<ClayWorkTableMenu> {
    private static final ResourceLocation TEXTURE = ClayiumMod.id("textures/gui/clayworktable.png");
    private final ClayPictureButton[] actionButtons = new ClayPictureButton[6];

    public ClayWorkTableScreen(ClayWorkTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 6;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;
        int[] texXs = {176, 192, 208, 224, 240, 176};
        int[] texYs = {32, 32, 32, 32, 32, 80};
        for (int i = 0; i < 6; i++) {
            int buttonId = i + 1;
            this.actionButtons[i] = new ClayPictureButton(
                    buttonId,
                    x + 40 + i * 16,
                    y + 52,
                    16,
                    16,
                    TEXTURE,
                    texXs[i],
                    texYs[i],
                    id -> PacketDistributor.sendToServer(new GuiButtonPayload(this.menu.containerId, id))
            );
            addRenderableWidget(this.actionButtons[i]);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        int progress = this.menu.getCookProgressScaled(80);
        if (progress > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 48, this.topPos + 29, 176, 0, progress, 16, 256, 256);
        }
        for (int i = 0; i < 6; i++) {
            if (this.actionButtons[i] != null) {
                this.actionButtons[i].active = this.menu.canPushButton(i + 1);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
