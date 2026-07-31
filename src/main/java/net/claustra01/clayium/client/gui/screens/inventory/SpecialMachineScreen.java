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
import net.claustra01.clayium.energy.ClayEnergyFormatter;
import net.minecraft.util.Mth;

public final class SpecialMachineScreen extends AbstractContainerScreen<SpecialMachineMenu> {
    private static final ResourceLocation BACK=Clayium.id("textures/gui/gui_back.png"),TOP=Clayium.id("textures/gui/gui_t.png"),BOTTOM=Clayium.id("textures/gui/gui_b.png"),LEFT=Clayium.id("textures/gui/gui_l.png"),RIGHT=Clayium.id("textures/gui/gui_r.png"),TL=Clayium.id("textures/gui/gui_tl.png"),TR=Clayium.id("textures/gui/gui_tr.png"),BL=Clayium.id("textures/gui/gui_bl.png"),BR=Clayium.id("textures/gui/gui_br.png"),PLAYER=Clayium.id("textures/gui/gui_playerinventory.png"),SLOT=Clayium.id("textures/gui/slot.png"),PROGRESS=Clayium.id("textures/gui/progressbarfurnace.png");
    public SpecialMachineScreen(SpecialMachineMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=176;imageHeight=menu.machineHeight()+94;titleLabelX=6;titleLabelY=6;inventoryLabelX=8;inventoryLabelY=menu.machineHeight();}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        tile(g,BACK,leftPos+4,topPos+4,168,imageHeight-8,8,8);tile(g,TOP,leftPos+4,topPos,168,4,1,4);tile(g,BOTTOM,leftPos+4,topPos+imageHeight-4,168,4,1,4);tile(g,LEFT,leftPos,topPos+4,4,imageHeight-8,4,1);tile(g,RIGHT,leftPos+172,topPos+4,4,imageHeight-8,4,1);whole(g,TL,leftPos,topPos);whole(g,TR,leftPos+172,topPos);whole(g,BL,leftPos,topPos+imageHeight-4);whole(g,BR,leftPos+172,topPos+imageHeight-4);
        drawMachineSlots(g);
        if(menu.kind()==SpecialMachineKind.CHEMICAL_METAL_SEPARATOR){
            g.blit(PROGRESS,leftPos+55,topPos+44,0,0,24,17);
            int width=menu.totalProgress()<=0?0:Mth.clamp(menu.progress()*24/menu.totalProgress(),0,24);
            if(width>0)g.blit(PROGRESS,leftPos+55,topPos+44,0,17,width,17);
        }
        g.blit(PLAYER,leftPos,topPos+menu.machineHeight(),0,0,176,94);
    }
    private void drawMachineSlots(GuiGraphics g){switch(menu.kind()){
        case AUTO_CLAY_CONDENSER->{for(int y=0;y<4;y++)for(int x=0;x<5;x++)small(g,43+x*18,18+y*18,0,0);small(g,151,18,96,0);}
        case AUTO_CRAFTER->{for(int y=0;y<3;y++)for(int x=0;x<3;x++){small(g,62+x*18,18+y*18,0,0);small(g,5+x*18,18+y*18,96,32);}for(int y=0;y<3;y++)for(int x=0;x<2;x++)small(g,135+x*18,18+y*18,0,0);}
        case CHEMICAL_METAL_SEPARATOR->{g.blit(SLOT,leftPos+20,topPos+39,0,32,26,26);for(int y=0;y<4;y++)for(int x=0;x<4;x++)small(g,85+x*18,17+y*18,0,0);}
    }}
    private void small(GuiGraphics g,int x,int y,int u,int v){g.blit(SLOT,leftPos+x-1,topPos+y-1,u,v,18,18);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,title,titleLabelX,titleLabelY,0x404040,false);
        if(menu.energyPerTick()>0)g.drawString(font,ClayEnergyFormatter.formatPerTick(menu.energyPerTick()),4,menu.machineHeight()-24,0x404040,false);
        g.drawString(font,Component.translatable("gui.clayium_neoforged.energy",ClayEnergyFormatter.format(menu.energy())),4,menu.machineHeight()-12,0x404040,false);
        Component tier=Component.translatable("gui.clayium_neoforged.tier",menu.tier());g.drawString(font,tier,imageWidth-6-font.width(tier),menu.machineHeight()-12,0x404040,false);
        g.drawString(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0x404040,false);
    }
    private static void tile(GuiGraphics g,ResourceLocation t,int x,int y,int w,int h,int tw,int th){for(int yy=0;yy<h;yy+=th)for(int xx=0;xx<w;xx+=tw)g.blit(t,x+xx,y+yy,0,0,Math.min(tw,w-xx),Math.min(th,h-yy),tw,th);}
    private static void whole(GuiGraphics g,ResourceLocation t,int x,int y){g.blit(t,x,y,0,0,4,4,4,4);}
}
