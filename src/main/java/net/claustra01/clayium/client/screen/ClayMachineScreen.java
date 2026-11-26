package net.claustra01.clayium.client.screen;

import java.util.List;
import java.util.Locale;
import net.claustra01.clayium.client.screen.legacy.LegacyButton;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.client.screen.legacy.LegacyNumerals;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.ClayMachineMenu;
import net.claustra01.clayium.multiblock.MultiblockControllerBlockEntity;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlockEntity;
import net.claustra01.clayium.multiblock.careactor.CaReactorFormula;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Original {@code GuiClayMachines} family: GuiClayEnergyTemp footer, GuiClayContainerTemp
 * resonance/overclock lines, GuiClayBlastFurnace/GuiClayReactor tier text, GuiCAReactor status.
 */
public final class ClayMachineScreen extends AbstractContainerScreen<ClayMachineMenu> {
    private static final int GAUGE_WIDTH = 10;
    private static final int GAUGE_HEIGHT = 18;
    private static final int INPUT_GAUGE_X = 19;
    private static final int OUTPUT_GAUGE_X = 147;
    private static final int GAUGE_Y = 34;

    private LegacyButton reactorStatus;

    public ClayMachineScreen(ClayMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = menu.machineAreaHeight() + 94;
        inventoryLabelY = menu.machineAreaHeight();
    }

    @Override
    protected void init() {
        super.init();
        reactorStatus = null;
        if (menu.machine() instanceof CaReactorCoreBlockEntity) {
            // GuiCAReactor.addButton: GuiButtonExt(1, machineGuiSizeX - 84, machineGuiSizeY - 1, 80, 10)
            reactorStatus = addRenderableWidget(new LegacyButton(
                    leftPos + imageWidth - 84, topPos + menu.machineAreaHeight() - 1, 80, 10,
                    Component.empty(), () -> { }));
            updateReactorStatus();
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateReactorStatus();
    }

    private void updateReactorStatus() {
        if (reactorStatus == null) {
            return;
        }
        boolean formed = menu.reactorFormed();
        reactorStatus.setMessage(Component.translatable(
                formed ? "gui.clayium.ca_reactor.constructed" : "gui.clayium.ca_reactor.invalid"));
        reactorStatus.active = !formed;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + inventoryLabelY,
                0, 0, 176, 94, 256, 256);
        ClayMachineBlockEntity machine = menu.machine();
        int inputSlots = machine == null ? 1 : machine.inputSlots();
        for (int input = 0; input < inputSlots; input++) {
            drawMachineSlot(graphics, ClayMachineMenu.inputX(machine, input),
                    ClayMachineMenu.inputY(machine, input), inputSlots, input, 32);
        }
        int outputSlots = machine == null ? 1 : ClayMachineMenu.displayedOutputSlots(machine);
        if (ClayMachineMenu.isChemicalMetalSeparator(machine) || ClayMachineMenu.isCentrifuge(machine)) {
            for (int output = 0; output < outputSlots; output++) {
                LegacyGuiGraphics.slot(graphics, leftPos + ClayMachineMenu.outputX(machine, output),
                        topPos + ClayMachineMenu.outputY(machine, output));
            }
        } else {
            for (int output = 0; output < outputSlots; output++) {
                drawMachineSlot(graphics, ClayMachineMenu.outputX(machine, output),
                        ClayMachineMenu.outputY(machine, output), outputSlots, output, 64);
            }
        }
        LegacyGuiGraphics.progress(graphics, leftPos + progressX(), topPos + progressY(),
                menu.progress(), menu.requiredProgress());
        if (hasFluidTanks()) {
            drawFluidGauge(graphics, menu.inputFluid(), INPUT_GAUGE_X);
            drawFluidGauge(graphics, menu.outputFluid(), OUTPUT_GAUGE_X);
        }
    }

    private int progressX() {
        return ClayMachineMenu.isChemicalMetalSeparator(menu.machine()) ? 55 : (imageWidth - 24) / 2;
    }

    private int progressY() {
        return ClayMachineMenu.isChemicalMetalSeparator(menu.machine()) ? 44 : 35;
    }

    private boolean hasFluidTanks() {
        return menu.machine() != null && menu.machine().series().recipeMethod().id().equals("chemical_reactor");
    }

    private void drawMachineSlot(GuiGraphics graphics, int x, int y, int count, int index, int pairU) {
        if (count == 1) {
            graphics.blit(LegacyGuiGraphics.SLOT, leftPos + x - 5, topPos + y - 5, 0, 32, 26, 26, 256, 256);
        } else if (count == 2) {
            graphics.blit(LegacyGuiGraphics.SLOT, leftPos + x - 1, topPos + y - 1, pairU,
                    index * 32, 18, 18, 256, 256);
        } else {
            LegacyGuiGraphics.slot(graphics, leftPos + x, topPos + y);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0 && menu.machine() != null && x >= leftPos + progressX() && x < leftPos + progressX() + 24
                && y >= topPos + progressY() && y < topPos + progressY() + 17
                && RecipeViewerNavigation.open(menu.machine().series().recipeMethod().location())) {
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    /** Recessed tank sized and centred like the neighbouring 18x18 item slot frames. */
    private void drawFluidGauge(GuiGraphics graphics, FluidStack fluid, int x) {
        int left = leftPos + x;
        int top = topPos + GAUGE_Y;
        graphics.fill(left, top, left + GAUGE_WIDTH, top + GAUGE_HEIGHT, 0xFF8B8B8B);
        graphics.fill(left, top, left + GAUGE_WIDTH - 1, top + 1, 0xFF373737);
        graphics.fill(left, top, left + 1, top + GAUGE_HEIGHT - 1, 0xFF373737);
        graphics.fill(left + 1, top + GAUGE_HEIGHT - 1, left + GAUGE_WIDTH, top + GAUGE_HEIGHT, 0xFFFFFFFF);
        graphics.fill(left + GAUGE_WIDTH - 1, top + 1, left + GAUGE_WIDTH, top + GAUGE_HEIGHT, 0xFFFFFFFF);
        if (fluid.isEmpty()) {
            return;
        }
        IClientFluidTypeExtensions appearance = IClientFluidTypeExtensions.of(fluid.getFluid());
        int tint = appearance.getTintColor(fluid);
        int inner = GAUGE_HEIGHT - 2;
        int fill = Math.min(inner, Math.max(1, fluid.getAmount() * inner / ClayMachineBlockEntity.FLUID_CAPACITY));
        int bottom = top + 1 + inner;
        graphics.enableScissor(left + 1, bottom - fill, left + GAUGE_WIDTH - 1, bottom);
        graphics.blit(left + 1, top + 1, 0, GAUGE_WIDTH - 2, inner,
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(appearance.getStillTexture(fluid)),
                (tint >> 16 & 0xFF) / 255.0F,
                (tint >> 8 & 0xFF) / 255.0F,
                (tint & 0xFF) / 255.0F,
                (tint >>> 24) / 255.0F);
        graphics.disableScissor();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (!hasFluidTanks()) {
            return;
        }
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        boolean input = x >= INPUT_GAUGE_X && x < INPUT_GAUGE_X + GAUGE_WIDTH;
        boolean output = x >= OUTPUT_GAUGE_X && x < OUTPUT_GAUGE_X + GAUGE_WIDTH;
        if ((input || output) && y >= GAUGE_Y && y < GAUGE_Y + GAUGE_HEIGHT) {
            FluidStack fluid = input ? menu.inputFluid() : menu.outputFluid();
            graphics.renderTooltip(font, List.of(
                    fluid.isEmpty() ? Component.translatable("gui.clayium.machine.empty") : fluid.getHoverName(),
                    Component.literal(fluid.getAmount() + " / " + ClayMachineBlockEntity.FLUID_CAPACITY + " mB"))
                    .stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        float scale = Math.min(1.0F, (imageWidth - 12.0F) / font.width(title));
        graphics.pose().pushPose();
        graphics.pose().translate(6, 6, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, title, 0, 0, 0x404040, false);
        graphics.pose().popPose();
        ClayMachineBlockEntity machine = menu.machine();
        int machineY = menu.machineAreaHeight();
        graphics.drawString(font, Component.translatable("gui.clayium.machine.energy",
                ClayEnergy.ofInternal(menu.storedEnergy()).formatGui()), 4, machineY - 12, 0x404040, false);
        if (machine instanceof MultiblockControllerBlockEntity) {
            Component tier = Component.translatable("gui.clayium.machine.tier", menu.tier());
            graphics.drawString(font, tier, imageWidth - 6 - font.width(tier), machineY - 12, 0x404040, false);
        }
        if (ClayMachineMenu.isCaMachine(machine)) {
            graphics.drawString(font, Component.translatable("gui.clayium.machine.resonance",
                    LegacyNumerals.resonance(menu.resonance())), 80, machineY - 12, 0x404040, false);
        }
        if (menu.overclockFactor() != 1.0D) {
            graphics.drawString(font, Component.translatable("gui.clayium.machine.overclock",
                    LegacyNumerals.resonance(menu.overclockFactor())), 80, machineY, 0x404040, false);
        }
        if (machine instanceof CaReactorCoreBlockEntity && menu.reactorFormed()) {
            graphics.drawString(font, Component.translatable("gui.clayium.ca_reactor.rank_size",
                    String.format(Locale.ROOT, "%2.2g", menu.reactorAverageRank() + 1.0D),
                    String.format(Locale.ROOT, "%2d", menu.reactorHulls())), 6, 16, 0x404040, false);
            CaReactorFormula.calculate(menu.reactorHulls(), menu.reactorAverageRank()).ifPresent(formula ->
                    graphics.drawString(font, Component.translatable("gui.clayium.ca_reactor.efficiency",
                            String.format(Locale.ROOT, "%g", formula.efficiency())),
                            64, machineY - 12, 0x404040, false));
        }
        if (machine != null && menu.state() == ClayMachineBlockEntity.MachineState.RUNNING) {
            int reservedStart = machine.inputSlots() + ClayMachineMenu.displayedOutputSlots(machine);
            for (int input = 0; input < machine.inputSlots(); input++) {
                if (menu.slots.get(input).getItem().isEmpty()) {
                    ItemStack reserved = menu.slots.get(reservedStart + input).getItem();
                    if (!reserved.isEmpty()) {
                        graphics.renderItem(reserved, ClayMachineMenu.inputX(machine, input),
                                ClayMachineMenu.inputY(machine, input));
                        graphics.renderItemDecorations(font, reserved,
                                ClayMachineMenu.inputX(machine, input), ClayMachineMenu.inputY(machine, input));
                    }
                }
            }
        }
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
