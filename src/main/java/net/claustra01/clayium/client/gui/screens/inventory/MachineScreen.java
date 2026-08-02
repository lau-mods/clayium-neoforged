/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.energy.ClayEnergyFormatter;
import net.claustra01.clayium.machine.MachineLayout;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.machine.ManufacturingMachineCatalog;
import net.claustra01.clayium.machine.SpecializedMachineCatalog;
import net.claustra01.clayium.world.inventory.MachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/** Original-style component-composed GUI shared by the first Clayium machines. */
public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final ResourceLocation BACK = Clayium.id("textures/gui/gui_back.png");
    private static final ResourceLocation TOP = Clayium.id("textures/gui/gui_t.png");
    private static final ResourceLocation BOTTOM = Clayium.id("textures/gui/gui_b.png");
    private static final ResourceLocation LEFT = Clayium.id("textures/gui/gui_l.png");
    private static final ResourceLocation RIGHT = Clayium.id("textures/gui/gui_r.png");
    private static final ResourceLocation TOP_LEFT = Clayium.id("textures/gui/gui_tl.png");
    private static final ResourceLocation TOP_RIGHT = Clayium.id("textures/gui/gui_tr.png");
    private static final ResourceLocation BOTTOM_LEFT = Clayium.id("textures/gui/gui_bl.png");
    private static final ResourceLocation BOTTOM_RIGHT = Clayium.id("textures/gui/gui_br.png");
    private static final ResourceLocation PLAYER_INVENTORY =
            Clayium.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = Clayium.id("textures/gui/slot.png");
    private static final ResourceLocation PROGRESS =
            Clayium.id("textures/gui/progressbarfurnace.png");
    private static final ResourceLocation BUTTON = Clayium.id("textures/gui/button.png");
    private ManualCraftButton manualCraftButton;

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = menu.machineHeight() + 94;
        titleLabelX = 6;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = menu.machineHeight();
    }

    public ResourceLocation machineId() {
        if (!(title.getContents() instanceof TranslatableContents translatable)) {
            return ClayiumMachineIds.CLAY_BENDING_MACHINE;
        }
        String prefix = "block." + Clayium.MODID + ".";
        String key = translatable.getKey();
        if (!key.startsWith(prefix)) {
            return ClayiumMachineIds.CLAY_BENDING_MACHINE;
        }
        String blockId = key.substring(prefix.length());
        if (blockId.equals("clay_bending_machine")) {
            return ClayiumMachineIds.CLAY_BENDING_MACHINE;
        }
        if (blockId.equals("elemental_milling_machine")) {
            return ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE;
        }
        if (blockId.equals("clay_blast_furnace")) {
            return ClayiumMachineIds.CLAY_BLAST_FURNACE;
        }
        ResourceLocation manufacturing = ManufacturingMachineCatalog.ENTRIES.stream()
                .filter(entry -> entry.blockId().equals(blockId))
                .map(ManufacturingMachineCatalog.Entry::machineId)
                .findFirst()
                .orElse(null);
        if (manufacturing != null) return manufacturing;
        return SpecializedMachineCatalog.ENTRIES.stream()
                .filter(entry -> entry.blockId().equals(blockId))
                .map(SpecializedMachineCatalog.Entry::machineId)
                .findFirst()
                .orElse(ClayiumMachineIds.CLAY_BENDING_MACHINE);
    }

    @Override
    protected void init() {
        super.init();
        manualCraftButton = addRenderableWidget(new ManualCraftButton(
                leftPos + 80,
                topPos + 56,
                button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                    }
                }));
        manualCraftButton.visible = menu.tier() <= 2;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (manualCraftButton != null) {
            manualCraftButton.visible = menu.tier() <= 2;
            manualCraftButton.active =
                    menu.stopReason() == net.claustra01.clayium.world.level.block.entity.MachineBlockEntity.StopReason.RUNNING
                            || menu.stopReason()
                                    == net.claustra01.clayium.world.level.block.entity.MachineBlockEntity.StopReason.INSUFFICIENT_ENERGY;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos + 4
                && mouseX < leftPos + 76
                && mouseY >= topPos + 58
                && mouseY < topPos + 70) {
            graphics.renderTooltip(
                    font,
                    Component.translatable(
                            "gui.clayium_neoforged.stop_reason."
                                    + menu.stopReason().name().toLowerCase()),
                    mouseX,
                    mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int machineHeight = menu.machineHeight();
        tile(graphics, BACK, leftPos + 4, topPos + 4, 168, imageHeight - 8, 8, 8);
        tile(graphics, TOP, leftPos + 4, topPos, 168, 4, 1, 4);
        tile(graphics, BOTTOM, leftPos + 4, topPos + imageHeight - 4, 168, 4, 1, 4);
        tile(graphics, LEFT, leftPos, topPos + 4, 4, imageHeight - 8, 4, 1);
        tile(graphics, RIGHT, leftPos + 172, topPos + 4, 4, imageHeight - 8, 4, 1);
        blitWhole(graphics, TOP_LEFT, leftPos, topPos, 4, 4);
        blitWhole(graphics, TOP_RIGHT, leftPos + 172, topPos, 4, 4);
        blitWhole(graphics, BOTTOM_LEFT, leftPos, topPos + imageHeight - 4, 4, 4);
        blitWhole(graphics, BOTTOM_RIGHT, leftPos + 172, topPos + imageHeight - 4, 4, 4);
        graphics.blit(PLAYER_INVENTORY, leftPos, topPos + machineHeight, 0, 0, 176, 94);
        drawMachineSlots(graphics);
        graphics.blit(PROGRESS, leftPos + 76, topPos + 35, 0, 0, 24, 17);
        int width = menu.totalProgress() <= 0
                ? 0
                : Mth.clamp(menu.progress() * 24 / menu.totalProgress(), 0, 24);
        graphics.blit(PROGRESS, leftPos + 76, topPos + 35, 0, 17, width, 17);
    }

    private void drawMachineSlots(GuiGraphics graphics) {
        if (menu.layout() == MachineLayout.ASSEMBLER || menu.layout() == MachineLayout.CHEMICAL) {
            graphics.blit(SLOT, leftPos + 31, topPos + 34, 32, 0, 18, 18);
            graphics.blit(SLOT, leftPos + 49, topPos + 34, 32, 32, 18, 18);
            if (menu.layout() == MachineLayout.CHEMICAL) {
                graphics.blit(SLOT, leftPos + 109, topPos + 34, 64, 0, 18, 18);
                graphics.blit(SLOT, leftPos + 127, topPos + 34, 64, 32, 18, 18);
                return;
            }
            graphics.blit(SLOT, leftPos + 111, topPos + 30, 0, 32, 26, 26);
            return;
        }
        graphics.blit(SLOT, leftPos + 39, topPos + 30, 0, 32, 26, 26);
        if (menu.layout() == MachineLayout.CENTRIFUGE) {
            int[] outputs = menu.layout().outputSlots(menu.machineTier());
            for (int index = 0; index < outputs.length; index++) {
                graphics.blit(
                        SLOT,
                        leftPos + 115,
                        topPos + 34 + 18 * index - 9 * (outputs.length - 1),
                        0,
                        0,
                        18,
                        18);
            }
            return;
        }
        graphics.blit(SLOT, leftPos + 111, topPos + 30, 0, 32, 26, 26);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(
                font,
                ClayEnergyFormatter.formatPerTick(menu.energyPerTick()),
                4,
                menu.machineHeight() - 24,
                0x404040,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "gui.clayium_neoforged.energy",
                        ClayEnergyFormatter.format(menu.energy())),
                4,
                menu.machineHeight() - 12,
                0x404040,
                false);
        Component tier = Component.translatable("gui.clayium_neoforged.tier", menu.tier());
        graphics.drawString(
                font, tier, imageWidth - 6 - font.width(tier), menu.machineHeight() - 12, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private static void tile(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height,
            int textureWidth,
            int textureHeight) {
        for (int drawY = 0; drawY < height; drawY += textureHeight) {
            for (int drawX = 0; drawX < width; drawX += textureWidth) {
                int drawWidth = Math.min(textureWidth, width - drawX);
                int drawHeight = Math.min(textureHeight, height - drawY);
                graphics.blit(
                        texture,
                        x + drawX,
                        y + drawY,
                        0.0F,
                        0.0F,
                        drawWidth,
                        drawHeight,
                        textureWidth,
                        textureHeight);
            }
        }
    }

    private static void blitWhole(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int textureWidth,
            int textureHeight) {
        graphics.blit(
                texture,
                x,
                y,
                0.0F,
                0.0F,
                textureWidth,
                textureHeight,
                textureWidth,
                textureHeight);
    }

    private static final class ManualCraftButton extends Button {
        private ManualCraftButton(int x, int y, OnPress onPress) {
            super(x, y, 16, 16, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                float partialTick) {
            int state = active ? (isHoveredOrFocused() ? 2 : 1) : 0;
            graphics.blit(BUTTON, getX(), getY(), 0, state * 16, 16, 16);
        }
    }
}
