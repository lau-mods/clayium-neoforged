package net.claustra01.clayium.client.screen.fluid;

import java.util.List;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.fluid.FluidTransportMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/** Fluid buffer: one recessed tank centred in the machine area and the stored amount below it. */
public final class FluidTransportScreen extends AbstractContainerScreen<FluidTransportMenu> {
    private static final int TANK_X = 63;
    private static final int TANK_Y = 18;
    private static final int TANK_WIDTH = 50;
    private static final int TANK_HEIGHT = 34;

    public FluidTransportScreen(FluidTransportMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = FluidTransportMenu.MACHINE_HEIGHT + 94;
        titleLabelX = 6;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = FluidTransportMenu.MACHINE_HEIGHT;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        if (x >= TANK_X && x < TANK_X + TANK_WIDTH && y >= TANK_Y && y < TANK_Y + TANK_HEIGHT) {
            FluidStack fluid = menu.fluid();
            graphics.renderTooltip(font, List.of(
                    fluid.isEmpty() ? Component.translatable("gui.clayium.fluid.empty") : fluid.getHoverName(),
                    Component.literal(menu.amount() + " / " + menu.capacity() + " mB"))
                    .stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + FluidTransportMenu.MACHINE_HEIGHT,
                0, 0, 176, 94, 256, 256);
        int left = leftPos + TANK_X;
        int top = topPos + TANK_Y;
        graphics.fill(left, top, left + TANK_WIDTH, top + TANK_HEIGHT, 0xFF8B8B8B);
        graphics.fill(left, top, left + TANK_WIDTH - 1, top + 1, 0xFF373737);
        graphics.fill(left, top, left + 1, top + TANK_HEIGHT - 1, 0xFF373737);
        graphics.fill(left + 1, top + TANK_HEIGHT - 1, left + TANK_WIDTH, top + TANK_HEIGHT, 0xFFFFFFFF);
        graphics.fill(left + TANK_WIDTH - 1, top + 1, left + TANK_WIDTH, top + TANK_HEIGHT, 0xFFFFFFFF);
        FluidStack fluid = menu.fluid();
        if (fluid.isEmpty() || menu.capacity() <= 0) {
            return;
        }
        IClientFluidTypeExtensions appearance = IClientFluidTypeExtensions.of(fluid.getFluid());
        int tint = appearance.getTintColor(fluid);
        int inner = TANK_HEIGHT - 2;
        int fill = (int)Math.min(inner, Math.max(1L, (long)menu.amount() * inner / menu.capacity()));
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(appearance.getStillTexture(fluid));
        int bottom = top + 1 + inner;
        graphics.enableScissor(left + 1, bottom - fill, left + TANK_WIDTH - 1, bottom);
        for (int tileX = left + 1; tileX < left + TANK_WIDTH - 1; tileX += 16) {
            for (int tileY = top + 1; tileY < bottom; tileY += 16) {
                graphics.blit(tileX, tileY, 0, 16, 16, sprite,
                        (tint >> 16 & 0xFF) / 255.0F, (tint >> 8 & 0xFF) / 255.0F,
                        (tint & 0xFF) / 255.0F, (tint >>> 24) / 255.0F);
            }
        }
        graphics.disableScissor();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LegacyGuiGraphics.TEXT_COLOR, false);
        String amount = menu.amount() + " / " + menu.capacity() + " mB";
        graphics.drawString(font, amount, (imageWidth - font.width(amount)) / 2, TANK_Y + TANK_HEIGHT + 4,
                LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                LegacyGuiGraphics.TEXT_COLOR, false);
    }
}
