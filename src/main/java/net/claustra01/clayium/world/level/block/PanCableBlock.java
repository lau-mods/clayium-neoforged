/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import net.claustra01.clayium.pan.PanConductor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PanCableBlock extends Block implements PanConductor {
    public static final MapCodec<PanCableBlock> CODEC = simpleCodec(PanCableBlock::new);
    private static final VoxelShape CENTER = box(6,6,6,10,10,10);
    public static final java.util.Map<Direction,BooleanProperty> CONNECTIONS=java.util.Map.of(
            Direction.DOWN,BooleanProperty.create("down"),Direction.UP,BooleanProperty.create("up"),
            Direction.NORTH,BooleanProperty.create("north"),Direction.SOUTH,BooleanProperty.create("south"),
            Direction.WEST,BooleanProperty.create("west"),Direction.EAST,BooleanProperty.create("east"));
    public PanCableBlock(Properties properties) {
        super(properties);
        BlockState state=stateDefinition.any();
        for(BooleanProperty property:CONNECTIONS.values())state=state.setValue(property,false);
        registerDefaultState(state);
    }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(CONNECTIONS.values().toArray(BooleanProperty[]::new));}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){
        BlockState state=defaultBlockState();
        for(Direction direction:Direction.values())state=state.setValue(CONNECTIONS.get(direction),connects(context.getLevel(),context.getClickedPos(),direction));
        return state;
    }
    @Override protected BlockState updateShape(BlockState state,Direction direction,BlockState neighborState,
                                               net.minecraft.world.level.LevelAccessor level,BlockPos pos,BlockPos neighborPos){
        return state.setValue(CONNECTIONS.get(direction),neighborState.getBlock() instanceof PanConductor);
    }
    @Override protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return true; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape result=CENTER;
        for (Direction direction:Direction.values()) if (state.getValue(CONNECTIONS.get(direction)))
            result=Shapes.or(result,arm(direction));
        return result;
    }
    private static boolean connects(BlockGetter level,BlockPos pos,Direction direction){return level.getBlockState(pos.relative(direction)).getBlock() instanceof PanConductor;}
    private static VoxelShape arm(Direction side) { return switch(side) {
        case DOWN -> box(6,0,6,10,6,10); case UP -> box(6,10,6,10,16,10);
        case NORTH -> box(6,6,0,10,10,6); case SOUTH -> box(6,6,10,10,10,16);
        case WEST -> box(0,6,6,6,10,10); case EAST -> box(10,6,6,16,10,10);
    }; }
}
