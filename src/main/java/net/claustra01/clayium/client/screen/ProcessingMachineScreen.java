package net.claustra01.clayium.client.screen;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.menu.ProcessingMachineMenu;
import net.claustra01.clayium.util.ClayiumFormatting;
import net.claustra01.clayium.util.UtilTier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for all Clayium processing machines, including Separator, Blast Furnace, Reactor, and CA Reactor.
 */
public class ProcessingMachineScreen extends ClayiumBaseScreen<ProcessingMachineMenu> {
    @Nullable
    private ClayPictureButton manualButton;
    @Nullable
    private Button caReactorStatusButton;

    public ProcessingMachineScreen(ProcessingMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.manualButton = null;
        this.caReactorStatusButton = null;
        int mode = this.menu.getProgressMode();
        if (mode == ProcessingMachineMenu.PROGRESS_CA_REACTOR) {
            this.caReactorStatusButton = Button.builder(Component.translatable("gui.CAReactor.constructed"), btn -> {})
                    .bounds(this.leftPos + this.menu.machineGuiSizeX - 84, this.topPos + this.menu.machineGuiSizeY - 1, 80, 12)
                    .build();
            this.caReactorStatusButton.active = false;
            addRenderableWidget(this.caReactorStatusButton);
        } else if (mode != ProcessingMachineMenu.PROGRESS_BLAST_FURNACE
                && mode != ProcessingMachineMenu.PROGRESS_REACTOR
                && UtilTier.canManualCraft(this.menu.getTier())) {
            this.manualButton = new ClayPictureButton(
                    ProcessingMachineMenu.BUTTON_MANUAL_CRAFT,
                    this.leftPos + (this.menu.machineGuiSizeX - 16) / 2,
                    this.topPos + 56,
                    16,
                    16,
                    BUTTON_TEXTURE,
                    0,
                    0,
                    this::sendGuiButton
            );
            addRenderableWidget(this.manualButton);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        int barSizeX = 24;
        int barSizeY = 17;
        int barX = this.menu.getProgressMode() == ProcessingMachineMenu.PROGRESS_SEPARATOR
                ? 55
                : (this.menu.machineGuiSizeX - barSizeX) / 2;
        int barY = this.menu.getProgressMode() == ProcessingMachineMenu.PROGRESS_SEPARATOR ? 44 : 35;

        guiGraphics.blit(PROGRESS_TEXTURE, this.leftPos + barX, this.topPos + barY, 0, 0, barSizeX, barSizeY, 256, 256);
        int progress = this.menu.getCraftProgressScaled(barSizeX);
        if (progress > 0) {
            guiGraphics.blit(PROGRESS_TEXTURE, this.leftPos + barX, this.topPos + barY, 0, barSizeY, progress, barSizeY, 256, 256);
        }

        if (this.manualButton != null) {
            this.manualButton.active = this.menu.canPushButton();
        }
        if (this.caReactorStatusButton != null) {
            boolean constructed = this.menu.isConstructed();
            this.caReactorStatusButton.setMessage(Component.translatable(
                    constructed ? "gui.CAReactor.constructed" : "gui.CAReactor.invalid"
            ));
            this.caReactorStatusButton.active = !constructed;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        drawClayEnergyText(guiGraphics, this.menu.getClayEnergy());
        drawOverclockText(guiGraphics, this.menu.getOverclockTotalFactor());

        int mode = this.menu.getProgressMode();
        if (this.menu.isCaCondenser()) {
            guiGraphics.drawString(
                    this.font,
                    Component.translatable("gui.Common.resonance", ClayiumFormatting.formatCAResonance(this.menu.getCaResonance())),
                    80,
                    this.menu.machineGuiSizeY - 12,
                    0x404040,
                    false
            );
        } else if (mode == ProcessingMachineMenu.PROGRESS_BLAST_FURNACE) {
            guiGraphics.drawString(
                    this.font,
                    Component.translatable("gui.Common.tier", this.menu.getRecipeTier()),
                    64,
                    this.menu.machineGuiSizeY - 12,
                    0x404040,
                    false
            );
        } else if (mode == ProcessingMachineMenu.PROGRESS_REACTOR) {
            long laser = this.menu.getReactorLaserEnergy();
            String txt = Component.translatable("gui.Common.tier", this.menu.getRecipeTier()).getString()
                    + "  "
                    + Component.translatable("gui.Common.clayLaser", ClayiumFormatting.formatLaser(laser)).getString();
            guiGraphics.drawString(this.font, txt, 64, this.menu.machineGuiSizeY - 12, 0x404040, false);
        } else if (mode == ProcessingMachineMenu.PROGRESS_CA_REACTOR && this.menu.isConstructed()) {
            guiGraphics.drawString(
                    this.font,
                    Component.translatable("gui.CAReactor.rankSize", this.menu.getCaRank() + 1.0, this.menu.getCaHullNum()),
                    6,
                    16,
                    0x404040,
                    false
            );
            double efficiency = this.menu.getMachine() instanceof net.claustra01.clayium.blockentity.CAReactorBlockEntity car
                    ? car.getEfficiency()
                    : 0.0;
            guiGraphics.drawString(
                    this.font,
                    Component.translatable("gui.CAReactor.efficiency", efficiency),
                    64,
                    this.menu.machineGuiSizeY - 12,
                    0x404040,
                    false
            );
        }
    }

    @Nullable
    @Override
    protected ClayContainerBlockEntity getEnergyPreviewSource() {
        return this.menu.getMachine();
    }
}
