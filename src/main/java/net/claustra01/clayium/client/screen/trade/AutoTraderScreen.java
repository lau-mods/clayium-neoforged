package net.claustra01.clayium.client.screen.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.client.screen.logistics.LogisticsBackground;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.trade.AutoTraderBlockEntity;
import net.claustra01.clayium.machine.trade.AutoTraderMenu;
import net.claustra01.clayium.machine.trade.TradeSelectPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class AutoTraderScreen extends AbstractContainerScreen<AutoTraderMenu> {
    private static final ResourceLocation PLAYER_INVENTORY = ClayiumMod.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = ClayiumMod.id("textures/gui/slot.png");
    private static final ResourceLocation TRADE_ARROWS = ClayiumMod.id("textures/gui/autotrader.png");

    private final List<Button> offerButtons = new ArrayList<>();

    public AutoTraderScreen(AutoTraderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 248;
        imageHeight = 220;
        inventoryLabelY = 129;
    }

    @Override
    protected void init() {
        super.init();
        for (int index = 0; index < AutoTraderBlockEntity.MAX_VISIBLE_OFFERS; index++) {
            int offerIndex = index;
            Button button = addRenderableWidget(Button.builder(
                            Component.empty(), pressed -> selectOffer(offerIndex))
                    .bounds(leftPos + 151, topPos + 29 + index * 9, 90, 9)
                    .build());
            offerButtons.add(button);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        for (int index = 0; index < offerButtons.size(); index++) {
            Button button = offerButtons.get(index);
            ItemStack result = menu.offerResult(index);
            button.visible = index < menu.offerCount() && !result.isEmpty();
            button.active = button.visible && index != menu.selectedOffer();
            if (button.visible) {
                String marker = index == menu.selectedOffer() ? "▶ " : "  ";
                String name = result.getHoverName().getString();
                button.setMessage(Component.literal(marker + font.plainSubstrByWidth(name, 70)));
            }
        }
    }

    private void selectOffer(int index) {
        PacketDistributor.sendToServer(new TradeSelectPayload(menu.pos(), index));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LogisticsBackground.draw(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(PLAYER_INVENTORY, leftPos, topPos + 128, 0, 0, 176, 90);
        graphics.blit(TRADE_ARROWS, leftPos + 91, topPos + 28, 22, 15, 86, 24, 22, 15, 256, 256);
        graphics.blit(TRADE_ARROWS, leftPos + 91, topPos + 50, 22, 15, 86, 54, 22, 15, 256, 256);
        for (int[] slot : new int[][] {{43, 26}, {43, 48}, {115, 37}, {128, 76}}) {
            graphics.blit(SLOT, leftPos + slot[0], topPos + slot[1], 0, 0, 18, 18);
        }
        renderOffer(graphics, menu.selectedCostA(), leftPos + 73, topPos + 27);
        renderOffer(graphics, menu.selectedCostB(), leftPos + 73, topPos + 49);
        renderOffer(graphics, menu.selectedResult(), leftPos + 134, topPos + 38);
    }

    private void renderOffer(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            graphics.renderFakeItem(stack, x, y);
            graphics.renderItemDecorations(font, stack, x, y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String titleText = font.plainSubstrByWidth(title.getString(), 130);
        graphics.drawString(font, titleText, 7, 6, 0x404040, false);
        graphics.drawString(font, Component.literal("Tier " + menu.tier()), 105, 6, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.auto_trader.offers"), 151, 17, 0x404040, false);
        graphics.drawString(font, Component.translatable(
                "gui.clayium.auto_trader.state." + menu.state().name().toLowerCase(Locale.ROOT)),
                7, 88, menu.state() == AutoTraderBlockEntity.State.TRADED ? 0x168A16 : 0x404040, false);
        graphics.drawString(font, Component.translatable(
                "gui.clayium.auto_trader.uses", menu.selectedUses(), menu.selectedMaxUses()),
                7, 103, 0x404040, false);
        graphics.drawString(font, Component.translatable(
                "gui.clayium.auto_trader.energy", ClayEnergy.ofInternal(menu.energyPerTrade()).format()),
                7, 77, 0x404040, false);
        graphics.drawString(font, Component.translatable(
                "gui.clayium.auto_trader.stored", ClayEnergy.ofInternal(menu.storedEnergy()).format()),
                7, 66, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
