package mods.clayium.render.block;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import mods.clayium.block.PANCable;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilDirection;
import mods.clayium.util.UtilRender;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

public class RenderPANCable implements ISimpleBlockRenderingHandler {
    public RenderPANCable() {
        super();
    }

    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (modelId == this.getRenderId()) {
            float o = PANCable.pipeWidth;
            UtilRender.renderInvCuboid(renderer, block, 0.5F - o, 0.5F - o, 0.5F - o, 0.5F + o, 0.5F + o, 0.5F + o, block.func_149691_a(metadata, 0));
        }
    }

    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        if (modelId == this.getRenderId()) {
            Tessellator tessellator = Tessellator.field_78398_a;
            float o = PANCable.pipeWidth;
            block.func_149676_a(0.5F - o, 0.5F - o, 0.5F - o, 0.5F + o, 0.5F + o, 0.5F + o);
            renderer.func_147775_a(block);
            renderer.func_147784_q(block, x, y, z);
            UtilDirection[] directions = new UtilDirection[]{
                UtilDirection.NORTH, UtilDirection.SOUTH, UtilDirection.EAST, UtilDirection.WEST, UtilDirection.UP, UtilDirection.DOWN
            };

            for (UtilDirection direction : directions) {
                if (((PANCable)block).checkPipeConnection(world, x, y, z, direction)) {
                    block.func_149676_a(
                        0.5F - o + (direction.offsetX == 1 ? o * 2.0F : 0.0F) - (direction.offsetX == -1 ? 0.5F - o : 0.0F),
                        0.5F - o + (direction.offsetY == 1 ? o * 2.0F : 0.0F) - (direction.offsetY == -1 ? 0.5F - o : 0.0F),
                        0.5F - o + (direction.offsetZ == 1 ? o * 2.0F : 0.0F) - (direction.offsetZ == -1 ? 0.5F - o : 0.0F),
                        0.5F + o - (direction.offsetX == -1 ? o * 2.0F : 0.0F) + (direction.offsetX == 1 ? 0.5F - o : 0.0F),
                        0.5F + o - (direction.offsetY == -1 ? o * 2.0F : 0.0F) + (direction.offsetY == 1 ? 0.5F - o : 0.0F),
                        0.5F + o - (direction.offsetZ == -1 ? o * 2.0F : 0.0F) + (direction.offsetZ == 1 ? 0.5F - o : 0.0F)
                    );
                    renderer.func_147775_a(block);
                    renderer.func_147784_q(block, x, y, z);
                }

                block.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    public int getRenderId() {
        return ClayiumCore.panCableRenderId;
    }
}
