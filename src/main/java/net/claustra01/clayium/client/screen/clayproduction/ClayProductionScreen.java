package net.claustra01.clayium.client.screen.clayproduction;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.clayproduction.ClayProductionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ClayProductionScreen extends AbstractContainerScreen<ClayProductionMenu> {
    public ClayProductionScreen(ClayProductionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = menu.machineHeight() + 94;
        inventoryLabelY = menu.machineHeight();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** Original GuiClayMachines, or GuiClayEnergyTemp over ContainerAutoClayCondenser. */
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + menu.machineHeight(),
                0, 0, 176, 94, 256, 256);
        if (menu.automatic()) {
            int grid = ClayProductionMenu.AUTO_ROWS * ClayProductionMenu.AUTO_COLUMNS;
            for (int index = 0; index < grid; index++) {
                LegacyGuiGraphics.slot(graphics, leftPos + menu.slots.get(index).x, topPos + menu.slots.get(index).y);
            }
            LegacyGuiGraphics.slot(graphics, leftPos + menu.slots.get(grid).x, topPos + menu.slots.get(grid).y,
                    LegacyGuiGraphics.SlotTexture.ENERGETIC_CLAY);
            return;
        }
        LegacyGuiGraphics.largeSlot(graphics, leftPos + 44, topPos + 35);
        LegacyGuiGraphics.largeSlot(graphics, leftPos + 116, topPos + 35);
        LegacyGuiGraphics.progress(graphics, leftPos + (imageWidth - 24) / 2, topPos + 35,
                menu.progress(), menu.requiredProgress());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 6, 6, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium_neoforged.machine.energy",
                ClayEnergy.ofInternal(menu.displayedEnergy()).formatGui()), 4, menu.machineHeight() - 12,
                0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
