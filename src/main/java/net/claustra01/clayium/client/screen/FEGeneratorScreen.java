package net.claustra01.clayium.client.screen;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.menu.FEGeneratorMenu;
import net.claustra01.clayium.util.ClayiumFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for CE-FE Converters matching 1.7.10 {@code GuiClayRFGenerator} telemetry layout
 * and Shift-held Energetic Clay slot preview.
 */
public class FEGeneratorScreen extends ClayiumBaseScreen<FEGeneratorMenu> {
    public FEGeneratorScreen(FEGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        drawClayEnergyText(guiGraphics, this.menu.getClayEnergy());

        guiGraphics.drawString(
                this.font,
                Component.translatable(
                        "gui.RFGenerator.storage",
                        ClayiumFormatting.formatFE(this.menu.getFeStored()),
                        ClayiumFormatting.formatFE(this.menu.getMaxFeStored())
                ),
                12,
                20,
                0x404040,
                false
        );
        guiGraphics.drawString(
                this.font,
                Component.translatable(
                        "gui.RFGenerator.convertRate",
                        ClayiumFormatting.formatClayEnergy(this.menu.getCeConsumptionPerTick()),
                        ClayiumFormatting.formatFE(this.menu.getFeProductionPerTick())
                ),
                12,
                40,
                0x404040,
                false
        );
        guiGraphics.drawString(
                this.font,
                Component.translatable(
                        "gui.RFGenerator.output",
                        ClayiumFormatting.formatFE(this.menu.getFeOutputPerTick())
                ),
                12,
                52,
                0x404040,
                false
        );

        double total = this.menu.getOverclockTotalFactor();
        double exp = this.menu.getOverclockExponent();
        if (exp == 1.0) {
            guiGraphics.drawString(
                    this.font,
                    Component.translatable("gui.RFGenerator.overclock", ClayiumFormatting.formatCAResonance(total)),
                    12,
                    64,
                    0x404040,
                    false
            );
        } else {
            guiGraphics.drawString(
                    this.font,
                    Component.translatable(
                            "gui.RFGenerator.overclockExponent",
                            ClayiumFormatting.formatCAResonance(total),
                            ClayiumFormatting.formatCAResonance(exp),
                            ClayiumFormatting.formatCAResonance(Math.pow(total, exp))
                    ),
                    12,
                    64,
                    0x404040,
                    false
            );
        }
    }

    @Nullable
    @Override
    protected ClayContainerBlockEntity getEnergyPreviewSource() {
        return this.menu.getGenerator();
    }
}
