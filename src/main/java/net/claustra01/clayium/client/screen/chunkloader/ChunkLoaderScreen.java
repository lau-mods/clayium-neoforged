package net.claustra01.clayium.client.screen.chunkloader;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.world.chunkloading.ChunkLoaderBlockEntity;
import net.claustra01.clayium.world.chunkloading.ChunkLoaderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

public final class ChunkLoaderScreen extends AbstractContainerScreen<ChunkLoaderMenu> {
    public ChunkLoaderScreen(ChunkLoaderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 112;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(
                ClayiumMod.id("textures/block/machinehull-5.png"),
                leftPos + 12, topPos + 30, 32, 32, 0, 0, 16, 16, 16, 16);
        graphics.blit(
                ClayiumMod.id("textures/block/chunkloader.png"),
                leftPos + 12, topPos + 30, 32, 32, 0, 0, 16, 16, 16, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, 0x404040, false);
        Component status = Component.translatable(
                "gui.clayium.chunk_loader.status." + menu.ticketStatus().getSerializedName());
        List<FormattedCharSequence> statusLines = font.split(status, imageWidth - 52 - 8);
        for (int line = 0; line < statusLines.size(); line++) {
            graphics.drawString(font, statusLines.get(line), 52, 32 + line * font.lineHeight, statusColor(), false);
        }
        graphics.drawString(
                font, Component.translatable("gui.clayium.chunk_loader.range"),
                52, 36 + statusLines.size() * font.lineHeight, 0x404040, false);
        if (menu.ticketStatus() == ChunkLoaderBlockEntity.TicketStatus.UNAVAILABLE) {
            List<FormattedCharSequence> rejectionLines = font.split(
                    Component.translatable("gui.clayium.chunk_loader.rejected"), imageWidth - 16);
            for (int line = 0; line < rejectionLines.size(); line++) {
                graphics.drawString(font, rejectionLines.get(line), 8, 78 + line * font.lineHeight, 0xAA2020, false);
            }
        }
    }

    private int statusColor() {
        return switch (menu.ticketStatus()) {
            case ACTIVE -> 0x168A16;
            case UNAVAILABLE -> 0xAA2020;
            case WAITING -> 0x6A6A6A;
        };
    }
}
