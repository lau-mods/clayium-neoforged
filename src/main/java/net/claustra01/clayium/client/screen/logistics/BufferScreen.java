package net.claustra01.clayium.client.screen.logistics;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import java.util.EnumMap;
import java.util.Map;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.logistics.BufferControlPayload;
import net.claustra01.clayium.logistics.BufferMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class BufferScreen extends AbstractContainerScreen<BufferMenu> {

    private final Map<Direction, Button> faceButtons = new EnumMap<>(Direction.class);
    private Button redstoneButton;
    private Button modeButton;
    private Button typeButton;
    private Button limitButton;
    private Button previousPageButton;
    private Button nextPageButton;

    public BufferScreen(BufferMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 220;
        imageHeight = 226;
        inventoryLabelX = 29;
        inventoryLabelY = 133;
    }

    @Override
    protected void init() {
        super.init();
        int index = 0;
        for (Direction direction : Direction.values()) {
            Button button = addRenderableWidget(Button.builder(
                            faceText(direction), pressed -> cycleFace(direction))
                    .bounds(leftPos + 7 + index * 34, topPos + 4, 32, 14)
                    .build());
            faceButtons.put(direction, button);
            index++;
        }
        modeButton = addRenderableWidget(Button.builder(modeText(), pressed -> send(BufferControlPayload.FILTER_MODE, 0))
                .bounds(leftPos + 7, topPos + 38, 42, 16).build());
        redstoneButton = addRenderableWidget(Button.builder(redstoneText(), pressed -> send(
                        BufferControlPayload.REDSTONE, menu.redstoneCondition().next().ordinal()))
                .bounds(leftPos + 51, topPos + 38, 48, 16).build());
        typeButton = addRenderableWidget(Button.builder(typeText(), pressed -> send(BufferControlPayload.RULE_TYPE, 0))
                .bounds(leftPos + 101, topPos + 38, 48, 16).build());
        addRenderableWidget(Button.builder(Component.literal("+"), pressed -> send(BufferControlPayload.ADD_RULE, 0))
                .bounds(leftPos + 151, topPos + 38, 18, 16).build());
        addRenderableWidget(Button.builder(Component.literal("−"), pressed -> send(BufferControlPayload.REMOVE_RULE, 0))
                .bounds(leftPos + 171, topPos + 38, 18, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Tag ▶"), pressed -> send(BufferControlPayload.NEXT_TAG, 0))
                .bounds(leftPos + 7, topPos + 56, 50, 12).build());
        limitButton = addRenderableWidget(Button.builder(limitText(), pressed -> send(
                        BufferControlPayload.TRANSFER_LIMIT, nextLimit()))
                .bounds(leftPos + 191, topPos + 38, 26, 16).build());
        previousPageButton = addRenderableWidget(Button.builder(Component.literal("◀"), pressed -> changePage(menu.page() - 1))
                .bounds(leftPos + 149, topPos + 125, 18, 14).build());
        nextPageButton = addRenderableWidget(Button.builder(Component.literal("▶"), pressed -> changePage(menu.page() + 1))
                .bounds(leftPos + 195, topPos + 125, 18, 14).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        faceButtons.forEach((direction, button) -> button.setMessage(faceText(direction)));
        modeButton.setMessage(modeText());
        redstoneButton.setMessage(redstoneText());
        typeButton.setMessage(typeText());
        limitButton.setMessage(limitText());
        previousPageButton.active = menu.page() > 0;
        nextPageButton.active = menu.page() < 3;
    }

    private void changePage(int page) {
        if (menu.clickMenuButton(minecraft.player, page)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, page);
        }
    }

    private void cycleFace(Direction direction) {
        send(direction.get3DDataValue(), menu.faceMode(direction).next().ordinal());
    }

    private void send(int action, int value) {
        PacketDistributor.sendToServer(new BufferControlPayload(menu.bufferPos(), action, value));
    }

    private int nextLimit() {
        int current = menu.transferLimit();
        int maximum = TierManager.bufferTransport(menu.tier()).maximumExtractItems();
        int[] choices = {1, 4, 16, 64, 128, 192, 256, 512, 1_024, 6_400};
        if (current == 0) {
            return Math.min(1, maximum);
        }
        for (int choice : choices) {
            if (choice > current && choice < maximum) {
                return choice;
            }
        }
        return current < maximum ? maximum : 0;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos + 21, topPos + 132, 0, 0, 176, 86);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 10; column++) {
                if (menu.page() * BufferMenu.PAGE_SIZE + row * 10 + column < BufferMenu.STORAGE_SLOTS) {
                    graphics.blit(LegacyGuiGraphics.SLOT, leftPos + 28 + column * 18, topPos + 68 + row * 18, 0, 0, 18, 18);
                }
            }
        }
        graphics.blit(LegacyGuiGraphics.SLOT, leftPos + 6, topPos + 82, 0, 0, 18, 18);
        graphics.blit(LegacyGuiGraphics.SLOT, leftPos + 6, topPos + 105, 0, 0, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String titleText = title.getString();
        if (font.width(titleText) > 150) {
            titleText = font.plainSubstrByWidth(titleText, 140) + "…";
        }
        graphics.drawString(font, titleText, 7, 23, 0x404040, false);
        Component tier = Component.literal("Tier " + menu.tier());
        graphics.drawString(font, tier, imageWidth - 7 - font.width(tier), 23, 0x404040, false);
        graphics.drawString(font, ruleSummary(), 60, 59, 0x404040, false);
        graphics.drawString(font, Component.literal("F"), 7, 69, 0x505050, false);
        graphics.drawString(font, Component.literal((menu.page() + 1) + "/4"), 172, 128, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private Component faceText(Direction direction) {
        String mode = switch (menu.faceMode(direction)) {
            case INPUT -> "I";
            case OUTPUT -> "O";
            case DISABLED -> "X";
        };
        return Component.literal(shortName(direction) + ":" + mode);
    }

    private Component modeText() {
        return Component.translatable("gui.clayium.buffer.mode." + menu.filterMode().name().toLowerCase());
    }

    private Component redstoneText() {
        return Component.translatable("gui.clayium.buffer.redstone." + menu.redstoneCondition().name().toLowerCase());
    }

    private Component typeText() {
        return Component.translatable("gui.clayium.buffer.rule." + menu.selectedRuleType().name().toLowerCase());
    }

    private Component limitText() {
        return Component.literal(menu.transferLimit() == 0 ? "∞" : Integer.toString(menu.transferLimit()));
    }

    private Component ruleSummary() {
        String summary = Component.translatable("gui.clayium.buffer.conditions", menu.ruleCount()).getString();
        if (menu.buffer() != null && !menu.buffer().filterSettings().rules().isEmpty()) {
            String names = menu.buffer().filterSettings().rules().stream().limit(4)
                    .map(rule -> Component.translatable(
                            "gui.clayium.buffer.rule." + rule.type().name().toLowerCase(java.util.Locale.ROOT)).getString())
                    .collect(java.util.stream.Collectors.joining(", "));
            summary += ": " + names;
        }
        int maximumWidth = imageWidth - 14;
        if (font.width(summary) > maximumWidth) {
            summary = font.plainSubstrByWidth(summary, maximumWidth - font.width("…")) + "…";
        }
        return Component.literal(summary);
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
}
