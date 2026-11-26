package net.claustra01.clayium.client.screen.area;

import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.client.screen.legacy.LegacyNumerals;
import net.claustra01.clayium.client.screen.legacy.LegacyPictureButton;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.world.area.AreaGuiHost;
import net.claustra01.clayium.world.area.AreaMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original GuiAreaMiner (GUI 16) and GuiAreaActivator (GUI 24). */
public final class AreaMachineScreen extends AbstractContainerScreen<AreaMachineMenu> {
    private LegacyPictureButton targetButton;
    private LegacyPictureButton rayTraceButton;
    private LegacyPictureButton sneakButton;

    public AreaMachineScreen(AreaMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = menu.machineHeight() + 94;
        inventoryLabelX = 8;
        inventoryLabelY = menu.machineHeight();
    }

    @Override
    protected void init() {
        super.init();
        boolean miner = !menu.isActivator();
        // GuiAreaMiner.addButton: Tier >= 9 shifts the buttons 10 px left; Tier 10 stacks them.
        int x = leftPos + (miner && menu.guiTier() >= 9 ? -10 : 0);
        int y = topPos;
        int[][] positions = miner && menu.guiTier() > 9
                ? new int[][] {{16, 16}, {16, 34}, {16, 52}, {16, 70}}
                : new int[][] {{16, 16}, {34, 16}, {16, 34}, {34, 34}};
        int[] textures = {16, 32, 64, 48};
        String[] names = {"start", "stop", "loop", "box"};
        for (int index = 0; index < 4; index++) {
            int action = index;
            addRenderableWidget(new LegacyPictureButton(x + positions[index][0], y + positions[index][1],
                    LegacyPictureButton.BUTTONS, textures[index], 0,
                    Component.translatable("gui.clayium.area." + names[index]), () -> click(action)));
        }
        if (menu.isActivator()) {
            int modeX = leftPos + imageWidth - 16 - 16;
            targetButton = addRenderableWidget(new LegacyPictureButton(modeX, topPos + 16,
                    LegacyPictureButton.BUTTONS, 16 * 5, 0, Component.translatable("gui.clayium.area.target"),
                    () -> click(AreaGuiHost.BUTTON_TARGET)));
            rayTraceButton = addRenderableWidget(new LegacyPictureButton(modeX, topPos + 34,
                    LegacyPictureButton.BUTTONS, 16 * 8, 0, Component.translatable("gui.clayium.area.ray_trace"),
                    () -> click(AreaGuiHost.BUTTON_RAY_TRACE)));
            sneakButton = addRenderableWidget(new LegacyPictureButton(modeX, topPos + 52,
                    LegacyPictureButton.BUTTONS, 16 * 10, 0, Component.translatable("gui.clayium.area.sneak"),
                    () -> click(AreaGuiHost.BUTTON_SNEAK)));
            updateModeButtons();
        }
    }

    private void click(int action) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateModeButtons();
    }

    /** GuiAreaActivator.func_146979_b re-textures the mode buttons every frame. */
    private void updateModeButtons() {
        if (targetButton == null) {
            return;
        }
        targetButton.setTexturePosition(16 * (5 + menu.activatorTarget()), 0);
        rayTraceButton.setTexturePosition(16 * (8 + (menu.activatorRayTrace() ? 1 : 0)), 0);
        sneakButton.setTexturePosition(16 * (10 + (menu.activatorSneaking() ? 1 : 0)), 0);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + menu.machineHeight(),
                0, 0, 176, 94, 256, 256);
        for (int index = 0; index < menu.gridSlots(); index++) {
            LegacyGuiGraphics.slot(graphics, leftPos + menu.slots.get(index).x, topPos + menu.slots.get(index).y);
        }
        for (int index = menu.gridSlots(); index < menu.gridSlots() + menu.filterSlots(); index++) {
            LegacyGuiGraphics.slot(graphics, leftPos + menu.slots.get(index).x, topPos + menu.slots.get(index).y,
                    LegacyGuiGraphics.SlotTexture.FILTER);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int machineY = menu.machineHeight();
        graphics.drawString(font, title, 6, 6, LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.clayium.machine.energy",
                ClayEnergy.ofInternal(menu.storedEnergy()).formatGui()), 4, machineY - 12,
                LegacyGuiGraphics.TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.clayium.machine.laser",
                LegacyNumerals.stackSize(menu.laserEnergy())), 64, machineY - 12,
                LegacyGuiGraphics.TEXT_COLOR, false);
        if (!menu.isActivator() && menu.guiTier() >= 9) {
            String[] keys = {"harvest", "fortune", "silk_touch"};
            for (int index = 0; index < keys.length; index++) {
                graphics.drawString(font, Component.translatable("gui.clayium.area.filter." + keys[index]),
                        imageWidth - 48, 21 + index * 18, LegacyGuiGraphics.TEXT_COLOR, false);
            }
        }
    }
}
