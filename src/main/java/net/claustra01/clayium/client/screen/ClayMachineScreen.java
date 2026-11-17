package net.claustra01.clayium.client.screen;

import java.util.EnumMap;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.ClayMachineMenu;
import net.claustra01.clayium.network.MachineFaceModePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClayMachineScreen extends AbstractContainerScreen<ClayMachineMenu> {
    private static final ResourceLocation BACKGROUND = ClayiumMod.id("textures/gui/back.png");
    private static final ResourceLocation PLAYER_INVENTORY = ClayiumMod.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = ClayiumMod.id("textures/gui/slot.png");
    private static final ResourceLocation PROGRESS = ClayiumMod.id("textures/gui/progressbarfurnace.png");
    private final Map<Direction, Button> faceButtons = new EnumMap<>(Direction.class);

    public ClayMachineScreen(ClayMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 166;
        inventoryLabelY = 72;
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
        graphics.blit(PLAYER_INVENTORY, leftPos, topPos + 72, 0, 0, 176, 90);
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos + imageHeight, 0);
        graphics.pose().scale(1.0F, -1.0F, 1.0F);
        graphics.blit(BACKGROUND, 0, 0, 0, 0, imageWidth, 4);
        graphics.pose().popPose();
        int inputSlots = menu.machine() == null ? 1 : menu.machine().inputSlots();
        for (int input = 0; input < inputSlots; input++) {
            graphics.blit(SLOT, leftPos + ClayMachineMenu.inputX(inputSlots, input) - 1, topPos + 34, 0, 0, 18, 18);
        }
        graphics.blit(SLOT, leftPos + 115, topPos + 34, 0, 0, 18, 18);
        graphics.blit(SLOT, leftPos + 43, topPos + 56, 0, 0, 18, 18);
        ClayMachineBlockEntity machine = menu.machine();
        if (machine != null) {
            ResourceLocation front = ClayiumMod.id("textures/block/" + machine.series().frontTexture() + ".png");
            graphics.blit(front, leftPos + 72, topPos + 35, 0, 0, 16, 16, 16, 16);
        }
        int width = menu.requiredProgress() <= 0
                ? 0
                : Math.min(24, (int)(24.0D * menu.progress() / Math.max(1L, menu.requiredProgress())));
        graphics.blit(PROGRESS, leftPos + 89, topPos + 36, 0, 0, 24, 16);
        if (width > 0) {
            graphics.blit(PROGRESS, leftPos + 89, topPos + 36, 0, 18, width, 16);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component tier = Component.literal("Tier " + menu.tier());
        int titleWidth = imageWidth - 14;
        float titleScale = Math.min(1.0F, (float)titleWidth / font.width(title));
        graphics.pose().pushPose();
        graphics.pose().scale(titleScale, titleScale, 1.0F);
        graphics.drawString(font, title, (int)(7 / titleScale), (int)(21 / titleScale), 0x404040, false);
        graphics.pose().popPose();
        graphics.drawString(font, tier, 7, 58, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.machine.state." + menu.state().name().toLowerCase()),
                80, 58, stateColor(), false);
        graphics.drawString(font, Component.literal(formatEnergy(menu.storedEnergy())), 80, 69, 0x404040, false);
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
