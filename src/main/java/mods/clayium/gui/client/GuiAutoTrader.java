package mods.clayium.gui.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileAutoTrader;
import mods.clayium.gui.container.ContainerAutoTrader;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import org.lwjgl.opengl.GL11;

public class GuiAutoTrader extends GuiClayEnergyTemp {
    private static final ResourceLocation field_147038_v = new ResourceLocation("textures/gui/container/villager.png");
    private GuiAutoTrader.MerchantButton field_147043_x;
    private GuiAutoTrader.MerchantButton field_147042_y;
    private TileAutoTrader trader;
    private int field_147041_z;

    public GuiAutoTrader(ContainerAutoTrader container, TileAutoTrader tile, Block block) {
        super(container, tile, block);
        this.trader = tile;
        this.overlayTexture = new ResourceLocation("clayium", "textures/gui/autotrader.png");
    }

    public void addButton() {
        int i = (this.field_146294_l - this.field_146999_f) / 2;
        int j = (this.field_146295_m - this.field_147000_g) / 2;
        this.field_146292_n.add(this.field_147043_x = new GuiAutoTrader.MerchantButton(1, i + 120 + 27, j + 24 - 1, true));
        this.field_146292_n.add(this.field_147042_y = new GuiAutoTrader.MerchantButton(2, i + 36 - 19, j + 24 - 1, false));
        this.field_147043_x.field_146124_l = false;
        this.field_147042_y.field_146124_l = false;
    }

    public void func_73876_c() {
        super.func_73876_c();
        if (this.trader.merchantRecipeList != null) {
            this.field_147043_x.field_146124_l = this.trader.currentRecipeIndex < this.trader.merchantRecipeList.size() - 1;
            this.field_147042_y.field_146124_l = this.trader.currentRecipeIndex > 0;
        } else {
            this.field_147042_y.field_146124_l = this.field_147043_x.field_146124_l = false;
        }
    }

    protected void func_146976_a(float partialTick, int mouseX, int mouseZ) {
        super.func_146976_a(partialTick, mouseX, mouseZ);
        if (this.trader.merchantRecipeList != null && !this.trader.merchantRecipeList.isEmpty()) {
            int i1 = this.trader.currentRecipeIndex;
            if (i1 >= 0 && i1 < this.trader.merchantRecipeList.size()) {
                MerchantRecipe merchantrecipe = (MerchantRecipe)this.trader.merchantRecipeList.get(i1);
                if (merchantrecipe.func_82784_g()) {
                    this.field_146297_k.func_110434_K().func_110577_a(field_147038_v);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDisable(2896);
                    this.func_73729_b(this.field_147003_i + 83, this.field_147009_r + 21, 212, 0, 28, 21);
                    this.func_73729_b(this.field_147003_i + 83, this.field_147009_r + 51, 212, 0, 28, 21);
                }
            }
        }
    }

    public void func_73863_a(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        super.func_73863_a(p_73863_1_, p_73863_2_, p_73863_3_);
        if (this.trader.merchantRecipeList != null && !this.trader.merchantRecipeList.isEmpty()) {
            int k = (this.field_146294_l - this.field_146999_f) / 2;
            int l = (this.field_146295_m - this.field_147000_g) / 2;
            int i1 = this.trader.currentRecipeIndex;
            if (i1 >= 0 && i1 < this.trader.merchantRecipeList.size()) {
                MerchantRecipe merchantrecipe = (MerchantRecipe)this.trader.merchantRecipeList.get(i1);
                GL11.glPushMatrix();
                ItemStack itemstack = merchantrecipe.func_77394_a();
                ItemStack itemstack1 = merchantrecipe.func_77396_b();
                ItemStack itemstack2 = merchantrecipe.func_77397_d();
                RenderHelper.func_74520_c();
                GL11.glDisable(2896);
                GL11.glEnable(32826);
                GL11.glEnable(2903);
                GL11.glEnable(2896);
                field_146296_j.field_77023_b = 100.0F;
                field_146296_j.func_82406_b(this.field_146289_q, this.field_146297_k.func_110434_K(), itemstack, k + 36, l + 24);
                field_146296_j.func_77021_b(this.field_146289_q, this.field_146297_k.func_110434_K(), itemstack, k + 36, l + 24);
                if (itemstack1 != null) {
                    field_146296_j.func_82406_b(this.field_146289_q, this.field_146297_k.func_110434_K(), itemstack1, k + 62, l + 24);
                    field_146296_j.func_77021_b(this.field_146289_q, this.field_146297_k.func_110434_K(), itemstack1, k + 62, l + 24);
                }

                field_146296_j.func_82406_b(this.field_146289_q, this.field_146297_k.func_110434_K(), itemstack2, k + 120, l + 24);
                field_146296_j.func_77021_b(this.field_146289_q, this.field_146297_k.func_110434_K(), itemstack2, k + 120, l + 24);
                field_146296_j.field_77023_b = 0.0F;
                GL11.glDisable(2896);
                if (this.func_146978_c(36, 24, 16, 16, p_73863_1_, p_73863_2_)) {
                    this.func_146285_a(itemstack, p_73863_1_, p_73863_2_);
                } else if (itemstack1 != null && this.func_146978_c(62, 24, 16, 16, p_73863_1_, p_73863_2_)) {
                    this.func_146285_a(itemstack1, p_73863_1_, p_73863_2_);
                } else if (this.func_146978_c(120, 24, 16, 16, p_73863_1_, p_73863_2_)) {
                    this.func_146285_a(itemstack2, p_73863_1_, p_73863_2_);
                }

                GL11.glPopMatrix();
                GL11.glEnable(2896);
                GL11.glEnable(2929);
                RenderHelper.func_74519_b();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    static class MerchantButton extends GuiButton {
        private final boolean field_146157_o;
        private static final String __OBFID = "CL_00000763";

        public MerchantButton(int p_i1095_1_, int p_i1095_2_, int p_i1095_3_, boolean p_i1095_4_) {
            super(p_i1095_1_, p_i1095_2_, p_i1095_3_, 12, 19, "");
            this.field_146157_o = p_i1095_4_;
        }

        public void func_146112_a(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_) {
            if (this.field_146125_m) {
                p_146112_1_.func_110434_K().func_110577_a(GuiAutoTrader.field_147038_v);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                boolean flag = p_146112_2_ >= this.field_146128_h
                    && p_146112_3_ >= this.field_146129_i
                    && p_146112_2_ < this.field_146128_h + this.field_146120_f
                    && p_146112_3_ < this.field_146129_i + this.field_146121_g;
                int k = 0;
                int l = 176;
                if (!this.field_146124_l) {
                    l += this.field_146120_f * 2;
                } else if (flag) {
                    l += this.field_146120_f;
                }

                if (!this.field_146157_o) {
                    k += this.field_146121_g;
                }

                this.func_73729_b(this.field_146128_h, this.field_146129_i, l, k, this.field_146120_f, this.field_146121_g);
            }
        }
    }
}
