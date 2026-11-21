package net.claustra01.clayium.client.screen;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.ClayMachineMenu;
import net.claustra01.clayium.network.MachineFaceModePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClayMachineScreen extends AbstractContainerScreen<ClayMachineMenu> {
    private static final ResourceLocation BACKGROUND = ClayiumMod.id("textures/gui/back.png");
    private static final ResourceLocation PLAYER_INVENTORY = ClayiumMod.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = ClayiumMod.id("textures/gui/slot.png");
    private static final ResourceLocation PROGRESS = ClayiumMod.id("textures/gui/progressbarfurnace.png");
    private final Map<Direction, Button> faceButtons = new EnumMap<>(Direction.class);

    public ClayMachineScreen(ClayMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 172;
        inventoryLabelY = 78;
    }

    @Override
    protected void init() {
        super.init();
        int index = 0;
        for (Direction direction : Direction.values()) {
            Button faceButton = addRenderableWidget(Button.builder(
                            faceButtonText(direction),
                            pressed -> cycleFace(direction))
                    .bounds(leftPos + 4 + index * 28, topPos + 4, 26, 14)
                    .build());
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
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(BACKGROUND, leftPos, topPos + imageHeight - 1, 0, 0, imageWidth, 1, 256, 256);
        graphics.blit(PLAYER_INVENTORY, leftPos, topPos + 78, 0, 0, 176, 90);
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos + imageHeight, 0);
        graphics.pose().scale(1.0F, -1.0F, 1.0F);
        graphics.blit(BACKGROUND, 0, 0, 0, 0, imageWidth, 4);
        graphics.pose().popPose();
        int inputSlots = menu.machine() == null ? 1 : menu.machine().inputSlots();
        for (int input = 0; input < inputSlots; input++) {
            graphics.blit(SLOT, leftPos + ClayMachineMenu.inputX(inputSlots, input) - 1, topPos + 34, 0, 0, 18, 18);
        }
        int outputSlots = menu.machine() == null ? 1 : menu.machine().outputSlots();
        for (int output = 0; output < outputSlots; output++) {
            graphics.blit(
                    SLOT,
                    leftPos + ClayMachineMenu.outputX(outputSlots, output) - 1,
                    topPos + ClayMachineMenu.outputY(outputSlots, output) - 1,
                    0, 0, 18, 18);
        }
        graphics.blit(SLOT, leftPos + 43, topPos + 56, 0, 0, 18, 18);
        ClayMachineBlockEntity machine = menu.machine();
        if (machine != null) {
            ResourceLocation hull = ClayiumMod.id("textures/block/machinehull-" + Math.max(0, machine.tier() - 1) + ".png");
            ResourceLocation front = ClayiumMod.id("textures/block/" + machine.series().frontTexture() + ".png");
            graphics.blit(hull, leftPos + 72, topPos + 35, 0, 0, 16, 16, 16, 16);
            graphics.blit(front, leftPos + 72, topPos + 35, 0, 0, 16, 16, 16, 16);
        }
        int width = menu.requiredProgress() <= 0
                ? 0
                : menu.progress() > 0 ? Math.max(1, Math.min(24,
                        (int)(24.0D * menu.progress() / menu.requiredProgress()))) : 0;
        graphics.blit(PROGRESS, leftPos + 89, topPos + 36, 0, 0, 24, 16);
        if (width > 0) {
            graphics.blit(PROGRESS, leftPos + 89, topPos + 36, 0, 18, width, 16);
        }
        if (menu.machine() != null && menu.machine().series().recipeMethod().id().equals("chemical_reactor")) {
            drawFluidGauge(graphics, menu.inputFluid(), 20);
            drawFluidGauge(graphics, menu.outputFluid(), 160);
        }
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
        int titleWidth = menu.machine() != null && menu.machine().outputSlots() > 1 ? 120 : imageWidth - 14;
        float titleScale = Math.min(1.0F, (float)titleWidth / font.width(title));
        graphics.pose().pushPose();
        graphics.pose().scale(titleScale, titleScale, 1.0F);
        graphics.drawString(font, title, (int)(7 / titleScale), (int)(21 / titleScale), 0x404040, false);
        graphics.pose().popPose();
        Component tier = Component.literal("Tier " + menu.tier());
        graphics.drawString(font, tier, 7, 58, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.machine.state." + menu.state().name().toLowerCase()),
                80, 58, stateColor(), false);
        graphics.drawString(font, Component.translatable(
                        "gui.clayium.machine.required_energy", formatEnergy(menu.requiredEnergy())),
                80, 69, 0x404040, false);
        ClayMachineBlockEntity machine = menu.machine();
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
        graphics.drawString(font, Component.translatable("gui.clayium.machine.inventory"),
                inventoryLabelX, inventoryLabelY, 0x404040, false);
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
