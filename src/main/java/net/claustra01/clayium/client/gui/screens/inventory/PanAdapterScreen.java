/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.world.inventory.PanAdapterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class PanAdapterScreen extends AbstractContainerScreen<PanAdapterMenu> {
    public PanAdapterScreen(PanAdapterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageWidth = 176; imageHeight = 180; inventoryLabelY = 86;
    }
    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("<"), b -> send(0)).bounds(leftPos + 74, topPos + 72, 20, 16).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> send(1)).bounds(leftPos + 96, topPos + 72, 20, 16).build());
    }
    private void send(int id) { if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id); }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xffc6c6c6);
        graphics.fill(leftPos + 7, topPos + 96, leftPos + 169, topPos + 179, 0xff8b8b8b);
        for (int row=0;row<3;row++) for(int column=0;column<3;column++) {
            slot(graphics, leftPos + 29 + column*18, topPos + 17 + row*18);
            slot(graphics, leftPos + 115 + column*18, topPos + 17 + row*18);
        }
        graphics.drawString(font, Component.literal((menu.page()+1)+" / "+menu.pages()), leftPos+78, topPos+77, 0x404040, false);
    }
    private static void slot(GuiGraphics g,int x,int y){g.fill(x,y,x+18,y+18,0xff373737);g.fill(x+1,y+1,x+17,y+17,0xffeeeeee);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick){super.render(graphics,mouseX,mouseY,partialTick);renderTooltip(graphics,mouseX,mouseY);}
}
