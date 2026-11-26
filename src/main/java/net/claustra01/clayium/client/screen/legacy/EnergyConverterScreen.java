package net.claustra01.clayium.client.screen.legacy;

import java.util.Locale;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.fe.EnergyConverterMenu;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class EnergyConverterScreen extends AbstractContainerScreen<EnergyConverterMenu> {
    public EnergyConverterScreen(EnergyConverterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 184;
        inventoryLabelY = 90;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float tick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + 90,
                0, 0, 176, 94, 256, 256);
        LegacyGuiGraphics.slot(graphics, leftPos + 35, topPos + 53);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 7, 6, LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.clayium_neoforged.converter.stored_ce",
                        ClayEnergy.ofInternal(menu.storedCe()).formatGui()),
                62, 31, LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.clayium_neoforged.converter.stored_fe",
                        formatFe(menu.storedFe()), formatFe(menu.capacity())),
                62, 44, LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.clayium_neoforged.converter.generation", formatFe(menu.generatedFe())), 62, 57,
                LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.clayium_neoforged.converter.output", formatFe(menu.outputLimit())), 62, 70,
                LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                LegacyGuiGraphics.TEXT_COLOR, false);
    }

    private static String formatFe(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    @EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent
        public static void register(RegisterMenuScreensEvent event) {
            event.register(EnergyConverterRegistration.MENU.get(), EnergyConverterScreen::new);
        }
    }
}
