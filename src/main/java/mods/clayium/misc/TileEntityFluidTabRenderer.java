package mods.clayium.misc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TileEntityFluidTabRenderer extends TileEntitySpecialRenderer {
    public TileEntityFluidTabRenderer() {
        super();
    }

    public void renderTileEntityCupAt(TileFluidTab par1Tile, double par2, double par4, double par6, float par8) {
        this.setRotation(par1Tile, (float)par2, (float)par4, (float)par6);
    }

    public void setTileEntityRenderer(TileEntityRendererDispatcher par1TileEntityRenderer) {
        super.func_147497_a(par1TileEntityRenderer);
    }

    public void setRotation(TileFluidTab par0Tile, float par1, float par2, float par3) {
        Tessellator tessellator = Tessellator.field_78398_a;
        if (par0Tile.getFluidIcon() != null) {
            GL11.glPushMatrix();
            GL11.glEnable(32826);
            GL11.glColor4f(2.0F, 2.0F, 2.0F, 0.75F);
            GL11.glTranslatef(par1, par2 + 0.5F, par3);
            GL11.glScalef(1.0F, -1.0F, -1.0F);
            GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
            IIcon iicon = par0Tile.getFluidIcon();
            float f14 = iicon.func_94209_e();
            float f15 = iicon.func_94212_f();
            float f4 = iicon.func_94206_g();
            float f5 = iicon.func_94210_h();
            this.func_147499_a(TextureMap.field_110575_b);
            float f = 0.0625F;
            tessellator.func_78382_b();
            tessellator.func_78375_b(1.0F, 0.0F, 0.0F);
            tessellator.func_78374_a(0.0 + (double)f, -0.4, -1.0 + (double)f, (double)f15, (double)f4);
            tessellator.func_78374_a(1.0 - (double)f, -0.4, -1.0 + (double)f, (double)f14, (double)f4);
            tessellator.func_78374_a(1.0 - (double)f, -0.4, 0.0 - (double)f, (double)f14, (double)f5);
            tessellator.func_78374_a(0.0 + (double)f, -0.4, 0.0 - (double)f, (double)f15, (double)f5);
            tessellator.func_78381_a();
            GL11.glDisable(32826);
            GL11.glPopMatrix();
        }
    }

    public void func_147500_a(TileEntity par1TileEntity, double par2, double par4, double par6, float par8) {
        this.renderTileEntityCupAt((TileFluidTab)par1TileEntity, par2, par4, par6, par8);
    }
}
