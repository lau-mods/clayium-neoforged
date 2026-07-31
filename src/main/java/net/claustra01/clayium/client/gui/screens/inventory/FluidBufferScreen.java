/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.FluidBufferMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;

public final class FluidBufferScreen extends AbstractContainerScreen<FluidBufferMenu> {
    private static final ResourceLocation PLAYER=Clayium.id("textures/gui/gui_playerinventory.png");
    private static final ResourceLocation BACK=Clayium.id("textures/gui/gui_back.png");
    private static final ResourceLocation TOP=Clayium.id("textures/gui/gui_t.png"),BOTTOM=Clayium.id("textures/gui/gui_b.png"),LEFT=Clayium.id("textures/gui/gui_l.png"),RIGHT=Clayium.id("textures/gui/gui_r.png"),TOP_LEFT=Clayium.id("textures/gui/gui_tl.png"),TOP_RIGHT=Clayium.id("textures/gui/gui_tr.png"),BOTTOM_LEFT=Clayium.id("textures/gui/gui_bl.png"),BOTTOM_RIGHT=Clayium.id("textures/gui/gui_br.png");
    public FluidBufferScreen(FluidBufferMenu menu, Inventory inv, Component title){super(menu,inv,title);imageHeight=166;inventoryLabelY=72;}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        tile(g,BACK,leftPos+4,topPos+4,168,imageHeight-8,8,8);
        tile(g,TOP,leftPos+4,topPos,168,4,1,4);tile(g,BOTTOM,leftPos+4,topPos+imageHeight-4,168,4,1,4);tile(g,LEFT,leftPos,topPos+4,4,imageHeight-8,4,1);tile(g,RIGHT,leftPos+172,topPos+4,4,imageHeight-8,4,1);
        whole(g,TOP_LEFT,leftPos,topPos);whole(g,TOP_RIGHT,leftPos+172,topPos);whole(g,BOTTOM_LEFT,leftPos,topPos+imageHeight-4);whole(g,BOTTOM_RIGHT,leftPos+172,topPos+imageHeight-4);
        g.blit(PLAYER,leftPos,topPos+72,0,0,176,94);
        g.fill(leftPos+79,topPos+17,leftPos+97,topPos+65,0xff202020);
        int h=menu.capacity()<=0?0:(int)(46L*menu.amount()/menu.capacity());
        drawFluid(g,h);
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){super.renderLabels(g,mx,my); Component amount=Component.literal(menu.amount()+" / "+menu.capacity()+" mB");g.drawString(font,amount,(imageWidth-font.width(amount))/2,66,0x404040,false);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
    private void drawFluid(GuiGraphics g,int height){
        var fluid=menu.fluid();
        if(height<=0||fluid.isEmpty())return;
        var extensions=IClientFluidTypeExtensions.of(fluid.getFluidType());
        int tint=extensions.getTintColor(fluid);
        float alpha=((tint>>>24)&255)/255.0F;
        g.setColor(((tint>>>16)&255)/255.0F,((tint>>>8)&255)/255.0F,(tint&255)/255.0F,alpha==0?1.0F:alpha);
        int x=leftPos+81,bottom=topPos+63,top=bottom-height;
        var sprite=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(extensions.getStillTexture(fluid));
        g.enableScissor(x,top,x+14,bottom);
        for(int y=bottom-16;y>=top-16;y-=16)g.blit(x,y,0,14,16,sprite);
        g.disableScissor();
        g.setColor(1,1,1,1);
    }
    private static void tile(GuiGraphics g,ResourceLocation texture,int x,int y,int width,int height,int tw,int th){for(int dy=0;dy<height;dy+=th)for(int dx=0;dx<width;dx+=tw)g.blit(texture,x+dx,y+dy,0,0,Math.min(tw,width-dx),Math.min(th,height-dy),tw,th);}
    private static void whole(GuiGraphics g,ResourceLocation texture,int x,int y){g.blit(texture,x,y,0,0,4,4,4,4);}
}
