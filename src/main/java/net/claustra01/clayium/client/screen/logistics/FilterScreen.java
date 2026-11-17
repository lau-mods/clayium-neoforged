package net.claustra01.clayium.client.screen.logistics;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.logistics.filter.FilterControlPayload;
import net.claustra01.clayium.logistics.filter.FilterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FilterScreen extends AbstractContainerScreen<FilterMenu> {
    private static final ResourceLocation INVENTORY = ClayiumMod.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = ClayiumMod.id("textures/gui/slot.png");
    private Button mode;
    private Button type;

    public FilterScreen(FilterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 200;
        inventoryLabelX = 8;
        inventoryLabelY = 108;
    }

    @Override
    protected void init() {
        super.init();
        mode = addRenderableWidget(Button.builder(modeText(), button -> send(0))
                .bounds(leftPos + 8, topPos + 30, 48, 18).build());
        type = addRenderableWidget(Button.builder(typeText(), button -> send(1))
                .bounds(leftPos + 58, topPos + 30, 50, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Tag ▶"), button -> send(2))
                .bounds(leftPos + 110, topPos + 30, 56, 18).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> send(3))
                .bounds(leftPos + 32, topPos + 77, 20, 18).build());
        addRenderableWidget(Button.builder(Component.literal("−"), button -> send(4))
                .bounds(leftPos + 54, topPos + 77, 20, 18).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        mode.setMessage(modeText());
        type.setMessage(typeText());
    }

    private Component modeText() {
        return Component.translatable("gui.clayium.buffer.mode." + menu.modeSelected().getSerializedName());
    }

    private Component typeText() {
        return Component.translatable("gui.clayium.buffer.rule." + menu.typeSelected().getSerializedName());
    }

    private void send(int action) {
        PacketDistributor.sendToServer(new FilterControlPayload(action));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LogisticsBackground.draw(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(INVENTORY, leftPos, topPos + 110, 0, 0, 176, 90);
        graphics.blit(SLOT, leftPos + 7, topPos + 76, 0, 0, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.buffer.conditions", menu.ruleCount()),
                8, 56, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
