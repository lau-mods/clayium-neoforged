/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.ResonatingCollectorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class ResonatingCollectorScreen extends AbstractContainerScreen<ResonatingCollectorMenu> {
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
    public ResonatingCollectorScreen(ResonatingCollectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageWidth=176; imageHeight=180; inventoryLabelY=86;
    }
    @Override protected void renderBg(GuiGraphics graphics,float partialTick,int mouseX,int mouseY) {
        tile(graphics,BACK,leftPos+4,topPos+4,168,imageHeight-8,8,8);
        tile(graphics,TOP,leftPos+4,topPos,168,4,1,4);
        tile(graphics,BOTTOM,leftPos+4,topPos+imageHeight-4,168,4,1,4);
        tile(graphics,LEFT,leftPos,topPos+4,4,imageHeight-8,4,1);
        tile(graphics,RIGHT,leftPos+172,topPos+4,4,imageHeight-8,4,1);
        whole(graphics,TL,leftPos,topPos); whole(graphics,TR,leftPos+172,topPos);
        whole(graphics,BL,leftPos,topPos+imageHeight-4); whole(graphics,BR,leftPos+172,topPos+imageHeight-4);
        for(int row=0;row<3;row++)for(int column=0;column<3;column++)
            graphics.blit(SLOT,leftPos+61+column*18,topPos+17+row*18,0,0,18,18);
        graphics.blit(PLAYER,leftPos,topPos+86,0,0,176,94);
    }
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        super.renderLabels(graphics,mouseX,mouseY);
        graphics.drawString(font,Component.translatable("gui.clayium_neoforged.resonance",
                String.format(java.util.Locale.ROOT,"%.3f",menu.resonance())),8,74,0x404040,false);
        String progress=String.format(java.util.Locale.ROOT,"%.1f%%",menu.progressRatio()*100.0D);
        graphics.drawString(font,progress,168-font.width(progress),74,0x404040,false);
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
        super.render(graphics,mouseX,mouseY,partialTick); renderTooltip(graphics,mouseX,mouseY);
    }
    private static void tile(GuiGraphics graphics,ResourceLocation texture,int x,int y,int width,int height,int tileWidth,int tileHeight){
        for(int yy=0;yy<height;yy+=tileHeight)for(int xx=0;xx<width;xx+=tileWidth)
            graphics.blit(texture,x+xx,y+yy,0,0,Math.min(tileWidth,width-xx),Math.min(tileHeight,height-yy),tileWidth,tileHeight);
    }
    private static void whole(GuiGraphics graphics,ResourceLocation texture,int x,int y){graphics.blit(texture,x,y,0,0,4,4,4,4);}
}
