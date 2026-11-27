package net.claustra01.clayium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Clay Crafting Board (quarter-height clay slab with top crafting grid overlay; non-pipable).
 */
public class ClayCraftingTableBlock extends ClayContainerBlock {
    public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);

    public ClayCraftingTableBlock(Properties properties) {
        super(properties, 0, "compressedclay-0", "claycraftingtable", "crafting", null, OverlayMode.TOP_ONLY);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Non-pipable slab
    }

    @Override
    public boolean canBePipe() {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
