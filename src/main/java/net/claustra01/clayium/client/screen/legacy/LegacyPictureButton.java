package net.claustra01.clayium.client.screen.legacy;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Original GuiPictureButton: 16x16 cell at (u, v + 16 * state); state 0 disabled, 1 normal, 2 hover. */
public final class LegacyPictureButton extends AbstractButton {
    public static final ResourceLocation BUTTONS = ClayiumMod.id("textures/gui/button.png");

    private final ResourceLocation texture;
    private final Runnable press;
    private int u;
    private int v;

    public LegacyPictureButton(int x, int y, ResourceLocation texture, int u, int v, Component narration, Runnable press) {
        super(x, y, 16, 16, narration);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.press = press;
    }

    public void setTexturePosition(int u, int v) {
        this.u = u;
        this.v = v;
    }

    @Override
    public void onPress() {
        press.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int state = !active ? 0 : isHoveredOrFocused() ? 2 : 1;
        graphics.blit(texture, getX(), getY(), u, v + state * 16, 16, 16, 256, 256);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
