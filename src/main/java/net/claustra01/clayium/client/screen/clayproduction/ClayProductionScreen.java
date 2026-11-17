package net.claustra01.clayium.client.screen.clayproduction;

import java.util.Locale;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.clayproduction.ClayProductionMenu;
import net.claustra01.clayium.machine.clayproduction.ClayProductionBlockEntity;
import net.claustra01.clayium.machine.clayproduction.ClayProductionType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class ClayProductionScreen extends AbstractContainerScreen<ClayProductionMenu> {
    private static final ResourceLocation BACKGROUND = ClayiumMod.id("textures/gui/back.png");
    private static final ResourceLocation PLAYER_INVENTORY = ClayiumMod.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = ClayiumMod.id("textures/gui/slot.png");
    private static final ResourceLocation PROGRESS = ClayiumMod.id("textures/gui/progressbarfurnace.png");

    public ClayProductionScreen(ClayProductionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 180;
        inventoryLabelY = 89;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(PLAYER_INVENTORY, leftPos, topPos + 88, 0, 0, 176, 90);
        for (int[] slot : new int[][] {{43, 45}, {115, 45}}) {
            graphics.blit(SLOT, leftPos + slot[0], topPos + slot[1], 0, 0, 18, 18);
        }
        if (menu.hasFuelSlot()) {
            graphics.blit(SLOT, leftPos + 43, topPos + 67, 0, 0, 18, 18);
        }
        if (menu.hasLimitSlot()) {
            graphics.blit(SLOT, leftPos + 65, topPos + 67, 0, 0, 18, 18);
        }
        int width = menu.requiredProgress() <= 0
                ? 0
                : Math.min(24, (int)(24.0D * menu.progress() / Math.max(1L, menu.requiredProgress())));
        graphics.blit(PROGRESS, leftPos + 88, topPos + 47, 0, 0, 24, 16);
        if (width > 0) {
            graphics.blit(PROGRESS, leftPos + 88, topPos + 47, 0, 16, width, 16);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String titleText = title.getString().replace("Clay Condenser", "Condenser")
                .replace("Clay Fabricator", "Fabricator");
        if (font.width(titleText) > imageWidth - 14 && titleText.contains(" Mk ")) {
            String suffix = titleText.substring(titleText.lastIndexOf(" Mk "));
            titleText = font.plainSubstrByWidth(titleText, imageWidth - 14 - font.width("…" + suffix))
                    + "…" + suffix;
        } else if (font.width(titleText) > imageWidth - 14) {
            titleText = font.plainSubstrByWidth(titleText, imageWidth - 14 - font.width("…")) + "…";
        }
        graphics.drawString(font, titleText, 7, 6, 0x404040, false);
        Component tier = Component.literal("Tier " + menu.tier());
        graphics.drawString(font, tier, imageWidth - 7 - font.width(tier), 19, 0x404040, false);
        graphics.drawString(font, Component.translatable(
                "gui.clayium.clay_production.state." + menu.state().name().toLowerCase(Locale.ROOT)), 87, 57,
                menu.state() == ClayProductionBlockEntity.State.RUNNING ? 0x168A16 : 0x404040, false);
        graphics.drawString(font, Component.translatable(
                "gui.clayium.clay_production.stage", Math.max(0, menu.productionLevel())), 87, 69, 0x404040, false);
        long remaining = Math.max(0L, menu.requiredProgress() - menu.progress());
        graphics.drawString(font, Component.translatable("gui.clayium.clay_production.remaining", remaining),
                7, 19, 0x404040, false);
        boolean condenser = menu.type().kind() == ClayProductionType.Kind.ENERGETIC_CONDENSER;
        graphics.drawString(font, Component.translatable(condenser
                ? "gui.clayium.clay_production.stored_ce" : "gui.clayium.clay_production.ce_per_tick",
                formatEnergy(condenser ? menu.storedEnergy() : menu.cePerTick())), 7, 32, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private static String formatEnergy(long internal) {
        return String.format(Locale.ROOT, "%.5f", internal / 100_000.0D);
    }
}
