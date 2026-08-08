/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.gui.screens.inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.energy.ClayEnergyFormatter;
import net.claustra01.clayium.pan.PanCoreEntry;
import net.claustra01.clayium.world.inventory.PanCoreMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original-style searchable PAN item/cost/consumption browser. */
public final class PanCoreScreen extends AbstractContainerScreen<PanCoreMenu> {
    private static final ResourceLocation BACK=Clayium.id("textures/gui/gui_back.png");
    private static final ResourceLocation TOP=Clayium.id("textures/gui/gui_t.png");
    private static final ResourceLocation BOTTOM=Clayium.id("textures/gui/gui_b.png");
    private static final ResourceLocation LEFT=Clayium.id("textures/gui/gui_l.png");
    private static final ResourceLocation RIGHT=Clayium.id("textures/gui/gui_r.png");
    private static final ResourceLocation TL=Clayium.id("textures/gui/gui_tl.png");
    private static final ResourceLocation TR=Clayium.id("textures/gui/gui_tr.png");
    private static final ResourceLocation BL=Clayium.id("textures/gui/gui_bl.png");
    private static final ResourceLocation BR=Clayium.id("textures/gui/gui_br.png");
    private static final ResourceLocation PLAYER=Clayium.id("textures/gui/gui_playerinventory.png");
    private static final int TABLE_X=11,TABLE_Y=18,TABLE_WIDTH=144,TABLE_HEIGHT=112;
    private EditBox search;
    private Sort sort=Sort.ITEM;
    private boolean reversed;
    private boolean detail;
    private int scrollRow;
    private PanCoreEntry hovered;

    public PanCoreScreen(PanCoreMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);imageWidth=176;imageHeight=PanCoreMenu.MACHINE_HEIGHT+94;inventoryLabelY=PanCoreMenu.MACHINE_HEIGHT;
    }

    @Override protected void init(){
        super.init();
        addRenderableWidget(sortButton("I",Sort.ITEM,88));
        addRenderableWidget(sortButton("M",Sort.COST,104));
        addRenderableWidget(sortButton("C",Sort.CONSUMPTION,120));
        addRenderableWidget(Button.builder(Component.literal("D"),button->{detail=!detail;scrollRow=0;})
                .bounds(leftPos+72,topPos+132,12,12).build());
        search=new EditBox(font,leftPos+88,topPos+132,77,12,Component.translatable("gui.clayium_neoforged.pan_core.search"));
        search.setMaxLength(128);search.setHint(Component.translatable("gui.clayium_neoforged.pan_core.search"));
        search.setResponder(value->scrollRow=0);addRenderableWidget(search);
    }

    private Button sortButton(String text,Sort selected,int x){
        return Button.builder(Component.literal(text),button->{
            if(sort==selected)reversed=!reversed;else{sort=selected;reversed=false;}scrollRow=0;
        }).bounds(leftPos+x,topPos+4,14,12).build();
    }

    @Override protected void renderBg(GuiGraphics graphics,float partialTick,int mouseX,int mouseY){
        tile(graphics,BACK,leftPos+4,topPos+4,168,imageHeight-8,8,8);
        tile(graphics,TOP,leftPos+4,topPos,168,4,1,4);tile(graphics,BOTTOM,leftPos+4,topPos+imageHeight-4,168,4,1,4);
        tile(graphics,LEFT,leftPos,topPos+4,4,imageHeight-8,4,1);tile(graphics,RIGHT,leftPos+172,topPos+4,4,imageHeight-8,4,1);
        whole(graphics,TL,leftPos,topPos);whole(graphics,TR,leftPos+172,topPos);whole(graphics,BL,leftPos,topPos+imageHeight-4);whole(graphics,BR,leftPos+172,topPos+imageHeight-4);
        graphics.fill(leftPos+TABLE_X-1,topPos+TABLE_Y-1,leftPos+TABLE_X+TABLE_WIDTH+11,topPos+TABLE_Y+TABLE_HEIGHT+1,0xff001e00);
        graphics.blit(PLAYER,leftPos,topPos+PanCoreMenu.MACHINE_HEIGHT,0,0,176,94);
    }

    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick){
        super.render(graphics,mouseX,mouseY,partialTick);renderEntries(graphics,mouseX,mouseY);
        if(hovered!=null){
            List<Component> tooltip=new ArrayList<>();tooltip.add(hovered.stack().getHoverName());
            tooltip.add(Component.translatable("gui.clayium_neoforged.pan_core.cost",format(hovered.cost())));
            tooltip.add(Component.translatable("gui.clayium_neoforged.pan_core.consumption",format(hovered.consumption())));
            if(hovered.prohibited())tooltip.add(Component.translatable("gui.clayium_neoforged.pan_core.prohibited"));
            graphics.renderComponentTooltip(font,tooltip,mouseX,mouseY);
        }
    }

    private void renderEntries(GuiGraphics graphics,int mouseX,int mouseY){
        List<PanCoreEntry> entries=visibleEntries();int columns=detail?1:9,rows=7;
        int maxRow=Math.max(0,(entries.size()+columns-1)/columns-rows);scrollRow=Math.min(scrollRow,maxRow);hovered=null;
        int start=scrollRow*columns;
        for(int visible=0;visible<columns*rows&&start+visible<entries.size();visible++){
            PanCoreEntry entry=entries.get(start+visible);int column=visible%columns,row=visible/columns;
            int x=leftPos+TABLE_X+column*16,y=topPos+TABLE_Y+row*16;
            graphics.renderItem(entry.stack(),x,y);
            if(entry.prohibited())graphics.fill(x,y,x+16,y+16,0x78c0001e);
            if(detail){
                graphics.drawString(font,format(entry.cost()),x+24,y+1,0xffdcdcdc,false);
                graphics.drawString(font,format(entry.consumption()),x+82,y+1,0xfffff0b0,false);
            }
            if(mouseX>=x&&mouseX<x+16&&mouseY>=y&&mouseY<y+16)hovered=entry;
        }
        int trackX=leftPos+156,trackY=topPos+TABLE_Y;
        graphics.fill(trackX,trackY,trackX+8,trackY+TABLE_HEIGHT,0xff303030);
        int knob=Math.max(8,TABLE_HEIGHT*rows/Math.max(rows,maxRow+rows));
        int knobY=trackY+(maxRow==0?0:(TABLE_HEIGHT-knob)*scrollRow/maxRow);
        graphics.fill(trackX+1,knobY,trackX+7,knobY+knob,0xffa0a0a0);
    }

    private List<PanCoreEntry> visibleEntries(){
        String query=search==null?"":search.getValue().toLowerCase(Locale.ROOT);
        Comparator<PanCoreEntry> comparator=switch(sort){
            case ITEM->Comparator.comparing(entry->BuiltInRegistries.ITEM.getKey(entry.stack().getItem()).toString());
            case COST->Comparator.comparingDouble(PanCoreEntry::cost);
            case CONSUMPTION->Comparator.comparingDouble(PanCoreEntry::consumption);
        };
        if(reversed)comparator=comparator.reversed();
        return menu.entries().stream().filter(entry->query.isEmpty()||entry.stack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query))
                .sorted(comparator).toList();
    }

    @Override public boolean mouseScrolled(double mouseX,double mouseY,double scrollX,double scrollY){
        if(mouseX>=leftPos+TABLE_X&&mouseX<leftPos+166&&mouseY>=topPos+TABLE_Y&&mouseY<topPos+TABLE_Y+TABLE_HEIGHT){
            scrollRow=Math.max(0,scrollRow+(scrollY<0?1:-1));return true;
        }
        return super.mouseScrolled(mouseX,mouseY,scrollX,scrollY);
    }

    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        graphics.drawString(font,title,6,6,0x404040,false);
        graphics.drawString(font,Component.translatable("gui.clayium_neoforged.pan_core.summary",menu.networkSize(),menu.conversionCount()),6,150,0x404040,false);
        graphics.drawString(font,playerInventoryTitle,8,PanCoreMenu.MACHINE_HEIGHT,0x404040,false);
    }

    private static String format(double value){
        return value<=Double.MAX_VALUE?ClayEnergyFormatter.format(Math.max(0.0D,value)):String.format(Locale.ROOT,"%.3e",value);
    }
    private static void tile(GuiGraphics graphics,ResourceLocation texture,int x,int y,int width,int height,int tw,int th){for(int yy=0;yy<height;yy+=th)for(int xx=0;xx<width;xx+=tw)graphics.blit(texture,x+xx,y+yy,0,0,Math.min(tw,width-xx),Math.min(th,height-yy),tw,th);}
    private static void whole(GuiGraphics graphics,ResourceLocation texture,int x,int y){graphics.blit(texture,x,y,0,0,4,4,4,4);}
    private enum Sort{ITEM,COST,CONSUMPTION}
}
