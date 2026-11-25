package net.claustra01.clayium.client.screen.trade;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.client.screen.legacy.LegacyButton;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.trade.AutoTraderMenu;
import net.claustra01.clayium.machine.trade.TradeSelectPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class AutoTraderScreen extends AbstractContainerScreen<AutoTraderMenu> {
    private static final ResourceLocation TRADE_ARROWS = ClayiumMod.id("textures/gui/autotrader.png");
    private static final ResourceLocation OUT_OF_STOCK =
            ResourceLocation.withDefaultNamespace("container/villager/out_of_stock");

    private LegacyButton previous;
    private LegacyButton next;

    public AutoTraderScreen(AutoTraderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 182;
        inventoryLabelY = 88;
    }

    @Override
    protected void init() {
        super.init();
        previous = addRenderableWidget(new LegacyButton(leftPos + 17, topPos + 23, 12, 19,
                Component.literal("<"), () -> selectOffer(menu.selectedOffer() - 1)));
        next = addRenderableWidget(new LegacyButton(leftPos + 147, topPos + 23, 12, 19,
                Component.literal(">"), () -> selectOffer(menu.selectedOffer() + 1)));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        previous.active = menu.selectedOffer() > 0;
        next.active = menu.selectedOffer() + 1 < menu.offerCount();
    }

    private void selectOffer(int index) {
        PacketDistributor.sendToServer(new TradeSelectPayload(menu.pos(), index));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + 88, 0, 0, 176, 94);
        graphics.blit(TRADE_ARROWS, leftPos, topPos, 0, 0, 176, 88, 256, 256);
        for (int[] slot : new int[][] {{35, 52}, {61, 52}, {119, 52}, {7, 52}}) {
            graphics.blit(LegacyGuiGraphics.SLOT, leftPos + slot[0], topPos + slot[1], 0, 0, 18, 18);
        }
        renderOffer(graphics, menu.selectedCostA(), leftPos + 36, topPos + 24);
        renderOffer(graphics, menu.selectedCostB(), leftPos + 62, topPos + 24);
        renderOffer(graphics, menu.selectedResult(), leftPos + 120, topPos + 24);
        if (menu.selectedOutOfStock()) {
            graphics.blitSprite(OUT_OF_STOCK, leftPos + 83, topPos + 21, 28, 21);
            graphics.blitSprite(OUT_OF_STOCK, leftPos + 83, topPos + 51, 28, 21);
        }
    }

    private void renderOffer(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            graphics.renderFakeItem(stack, x, y);
            graphics.renderItemDecorations(font, stack, x, y);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderOfferTooltip(graphics, menu.selectedCostA(), 36, 24, mouseX, mouseY);
        renderOfferTooltip(graphics, menu.selectedCostB(), 62, 24, mouseX, mouseY);
        renderOfferTooltip(graphics, menu.selectedResult(), 120, 24, mouseX, mouseY);
    }

    private void renderOfferTooltip(
            GuiGraphics graphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (!stack.isEmpty() && mouseX >= leftPos + x && mouseX < leftPos + x + 16
                && mouseY >= topPos + y && mouseY < topPos + y + 16) {
            graphics.renderTooltip(font, stack, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String titleText = font.plainSubstrByWidth(title.getString(), 110);
        graphics.drawString(font, titleText, 7, 6, 0x404040, false);
        String cost = Component.translatable(
                "gui.clayium.auto_trader.energy", ClayEnergy.ofInternal(menu.energyPerTrade()).format()).getString();
        String stored = Component.translatable(
                "gui.clayium.auto_trader.stored", ClayEnergy.ofInternal(menu.storedEnergy()).format()).getString();
        drawFitted(graphics, cost, 7, 70, 162);
        drawFitted(graphics, stored, 7, 80, 162);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private void drawFitted(GuiGraphics graphics, String label, int x, int y, int maximumWidth) {
        float scale = Math.min(1.0F, maximumWidth / (float)Math.max(1, font.width(label)));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, label, 0, 0, 0x404040, false);
        graphics.pose().popPose();
    }
}
