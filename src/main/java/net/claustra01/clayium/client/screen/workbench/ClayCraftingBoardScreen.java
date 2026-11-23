package net.claustra01.clayium.client.screen.workbench;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.client.screen.RecipeViewerNavigation;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.workbench.ClayCraftingBoardMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;

public final class ClayCraftingBoardScreen extends AbstractContainerScreen<ClayCraftingBoardMenu> {
    private static final ResourceLocation CRAFTING = ClayiumMod.id("textures/gui/claycraftingtable.png");

    public ClayCraftingBoardScreen(ClayCraftingBoardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = menu.machineHeight() + 94;
        inventoryLabelY = menu.machineHeight();
    }

    @Override
    protected void init() {
        super.init();
        // EMI can center its search box in the bottom 21 pixels on short screens.
        if (ModList.get().isLoaded("emi")) {
            topPos = Math.max(0, Math.min(topPos, height - 21 - imageHeight));
        }
        addRenderableWidget(RecipeViewerNavigation.button(leftPos, topPos, RecipeViewerNavigation.CRAFTING));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(CRAFTING, leftPos, topPos, 0, 0, 176, 72, 256, 256);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                drawSlot(graphics, 29 + column * 18, 16 + row * 18);
            }
        }
        for (int slot = 0; slot < menu.adjacentSlotCount(); slot++) {
            drawSlot(graphics, 7 + slot % 9 * 18, 72 + slot / 9 * 18);
        }
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + menu.machineHeight(),
                0, 0, 176, 94, 256, 256);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        LegacyGuiGraphics.slot(graphics, leftPos + x + 1, topPos + y + 1);
    }
}
