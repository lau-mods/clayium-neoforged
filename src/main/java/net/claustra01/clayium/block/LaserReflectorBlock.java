package net.claustra01.clayium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Laser Reflector (Tier 7 translucent crystal pyramid oriented toward FACING; non-pipable).
 */
public class LaserReflectorBlock extends DirectionalClayContainerBlock {
    public static final VoxelShape SHAPE_Y = Block.box(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);
    public static final VoxelShape SHAPE_Z = Block.box(4.0, 4.0, 2.0, 12.0, 12.0, 14.0);
    public static final VoxelShape SHAPE_X = Block.box(2.0, 4.0, 4.0, 14.0, 12.0, 12.0);

    public LaserReflectorBlock(Properties properties) {
        super(properties, 7, "laserreflector", null, "laser", null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean canBePipe() {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForFacing(state.getValue(FACING));
    }

    public static VoxelShape shapeForFacing(Direction dir) {
        return switch (dir) {
            case UP, DOWN -> SHAPE_Y;
            case NORTH, SOUTH -> SHAPE_Z;
            case EAST, WEST -> SHAPE_X;
        };
    }
}
