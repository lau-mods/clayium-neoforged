package net.claustra01.clayium.client.screen.legacy;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.waterwheel.WaterWheelMenu;
import net.claustra01.clayium.registry.WaterWheelRegistration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class WaterWheelScreen extends AbstractContainerScreen<WaterWheelMenu> {
    public WaterWheelScreen(WaterWheelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 126;
        inventoryLabelY = 32;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float tick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + 32, 0, 0, 176, 94);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 7, 6, LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.clayium_neoforged.water_wheel.progress", menu.progress()), 6, 20,
                LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.clayium_neoforged.water_wheel.flow", menu.flowingWater()), 90, 32,
                LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.clayium_neoforged.water_wheel.efficiency", menu.efficiency()), 90, 20,
                LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, 8, 32, LegacyGuiGraphics.TEXT_COLOR, false);
    }

    @EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent
        public static void register(RegisterMenuScreensEvent event) {
            event.register(WaterWheelRegistration.MENU.get(), WaterWheelScreen::new);
        }
    }
}
