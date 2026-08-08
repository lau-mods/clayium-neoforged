/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import net.claustra01.clayium.pan.PanConductor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PanCableBlock extends Block implements PanConductor {
    public static final MapCodec<PanCableBlock> CODEC = simpleCodec(PanCableBlock::new);
    private static final VoxelShape CENTER = box(6,6,6,10,10,10);
    public PanCableBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return true; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape result=CENTER;
        for (Direction direction:Direction.values()) if (level.getBlockState(pos.relative(direction)).getBlock() instanceof PanConductor)
            result=Shapes.or(result,arm(direction));
        return result;
    }
    private static VoxelShape arm(Direction side) { return switch(side) {
        case DOWN -> box(6,0,6,10,6,10); case UP -> box(6,10,6,10,16,10);
        case NORTH -> box(6,6,0,10,10,6); case SOUTH -> box(6,6,10,10,10,16);
        case WEST -> box(0,6,6,6,10,10); case EAST -> box(10,6,6,16,10,10);
    }; }
}
