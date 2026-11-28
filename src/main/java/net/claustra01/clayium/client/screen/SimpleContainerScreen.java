package net.claustra01.clayium.client.screen;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.menu.AutoCrafterMenu;
import net.claustra01.clayium.menu.ClayiumBaseMenu;
import net.claustra01.clayium.menu.EnergyOnlyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Shared screen implementation for Auto Clay Condenser, Auto Crafter, Multitrack Buffer,
 * Distributor, Energy-only Fabricators, Whitelist Filter, and Gadget Holder.
 */
public class SimpleContainerScreen<T extends ClayiumBaseMenu> extends ClayiumBaseScreen<T> {
    public SimpleContainerScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (this.menu instanceof AutoCrafterMenu ac) {
            drawClayEnergyText(guiGraphics, ac.getClayEnergy());
        } else if (this.menu instanceof EnergyOnlyMenu eo) {
            drawClayEnergyText(guiGraphics, eo.getClayEnergy());
        }
    }

    @Nullable
    @Override
    protected ClayContainerBlockEntity getEnergyPreviewSource() {
        if (this.menu instanceof AutoCrafterMenu ac) {
            return ac.getCrafter();
        }
        if (this.menu instanceof EnergyOnlyMenu eo) {
            return eo.getContainerEntity();
        }
        return null;
    }
}
