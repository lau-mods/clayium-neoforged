package net.claustra01.clayium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Clay Marker blocks (6x6x6 centered VoxelShape matching 0.5 +/- 0.1875 original bounds).
 */
public class ClayMarkerBlock extends TieredBlock {
    public static final VoxelShape SHAPE = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

    public ClayMarkerBlock(Properties properties, int tier, String textureName) {
        super(properties, tier, textureName);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
