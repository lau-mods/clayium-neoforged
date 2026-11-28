package net.claustra01.clayium.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Custom textured GUI button matching 1.7.10 {@code GuiPictureButton} sprite layout:
 * base at {@code (u, v)}, disabled at {@code (u, v + height)}, hovered at {@code (u, v + 2*height)}.
 */
public class ClayPictureButton extends AbstractButton {
    private final ResourceLocation texture;
    private final int u;
    private final int v;
    private final int buttonId;
    private final IntConsumer onClick;

    public ClayPictureButton(
            int buttonId,
            int x,
            int y,
            int width,
            int height,
            ResourceLocation texture,
            int u,
            int v,
            IntConsumer onClick) {
        super(x, y, width, height, Component.empty());
        this.buttonId = buttonId;
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.onClick = onClick;
    }

    @Override
    public void onPress() {
        if (this.active) {
            this.onClick.accept(this.buttonId);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        int stateOffset = !this.active ? 2 : (isHoveredOrFocused() ? 1 : 0);
        guiGraphics.blit(
                this.texture,
                getX(),
                getY(),
                this.u,
                this.v + stateOffset * this.height,
                this.width,
                this.height,
                256,
                256
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }
}
