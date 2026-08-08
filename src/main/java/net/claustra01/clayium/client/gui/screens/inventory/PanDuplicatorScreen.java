/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import net.claustra01.clayium.world.inventory.PanDuplicatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.util.Mth;
import java.util.Locale;

public final class PanDuplicatorScreen extends AbstractDedicatedMachineScreen<PanDuplicatorMenu> {
    public PanDuplicatorScreen(PanDuplicatorMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Override protected void drawDevice(GuiGraphics graphics) {
        smallSlot(graphics,32,35,32,0);
        smallSlot(graphics,50,35,32,32);
        graphics.blit(SLOT,leftPos+111,topPos+30,0,32,26,26);
        graphics.blit(PROGRESS,leftPos+76,topPos+35,0,0,24,17);
        int width=menu.totalProgress()<=0?0:Mth.clamp(menu.progress()*24/menu.totalProgress(),0,24);
        graphics.blit(PROGRESS,leftPos+76,topPos+35,0,17,width,17);
    }
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        super.renderLabels(graphics,mouseX,mouseY);
        Component status=Component.translatable("gui.clayium_neoforged.pan_status."+
                menu.workStatus().name().toLowerCase(Locale.ROOT));
        graphics.drawString(font,status,imageWidth-6-font.width(status),18,0x404040,false);
    }
    @Override protected int tier() { return menu.tier(); }
    @Override protected long energyPerTick() { return menu.energyPerTick(); }
}
