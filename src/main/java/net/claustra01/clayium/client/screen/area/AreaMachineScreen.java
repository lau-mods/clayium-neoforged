package net.claustra01.clayium.client.screen.area;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.world.area.AreaMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import net.claustra01.clayium.world.area.AreaMachineControlPayload;

public final class AreaMachineScreen extends AbstractContainerScreen<AreaMachineMenu> {
    private Button targetButton;
    private Button rayTraceButton;
    private Button sneakButton;

    public AreaMachineScreen(AreaMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 215;
        inventoryLabelY = 121;
    }

    @Override
    protected void init() {
        super.init();
        if (!menu.isActivator()) {
            return;
        }
        targetButton = addRenderableWidget(Button.builder(Component.empty(),
                button -> send(0)).bounds(leftPos + 128, topPos + 25, 96, 16).build());
        rayTraceButton = addRenderableWidget(Button.builder(Component.empty(),
                button -> send(1)).bounds(leftPos + 128, topPos + 43, 96, 16).build());
        sneakButton = addRenderableWidget(Button.builder(Component.empty(),
                button -> send(2)).bounds(leftPos + 128, topPos + 61, 96, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.clayium.area.remember"),
                button -> send(3)).bounds(leftPos + 128, topPos + 79, 96, 16).build());
        updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        if (targetButton == null) {
            return;
        }
        String target = switch (menu.activatorTarget()) {
            case 1 -> "entity";
            case 2 -> "both";
            default -> "block";
        };
        targetButton.setMessage(Component.translatable("gui.clayium.area.target",
                Component.translatable("gui.clayium.area.target." + target)));
        rayTraceButton.setMessage(Component.translatable("gui.clayium.area.ray_trace",
                enabled(menu.activatorRayTrace())));
        sneakButton.setMessage(Component.translatable("gui.clayium.area.sneak",
                enabled(menu.activatorSneaking())));
    }

    private static Component enabled(boolean enabled) {
        return Component.translatable(enabled ? "gui.clayium.area.on" : "gui.clayium.area.off");
    }

    private void send(int action) {
        PacketDistributor.sendToServer(new AreaMachineControlPayload(menu.position(), action));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : menu.slots) {
            int x = slot.x;
            int y = slot.y;
            LegacyGuiGraphics.slot(graphics, leftPos + x, topPos + y);
        }
        LegacyGuiGraphics.progress(graphics, leftPos + 180, topPos + 136,
                menu.progress(), menu.requiredProgress());
        graphics.drawString(font, "H", leftPos + 13, topPos + 78, 0x404040, false);
        graphics.drawString(font, "F", leftPos + 31, topPos + 78, 0x404040, false);
        graphics.drawString(font, "S", leftPos + 49, topPos + 78, 0x404040, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 7, 7, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        int statusY = menu.isActivator() ? 99 : 27;
        drawFitted(graphics, menu.status().name(), statusY);
        drawFitted(graphics, String.format(java.util.Locale.ROOT, "%.3f CE", menu.storedEnergy() / 100_000.0D),
                statusY + 11);
        drawFitted(graphics, menu.progress() + "/" + menu.requiredProgress(), statusY + 22);
        if (!menu.isActivator()) {
            drawFitted(graphics, "Mode: " + menu.machineType().name(), 60);
        }
    }

    private void drawFitted(GuiGraphics graphics, String value, int y) {
        float scale = Math.min(1.0F, 95.0F / Math.max(1, font.width(value)));
        graphics.pose().pushPose();
        graphics.pose().translate(128, y, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, value, 0, 0, 0x404040, false);
        graphics.pose().popPose();
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem()) {
            String filter = switch (menu.slots.indexOf(hoveredSlot)) {
                case 1 -> "harvest";
                case 2 -> "fortune";
                case 3 -> "silk_touch";
                default -> null;
            };
            if (filter != null) {
                graphics.renderTooltip(font, Component.translatable("gui.clayium.area.filter." + filter), mouseX, mouseY);
            }
        }
    }
}
