package net.claustra01.clayium.client.screen.logistics.multitrack;

import java.util.EnumMap;
import java.util.Map;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.logistics.distributor.DistributorControlPayload;
import net.claustra01.clayium.logistics.distributor.DistributorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DistributorScreen extends AbstractContainerScreen<DistributorMenu> {
    private final Map<Direction, Button> buttons = new EnumMap<>(Direction.class);
    public DistributorScreen(DistributorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageWidth = 230; imageHeight = 212; inventoryLabelX = 35; inventoryLabelY = 121;
    }


    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void init() {
        super.init(); int index = 0;
        for (Direction direction : Direction.values()) {
            Button button = addRenderableWidget(Button.builder(text(direction), pressed -> {
                var next = menu.faceMode(direction).next();
                PacketDistributor.sendToServer(new DistributorControlPayload(menu.pos(), direction.get3DDataValue(), next.ordinal()));
            }).bounds(leftPos + 8 + index * 36, topPos + 24, 34, 16).build());
            buttons.put(direction, button); index++;
        }
    }
    @Override protected void containerTick() { super.containerTick(); buttons.forEach((direction, button) -> button.setMessage(text(direction))); }
    private Component text(Direction direction) {
        String mode = switch (menu.faceMode(direction)) { case INPUT -> "I"; case OUTPUT -> "O"; case DISABLED -> "X"; };
        return Component.literal(direction.getName().substring(0, 1).toUpperCase() + ":" + mode);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos + 27, topPos + 121, 0, 0, 176, 86);
        for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++)
            graphics.blit(LegacyGuiGraphics.SLOT, leftPos + 61 + column * 18, topPos + 53 + row * 18, 0, 0, 18, 18);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 7, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
