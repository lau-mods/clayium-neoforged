package net.claustra01.clayium.client.screen;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.menu.ClayCraftingTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for Clay Crafting Table (Tier 0) displaying 3x3 grid and optional adjacent chest overlay.
 */
public class ClayCraftingTableScreen extends ClayiumBaseScreen<ClayCraftingTableMenu> {
    public ClayCraftingTableScreen(ClayCraftingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.overlayTexture = ClayiumMod.id("textures/gui/claycraftingtable.png");
    }
}
