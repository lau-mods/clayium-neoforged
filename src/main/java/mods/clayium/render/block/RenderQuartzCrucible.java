package mods.clayium.render.block;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilRender;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class RenderQuartzCrucible implements ISimpleBlockRenderingHandler {
    private IIcon boxIIcon;

    public RenderQuartzCrucible() {
        super();
    }

    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        this.boxIIcon = Blocks.field_150371_ca.func_149733_h(1);
        if (modelID == this.getRenderId()) {
            UtilRender.renderInvCuboid(renderer, block, 0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F, this.boxIIcon);
            UtilRender.renderInvCuboid(renderer, block, 0.0F, 0.0625F, 0.0F, 1.0F, 0.75F, 0.0625F, this.boxIIcon);
            UtilRender.renderInvCuboid(renderer, block, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.75F, 1.0F, this.boxIIcon);
            UtilRender.renderInvCuboid(renderer, block, 0.0F, 0.0625F, 0.0625F, 0.0625F, 0.75F, 0.9375F, this.boxIIcon);
            UtilRender.renderInvCuboid(renderer, block, 0.9375F, 0.0625F, 0.0625F, 1.0F, 0.75F, 0.9375F, this.boxIIcon);
        }
    }

    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        this.boxIIcon = Blocks.field_150371_ca.func_149733_h(1);
        if (modelId == this.getRenderId()) {
            Tessellator tessellator = Tessellator.field_78398_a;
            renderer.func_147757_a(this.boxIIcon);
            block.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            renderer.func_147757_a(this.boxIIcon);
            block.func_149676_a(0.0F, 0.0625F, 0.0F, 1.0F, 0.75F, 0.0625F);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            renderer.func_147757_a(this.boxIIcon);
            block.func_149676_a(0.0F, 0.0625F, 0.9375F, 1.0F, 0.75F, 1.0F);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            renderer.func_147757_a(this.boxIIcon);
            block.func_149676_a(0.0F, 0.0625F, 0.0625F, 0.0625F, 0.75F, 0.9375F);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            renderer.func_147757_a(this.boxIIcon);
            block.func_149676_a(0.9375F, 0.0625F, 0.0625F, 1.0F, 0.75F, 0.9375F);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            renderer.func_147771_a();
            int meta = world.func_72805_g(x, y, z);
            if (meta > 0) {
                block.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, (float)(meta + 1) / 16.0F, 1.0F);
                renderer.func_147775_a(block);
                tessellator.func_78380_c(240);
                tessellator.func_78386_a(1.0F, 1.0F, 1.0F);
                renderer.func_147806_b(block, (double)x, (double)y, (double)z, renderer.func_147745_b(Blocks.field_150427_aO));
            }

            block.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
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
        return ClayiumCore.quartzCrucibleRenderId;
    }
}
