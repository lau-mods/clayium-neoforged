/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.energy.ClayEnergyFormatter;
import net.claustra01.clayium.world.inventory.SaltExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class SaltExtractorScreen extends AbstractContainerScreen<SaltExtractorMenu>{
    private static final ResourceLocation BACK=Clayium.id("textures/gui/gui_back.png"),PLAYER=Clayium.id("textures/gui/gui_playerinventory.png"),SLOT=Clayium.id("textures/gui/slot.png"),PROGRESS=Clayium.id("textures/gui/progress.png");
    public SaltExtractorScreen(SaltExtractorMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=menu.machineHeight()+94;inventoryLabelY=menu.machineHeight();}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){for(int y=0;y<imageHeight;y+=8)for(int x=0;x<imageWidth;x+=8)g.blit(BACK,leftPos+x,topPos+y,0,0,Math.min(8,imageWidth-x),Math.min(8,imageHeight-y),8,8);int x=(176-menu.columns()*18)/2;for(int slot=0;slot<menu.outputs();slot++)g.blit(SLOT,leftPos+x+slot%menu.columns()*18,topPos+17+slot/menu.columns()*18,0,0,18,18);g.blit(SLOT,leftPos+150,topPos+menu.machineHeight()-23,0,0,18,18);g.blit(PLAYER,leftPos,topPos+menu.machineHeight(),0,0,176,94);int width=menu.totalProgress()==0?0:Mth.clamp(menu.progress()*24/menu.totalProgress(),0,24);g.blit(PROGRESS,leftPos+76,topPos+menu.machineHeight()-23,0,0,24,17);g.blit(PROGRESS,leftPos+76,topPos+menu.machineHeight()-23,0,17,width,17);}
    @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY){g.drawString(font,title,8,6,0x404040,false);g.drawString(font,ClayEnergyFormatter.formatPerTick(menu.energyPerTick()),4,menu.machineHeight()-24,0x404040,false);g.drawString(font,Component.translatable("gui.clayium_neoforged.energy",ClayEnergyFormatter.format(menu.energy())),4,menu.machineHeight()-12,0x404040,false);Component tier=Component.translatable("gui.clayium_neoforged.tier",menu.tier());g.drawString(font,tier,imageWidth-6-font.width(tier),menu.machineHeight()-12,0x404040,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,0x404040,false);}
}
