package mods.clayium.util;

import mods.clayium.block.tile.IAxisAlignedBBContainer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

public class UtilRender {
    public static UtilRender INSTANCE = new UtilRender();

    public UtilRender() {
        super();
    }

    public static void renderInvCuboid(RenderBlocks renderer, Block block, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, IIcon icon) {
        Tessellator tessellator = Tessellator.field_78398_a;
        float minX_ = (float)block.func_149704_x();
        float maxX_ = (float)block.func_149753_y();
        float minY_ = (float)block.func_149665_z();
        float maxY_ = (float)block.func_149669_A();
        float minZ_ = (float)block.func_149706_B();
        float maxZ_ = (float)block.func_149693_C();
        block.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        renderer.func_147775_a(block);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        block.func_149676_a(minX, minY, minZ, maxX, maxY, maxZ);
        renderer.func_147775_a(block);
        tessellator.func_78382_b();
        tessellator.func_78375_b(0.0F, -1.0F, 0.0F);
        renderer.func_147768_a(block, 0.0, 0.0, 0.0, icon);
        tessellator.func_78381_a();
        tessellator.func_78382_b();
        tessellator.func_78375_b(0.0F, 1.0F, 0.0F);
        renderer.func_147806_b(block, 0.0, 0.0, 0.0, icon);
        tessellator.func_78381_a();
        tessellator.func_78382_b();
        tessellator.func_78375_b(0.0F, 0.0F, -1.0F);
        renderer.func_147764_f(block, 0.0, 0.0, 0.0, icon);
        tessellator.func_78381_a();
        tessellator.func_78382_b();
        tessellator.func_78375_b(0.0F, 0.0F, 1.0F);
        renderer.func_147798_e(block, 0.0, 0.0, 0.0, icon);
        tessellator.func_78381_a();
        tessellator.func_78382_b();
        tessellator.func_78375_b(-1.0F, 0.0F, 0.0F);
        renderer.func_147761_c(block, 0.0, 0.0, 0.0, icon);
        tessellator.func_78381_a();
        tessellator.func_78382_b();
        tessellator.func_78375_b(1.0F, 0.0F, 0.0F);
        renderer.func_147734_d(block, 0.0, 0.0, 0.0, icon);
        tessellator.func_78381_a();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        block.func_149676_a(minX_, minY_, minZ_, maxX_, maxY_, maxZ_);
        renderer.func_147775_a(block);
    }

    public static void renderAxisAlignedBB(IAxisAlignedBBContainer tile, float r, float g, float b, float alpha, boolean border) {
        int state = tile.getBoxAppearance();
        int pass = MinecraftForgeClient.getRenderPass();
        if (state != 0 && pass == 1 && tile.hasAxisAlignedBB()) {
            double minX = tile.getAxisAlignedBB().field_72340_a - TileEntityRendererDispatcher.field_147554_b;
            double minY = tile.getAxisAlignedBB().field_72338_b - TileEntityRendererDispatcher.field_147555_c;
            double minZ = tile.getAxisAlignedBB().field_72339_c - TileEntityRendererDispatcher.field_147552_d;
            double maxX = tile.getAxisAlignedBB().field_72336_d - TileEntityRendererDispatcher.field_147554_b;
            double maxY = tile.getAxisAlignedBB().field_72337_e - TileEntityRendererDispatcher.field_147555_c;
            double maxZ = tile.getAxisAlignedBB().field_72334_f - TileEntityRendererDispatcher.field_147552_d;
            GL11.glDepthMask(false);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(32823);
            GL11.glPolygonOffset(-1.0F, -1.0F);
            GL11.glDisable(2912);
            if (state == 2) {
                GL11.glDisable(2929);
            }

            GL11.glDisable(2896);
            GL11.glDisable(2884);
            Tessellator tessellator = Tessellator.field_78398_a;
            GL11.glDisable(3553);
            tessellator.func_78382_b();
            tessellator.func_78369_a(r, g, b, alpha);
            tessellator.func_78377_a(minX, minY, minZ);
            tessellator.func_78377_a(minX, minY, maxZ);
            tessellator.func_78377_a(minX, maxY, maxZ);
            tessellator.func_78377_a(minX, maxY, minZ);
            tessellator.func_78377_a(maxX, minY, minZ);
            tessellator.func_78377_a(maxX, minY, maxZ);
            tessellator.func_78377_a(maxX, maxY, maxZ);
            tessellator.func_78377_a(maxX, maxY, minZ);
            tessellator.func_78377_a(minX, minY, minZ);
            tessellator.func_78377_a(minX, minY, maxZ);
            tessellator.func_78377_a(maxX, minY, maxZ);
            tessellator.func_78377_a(maxX, minY, minZ);
            tessellator.func_78377_a(minX, maxY, minZ);
            tessellator.func_78377_a(minX, maxY, maxZ);
            tessellator.func_78377_a(maxX, maxY, maxZ);
            tessellator.func_78377_a(maxX, maxY, minZ);
            tessellator.func_78377_a(minX, minY, minZ);
            tessellator.func_78377_a(maxX, minY, minZ);
            tessellator.func_78377_a(maxX, maxY, minZ);
            tessellator.func_78377_a(minX, maxY, minZ);
            tessellator.func_78377_a(minX, minY, maxZ);
            tessellator.func_78377_a(maxX, minY, maxZ);
            tessellator.func_78377_a(maxX, maxY, maxZ);
            tessellator.func_78377_a(minX, maxY, maxZ);
            tessellator.func_78381_a();
            if (border) {
                GL11.glLineWidth(2.0F);
                tessellator.func_78371_b(2);
                tessellator.func_78369_a(r, g, b, 1.0F);
                tessellator.func_78377_a(minX, minY, minZ);
                tessellator.func_78377_a(minX, minY, maxZ);
                tessellator.func_78377_a(maxX, minY, maxZ);
                tessellator.func_78377_a(maxX, minY, minZ);
                tessellator.func_78381_a();
                tessellator.func_78371_b(2);
                tessellator.func_78369_a(r, g, b, 1.0F);
                tessellator.func_78377_a(minX, maxY, minZ);
                tessellator.func_78377_a(minX, maxY, maxZ);
                tessellator.func_78377_a(maxX, maxY, maxZ);
                tessellator.func_78377_a(maxX, maxY, minZ);
                tessellator.func_78381_a();
                tessellator.func_78371_b(1);
                tessellator.func_78369_a(r, g, b, 1.0F);
                tessellator.func_78377_a(minX, minY, minZ);
                tessellator.func_78377_a(minX, maxY, minZ);
                tessellator.func_78377_a(minX, minY, maxZ);
                tessellator.func_78377_a(minX, maxY, maxZ);
                tessellator.func_78377_a(maxX, minY, minZ);
                tessellator.func_78377_a(maxX, maxY, minZ);
                tessellator.func_78377_a(maxX, minY, maxZ);
                tessellator.func_78377_a(maxX, maxY, maxZ);
                tessellator.func_78381_a();
            }

            GL11.glEnable(3553);
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
            GL11.glEnable(2912);
            if (state == 2) {
                GL11.glEnable(2929);
            }

            GL11.glDisable(32823);
            GL11.glEnable(2896);
            GL11.glEnable(2884);
        }
    }

    public static void renderAxisAlignedBB(IAxisAlignedBBContainer tile, float r, float g, float b) {
        renderAxisAlignedBB(tile, r, g, b, 0.3F, true);
    }

    public static void renderBox(
        double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int state, float r, float g, float b, float alpha, boolean border
    ) {
        renderAxisAlignedBB(INSTANCE.new AABBforRender(minX, minY, minZ, maxX, maxY, maxZ, state), r, g, b, alpha, border);
    }

    public static void renderBox(
        double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int state, float r, float g, float b, boolean border
    ) {
        renderBox(minX, minY, minZ, maxX, maxY, maxZ, state, r, g, b, 0.3F, true);
    }

    public static void renderBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int state, float r, float g, float b) {
        renderBox(minX, minY, minZ, maxX, maxY, maxZ, state, r, g, b, true);
    }

    public static void setLightValue(int sky, int block) {
        int i = sky << 20 | block << 4;
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, (float)j / 1.0F, (float)k / 1.0F);
    }

    public class AABBforRender implements IAxisAlignedBBContainer {
        private AxisAlignedBB aabb;
        private int state;

        public AABBforRender(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int state) {
            super();
            this.aabb = AxisAlignedBB.func_72330_a(minX, minY, minZ, maxX, maxY, maxZ);
            this.state = state;
        }

        public AxisAlignedBB getAxisAlignedBB() {
            return this.aabb;
        }

        public boolean hasAxisAlignedBB() {
            return true;
        }

        public int getBoxAppearance() {
            return this.state;
        }

        public void setAxisAlignedBB(AxisAlignedBB aabb) {
            this.aabb = aabb;
        }
    }
}
