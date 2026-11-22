package net.claustra01.clayium.client.screen.area;

import net.claustra01.clayium.client.screen.logistics.LogisticsBackground;
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
        imageHeight = 182;
        inventoryLabelY = 88;
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
        LogisticsBackground.draw(graphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : menu.slots) {
            int x = slot.x;
            int y = slot.y;
            graphics.fill(leftPos + x - 1, topPos + y - 1, leftPos + x + 17, topPos + y + 17, 0xFF555555);
            graphics.fill(leftPos + x, topPos + y, leftPos + x + 16, topPos + y + 16, 0xFFAAAAAA);
        }
        graphics.drawString(font, "H", leftPos + 13, topPos + 78, 0x404040, false);
        graphics.drawString(font, "F", leftPos + 31, topPos + 78, 0x404040, false);
        graphics.drawString(font, "S", leftPos + 49, topPos + 78, 0x404040, false);
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
