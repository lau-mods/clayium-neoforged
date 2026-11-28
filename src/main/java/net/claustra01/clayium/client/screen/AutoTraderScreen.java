package net.claustra01.clayium.client.screen;

import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.menu.AutoTraderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * Screen for Auto Trader (Tier 8): displays active Villager/Merchant trade offer, out-of-stock marker,
 * next/prev trade selector buttons, and CE readout.
 */
public class AutoTraderScreen extends ClayiumBaseScreen<AutoTraderMenu> {
    @Nullable
    private Button nextButton;
    @Nullable
    private Button prevButton;

    public AutoTraderScreen(AutoTraderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.overlayTexture = ClayiumMod.id("textures/gui/autotrader.png");
    }

    @Override
    protected void init() {
        super.init();
        this.nextButton = Button.builder(Component.literal(">"), btn -> sendGuiButton(AutoTraderMenu.BUTTON_NEXT_RECIPE))
                .bounds(this.leftPos + 147, this.topPos + 23, 12, 20)
                .build();
        this.prevButton = Button.builder(Component.literal("<"), btn -> sendGuiButton(AutoTraderMenu.BUTTON_PREV_RECIPE))
                .bounds(this.leftPos + 17, this.topPos + 23, 12, 20)
                .build();
        addRenderableWidget(this.nextButton);
        addRenderableWidget(this.prevButton);
        updateButtonStates();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtonStates();
    }

    private void updateButtonStates() {
        MerchantOffers offers = this.menu.getOffers();
        int idx = this.menu.getCurrentRecipeIndex();
        if (this.nextButton != null) {
            this.nextButton.active = offers != null && !offers.isEmpty() && idx < offers.size() - 1;
        }
        if (this.prevButton != null) {
            this.prevButton.active = offers != null && !offers.isEmpty() && idx > 0;
        }
    }

    @Nullable
    private MerchantOffer getSelectedOffer() {
        MerchantOffers offers = this.menu.getOffers();
        int idx = this.menu.getCurrentRecipeIndex();
        if (offers != null && idx >= 0 && idx < offers.size()) {
            return offers.get(idx);
        }
        return null;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        updateButtonStates();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        drawClayEnergyText(guiGraphics, this.menu.getClayEnergy());
        MerchantOffer offer = getSelectedOffer();
        if (offer != null && offer.isOutOfStock()) {
            guiGraphics.drawString(this.font, "X", 94, 28, 0xFFCC2222, false);
            guiGraphics.drawString(this.font, "X", 94, 58, 0xFFCC2222, false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        MerchantOffer offer = getSelectedOffer();
        if (offer == null) {
            return;
        }
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        ItemStack result = offer.getResult();

        drawPreviewItem(guiGraphics, costA, this.leftPos + 36, this.topPos + 24);
        if (!costB.isEmpty()) {
            drawPreviewItem(guiGraphics, costB, this.leftPos + 62, this.topPos + 24);
        }
        drawPreviewItem(guiGraphics, result, this.leftPos + 120, this.topPos + 24);

        if (!costA.isEmpty() && isHovering(36, 24, 16, 16, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, costA, mouseX, mouseY);
        } else if (!costB.isEmpty() && isHovering(62, 24, 16, 16, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, costB, mouseX, mouseY);
        } else if (!result.isEmpty() && isHovering(120, 24, 16, 16, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, result, mouseX, mouseY);
        }
    }

    private void drawPreviewItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        guiGraphics.renderItem(stack, x, y);
        guiGraphics.renderItemDecorations(this.font, stack, x, y);
    }
}
