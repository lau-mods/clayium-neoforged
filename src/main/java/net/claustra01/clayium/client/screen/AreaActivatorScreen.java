package net.claustra01.clayium.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.menu.AreaActivatorMenu;
import net.claustra01.clayium.util.ClayiumFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for Activator (T6) and Area Activator (T8) including left work buttons and right target/raytrace/sneak toggles.
 */
public class AreaActivatorScreen extends ClayiumBaseScreen<AreaActivatorMenu> {
    public AreaActivatorScreen(AreaActivatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int[] texXs = {16, 32, 64, 48};
        for (int i = 0; i < 4; i++) {
            addRenderableWidget(new ClayPictureButton(
                    i,
                    this.leftPos + 16 + (i % 2) * 18,
                    this.topPos + 16 + (i / 2) * 18,
                    16,
                    16,
                    BUTTON_TEXTURE,
                    texXs[i],
                    0,
                    this::sendGuiButton
            ));
        }
        addRenderableWidget(new DynamicModeButton(
                AreaActivatorMenu.BUTTON_CYCLE_TARGET,
                this.leftPos + 144,
                this.topPos + 16,
                () -> 16 * (5 + this.menu.getTargetMode())
        ));
        addRenderableWidget(new DynamicModeButton(
                AreaActivatorMenu.BUTTON_TOGGLE_RAYTRACE,
                this.leftPos + 144,
                this.topPos + 34,
                () -> 16 * (8 + (this.menu.isRayTraceEnabled() ? 1 : 0))
        ));
        addRenderableWidget(new DynamicModeButton(
                AreaActivatorMenu.BUTTON_TOGGLE_SNEAK,
                this.leftPos + 144,
                this.topPos + 52,
                () -> 16 * (10 + (this.menu.isSneaking() ? 1 : 0))
        ));
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
    }

    @Nullable
    @Override
    protected ClayContainerBlockEntity getEnergyPreviewSource() {
        return this.menu.getActivator();
    }

    private class DynamicModeButton extends AbstractButton {
        private final int buttonId;
        private final java.util.function.IntSupplier uSupplier;

        DynamicModeButton(int buttonId, int x, int y, java.util.function.IntSupplier uSupplier) {
            super(x, y, 16, 16, Component.empty());
            this.buttonId = buttonId;
            this.uSupplier = uSupplier;
        }

        @Override
        public void onPress() {
            sendGuiButton(this.buttonId);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableBlend();
            int stateOffset = !this.active ? 2 : (isHoveredOrFocused() ? 1 : 0);
            guiGraphics.blit(
                    BUTTON_TEXTURE,
                    getX(),
                    getY(),
                    this.uSupplier.getAsInt(),
                    stateOffset * 16,
                    16,
                    16,
                    256,
                    256
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            defaultButtonNarrationText(narration);
        }
    }
}
