package mods.clayium.misc;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.core.ClayiumCore;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderFluidTab implements ISimpleBlockRenderingHandler {
    private IIcon boxIIcon;

    public RenderFluidTab() {
        super();
    }

    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        this.boxIIcon = Blocks.field_150344_f.func_149733_h(1);
        if (modelID == this.getRenderId()) {
            this.renderInvCuboid(renderer, block, 0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F, this.boxIIcon);
            this.renderInvCuboid(renderer, block, 0.0F, 0.125F, 0.0F, 1.0F, 1.0F, 0.0625F, this.boxIIcon);
            this.renderInvCuboid(renderer, block, 0.0F, 0.125F, 0.9375F, 1.0F, 1.0F, 1.0F, this.boxIIcon);
            this.renderInvCuboid(renderer, block, 0.0F, 0.125F, 0.0625F, 0.0625F, 1.0F, 0.9375F, this.boxIIcon);
            this.renderInvCuboid(renderer, block, 0.9375F, 0.125F, 0.0625F, 1.0F, 1.0F, 0.9375F, this.boxIIcon);
        }
    }

    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        this.boxIIcon = Blocks.field_150344_f.func_149733_h(1);
        if (modelId == this.getRenderId()) {
            renderer.func_147757_a(this.boxIIcon);
            block.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            renderer.func_147757_a(this.boxIIcon);
            block.func_149676_a(0.0F, 0.125F, 0.0F, 1.0F, 1.0F, 0.0625F);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            renderer.func_147757_a(this.boxIIcon);
            block.func_149676_a(0.0F, 0.125F, 0.9375F, 1.0F, 1.0F, 1.0F);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            renderer.func_147757_a(this.boxIIcon);
            block.func_149676_a(0.0F, 0.125F, 0.0625F, 0.0625F, 1.0F, 0.9375F);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            renderer.func_147757_a(this.boxIIcon);
            block.func_149676_a(0.9375F, 0.125F, 0.0625F, 1.0F, 1.0F, 0.9375F);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            renderer.func_147771_a();
            block.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            renderer.func_147775_a(block);
            return true;
        } else {
            return false;
        }
    }

    public boolean shouldRender3DInInventory(int a) {
        return true;
    }

    public int getRenderId() {
        return ClayiumCore.fluidTabRenderId;
    }

    private void renderInvCuboid(RenderBlocks renderer, Block block, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, IIcon icon) {
        Tessellator tessellator = Tessellator.field_78398_a;
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
        block.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        renderer.func_147775_a(block);
    }
}
