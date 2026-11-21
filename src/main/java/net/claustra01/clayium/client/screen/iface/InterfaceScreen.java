package net.claustra01.clayium.client.screen.iface;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.client.screen.logistics.LogisticsBackground;
import net.claustra01.clayium.machine.iface.InterfaceBlockEntity;
import net.claustra01.clayium.machine.iface.InterfaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class InterfaceScreen extends AbstractContainerScreen<InterfaceMenu> {
    public InterfaceScreen(InterfaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 112;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LogisticsBackground.draw(graphics, leftPos, topPos, imageWidth, imageHeight);
        InterfaceBlockEntity blockEntity = menu.blockEntity();
        if (blockEntity != null) {
            ResourceLocation hull = ClayiumMod.id(
                    "textures/block/machinehull-" + Math.max(0, menu.tier() - 1) + ".png");
            ResourceLocation front = ClayiumMod.id(
                    "textures/block/" + blockEntity.interfaceType().texture() + ".png");
            graphics.blit(hull, leftPos + 8, topPos + 24, 32, 32, 0, 0, 16, 16, 16, 16);
            graphics.blit(front, leftPos + 8, topPos + 24, 32, 32, 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        drawFit(graphics, title, 7, 7, 162, 0x404040);
        graphics.drawString(font, Component.translatable("gui.clayium.interface.tier", menu.tier()), 48, 25, 0x404040, false);
        Component status = Component.translatable(
                "gui.clayium.interface.status." + menu.status().name().toLowerCase());
        graphics.drawString(font, status, 48, 37, statusColor(), false);

        Component dimension = Component.literal("-");
        InterfaceBlockEntity blockEntity = menu.blockEntity();
        if (blockEntity != null && blockEntity.target() != null) {
            dimension = Component.literal(blockEntity.target().dimension().location().toString());
        }
        graphics.drawString(font, Component.translatable("gui.clayium.interface.target"), 8, 64, 0x404040, false);
        drawFit(graphics, dimension, 8, 76, 160, 0x404040);
        Component position = blockEntity != null && blockEntity.target() != null
                ? Component.literal(blockEntity.target().pos().getX() + ", "
                        + blockEntity.target().pos().getY() + ", " + blockEntity.target().pos().getZ())
                : Component.literal("-");
        drawFit(graphics, position, 8, 89, 160, 0x404040);
    }

    private int statusColor() {
        return switch (menu.status()) {
            case CONNECTED -> 0x168A16;
            case DISCONNECTED -> 0xAA2020;
            case UNLINKED -> 0x6A6A6A;
        };
    }

    private void drawFit(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        int textWidth = font.width(text);
        float scale = textWidth <= width ? 1.0F : (float)width / textWidth;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, (int)(x / scale), (int)(y / scale), color, false);
        graphics.pose().popPose();
    }
}
