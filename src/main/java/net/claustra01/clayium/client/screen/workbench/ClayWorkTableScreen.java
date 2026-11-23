package net.claustra01.clayium.client.screen.workbench;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.client.screen.RecipeViewerNavigation;
import net.claustra01.clayium.workbench.ClayWorkTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class ClayWorkTableScreen extends AbstractContainerScreen<ClayWorkTableMenu> {
    private static final ResourceLocation TEXTURE = ClayiumMod.id("textures/gui/clayworktable.png");
    public ClayWorkTableScreen(ClayWorkTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(RecipeViewerNavigation.button(leftPos, topPos,
                net.claustra01.clayium.recipe.ClayiumRecipeMethod.CLAY_WORK_TABLE.location()));
        for (int action = 1; action <= 6; action++) {
            int selectedAction = action;
            Component description = Component.translatable("button.clayium.work_action." + action);
            AbstractButton button = new AbstractButton(
                    leftPos + 40 + (action - 1) * 16, topPos + 52, 16, 16, description) {
                @Override
                public void onPress() {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, selectedAction);
                }

                @Override
                protected void updateWidgetNarration(NarrationElementOutput output) {
                    defaultButtonNarrationText(output);
                }

                @Override
                protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                    int column = selectedAction == 6 ? 0 : selectedAction - 1;
                    int row = selectedAction == 6 ? 80 : 32;
                    int state = !active ? 2 : isHoveredOrFocused() ? 1 : 0;
                    graphics.blit(TEXTURE, getX(), getY(), 176 + column * 16, row + state * 16,
                            16, 16, 256, 256);
                }
            };
            button.setTooltip(Tooltip.create(description));
            addRenderableWidget(button);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int progress = menu.scaledProgress(80);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 48, topPos + 29, 176, 0, progress, 16, 256, 256);
        }
    }

}
