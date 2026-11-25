package net.claustra01.clayium.client.screen.logistics.multitrack;

import java.util.EnumMap;
import java.util.Map;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.multitrack.MultitrackBufferBlockEntity;
import net.claustra01.clayium.logistics.multitrack.MultitrackBufferMenu;
import net.claustra01.clayium.logistics.multitrack.MultitrackControlPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class MultitrackBufferScreen extends AbstractContainerScreen<MultitrackBufferMenu> {
    private final Map<Direction, Button> faceButtons = new EnumMap<>(Direction.class);

    public MultitrackBufferScreen(MultitrackBufferMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 230;
        inventoryLabelX = 35;
        inventoryLabelY = 132;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("◀"), button -> select(menu.selectedTrack() - 1))
                .bounds(leftPos + 8, topPos + 22, 20, 16).build());
        addRenderableWidget(Button.builder(Component.literal("▶"), button -> select(menu.selectedTrack() + 1))
                .bounds(leftPos + 202, topPos + 22, 20, 16).build());
        int index = 0;
        for (Direction direction : Direction.values()) {
            Button button = addRenderableWidget(Button.builder(faceText(direction), pressed -> cycleFace(direction))
                    .bounds(leftPos + 8 + index * 36, topPos + 40, 34, 16).build());
            faceButtons.put(direction, button);
            index++;
        }
    }

    @Override protected void containerTick() {
        super.containerTick();
        faceButtons.forEach((direction, button) -> button.setMessage(faceText(direction)));
    }

    private void select(int track) {
        PacketDistributor.sendToServer(new MultitrackControlPayload(menu.bufferPos(),
                MultitrackControlPayload.SELECT_TRACK, Math.floorMod(track, menu.trackCount())));
    }

    private void cycleFace(Direction direction) {
        BufferBlockEntity.FaceMode next = menu.faceMode(direction).next();
        PacketDistributor.sendToServer(new MultitrackControlPayload(menu.bufferPos(),
                direction.get3DDataValue(), next.ordinal()));
        if (next != BufferBlockEntity.FaceMode.DISABLED) {
            PacketDistributor.sendToServer(new MultitrackControlPayload(menu.bufferPos(),
                    MultitrackControlPayload.SELECT_FACE_TRACK,
                    direction.get3DDataValue() * MultitrackBufferBlockEntity.TRACKS + menu.selectedTrack()));
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos + 27, topPos + 132, 0, 0, 176, 86);
        for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++)
            graphics.blit(LegacyGuiGraphics.SLOT, leftPos + 61 + column * 18, topPos + 60 + row * 18, 0, 0, 18, 18);
        graphics.blit(LegacyGuiGraphics.SLOT, leftPos + 125, topPos + 78, 0, 0, 18, 18);
        int color = net.minecraft.world.item.DyeColor.byId(menu.selectedTrack()).getTextureDiffuseColor() | 0xFF000000;
        graphics.fill(leftPos + 34, topPos + 24, leftPos + 46, topPos + 35, color);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component color = Component.translatable("color.minecraft."
                + net.minecraft.world.item.DyeColor.byId(menu.selectedTrack()).getName());
        Component track = Component.translatable("gui.clayium.multitrack.track", menu.selectedTrack() + 1, color);
        graphics.drawString(font, track, (imageWidth - font.width(track)) / 2, 25, 0x404040, false);
        graphics.drawString(font, title, 8, 7, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private Component faceText(Direction direction) {
        String mode = switch (menu.faceMode(direction)) {
            case INPUT -> "I";
            case OUTPUT -> "O";
            case DISABLED -> "X";
            case INPUT_OUTPUT -> "B";
        };
        return Component.literal(direction.getName().substring(0, 1).toUpperCase()
                + ":" + mode + (menu.faceTrack(direction) + 1));
    }
}
