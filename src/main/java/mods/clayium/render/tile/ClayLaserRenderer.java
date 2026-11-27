package mods.clayium.render.tile;

import mods.clayium.block.laser.ClayLaser;
import mods.clayium.block.laser.IClayLaserManager;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilRender;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ClayLaserRenderer extends TileEntitySpecialRenderer {
    TileEntity tile;

    public ClayLaserRenderer() {
        super();
    }

    public void func_147500_a(TileEntity p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_, float p_147500_8_) {
        this.tile = p_147500_1_;
        if (p_147500_1_ instanceof IClayLaserManager) {
            this.renderClayLaser((IClayLaserManager)p_147500_1_, p_147500_2_, p_147500_4_, p_147500_6_);
        }
    }

    public void renderClayLaser(IClayLaserManager manager, double x, double y, double z) {
        Tessellator tessellator = Tessellator.field_78398_a;
        if (manager.isIrradiating()) {
            UtilRender.setLightValue(15, 15);
            GL11.glPushMatrix();
            GL11.glEnable(32826);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F);
            GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
            switch (manager.getDirection()) {
                case UP:
                    GL11.glRotatef(0.0F, 1.0F, 0.0F, 0.0F);
                    break;
                case DOWN:
                    GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                    break;
                case NORTH:
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                    break;
                case SOUTH:
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                    break;
                case WEST:
                    GL11.glRotatef(-90.0F, 0.0F, 0.0F, -1.0F);
                    break;
                case EAST:
                    GL11.glRotatef(90.0F, 0.0F, 0.0F, -1.0F);
            }

            ClayLaser laser = manager.getClayLaser();
            int[] num = laser.numbers;
            int str = 0;
            int max = 0;

            for (int n : num) {
                str += n;
                max = Math.max(max, n);
            }

            float scale = 1.0F - 14.0625F / (float)(str + 14);
            GL11.glScalef(scale, 1.0F, scale);
            GL11.glTranslatef(0.0F, -scale / 6.0F, 0.0F);
            GL11.glScalef(1.0F, (float)manager.getLaserLength() + scale / 3.0F, 1.0F);
            GL11.glDisable(2896);
            GL11.glDisable(2884);
            GL11.glDepthMask(false);
            this.func_147499_a(new ResourceLocation("clayium", "textures/blocks/laser.png"));
            float f14 = 0.0F;
            float f15 = 1.0F;
            float f4 = 0.0F;
            float f5 = 1.0F;
            int k = ClayiumCore.cfgLaserQuality;
            int i = 0;

            for (int var24 = 0; var24 < k; var24++) {
                tessellator.func_78382_b();
                tessellator.func_78370_a(255 * num[2] / max, 255 * num[1] / max, 255 * num[0] / max, (int)(26.0F + scale * 26.0F) * 8 / k);
                tessellator.func_78374_a(0.0, 1.0, 0.5, (double)f14, (double)f4);
                tessellator.func_78374_a(0.0, 1.0, -0.5, (double)f15, (double)f4);
                tessellator.func_78374_a(0.0, 0.0, -0.5, (double)f15, (double)f5);
                tessellator.func_78374_a(0.0, 0.0, 0.5, (double)f14, (double)f5);
                tessellator.func_78381_a();
                GL11.glRotatef(180.0F / (float)k, 0.0F, 1.0F, 0.0F);
            }

            GL11.glEnable(2896);
            GL11.glEnable(2884);
            GL11.glDisable(32826);
            GL11.glDisable(3042);
            GL11.glDepthMask(true);
            GL11.glPopMatrix();
        }
    }
}
