package net.claustra01.clayium.client.screen;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.components.Tooltip;
import net.claustra01.clayium.client.screen.legacy.LegacyButton;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.ClayMachineMenu;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlockEntity;
import net.claustra01.clayium.network.MachineFaceModePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClayMachineScreen extends AbstractContainerScreen<ClayMachineMenu> {
    private final Map<Direction, LegacyButton> faceButtons = new EnumMap<>(Direction.class);

    private boolean configuringFaces;

    public ClayMachineScreen(ClayMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = menu.machine() instanceof CaReactorCoreBlockEntity ? 196 : 172;
        inventoryLabelY = menu.machine() instanceof CaReactorCoreBlockEntity ? 102 : 78;
    }

    @Override
    protected void init() {
        super.init();
        faceButtons.clear();
        LegacyButton toggle = addRenderableWidget(new LegacyButton(leftPos + 149, topPos + 4, 23, 14,
                Component.literal("I/O"), () -> {
                    configuringFaces = !configuringFaces;
                    faceButtons.values().forEach(button -> button.visible = configuringFaces);
                }));
        toggle.setTooltip(Tooltip.create(Component.translatable("gui.clayium.machine.faces")));
        int index = 0;
        for (Direction direction : Direction.values()) {
            LegacyButton faceButton = addRenderableWidget(new LegacyButton(
                    leftPos + 4 + index * 24, topPos + 4, 23, 14,
                    faceButtonText(direction), () -> cycleFace(direction)));
            faceButton.visible = configuringFaces;
            faceButtons.put(direction, faceButton);
            index++;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        faceButtons.forEach((direction, button) -> button.setMessage(faceButtonText(direction)));
    }

    private void cycleFace(Direction direction) {
        ClayMachineBlockEntity machine = menu.machine();
        if (machine == null) {
            return;
        }
        ClayMachineBlockEntity.FaceMode next = machine.faceMode(direction).next();
        PacketDistributor.sendToServer(new MachineFaceModePayload(
                menu.machinePos(), direction.get3DDataValue(), next.ordinal()));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + inventoryLabelY,
                0, 0, 176, 94, 256, 256);
        int inputSlots = menu.machine() == null ? 1 : menu.machine().inputSlots();
        for (int input = 0; input < inputSlots; input++) {
            drawMachineSlot(graphics, ClayMachineMenu.inputX(inputSlots, input), 35, inputSlots, input, 32);
        }
        int outputSlots = menu.machine() == null ? 1 : Math.min(menu.machine().outputSlots(), 4);
        for (int output = 0; output < outputSlots; output++) {
            drawMachineSlot(graphics, ClayMachineMenu.outputX(outputSlots, output),
                    ClayMachineMenu.outputY(outputSlots, output), outputSlots, output, 64);
        }
        LegacyGuiGraphics.slot(graphics, leftPos + 44, topPos + 57);
        if (!configuringFaces && menu.machine() != null) {
            graphics.renderItem(new ItemStack(menu.machine().getBlockState().getBlock()), leftPos + 131, topPos + 4);
        }
        LegacyGuiGraphics.progress(graphics, leftPos + 76, topPos + 35,
                menu.progress(), menu.requiredProgress());
        if (menu.machine() != null && menu.machine().series().recipeMethod().id().equals("chemical_reactor")) {
            drawFluidGauge(graphics, menu.inputFluid(), 20);
            drawFluidGauge(graphics, menu.outputFluid(), 160);
        }
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
        if (button == 0 && menu.machine() != null && x >= leftPos + 76 && x < leftPos + 100
                && y >= topPos + 35 && y < topPos + 52
                && RecipeViewerNavigation.open(menu.machine().series().recipeMethod().location())) {
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    private void drawFluidGauge(GuiGraphics graphics, FluidStack fluid, int x) {
        int left = leftPos + x;
        int top = topPos + 35;
        graphics.fill(left, top, left + 8, top + 32, 0xFF303030);
        if (!fluid.isEmpty()) {
            IClientFluidTypeExtensions appearance = IClientFluidTypeExtensions.of(fluid.getFluid());
            int tint = appearance.getTintColor(fluid);
            int fill = Math.min(30, Math.max(1, fluid.getAmount() * 30 / 16_000));
            graphics.enableScissor(left + 1, top + 31 - fill, left + 7, top + 31);
            graphics.blit(left + 1, top + 1, 0, 6, 30,
                    Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                            .apply(appearance.getStillTexture(fluid)),
                    (tint >> 16 & 0xFF) / 255.0F,
                    (tint >> 8 & 0xFF) / 255.0F,
                    (tint & 0xFF) / 255.0F,
                    (tint >>> 24) / 255.0F);
            graphics.disableScissor();
        }
        graphics.renderOutline(left, top, 8, 32, 0xFF707070);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (menu.machine() == null || !menu.machine().series().recipeMethod().id().equals("chemical_reactor")) {
            return;
        }
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        if ((x >= 20 && x < 28 || x >= 160 && x < 168) && y >= 35 && y < 67) {
            FluidStack fluid = x < 28 ? menu.inputFluid() : menu.outputFluid();
            graphics.renderTooltip(font, List.of(
                    fluid.isEmpty() ? Component.translatable("gui.clayium.machine.empty") : fluid.getHoverName(),
                    Component.literal(fluid.getAmount() + " / 16000 mB"))
                    .stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!configuringFaces) {
            float scale = Math.min(1.0F, 122.0F / font.width(title));
            graphics.pose().pushPose();
            graphics.pose().translate(6, 6, 0);
            graphics.pose().scale(scale, scale, 1);
            graphics.drawString(font, title, 0, 0, 0x404040, false);
            graphics.pose().popPose();
        }
        Component tier = Component.literal("Tier " + menu.tier());
        graphics.drawString(font, tier, 40 - font.width(tier), 58, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.machine.state." + menu.state().name().toLowerCase()),
                65, 58, stateColor(), false);
        graphics.drawString(font, Component.translatable(
                        "gui.clayium.machine.required_energy", formatEnergy(menu.requiredEnergy())),
                65, 68, 0x404040, false);
        ClayMachineBlockEntity machine = menu.machine();
        if (machine instanceof CaReactorCoreBlockEntity) {
            Component structure = Component.translatable(
                    "gui.clayium.ca_reactor.structure",
                    Component.translatable(menu.reactorFormed()
                            ? "gui.clayium.ca_reactor.formed" : "gui.clayium.ca_reactor.incomplete"),
                    menu.reactorCoils(), menu.reactorHulls());
            Component rank = Component.translatable(
                    "gui.clayium.ca_reactor.rank",
                    String.format(java.util.Locale.ROOT, "%.2f", menu.reactorAverageRank()));
            float scale = Math.min(1.0F, 162.0F / font.width(structure));
            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, structure, (int)(7 / scale), (int)(80 / scale),
                    menu.reactorFormed() ? 0x168A16 : 0xAA2020, false);
            graphics.drawString(font, rank, (int)(7 / scale), (int)(91 / scale), 0x404040, false);
            graphics.pose().popPose();
        }
        if (machine != null && menu.state() == ClayMachineBlockEntity.MachineState.RUNNING) {
            int reservedStart = machine.inputSlots() + machine.outputSlots();
            for (int input = 0; input < machine.inputSlots(); input++) {
                if (menu.slots.get(input).getItem().isEmpty()) {
                    ItemStack reserved = menu.slots.get(reservedStart + input).getItem();
                    if (!reserved.isEmpty()) {
                        graphics.renderItem(reserved, ClayMachineMenu.inputX(machine.inputSlots(), input), 35);
                        graphics.renderItemDecorations(font, reserved,
                                ClayMachineMenu.inputX(machine.inputSlots(), input), 35);
                    }
                }
            }
        }
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private int stateColor() {
        return switch (menu.state()) {
            case RUNNING -> 0x168A16;
            case OUTPUT_FULL -> 0xAA2020;
            case WAITING_FOR_ENERGY -> 0xAA6A00;
            case RECIPE_CHANGED -> 0xAA6A00;
            case IDLE -> 0x404040;
        };
    }

    private Component faceButtonText(Direction direction) {
        ClayMachineBlockEntity machine = menu.machine();
        String mode = machine == null ? "-" : switch (machine.faceMode(direction)) {
            case INPUT -> "I";
            case OUTPUT -> "O";
            case DISABLED -> "X";
        };
        return Component.literal(shortName(direction) + ":" + mode);
    }

    private static String shortName(Direction direction) {
        return switch (direction) {
            case DOWN -> "D";
            case UP -> "U";
            case NORTH -> "N";
            case SOUTH -> "S";
            case WEST -> "W";
            case EAST -> "E";
        };
    }

    private static String formatEnergy(long internal) {
        return String.format(java.util.Locale.ROOT, "%.3f CE", internal / 100_000.0D);
    }
}
