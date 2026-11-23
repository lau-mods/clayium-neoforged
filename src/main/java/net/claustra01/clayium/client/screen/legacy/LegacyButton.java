package net.claustra01.clayium.client.screen.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class LegacyButton extends AbstractButton {
    private final Runnable press;

    public LegacyButton(int x, int y, int width, int height, Component message, Runnable press) {
        super(x, y, width, height, message);
        this.press = press;
    }

    @Override
    public void onPress() {
        press.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        String sprite = !active ? "widget/button_disabled"
                : isHoveredOrFocused() ? "widget/button_highlighted" : "widget/button";
        graphics.blitSprite(ResourceLocation.withDefaultNamespace(sprite),
                getX(), getY(), getWidth(), getHeight());
        int color = active ? 0xFFFFFF : 0xA0A0A0;
        graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
