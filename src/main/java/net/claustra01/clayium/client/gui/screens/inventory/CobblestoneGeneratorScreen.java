/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.CobblestoneGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class CobblestoneGeneratorScreen extends AbstractContainerScreen<CobblestoneGeneratorMenu> {
    private static final ResourceLocation BACK=Clayium.id("textures/gui/gui_back.png"),TOP=Clayium.id("textures/gui/gui_t.png"),BOTTOM=Clayium.id("textures/gui/gui_b.png"),LEFT=Clayium.id("textures/gui/gui_l.png"),RIGHT=Clayium.id("textures/gui/gui_r.png"),TL=Clayium.id("textures/gui/gui_tl.png"),TR=Clayium.id("textures/gui/gui_tr.png"),BL=Clayium.id("textures/gui/gui_bl.png"),BR=Clayium.id("textures/gui/gui_br.png"),PLAYER=Clayium.id("textures/gui/gui_playerinventory.png"),SLOT=Clayium.id("textures/gui/slot.png");
    public CobblestoneGeneratorScreen(CobblestoneGeneratorMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageHeight=menu.machineHeight()+94;inventoryLabelY=menu.machineHeight();}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){tile(g,BACK,leftPos+4,topPos+4,168,imageHeight-8,8,8);tile(g,TOP,leftPos+4,topPos,168,4,1,4);tile(g,BOTTOM,leftPos+4,topPos+imageHeight-4,168,4,1,4);tile(g,LEFT,leftPos,topPos+4,4,imageHeight-8,4,1);tile(g,RIGHT,leftPos+172,topPos+4,4,imageHeight-8,4,1);whole(g,TL,leftPos,topPos);whole(g,TR,leftPos+172,topPos);whole(g,BL,leftPos,topPos+imageHeight-4);whole(g,BR,leftPos+172,topPos+imageHeight-4);int ox=(176-menu.columns()*18)/2+1;for(int s=0;s<menu.columns()*menu.rows();s++)g.blit(SLOT,leftPos+ox+s%menu.columns()*18-1,topPos+17+s/menu.columns()*18,0,0,18,18);g.blit(PLAYER,leftPos,topPos+menu.machineHeight(),0,0,176,94);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,6,0x404040,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,0x404040,false);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
    private static void tile(GuiGraphics g,ResourceLocation t,int x,int y,int w,int h,int tw,int th){for(int dy=0;dy<h;dy+=th)for(int dx=0;dx<w;dx+=tw)g.blit(t,x+dx,y+dy,0,0,Math.min(tw,w-dx),Math.min(th,h-dy),tw,th);}private static void whole(GuiGraphics g,ResourceLocation t,int x,int y){g.blit(t,x,y,0,0,4,4,4,4);}
}
