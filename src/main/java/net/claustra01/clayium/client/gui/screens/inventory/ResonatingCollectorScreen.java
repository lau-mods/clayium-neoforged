/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.world.inventory.ResonatingCollectorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class ResonatingCollectorScreen extends AbstractContainerScreen<ResonatingCollectorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    public ResonatingCollectorScreen(ResonatingCollectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageHeight=166; inventoryLabelY=72;
    }
    @Override protected void renderBg(GuiGraphics graphics,float partialTick,int mouseX,int mouseY) {
        int x=(width-imageWidth)/2, y=(height-imageHeight)/2;
        graphics.blit(TEXTURE,x,y,0,0,imageWidth,71);
        graphics.blit(TEXTURE,x,y+71,0,126,imageWidth,96);
    }
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        super.renderLabels(graphics,mouseX,mouseY);
        graphics.drawString(font,Component.translatable("gui.clayium_neoforged.resonance",
                String.format(java.util.Locale.ROOT,"%.3f",menu.resonance())),8,60,0x404040,false);
        String progress=String.format(java.util.Locale.ROOT,"%.1f%%",menu.progressRatio()*100.0D);
        graphics.drawString(font,progress,168-font.width(progress),60,0x404040,false);
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
        super.render(graphics,mouseX,mouseY,partialTick); renderTooltip(graphics,mouseX,mouseY);
    }
}
