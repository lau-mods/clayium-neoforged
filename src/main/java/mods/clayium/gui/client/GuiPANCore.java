package mods.clayium.gui.client;

import cpw.mods.fml.client.config.GuiButtonExt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import mods.clayium.block.tile.TilePANCore;
import mods.clayium.core.ClayiumCore;
import mods.clayium.gui.container.ContainerTemp;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiPANCore extends GuiClayEnergyTemp {
    private List<TilePANCore.ItemStackWithEnergy> conversionList;
    private Set<TilePANCore.ItemStackWithEnergy> prohibitedList;
    private Comparator<TilePANCore.ItemStackWithEnergy> comp = new TilePANCore.ItemStackComparator();
    private GuiPANCore.ReversibleComparator<TilePANCore.ItemStackWithEnergy>[] comparators = new GuiPANCore.ReversibleComparator[3];
    protected int slidery;
    protected int sliderymax;
    protected GuiPANCore.GuiSlider slider;
    protected int offsetX;
    protected int offsetY;
    protected int tableX;
    protected int tableY;
    protected int sliderWidth;
    protected int marginR;
    protected int marginB;
    protected int tableWidth;
    protected int tableHeight;
    protected boolean detail;
    private TilePANCore.ItemStackWithEnergy tooltipStack;

    public GuiPANCore(ContainerTemp container, TilePANCore tile, Block block) {
        super(container, tile, block);
        this.comparators[0] = new GuiPANCore.ReversibleComparator<>(this.comp);
        this.comparators[1] = new GuiPANCore.ReversibleComparator<>(new GuiPANCore.CostComparator());
        this.comparators[2] = new GuiPANCore.ReversibleComparator<>(new GuiPANCore.ConsumptionComparator());
        this.slidery = 0;
        this.sliderymax = 0;
        this.tableX = 11;
        this.tableY = 18;
        this.sliderWidth = 10;
        this.marginR = 11;
        this.marginB = 14;
        this.detail = false;
        this.tooltipStack = null;
    }

    public void addButton() {
        ContainerTemp container = (ContainerTemp)this.field_147002_h;
        this.offsetX = (this.field_146294_l - this.field_146999_f) / 2;
        this.offsetY = (this.field_146295_m - this.field_147000_g) / 2;
        this.field_146292_n.add(new GuiButtonExt(0, this.offsetX + container.machineGuiSizeX - 80 - 6, 5, 12, 12, "I"));
        this.field_146292_n.add(new GuiButtonExt(1, this.offsetX + container.machineGuiSizeX - 64 - 6, 5, 12, 12, "M"));
        this.field_146292_n.add(new GuiButtonExt(2, this.offsetX + container.machineGuiSizeX - 48 - 6, 5, 12, 12, "C"));
        this.field_146292_n.add(new GuiButtonExt(3, this.offsetX + container.machineGuiSizeX - 32 - 4, 5, 30, 12, "Dump"));
        this.tableWidth = container.machineGuiSizeX - this.tableX - this.sliderWidth - this.marginR;
        this.tableHeight = container.machineGuiSizeY - this.tableY - this.marginB;
        this.slider = new GuiPANCore.GuiSlider(
            4, this.offsetX + this.tableX + this.tableWidth, this.offsetY + this.tableY, this.sliderWidth, this.tableHeight, false
        );
        this.field_146292_n.add(this.slider);
        this.field_146292_n
            .add(new GuiButtonExt(5, this.offsetX + this.field_146999_f / 2 - 16, this.offsetY + this.tableY + this.tableHeight + 3, 12, 12, "D"));
    }

    public void addTextField() {
        GuiTextField textField = new GuiTextField(
            this.field_146289_q,
            this.offsetX + this.field_146999_f / 2,
            this.offsetY + this.tableY + this.tableHeight + 3,
            this.field_146999_f / 2 - this.marginR,
            12
        );
        textField.func_146193_g(-1);
        textField.func_146204_h(-1);
        textField.func_146185_a(true);
        textField.func_146203_f(128);
        this.addTextFieldToList(textField);
    }

    protected void func_146284_a(GuiButton guibutton) {
        super.func_146284_a(guibutton);
        int id = guibutton.field_146127_k;
        if (id <= 2 && this.conversionList != null) {
            this.comparators[id].setReversed(!this.comparators[id].isReversed());
            Collections.sort(this.conversionList, this.comparators[id]);
        }

        if (id == 3 && this.conversionList != null) {
            Pattern pattern = null;

            try {
                pattern = Pattern.compile(this.textFields.get(0).func_146179_b(), 2);
            } catch (PatternSyntaxException var8) {
                ClayiumCore.logger.error("Illegal Pattern! \n" + var8.getMessage());
            }

            for (TilePANCore.ItemStackWithEnergy item : this.conversionList) {
                boolean flag = false;
                if (pattern != null && item.itemstack != null) {
                    Matcher matcher = pattern.matcher(item.itemstack.func_82833_r());
                    flag = matcher.find();
                }

                if (item.itemstack != null && flag) {
                    this.field_146297_k.field_71439_g.func_145747_a(new ChatComponentText(item.toString()));
                }
            }
        }

        if (id == 5) {
            this.detail = !this.detail;
        }
    }

    public void func_146976_a(float partialTick, int mouseX, int mouseZ) {
        super.func_146976_a(partialTick, mouseX, mouseZ);
        func_73734_a(
            this.offsetX + this.tableX - 1,
            this.offsetY + this.tableY - 1,
            this.offsetX + this.tableX + this.tableWidth + this.sliderWidth + 1,
            this.offsetY + this.tableY + this.tableHeight + 1,
            -16769536
        );
    }

    public void func_146274_d() {
        super.func_146274_d();
        int i = Mouse.getEventDWheel();
        if (i > 0) {
            this.slider.sliderNormalizedPos = this.slider.sliderNormalizedPos - 1.0F / (float)(this.sliderymax + 1);
        } else if (i < 0) {
            this.slider.sliderNormalizedPos = this.slider.sliderNormalizedPos + 1.0F / (float)(this.sliderymax + 1);
        }

        this.slider.sliderNormalizedPos = Math.min(1.0F, Math.max(this.slider.sliderNormalizedPos, 0.0F));
    }

    public void setItemList(Set<TilePANCore.ItemStackWithEnergy> ingreds, Set<TilePANCore.ItemStackWithEnergy> prohibiteds) {
        if (ingreds != null) {
            this.conversionList = new ArrayList<>();
            this.conversionList.addAll(ingreds);
            Collections.sort(this.conversionList, this.comp);
            this.prohibitedList = prohibiteds;
        }
    }

    public void func_146979_b(int mouseX, int mouseZ) {
        super.func_146979_b(mouseX, mouseZ);
        GL11.glPushMatrix();
        RenderHelper.func_74520_c();
        GL11.glDisable(2896);
        GL11.glEnable(32826);
        GL11.glEnable(2903);
        if (this.conversionList != null) {
            this.slidery = Math.min((int)((float)(this.sliderymax + 1) * this.slider.sliderNormalizedPos), this.sliderymax);
            this.tooltipStack = null;
            int ix = Math.max(this.tableWidth / 16, 1);
            int iy = Math.max(this.tableHeight / 16, 1);
            if (this.detail) {
                ix = 1;
            }

            int count = 0;
            Pattern pattern = null;

            try {
                pattern = Pattern.compile(this.textFields.get(0).func_146179_b(), 2);
            } catch (PatternSyntaxException var15) {
                ClayiumCore.logger.error("Illegal Pattern! \n" + var15.getMessage());
            }

            for (TilePANCore.ItemStackWithEnergy item : this.conversionList) {
                boolean flag = false;
                if (pattern != null && item.itemstack != null) {
                    Matcher matcher = pattern.matcher(item.itemstack.func_82833_r());
                    flag = matcher.find();
                }

                if (item.itemstack != null && flag) {
                    int px = count % ix;
                    int py = count / ix;
                    if (py >= this.slidery && py < this.slidery + iy) {
                        int cx = this.tableX + px * 16;
                        int cy = this.tableY + (py - this.slidery) * 16;
                        if (this.prohibitedList.contains(item)) {
                            GL11.glDisable(2896);
                            GL11.glDisable(2929);
                            RenderHelper.func_74518_a();
                            func_73734_a(cx, cy, cx + 16, cy + 16, 2026380830);
                            RenderHelper.func_74520_c();
                            GL11.glEnable(2896);
                            GL11.glEnable(2929);
                        }

                        this.drawItemStack(item, cx, cy, mouseX, mouseZ);
                        if (this.detail) {
                            this.field_146289_q.func_78276_b(UtilLocale.ClayEnergyNumeral(item.cost) + "CE", cx + 24, cy + 4, -2302756);
                            this.field_146289_q
                                .func_78276_b(
                                    UtilLocale.ClayEnergyNumeral(item.consumption) + "CE", cx + 24 + (this.tableWidth - cx - 24) / 2 + 6, cy + 4, -986896
                                );
                        }
                    }

                    count++;
                }
            }

            this.sliderymax = Math.max((count - 1) / ix - iy + 1, 0);
        }

        GL11.glPopMatrix();
        GL11.glEnable(2896);
        GL11.glEnable(2929);
        RenderHelper.func_74520_c();
    }

    public void func_73863_a(int mouseX, int mouseZ, float p_73863_3_) {
        super.func_73863_a(mouseX, mouseZ, p_73863_3_);
        if (this.tooltipStack != null && this.tooltipStack.itemstack != null) {
            ItemStack stack = this.tooltipStack.itemstack;
            this.renderToolTipWithAdditionalText(
                stack,
                mouseX,
                mouseZ,
                " [" + UtilLocale.ClayEnergyNumeral(this.tooltipStack.cost) + "CE : " + UtilLocale.ClayEnergyNumeral(this.tooltipStack.consumption) + "CE ]"
            );
            GL11.glDisable(2896);
            GL11.glEnable(32826);
            GL11.glEnable(2903);
        }
    }

    protected void drawItemStack(TilePANCore.ItemStackWithEnergy itemstack, int x, int y, int mousex, int mousey) {
        if (itemstack != null && itemstack.itemstack != null) {
            RenderHelper.func_74520_c();
            GL11.glEnable(2896);
            field_146296_j.field_77023_b = -10.0F;
            GL11.glEnable(32826);
            GL11.glEnable(2929);
            field_146296_j.func_82406_b(this.field_146289_q, this.field_146297_k.func_110434_K(), itemstack.itemstack, x, y);
            field_146296_j.func_77021_b(this.field_146289_q, this.field_146297_k.func_110434_K(), itemstack.itemstack, x, y);
            if (this.func_146978_c(x + 1, y + 1, 14, 14, mousex, mousey)) {
                this.tooltipStack = itemstack;
            }
        }
    }

    protected void renderToolTipWithAdditionalText(ItemStack p_146285_1_, int p_146285_2_, int p_146285_3_, String string) {
        List list = p_146285_1_.func_82840_a(this.field_146297_k.field_71439_g, this.field_146297_k.field_71474_y.field_82882_x);
        list.add(1, EnumChatFormatting.WHITE + string);

        for (int k = 0; k < list.size(); k++) {
            if (k == 0) {
                list.set(
                    k,
                    p_146285_1_.func_77953_t().field_77937_e
                        + (String)list.get(k)
                        + " "
                        + Item.func_150891_b(p_146285_1_.func_77973_b())
                        + (p_146285_1_.func_77960_j() != 0 ? ":" + p_146285_1_.func_77960_j() : "")
                );
            } else {
                list.set(k, EnumChatFormatting.GRAY + (String)list.get(k));
            }
        }

        FontRenderer font = p_146285_1_.func_77973_b().getFontRenderer(p_146285_1_);
        this.drawHoveringText(list, p_146285_2_, p_146285_3_, font == null ? this.field_146289_q : font);
    }

    static class ConsumptionComparator implements Comparator<TilePANCore.ItemStackWithEnergy> {
        ConsumptionComparator() {
            super();
        }

        public int compare(TilePANCore.ItemStackWithEnergy o1, TilePANCore.ItemStackWithEnergy o2) {
            double d1 = o1 == null ? 0.0 : o1.consumption;
            double d2 = o2 == null ? 0.0 : o2.consumption;
            if (d1 > d2) {
                return 1;
            } else {
                return d1 < d2 ? -1 : 0;
            }
        }
    }

    static class CostComparator implements Comparator<TilePANCore.ItemStackWithEnergy> {
        CostComparator() {
            super();
        }

        public int compare(TilePANCore.ItemStackWithEnergy o1, TilePANCore.ItemStackWithEnergy o2) {
            double d1 = o1 == null ? 0.0 : o1.cost;
            double d2 = o2 == null ? 0.0 : o2.cost;
            if (d1 > d2) {
                return 1;
            } else {
                return d1 < d2 ? -1 : 0;
            }
        }
    }

    public class GuiSlider extends GuiButton {
        public float sliderNormalizedPos = 0.0F;
        protected boolean isDragged = false;
        protected int knobLength = 10;
        protected boolean horizontal = false;

        public GuiSlider(int buttonId, int x, int y, int sizeX, int sizeY, boolean horizontal) {
            super(buttonId, x, y, sizeX, sizeY, "");
            this.horizontal = horizontal;
        }

        public int func_146114_a(boolean p_146114_1_) {
            return 0;
        }

        protected void func_146119_b(Minecraft minecraft, int mouseX, int mouseY) {
            if (this.field_146125_m) {
                if (this.isDragged) {
                    if (this.horizontal) {
                        this.sliderNormalizedPos = (float)(mouseX - (this.field_146128_h + this.knobLength / 2))
                            / (float)(this.field_146120_f - this.knobLength);
                    } else {
                        this.sliderNormalizedPos = (float)(mouseY - (this.field_146129_i + this.knobLength / 2))
                            / (float)(this.field_146121_g - this.knobLength);
                    }

                    if (this.sliderNormalizedPos < 0.0F) {
                        this.sliderNormalizedPos = 0.0F;
                    }

                    if (this.sliderNormalizedPos > 1.0F) {
                        this.sliderNormalizedPos = 1.0F;
                    }
                }

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                if (!this.horizontal) {
                    func_73734_a(
                        this.field_146128_h + 1,
                        this.field_146129_i + (int)(this.sliderNormalizedPos * (float)(this.field_146121_g - this.knobLength)) + 1,
                        this.field_146128_h + this.field_146120_f - 1,
                        this.field_146129_i
                            + (int)(this.sliderNormalizedPos * (float)(this.field_146121_g - this.knobLength))
                            + this.knobLength / 2
                            + this.knobLength / 2
                            - 1,
                        -14795746
                    );
                }
            }
        }

        public void func_146112_a(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_) {
            if (this.field_146125_m) {
                func_73734_a(
                    this.field_146128_h, this.field_146129_i, this.field_146128_h + this.field_146120_f, this.field_146129_i + this.field_146121_g, -6232416
                );
                this.func_146119_b(p_146112_1_, p_146112_2_, p_146112_3_);
            }
        }

        public boolean func_146116_c(Minecraft p_146116_1_, int p_146116_2_, int p_146116_3_) {
            if (super.func_146116_c(p_146116_1_, p_146116_2_, p_146116_3_)) {
                this.sliderNormalizedPos = (float)(p_146116_2_ - (this.field_146128_h + 4)) / (float)(this.field_146120_f - 8);
                if (this.sliderNormalizedPos < 0.0F) {
                    this.sliderNormalizedPos = 0.0F;
                }

                if (this.sliderNormalizedPos > 1.0F) {
                    this.sliderNormalizedPos = 1.0F;
                }

                this.isDragged = true;
                return true;
            } else {
                return false;
            }
        }

        public void func_146118_a(int p_146118_1_, int p_146118_2_) {
            this.isDragged = false;
        }
    }

    static class ReversibleComparator<T> implements Comparator<T> {
        private Comparator<T>[] comps;
        private boolean reversed;

        public boolean isReversed() {
            return this.reversed;
        }

        public void setReversed(boolean reversed) {
            this.reversed = reversed;
        }

        ReversibleComparator(Comparator<T>[] comps) {
            super();
            this.comps = comps;
        }

        ReversibleComparator(Comparator<T> comp) {
            super();
            this.comps = new Comparator[]{comp};
        }

        public int compare(T o1, T o2) {
            if (this.comps != null) {
                for (Comparator<T> comp : this.comps) {
                    int c = comp.compare(o1, o2);
                    if (c != 0) {
                        return this.reversed ? -c : c;
                    }
                }
            }

            return 0;
        }
    }
}
