package mods.clayium.render.item;

import java.util.Random;
import mods.clayium.item.IItemExRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import org.lwjgl.opengl.GL11;

public class ItemDamagedRenderer implements IItemRenderer {
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private RenderManager renderManager = RenderManager.field_78727_a;
    private Random random = new Random();
    private RenderBlocks renderBlocksIr = new RenderBlocks();

    public ItemDamagedRenderer() {
        super();
    }

    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        switch (type) {
            case ENTITY:
                return true;
            case EQUIPPED:
                return true;
            case EQUIPPED_FIRST_PERSON:
                return true;
            case INVENTORY:
                return true;
            case FIRST_PERSON_MAP:
                return false;
            default:
                return false;
        }
    }

    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        switch (helper) {
            case ENTITY_ROTATION:
                return this.renderManager.field_78733_k.field_74347_j;
            case ENTITY_BOBBING:
                return true;
            case EQUIPPED_BLOCK:
                return true;
            case BLOCK_3D:
                return false;
            case INVENTORY_BLOCK:
                return false;
            default:
                return false;
        }
    }

    public void renderItem(ItemRenderType type, ItemStack itemstack, Object... data) {
        byte b0 = 1;
        if (itemstack.field_77994_a > 1) {
            b0 = 2;
        }

        if (itemstack.field_77994_a > 5) {
            b0 = 3;
        }

        if (itemstack.field_77994_a > 20) {
            b0 = 4;
        }

        if (itemstack.field_77994_a > 40) {
            b0 = 5;
        }

        b0 = this.getMiniItemCount(itemstack, b0);
        switch (type) {
            case ENTITY:
                RenderBlocks render = (RenderBlocks)data[0];
                EntityItem entity = (EntityItem)data[1];
                GL11.glTranslatef(0.0F, -0.05F, 0.0F);
                Item item = itemstack.func_77973_b();
                if (item instanceof IItemExRenderer) {
                    ((IItemExRenderer)item).preRenderItem(type, itemstack, data);
                }

                for (int j = 0; j < item.getRenderPasses(itemstack.func_77960_j()); j++) {
                    this.random.setSeed(187L);
                    IIcon iicon1 = item.getIcon(itemstack, j);
                    int k = item.func_82790_a(itemstack, j);
                    float f5 = (float)(k >> 16 & 0xFF) / 255.0F;
                    float f6 = (float)(k >> 8 & 0xFF) / 255.0F;
                    float f7 = (float)(k & 0xFF) / 255.0F;
                    GL11.glColor4f(f5, f6, f7, 1.0F);
                    if (item instanceof IItemExRenderer) {
                        ((IItemExRenderer)item).preRenderItemPass(type, itemstack, j, data);
                    }

                    this.renderDroppedItem(entity, iicon1, b0, 0.0F, f5, f6, f7, j);
                    if (item instanceof IItemExRenderer) {
                        ((IItemExRenderer)item).postRenderItemPass(type, itemstack, j, data);
                    }
                }

                if (item instanceof IItemExRenderer) {
                    ((IItemExRenderer)item).postRenderItem(type, itemstack, data);
                }
                break;
            case EQUIPPED:
                RenderBlocks render1 = (RenderBlocks)data[0];
                EntityLivingBase entity1 = (EntityLivingBase)data[1];
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                Item item1 = itemstack.func_77973_b();
                if (item1 instanceof IItemExRenderer) {
                    ((IItemExRenderer)item1).preRenderItem(type, itemstack, data);
                }

                for (int x = 0; x < item1.getRenderPasses(itemstack.func_77960_j()); x++) {
                    int k1 = item1.func_82790_a(itemstack, x);
                    float f10 = (float)(k1 >> 16 & 0xFF) / 255.0F;
                    float f11 = (float)(k1 >> 8 & 0xFF) / 255.0F;
                    float f12 = (float)(k1 & 0xFF) / 255.0F;
                    GL11.glColor4f(1.0F * f10, 1.0F * f11, 1.0F * f12, 1.0F);
                    if (item1 instanceof IItemExRenderer) {
                        ((IItemExRenderer)item1).preRenderItemPass(type, itemstack, x, data);
                    }

                    this.renderItem(entity1, itemstack, x, ItemRenderType.EQUIPPED_FIRST_PERSON);
                    if (item1 instanceof IItemExRenderer) {
                        ((IItemExRenderer)item1).postRenderItemPass(type, itemstack, x, data);
                    }
                }

                if (item1 instanceof IItemExRenderer) {
                    ((IItemExRenderer)item1).postRenderItem(type, itemstack, data);
                }
                break;
            case EQUIPPED_FIRST_PERSON:
                RenderBlocks render2 = (RenderBlocks)data[0];
                EntityLivingBase entity2 = (EntityLivingBase)data[1];
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                Item item2 = itemstack.func_77973_b();
                if (item2 instanceof IItemExRenderer) {
                    ((IItemExRenderer)item2).preRenderItem(type, itemstack, data);
                }

                for (int x = 0; x < item2.getRenderPasses(itemstack.func_77960_j()); x++) {
                    int k1x = item2.func_82790_a(itemstack, x);
                    float f10x = (float)(k1x >> 16 & 0xFF) / 255.0F;
                    float f11x = (float)(k1x >> 8 & 0xFF) / 255.0F;
                    float f12x = (float)(k1x & 0xFF) / 255.0F;
                    GL11.glColor4f(1.0F * f10x, 1.0F * f11x, 1.0F * f12x, 1.0F);
                    if (item2 instanceof IItemExRenderer) {
                        ((IItemExRenderer)item2).preRenderItemPass(type, itemstack, x, data);
                    }

                    this.renderItem(entity2, itemstack, x, ItemRenderType.EQUIPPED_FIRST_PERSON);
                    if (item2 instanceof IItemExRenderer) {
                        ((IItemExRenderer)item2).postRenderItemPass(type, itemstack, x, data);
                    }
                }

                if (item2 instanceof IItemExRenderer) {
                    ((IItemExRenderer)item2).postRenderItem(type, itemstack, data);
                }
                break;
            case INVENTORY:
                RenderBlocks render3 = (RenderBlocks)data[0];
                Item item3 = itemstack.func_77973_b();
                TextureManager texturemanager = Minecraft.func_71410_x().func_110434_K();
                RenderItem renderi = RenderItem.getInstance();
                renderi.field_77023_b = 50.0F;
                OpenGlHelper.func_148821_a(0, 0, 0, 0);
                GL11.glColorMask(false, false, false, true);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                Tessellator tessellator = Tessellator.field_78398_a;
                tessellator.func_78382_b();
                tessellator.func_78378_d(-1);
                tessellator.func_78377_a(-2.0, 18.0, 0.0);
                tessellator.func_78377_a(18.0, 18.0, 0.0);
                tessellator.func_78377_a(18.0, -2.0, 0.0);
                tessellator.func_78377_a(-2.0, -2.0, 0.0);
                tessellator.func_78381_a();
                GL11.glColorMask(true, true, true, true);
                GL11.glEnable(3553);
                GL11.glEnable(3008);
                if (item3 instanceof IItemExRenderer) {
                    ((IItemExRenderer)item3).preRenderItem(type, itemstack, data);
                }

                for (int l = 0; l < item3.getRenderPasses(itemstack.func_77960_j()); l++) {
                    OpenGlHelper.func_148821_a(770, 771, 1, 0);
                    int i1 = item3.func_82790_a(itemstack, l);
                    float fx = (float)(i1 >> 16 & 0xFF) / 255.0F;
                    float f1 = (float)(i1 >> 8 & 0xFF) / 255.0F;
                    float f2 = (float)(i1 & 0xFF) / 255.0F;
                    GL11.glColor4f(fx, f1, f2, 1.0F);
                    GL11.glDisable(2896);
                    GL11.glEnable(3008);
                    if (item3 instanceof IItemExRenderer) {
                        ((IItemExRenderer)item3).preRenderItemPass(type, itemstack, l, data);
                    }

                    texturemanager.func_110577_a(itemstack.func_94608_d() == 0 ? TextureMap.field_110575_b : TextureMap.field_110576_c);
                    IIcon iicon = item3.getIcon(itemstack, l);
                    if (iicon != null) {
                        renderi.func_94149_a(0, 0, iicon, 16, 16);
                    }

                    if (item3 instanceof IItemExRenderer) {
                        ((IItemExRenderer)item3).postRenderItemPass(type, itemstack, l, data);
                    }

                    GL11.glDisable(3008);
                    GL11.glEnable(2896);
                    if (itemstack.hasEffect(l)) {
                        renderi.renderEffect(texturemanager, 0, 0);
                    }
                }

                if (item3 instanceof IItemExRenderer) {
                    ((IItemExRenderer)item3).postRenderItem(type, itemstack, data);
                }
                break;
            case FIRST_PERSON_MAP:
                EntityPlayer player = (EntityPlayer)data[0];
                TextureManager engine = (TextureManager)data[1];
                MapData f = (MapData)data[2];
        }
    }

    private void renderDroppedItem(
        EntityItem p_77020_1_, IIcon p_77020_2_, int p_77020_3_, float p_77020_4_, float p_77020_5_, float p_77020_6_, float p_77020_7_, int pass
    ) {
        Tessellator tessellator = Tessellator.field_78398_a;
        if (p_77020_2_ == null) {
            TextureManager texturemanager = Minecraft.func_71410_x().func_110434_K();
            ResourceLocation resourcelocation = texturemanager.func_130087_a(p_77020_1_.func_92059_d().func_94608_d());
            p_77020_2_ = ((TextureMap)texturemanager.func_110581_b(resourcelocation)).func_110572_b("missingno");
        }

        float f14 = p_77020_2_.func_94209_e();
        float f15 = p_77020_2_.func_94212_f();
        float f4 = p_77020_2_.func_94206_g();
        float f5 = p_77020_2_.func_94210_h();
        float f6 = 1.0F;
        float f7 = 0.5F;
        float f8 = 0.25F;
        if (this.renderManager.field_78733_k.field_74347_j) {
            GL11.glPushMatrix();
            if (RenderItem.field_82407_g) {
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            }

            float f9 = 0.0625F;
            float f10 = 0.021875F;
            ItemStack itemstack = p_77020_1_.func_92059_d();
            int j = itemstack.field_77994_a;
            byte b0;
            if (j < 2) {
                b0 = 1;
            } else if (j < 16) {
                b0 = 2;
            } else if (j < 32) {
                b0 = 3;
            } else {
                b0 = 4;
            }

            b0 = this.getMiniItemCount(itemstack, b0);
            GL11.glTranslatef(-f7, -f8, -((f9 + f10) * (float)b0 / 2.0F));

            for (int k = 0; k < b0; k++) {
                if (k > 0 && this.shouldSpreadItems()) {
                    float x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
                    float y = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
                    float z = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
                    GL11.glTranslatef(x, y, f9 + f10);
                } else {
                    GL11.glTranslatef(0.0F, 0.0F, f9 + f10);
                }

                if (itemstack.func_94608_d() == 0) {
                    this.bindTexture(TextureMap.field_110575_b);
                } else {
                    this.bindTexture(TextureMap.field_110576_c);
                }

                GL11.glColor4f(p_77020_5_, p_77020_6_, p_77020_7_, 1.0F);
                ItemRenderer.func_78439_a(tessellator, f15, f4, f14, f5, p_77020_2_.func_94211_a(), p_77020_2_.func_94216_b(), f9);
                if (itemstack.hasEffect(pass)) {
                    GL11.glDepthFunc(514);
                    GL11.glDisable(2896);
                    this.renderManager.field_78724_e.func_110577_a(RES_ITEM_GLINT);
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(768, 1);
                    float f11 = 0.76F;
                    GL11.glColor4f(0.5F * f11, 0.25F * f11, 0.8F * f11, 1.0F);
                    GL11.glMatrixMode(5890);
                    GL11.glPushMatrix();
                    float f12 = 0.125F;
                    GL11.glScalef(f12, f12, f12);
                    float f13 = (float)(Minecraft.func_71386_F() % 3000L) / 3000.0F * 8.0F;
                    GL11.glTranslatef(f13, 0.0F, 0.0F);
                    GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
                    ItemRenderer.func_78439_a(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 255, 255, f9);
                    GL11.glPopMatrix();
                    GL11.glPushMatrix();
                    GL11.glScalef(f12, f12, f12);
                    f13 = (float)(Minecraft.func_71386_F() % 4873L) / 4873.0F * 8.0F;
                    GL11.glTranslatef(-f13, 0.0F, 0.0F);
                    GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
                    ItemRenderer.func_78439_a(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 255, 255, f9);
                    GL11.glPopMatrix();
                    GL11.glMatrixMode(5888);
                    GL11.glDisable(3042);
                    GL11.glEnable(2896);
                    GL11.glDepthFunc(515);
                }
            }

            GL11.glPopMatrix();
        } else {
            p_77020_5_ = (float)((double)p_77020_5_ / 1.4);
            p_77020_6_ = (float)((double)p_77020_6_ / 1.4);
            p_77020_7_ = (float)((double)p_77020_7_ / 1.4);

            for (int l = 0; l < p_77020_3_; l++) {
                GL11.glPushMatrix();
                if (l > 0) {
                    float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
                    float f16 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
                    float f17 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
                    GL11.glTranslatef(f10, f16, f17);
                }

                if (!RenderItem.field_82407_g) {
                    GL11.glRotatef(180.0F - this.renderManager.field_78735_i, 0.0F, 1.0F, 0.0F);
                }

                if (p_77020_1_.func_92059_d().func_94608_d() == 0) {
                    this.bindTexture(TextureMap.field_110575_b);
                } else {
                    this.bindTexture(TextureMap.field_110576_c);
                }

                GL11.glColor4f(p_77020_5_, p_77020_6_, p_77020_7_, 1.0F);
                tessellator.func_78382_b();
                tessellator.func_78375_b(0.0F, 1.0F, 0.0F);
                tessellator.func_78374_a((double)(0.0F - f7), (double)(0.0F - f8), 0.0, (double)f14, (double)f5);
                tessellator.func_78374_a((double)(f6 - f7), (double)(0.0F - f8), 0.0, (double)f15, (double)f5);
                tessellator.func_78374_a((double)(f6 - f7), (double)(1.0F - f8), 0.0, (double)f15, (double)f4);
                tessellator.func_78374_a((double)(0.0F - f7), (double)(1.0F - f8), 0.0, (double)f14, (double)f4);
                tessellator.func_78375_b(0.0F, -1.0F, 0.0F);
                tessellator.func_78374_a((double)(0.0F - f7), (double)(1.0F - f8), 0.0, (double)f15, (double)f4);
                tessellator.func_78374_a((double)(f6 - f7), (double)(1.0F - f8), 0.0, (double)f14, (double)f4);
                tessellator.func_78374_a((double)(f6 - f7), (double)(0.0F - f8), 0.0, (double)f14, (double)f5);
                tessellator.func_78374_a((double)(0.0F - f7), (double)(0.0F - f8), 0.0, (double)f15, (double)f5);
                tessellator.func_78381_a();
                GL11.glPopMatrix();
            }
        }
    }

    protected void bindTexture(ResourceLocation p_110776_1_) {
        this.renderManager.field_78724_e.func_110577_a(p_110776_1_);
    }

    public byte getMiniItemCount(ItemStack stack, byte original) {
        return original;
    }

    public boolean shouldSpreadItems() {
        return true;
    }

    public void renderItem(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, ItemRenderType type) {
        GL11.glPushMatrix();
        TextureManager texturemanager = Minecraft.func_71410_x().func_110434_K();
        IIcon iicon = p_78443_1_.func_70620_b(p_78443_2_, p_78443_3_);
        if (iicon == null) {
            GL11.glPopMatrix();
        } else {
            texturemanager.func_110577_a(texturemanager.func_130087_a(p_78443_2_.func_94608_d()));
            TextureUtil.func_152777_a(false, false, 1.0F);
            Tessellator tessellator = Tessellator.field_78398_a;
            float f = iicon.func_94209_e();
            float f1 = iicon.func_94212_f();
            float f2 = iicon.func_94206_g();
            float f3 = iicon.func_94210_h();
            float f4 = 0.0F;
            float f5 = 0.3F;
            GL11.glEnable(32826);
            GL11.glTranslatef(-f4, -f5, 0.0F);
            float f6 = 1.5F;
            GL11.glScalef(f6, f6, f6);
            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
            ItemRenderer.func_78439_a(tessellator, f1, f2, f, f3, iicon.func_94211_a(), iicon.func_94216_b(), 0.0625F);
            if (p_78443_2_.hasEffect(p_78443_3_)) {
                GL11.glDepthFunc(514);
                GL11.glDisable(2896);
                texturemanager.func_110577_a(RES_ITEM_GLINT);
                GL11.glEnable(3042);
                OpenGlHelper.func_148821_a(768, 1, 1, 0);
                float f7 = 0.76F;
                GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
                GL11.glMatrixMode(5890);
                GL11.glPushMatrix();
                float f8 = 0.125F;
                GL11.glScalef(f8, f8, f8);
                float f9 = (float)(Minecraft.func_71386_F() % 3000L) / 3000.0F * 8.0F;
                GL11.glTranslatef(f9, 0.0F, 0.0F);
                GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
                ItemRenderer.func_78439_a(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                GL11.glScalef(f8, f8, f8);
                f9 = (float)(Minecraft.func_71386_F() % 4873L) / 4873.0F * 8.0F;
                GL11.glTranslatef(-f9, 0.0F, 0.0F);
                GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
                ItemRenderer.func_78439_a(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
                GL11.glPopMatrix();
                GL11.glMatrixMode(5888);
                GL11.glDisable(3042);
                GL11.glEnable(2896);
                GL11.glDepthFunc(515);
            }

            GL11.glDisable(32826);
            texturemanager.func_110577_a(texturemanager.func_130087_a(p_78443_2_.func_94608_d()));
            TextureUtil.func_147945_b();
            GL11.glPopMatrix();
        }
    }
}
