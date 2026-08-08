/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.PanAdapterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class PanAdapterScreen extends AbstractContainerScreen<PanAdapterMenu> {
    private static final ResourceLocation BACK = Clayium.id("textures/gui/gui_back.png");
    private static final ResourceLocation TOP = Clayium.id("textures/gui/gui_t.png");
    private static final ResourceLocation BOTTOM = Clayium.id("textures/gui/gui_b.png");
    private static final ResourceLocation LEFT = Clayium.id("textures/gui/gui_l.png");
    private static final ResourceLocation RIGHT = Clayium.id("textures/gui/gui_r.png");
    private static final ResourceLocation TL = Clayium.id("textures/gui/gui_tl.png");
    private static final ResourceLocation TR = Clayium.id("textures/gui/gui_tr.png");
    private static final ResourceLocation BL = Clayium.id("textures/gui/gui_bl.png");
    private static final ResourceLocation BR = Clayium.id("textures/gui/gui_br.png");
    private static final ResourceLocation PLAYER = Clayium.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation SLOT = Clayium.id("textures/gui/slot.png");

    public PanAdapterScreen(PanAdapterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageWidth = 176; imageHeight = 190; inventoryLabelY = 96;
    }
    @Override protected void init() {
        super.init();
        if (menu.pages() > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> send(0)).bounds(leftPos + 4, topPos + 36, 16, 16).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> send(1)).bounds(leftPos + 156, topPos + 36, 16, 16).build());
        }
    }
    private void send(int id) { if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id); }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        tile(graphics, BACK, leftPos + 4, topPos + 4, 168, imageHeight - 8, 8, 8);
        tile(graphics, TOP, leftPos + 4, topPos, 168, 4, 1, 4);
        tile(graphics, BOTTOM, leftPos + 4, topPos + imageHeight - 4, 168, 4, 1, 4);
        tile(graphics, LEFT, leftPos, topPos + 4, 4, imageHeight - 8, 4, 1);
        tile(graphics, RIGHT, leftPos + 172, topPos + 4, 4, imageHeight - 8, 4, 1);
        whole(graphics, TL, leftPos, topPos); whole(graphics, TR, leftPos + 172, topPos);
        whole(graphics, BL, leftPos, topPos + imageHeight - 4); whole(graphics, BR, leftPos + 172, topPos + imageHeight - 4);
        for (int row=0;row<3;row++) for(int column=0;column<3;column++) {
            slot(graphics, 32 + column*18, 18 + row*18, 96, 32);
            slot(graphics, 92 + column*18, 18 + row*18, 0, 0);
        }
        for (int column=0;column<9;column++) slot(graphics, 8 + column*18, 74, 0, 0);
        graphics.blit(PLAYER, leftPos, topPos + 96, 0, 0, 176, 94);
    }
    private void slot(GuiGraphics graphics,int x,int y,int u,int v){graphics.blit(SLOT,leftPos+x-1,topPos+y-1,u,v,18,18);}
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY) {
        graphics.drawString(font,title,6,6,0x404040,false);
        if(menu.pages()>1){
            Component page=Component.literal((menu.page()+1)+"/"+menu.pages());
            graphics.drawString(font,page,imageWidth-6-font.width(page),64,0x404040,false);
        }
        graphics.drawString(font,playerInventoryTitle,8,96,0x404040,false);
    }
    private static void tile(GuiGraphics graphics,ResourceLocation texture,int x,int y,int width,int height,int tileWidth,int tileHeight){
        for(int yy=0;yy<height;yy+=tileHeight)for(int xx=0;xx<width;xx+=tileWidth)
            graphics.blit(texture,x+xx,y+yy,0,0,Math.min(tileWidth,width-xx),Math.min(tileHeight,height-yy),tileWidth,tileHeight);
    }
    private static void whole(GuiGraphics graphics,ResourceLocation texture,int x,int y){graphics.blit(texture,x,y,0,0,4,4,4,4);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick){super.render(graphics,mouseX,mouseY,partialTick);renderTooltip(graphics,mouseX,mouseY);}
}
