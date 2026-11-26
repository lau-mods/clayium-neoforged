package net.claustra01.clayium.energy.fe;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class EnergyConverterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");

    private final EnergyConverterSpecification specification;

    public EnergyConverterBlock(Properties properties, EnergyConverterSpecification specification) {
        super(properties);
        this.specification = specification;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PIPE, false));
    }

    public EnergyConverterSpecification specification() {
        return specification;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(PIPE) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(PIPE, false);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, PIPE);
    }

    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(
            BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
            net.minecraft.world.phys.shapes.CollisionContext context) {
        if (!state.getValue(PIPE)) return net.minecraft.world.phys.shapes.Shapes.block();
        return level.getBlockEntity(pos) instanceof EnergyConverterBlockEntity device
                ? net.claustra01.clayium.logistics.PipeShapes.forDevice(device)
                : net.minecraft.world.phys.shapes.Shapes.block();
    }

    @Override protected net.minecraft.world.phys.shapes.VoxelShape getOcclusionShape(
            BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return net.claustra01.clayium.logistics.PipeShapes.occlusionShape(state, PIPE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyConverterBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof EnergyConverterBlockEntity converter) {
            serverPlayer.openMenu(converter, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(
                        blockEntityType,
                        EnergyConverterRegistration.BLOCK_ENTITY.get(),
                        EnergyConverterBlockEntity::serverTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof EnergyConverterBlockEntity converter) {
            Containers.dropContents(level, pos, converter.dropInventory());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
