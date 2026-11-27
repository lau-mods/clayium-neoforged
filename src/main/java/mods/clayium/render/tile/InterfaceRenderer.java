package mods.clayium.render.tile;

import mods.clayium.block.tile.ISynchronizedInterface;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilRender;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class InterfaceRenderer extends TileEntitySpecialRenderer {
    private int count = 0;

    public InterfaceRenderer() {
        super();
    }

    public void func_147500_a(TileEntity tile, double x, double y, double z, float partialTicktime) {
        if (tile != null
            && tile instanceof ISynchronizedInterface
            && ((ISynchronizedInterface)tile).isSynced()
            && ((ISynchronizedInterface)tile).acceptCoordChanger()) {
            MovingObjectPosition p = Minecraft.func_71410_x().field_71451_h.func_70614_a(9999.0, 0.0F);
            boolean b = p.field_72313_a == MovingObjectType.BLOCK
                && p.field_72311_b == tile.field_145851_c
                && p.field_72312_c == tile.field_145848_d
                && p.field_72309_d == tile.field_145849_e;
            if (b) {
                ISynchronizedInterface in = (ISynchronizedInterface)tile;
                World cworld = in.getCoreWorld();
                if (cworld != null) {
                    int cx = tile.field_145851_c + in.getCoreBlockXCoord();
                    int cy = tile.field_145848_d + in.getCoreBlockYCoord();
                    int cz = tile.field_145849_e + in.getCoreBlockZCoord();
                    Block cblock = cworld.func_147439_a(
                        tile.field_145851_c + in.getCoreBlockXCoord(),
                        tile.field_145848_d + in.getCoreBlockYCoord(),
                        tile.field_145849_e + in.getCoreBlockZCoord()
                    );
                    if (cblock != null) {
                        ItemStack itemstack = UtilBuilder.getItemBlock(cworld, cx, cy, cz);
                        float ticktime = (float)tile.func_145831_w().func_72820_D() + partialTicktime;
                        GL11.glPushMatrix();
                        GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
                        float f = 0.8F;
                        ForgeDirection direction = ForgeDirection.getOrientation(p.field_72310_e);
                        switch (direction) {
                            case UP:
                                GL11.glTranslatef(0.0F, f, 0.0F);
                                break;
                            case DOWN:
                                GL11.glTranslatef(0.0F, -f, 0.0F);
                                break;
                            case NORTH:
                                GL11.glTranslatef(0.0F, 0.0F, -f);
                                break;
                            case SOUTH:
                                GL11.glTranslatef(0.0F, 0.0F, f);
                                break;
                            case WEST:
                                GL11.glTranslatef(-f, 0.0F, 0.0F);
                                break;
                            case EAST:
                                GL11.glTranslatef(f, 0.0F, 0.0F);
                        }

                        GL11.glPushMatrix();
                        GL11.glTranslatef(0.0F, 0.05F, 0.0F);
                        GL11.glRotatef(ticktime * 2.0F, 0.0F, 1.0F, 0.0F);
                        float f1 = 0.25F;
                        GL11.glScalef(f1, f1, f1);
                        GL11.glTranslatef(0.0F, 0.0F, 0.0F);
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);
                        GL11.glEnable(32826);
                        GL11.glAlphaFunc(516, 0.0F);
                        GL11.glEnable(3042);
                        OpenGlHelper.func_148821_a(770, 771, 1, 0);
                        TextureManager texturemanager = Minecraft.func_71410_x().func_110434_K();
                        texturemanager.func_110577_a(texturemanager.func_130087_a(itemstack.func_94608_d()));
                        RenderBlocks render = RenderBlocks.getInstance();
                        render.field_147844_c = false;
                        render.func_147800_a(cblock, itemstack.func_77960_j(), 0.0F);
                        GL11.glPopMatrix();
                        GL11.glDisable(2896);
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                        GL11.glEnable(32826);
                        GL11.glAlphaFunc(516, 0.0F);
                        GL11.glEnable(3042);
                        GL11.glPushMatrix();
                        GL11.glTranslatef(0.0F, 0.4F, 0.0F);
                        GL11.glRotatef(180.0F - RenderManager.field_78727_a.field_78735_i, 0.0F, 1.0F, 0.0F);
                        GL11.glPushMatrix();
                        f1 = 0.01F;
                        GL11.glScalef(f1, f1, f1);
                        GL11.glDisable(2929);
                        drawString(itemstack.func_82833_r());
                        GL11.glEnable(2929);
                        GL11.glPopMatrix();
                        GL11.glTranslatef(0.0F, -0.1F, 0.0F);
                        GL11.glPushMatrix();
                        f1 = 0.005F;
                        GL11.glScalef(f1, f1, f1);
                        GL11.glDisable(2929);
                        drawString("" + cx + "," + cy + "," + cz + ";" + cworld.field_73011_w.func_80007_l());
                        GL11.glEnable(2929);
                        GL11.glPopMatrix();
                        GL11.glPopMatrix();
                        GL11.glEnable(2896);
                        GL11.glPopMatrix();
                        if (cworld.field_73011_w.field_76574_g == tile.func_145831_w().field_73011_w.field_76574_g) {
                            float cr = (float)(Math.sin((double)(ticktime * 0.1F) + 0.0) + 1.0) * 0.5F;
                            float cg = (float)(Math.sin((double)(ticktime * 0.1F) + (Math.PI * 2.0 / 3.0)) + 1.0) * 0.5F;
                            float cb = (float)(Math.sin((double)(ticktime * 0.1F) + (Math.PI * 4.0 / 3.0)) + 1.0) * 0.5F;
                            float cf = (float)(Math.sin((double)(ticktime * 0.12F) + 0.0) + 1.0) * 0.15F + 0.2F;
                            UtilRender.renderBox(
                                (double)cx + cblock.func_149704_x(),
                                (double)cy + cblock.func_149665_z(),
                                (double)cz + cblock.func_149706_B(),
                                (double)cx + cblock.func_149753_y(),
                                (double)cy + cblock.func_149669_A(),
                                (double)cz + cblock.func_149693_C(),
                                2,
                                cr,
                                cg,
                                cb,
                                cf,
                                false
                            );
                            Tessellator tessellator = Tessellator.field_78398_a;
                            GL11.glDisable(3553);
                            GL11.glDisable(2929);
                            GL11.glAlphaFunc(516, 0.1F);
                            GL11.glEnable(3042);
                            OpenGlHelper.func_148821_a(770, 771, 1, 0);
                            tessellator.func_78371_b(1);
                            tessellator.func_78369_a(cr, cg, cb, cf);
                            tessellator.func_78377_a(x + 0.5, y + 0.5, z + 0.5);
                            tessellator.func_78377_a(
                                x + (double)in.getCoreBlockXCoord() + 0.5, y + (double)in.getCoreBlockYCoord() + 0.5, z + (double)in.getCoreBlockZCoord() + 0.5
                            );
                            tessellator.func_78381_a();
                            GL11.glEnable(2929);
                            GL11.glEnable(3553);
                        }

                        GL11.glAlphaFunc(516, 0.1F);
                        GL11.glEnable(3042);
                        OpenGlHelper.func_148821_a(770, 771, 1, 0);
                    }
                }
            }
        }
    }

    public static void drawString(String s) {
        GL11.glPushMatrix();
        GL11.glScalef(1.0F, -1.0F, 1.0F);
        FontRenderer fontrenderer = RenderManager.field_78727_a.func_78716_a();
        int i = fontrenderer.func_78256_a(s);
        GL11.glEnable(32823);
        GL11.glPolygonOffset(0.1F, 1.0F);
        GL11.glDisable(3553);
        Tessellator tessellator = Tessellator.field_78398_a;
        tessellator.func_78382_b();
        i /= 2;
        tessellator.func_78369_a(0.0F, 0.0F, 0.0F, 0.5F);
        tessellator.func_78377_a((double)(-i - 1), -1.0, 0.0);
        tessellator.func_78377_a((double)(-i - 1), 8.0, 0.0);
        tessellator.func_78377_a((double)(i + 1), 8.0, 0.0);
        tessellator.func_78377_a((double)(i + 1), -1.0, 0.0);
        tessellator.func_78381_a();
        GL11.glEnable(3553);
        GL11.glDisable(32823);
        fontrenderer.func_78276_b(s, -fontrenderer.func_78256_a(s) / 2, 0, -1);
        GL11.glPopMatrix();
    }
}
