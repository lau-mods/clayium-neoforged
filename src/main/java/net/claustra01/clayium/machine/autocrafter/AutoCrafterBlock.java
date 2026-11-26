package net.claustra01.clayium.machine.autocrafter;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.BlockHitResult;

public final class AutoCrafterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");

    private final AutoCrafterSpecification specification;

    public AutoCrafterBlock(Properties properties, AutoCrafterSpecification specification) {
        super(properties);
        this.specification = specification;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PIPE, false));
    }

    public AutoCrafterSpecification specification() {
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
        return level.getBlockEntity(pos) instanceof AutoCrafterBlockEntity device
                ? net.claustra01.clayium.logistics.PipeShapes.forDevice(device)
                : net.minecraft.world.phys.shapes.Shapes.block();
    }

    @Override protected net.minecraft.world.phys.shapes.VoxelShape getOcclusionShape(
            BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return net.claustra01.clayium.logistics.PipeShapes.occlusionShape(state, PIPE);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof AutoCrafterBlockEntity autoCrafter) {
            serverPlayer.openMenu(autoCrafter, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AutoCrafterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(blockEntityType, AutoCrafterRegistration.BLOCK_ENTITY.get(),
                        AutoCrafterBlockEntity::serverTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AutoCrafterBlockEntity autoCrafter) {
            Containers.dropContents(level, pos, autoCrafter.dropInventory());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
