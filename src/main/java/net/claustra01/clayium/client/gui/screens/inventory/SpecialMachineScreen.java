/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.machine.SpecialMachineKind;
import net.claustra01.clayium.world.inventory.SpecialMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class SpecialMachineScreen extends AbstractContainerScreen<SpecialMachineMenu> {
    private static final ResourceLocation BACK=Clayium.id("textures/gui/gui_back.png"),TOP=Clayium.id("textures/gui/gui_t.png"),BOTTOM=Clayium.id("textures/gui/gui_b.png"),LEFT=Clayium.id("textures/gui/gui_l.png"),RIGHT=Clayium.id("textures/gui/gui_r.png"),TL=Clayium.id("textures/gui/gui_tl.png"),TR=Clayium.id("textures/gui/gui_tr.png"),BL=Clayium.id("textures/gui/gui_bl.png"),BR=Clayium.id("textures/gui/gui_br.png"),PLAYER=Clayium.id("textures/gui/gui_playerinventory.png"),SLOT=Clayium.id("textures/gui/slot.png"),PROGRESS=Clayium.id("textures/gui/progressbarfurnace.png");
    public SpecialMachineScreen(SpecialMachineMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=176;imageHeight=menu.machineHeight()+94;inventoryLabelY=menu.machineHeight();}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        tile(g,BACK,leftPos+4,topPos+4,168,imageHeight-8,8,8);tile(g,TOP,leftPos+4,topPos,168,4,1,4);tile(g,BOTTOM,leftPos+4,topPos+imageHeight-4,168,4,1,4);tile(g,LEFT,leftPos,topPos+4,4,imageHeight-8,4,1);tile(g,RIGHT,leftPos+172,topPos+4,4,imageHeight-8,4,1);whole(g,TL,leftPos,topPos);whole(g,TR,leftPos+172,topPos);whole(g,BL,leftPos,topPos+imageHeight-4);whole(g,BR,leftPos+172,topPos+imageHeight-4);
        for(SlotPosition p:positions())g.blit(SLOT,leftPos+p.x-1,topPos+p.y-1,0,0,18,18);
        if(menu.kind()!=SpecialMachineKind.AUTO_CLAY_CONDENSER){int width=menu.totalProgress()==0?0:Math.min(22,menu.progress()*22/menu.totalProgress());if(width>0)g.blit(PROGRESS,leftPos+77,topPos+65,0,0,width,7);}
        g.blit(PLAYER,leftPos,topPos+menu.machineHeight(),0,0,176,94);
    }
    private java.util.List<SlotPosition> positions(){java.util.List<SlotPosition> r=new java.util.ArrayList<>();switch(menu.kind()){
        case AUTO_CLAY_CONDENSER->{for(int y=0;y<4;y++)for(int x=0;x<5;x++)r.add(new SlotPosition(44+x*18,18+y*18));r.add(new SlotPosition(152,18));}
        case AUTO_CRAFTER->{for(int y=0;y<3;y++)for(int x=0;x<3;x++){r.add(new SlotPosition(62+x*18,18+y*18));r.add(new SlotPosition(5+x*18,18+y*18));}for(int y=0;y<3;y++)for(int x=0;x<2;x++)r.add(new SlotPosition(135+x*18,18+y*18));if(menu.tier()>=6)r.add(new SlotPosition(8,66));}
        case CHEMICAL_METAL_SEPARATOR->{r.add(new SlotPosition(8,32));for(int y=0;y<2;y++)for(int x=0;x<8;x++)r.add(new SlotPosition(26+x*18,18+y*18));r.add(new SlotPosition(152,54));}
    }return r;}
    private record SlotPosition(int x,int y){}
    private static void tile(GuiGraphics g,ResourceLocation t,int x,int y,int w,int h,int tw,int th){for(int yy=0;yy<h;yy+=th)for(int xx=0;xx<w;xx+=tw)g.blit(t,x+xx,y+yy,0,0,Math.min(tw,w-xx),Math.min(th,h-yy),tw,th);}
    private static void whole(GuiGraphics g,ResourceLocation t,int x,int y){g.blit(t,x,y,0,0,4,4,4,4);}
}
