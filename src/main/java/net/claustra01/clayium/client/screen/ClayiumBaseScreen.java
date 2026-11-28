package net.claustra01.clayium.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.menu.ClaySlot;
import net.claustra01.clayium.menu.ClayiumBaseMenu;
import net.claustra01.clayium.menu.SlotTextureKind;
import net.claustra01.clayium.network.GuiButtonPayload;
import net.claustra01.clayium.util.ClayiumFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Base screen matching 1.7.10 {@code GuiTemp}, {@code GuiClayContainerTemp}, and {@code GuiClayEnergyTemp}:
 * 9-slice dynamic sizing, per-slot sprite rendering, optional overlay texture, CE/Overclock text,
 * and Shift-held energy slot preview.
 */
public class ClayiumBaseScreen<T extends ClayiumBaseMenu> extends AbstractContainerScreen<T> {
    protected static final ResourceLocation SLOT_TEXTURE = ClayiumMod.id("textures/gui/slot.png");
    protected static final ResourceLocation BUTTON_TEXTURE = ClayiumMod.id("textures/gui/button.png");
    protected static final ResourceLocation PROGRESS_TEXTURE = ClayiumMod.id("textures/gui/progressbarfurnace.png");
    protected static final ResourceLocation GUI_BACK = ClayiumMod.id("textures/gui/gui_back.png");
    protected static final ResourceLocation GUI_TOP = ClayiumMod.id("textures/gui/gui_t.png");
    protected static final ResourceLocation GUI_BOTTOM = ClayiumMod.id("textures/gui/gui_b.png");
    protected static final ResourceLocation GUI_LEFT = ClayiumMod.id("textures/gui/gui_l.png");
    protected static final ResourceLocation GUI_RIGHT = ClayiumMod.id("textures/gui/gui_r.png");
    protected static final ResourceLocation GUI_TL = ClayiumMod.id("textures/gui/gui_tl.png");
    protected static final ResourceLocation GUI_TR = ClayiumMod.id("textures/gui/gui_tr.png");
    protected static final ResourceLocation GUI_BL = ClayiumMod.id("textures/gui/gui_bl.png");
    protected static final ResourceLocation GUI_BR = ClayiumMod.id("textures/gui/gui_br.png");
    protected static final ResourceLocation GUI_PLAYER = ClayiumMod.id("textures/gui/gui_playerinventory.png");

    @Nullable
    protected ResourceLocation overlayTexture = null;

    public ClayiumBaseScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = menu.machineGuiSizeX;
        this.imageHeight = menu.getTotalHeight();
        this.titleLabelX = 6;
        this.titleLabelY = 6;
        this.inventoryLabelX = menu.playerSlotOffsetX + 8;
        this.inventoryLabelY = menu.playerSlotOffsetY;
    }

    protected void sendGuiButton(int buttonId) {
        PacketDistributor.sendToServer(new GuiButtonPayload(this.menu.containerId, buttonId));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        guiGraphics.blit(GUI_BACK, x + 4, y + 4, w - 8, h - 8, 0, 0, 8, 8, 8, 8);
        guiGraphics.blit(GUI_TOP, x + 4, y, w - 8, 4, 0, 0, 1, 4, 1, 4);
        guiGraphics.blit(GUI_BOTTOM, x + 4, y + h - 4, w - 8, 4, 0, 0, 1, 4, 1, 4);
        guiGraphics.blit(GUI_LEFT, x, y + 4, 4, h - 8, 0, 0, 4, 1, 4, 1);
        guiGraphics.blit(GUI_RIGHT, x + w - 4, y + 4, 4, h - 8, 0, 0, 4, 1, 4, 1);
        guiGraphics.blit(GUI_TL, x, y, 0, 0, 4, 4, 4, 4);
        guiGraphics.blit(GUI_TR, x + w - 4, y, 0, 0, 4, 4, 4, 4);
        guiGraphics.blit(GUI_BL, x, y + h - 4, 0, 0, 4, 4, 4, 4);
        guiGraphics.blit(GUI_BR, x + w - 4, y + h - 4, 0, 0, 4, 4, 4, 4);
        guiGraphics.blit(
                GUI_PLAYER,
                x + this.menu.playerSlotOffsetX,
                y + this.menu.playerSlotOffsetY,
                0,
                0,
                176,
                94,
                256,
                256
        );

        int machineSlots = this.menu.getMachineSlotCount();
        for (int i = 0; i < machineSlots && i < this.menu.slots.size(); i++) {
            Slot slot = this.menu.slots.get(i);
            if (!slot.isActive()) {
                continue;
            }
            SlotTextureKind kind = slot instanceof ClaySlot claySlot ? claySlot.getTextureKind() : SlotTextureKind.SMALL;
            guiGraphics.blit(
                    SLOT_TEXTURE,
                    x + slot.x - (kind.sizeX - 16) / 2,
                    y + slot.y - (kind.sizeY - 16) / 2,
                    kind.textureX,
                    kind.textureY,
                    kind.sizeX,
                    kind.sizeY,
                    256,
                    256
            );
        }

        if (this.overlayTexture != null) {
            guiGraphics.blit(
                    this.overlayTexture,
                    x,
                    y,
                    0,
                    0,
                    this.menu.machineGuiSizeX,
                    this.menu.machineGuiSizeY,
                    256,
                    256
            );
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.menu.drawInventoryName()) {
            guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        }
        if (this.menu.drawPlayerInventoryName()) {
            guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderClayEnergyPreview(guiGraphics, mouseX, mouseY);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Nullable
    protected ClayContainerBlockEntity getEnergyPreviewSource() {
        return null;
    }

    protected void renderClayEnergyPreview(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!Screen.hasShiftDown()) {
            return;
        }
        ClayContainerBlockEntity container = getEnergyPreviewSource();
        if (container == null || container.clayEnergySlot < 0 || container.clayEnergySlot >= container.getInventorySize()) {
            return;
        }
        ItemStack stack = container.getStack(container.clayEnergySlot);
        if (stack.isEmpty()) {
            return;
        }
        int itemX = this.leftPos - 12;
        int itemY = this.topPos + this.menu.machineGuiSizeY - 16;
        guiGraphics.renderItem(stack, itemX, itemY);
        guiGraphics.renderItemDecorations(this.font, stack, itemX, itemY);
        if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
            guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
        }
    }

    protected void drawClayEnergyText(GuiGraphics guiGraphics, long ceUnits) {
        String formatted = ClayiumFormatting.formatClayEnergy(ceUnits, false);
        guiGraphics.drawString(
                this.font,
                Component.translatable("gui.Common.energy", formatted),
                4,
                this.menu.machineGuiSizeY - 12,
                0x404040,
                false
        );
    }

    protected void drawOverclockText(GuiGraphics guiGraphics, double factor) {
        if (factor == 1.0) {
            return;
        }
        String text = Component.translatable("gui.Common.overclockFactor", ClayiumFormatting.formatCAResonance(factor)).getString();
        int width = this.font.width(text);
        guiGraphics.drawString(
                this.font,
                text,
                this.menu.machineGuiSizeX - width - 6,
                6,
                0x404040,
                false
        );
    }
}
