package net.claustra01.clayium.client.screen.fluid;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.client.screen.logistics.LogisticsBackground;
import net.claustra01.clayium.fluid.FluidTransportBlockEntity;
import net.claustra01.clayium.fluid.FluidTransportControlPayload;
import net.claustra01.clayium.fluid.FluidTransportMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FluidTransportScreen extends AbstractContainerScreen<FluidTransportMenu> {
    private static final ResourceLocation PLAYER_INVENTORY = ClayiumMod.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = ClayiumMod.id("textures/gui/slot.png");

    private final Map<Direction, Button> faceButtons = new EnumMap<>(Direction.class);
    private Button redstoneButton;
    private Button filterModeButton;

    public FluidTransportScreen(FluidTransportMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 194;
        inventoryLabelX = 8;
        inventoryLabelY = 104;
    }

    @Override
    protected void init() {
        super.init();
        int index = 0;
        for (Direction direction : Direction.values()) {
            Button button = addRenderableWidget(Button.builder(faceText(direction), pressed -> cycleFace(direction))
                    .bounds(leftPos + 7 + index * 27, topPos + 19, 26, 14).build());
            faceButtons.put(direction, button);
            index++;
        }
        redstoneButton = addRenderableWidget(Button.builder(redstoneText(), pressed -> send(
                        FluidTransportControlPayload.REDSTONE, menu.redstoneCondition().next().ordinal()))
                .bounds(leftPos + 7, topPos + 35, 54, 16).build());
        filterModeButton = addRenderableWidget(Button.builder(filterModeText(), pressed -> send(
                        FluidTransportControlPayload.FILTER_MODE, 0))
                .bounds(leftPos + 116, topPos + 35, 53, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.clayium.fluid.filter.set"), pressed -> send(
                        FluidTransportControlPayload.FILTER_CURRENT, 0))
                .bounds(leftPos + 106, topPos + 73, 30, 14).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.clayium.fluid.filter.clear"), pressed -> send(
                        FluidTransportControlPayload.FILTER_CLEAR, 0))
                .bounds(leftPos + 138, topPos + 73, 31, 14).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        faceButtons.forEach((direction, button) -> button.setMessage(faceText(direction)));
        redstoneButton.setMessage(redstoneText());
        filterModeButton.setMessage(filterModeText());
    }

    private void cycleFace(Direction direction) {
        send(direction.get3DDataValue(), menu.faceMode(direction).next().ordinal());
    }

    private void send(int action, int value) {
        PacketDistributor.sendToServer(new FluidTransportControlPayload(menu.transportPos(), action, value));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LogisticsBackground.draw(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(PLAYER_INVENTORY, leftPos, topPos + 104, 0, 0, 176, 86);
        graphics.blit(SLOT, leftPos + 151, topPos + 53, 0, 0, 18, 18);
        drawTank(graphics, menu.fluid());
    }

    private void drawTank(GuiGraphics graphics, FluidStack fluid) {
        int left = leftPos + 69;
        int top = topPos + 38;
        graphics.fill(left, top, left + 36, top + 50, 0xFF202020);
        if (!fluid.isEmpty()) {
            IClientFluidTypeExtensions appearance = IClientFluidTypeExtensions.of(fluid.getFluid());
            int tint = appearance.getTintColor(fluid);
            int fill = Math.min(48, Math.max(1, (int)((long)menu.amount() * 48L / menu.capacity())));
            graphics.enableScissor(left + 1, top + 49 - fill, left + 35, top + 49);
            graphics.blit(left + 1, top + 1, 0, 34, 48,
                    Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                            .apply(appearance.getStillTexture(fluid)),
                    (tint >> 16 & 0xFF) / 255.0F,
                    (tint >> 8 & 0xFF) / 255.0F,
                    (tint & 0xFF) / 255.0F,
                    (tint >>> 24) / 255.0F);
            graphics.disableScissor();
        }
        graphics.renderOutline(left, top, 36, 50, 0xFF808080);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        if (x >= 69 && x < 105 && y >= 38 && y < 88) {
            FluidStack fluid = menu.fluid();
            graphics.renderTooltip(font, List.of(
                    fluid.isEmpty() ? Component.translatable("gui.clayium.fluid.empty") : fluid.getHoverName(),
                    Component.literal(menu.amount() + " / " + menu.capacity() + " mB"),
                    Component.translatable("gui.clayium.fluid.pending", menu.pendingAmount() + " mB"))
                    .stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String titleText = title.getString();
        if (font.width(titleText) > 118) {
            titleText = font.plainSubstrByWidth(titleText, 112) + "…";
        }
        graphics.drawString(font, titleText, 7, 7, 0x404040, false);
        Component tier = Component.translatable("gui.clayium.fluid.tier", menu.tier());
        graphics.drawString(font, tier, imageWidth - 7 - font.width(tier), 7, 0x404040, false);
        Component amount = Component.literal(menu.amount() + " / " + menu.capacity() + " mB");
        graphics.drawString(font, amount, 87 - font.width(amount) / 2, 90, 0x404040, false);
        graphics.drawString(font, compactAmount(menu.transferPerTick(), " mB/t"), 7, 56, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.clayium.fluid.pending", "").getString().trim(),
                7, 68, 0x404040, false);
        graphics.drawString(font, compactAmount(menu.pendingAmount(), " mB"), 7, 78, 0x404040, false);
        String filter = menu.filteredFluidName();
        if (font.width(filter) > 32) {
            filter = font.plainSubstrByWidth(filter, 26) + "…";
        }
        graphics.drawString(font, filter, 116, 61, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private String compactAmount(int amount, String unit) {
        String exact = amount + unit;
        if (font.width(exact) <= 60) {
            return exact;
        }
        String suffix = amount >= 1_000_000 ? "M" : "k";
        int divisor = amount >= 1_000_000 ? 1_000_000 : 1_000;
        String compact = amount / divisor + suffix + unit;
        if (font.width(compact) <= 60) {
            return compact;
        }
        return font.plainSubstrByWidth(compact, 54) + "…";
    }

    private Component faceText(Direction direction) {
        String mode = switch (menu.faceMode(direction)) {
            case INPUT -> "I";
            case OUTPUT -> "O";
            case BOTH -> "B";
            case DISABLED -> "X";
        };
        return Component.literal(shortName(direction) + ":" + mode);
    }

    private Component redstoneText() {
        return Component.translatable(
                "gui.clayium.fluid.redstone." + menu.redstoneCondition().name().toLowerCase(java.util.Locale.ROOT));
    }

    private Component filterModeText() {
        return Component.translatable(menu.filterBlacklist()
                ? "gui.clayium.fluid.filter.blacklist" : "gui.clayium.fluid.filter.whitelist");
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
