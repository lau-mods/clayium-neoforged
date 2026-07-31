/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.FluidBufferMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class FluidBufferScreen extends AbstractContainerScreen<FluidBufferMenu> {
    private static final ResourceLocation PLAYER=Clayium.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation BACK=Clayium.id("textures/gui/gui_back.png");
    public FluidBufferScreen(FluidBufferMenu menu, Inventory inv, Component title){super(menu,inv,title);imageHeight=166;inventoryLabelY=72;}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        for(int y=0;y<72;y+=8) for(int x=0;x<176;x+=8) g.blit(BACK,leftPos+x,topPos+y,0,0,Math.min(8,176-x),Math.min(8,72-y),8,8);
        g.blit(PLAYER,leftPos,topPos+72,0,0,176,94);
        g.fill(leftPos+79,topPos+17,leftPos+97,topPos+65,0xff202020);
        int h=menu.capacity()<=0?0:(int)(46L*menu.amount()/menu.capacity());
        g.fill(leftPos+81,topPos+63-h,leftPos+95,topPos+63,0xff3f76e4);
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){super.renderLabels(g,mx,my); Component amount=Component.literal(menu.amount()+" / "+menu.capacity()+" mB");g.drawString(font,amount,(imageWidth-font.width(amount))/2,66,0x404040,false);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
