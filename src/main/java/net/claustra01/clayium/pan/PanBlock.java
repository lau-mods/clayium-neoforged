package net.claustra01.clayium.pan;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.PanRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class PanBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");

    private final Kind kind;
    private final int tier;

    public PanBlock(Properties properties, Kind kind, int tier) {
        super(properties);
        this.kind = kind;
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PIPE, false));
    }

    public Kind kind() {
        return kind;
    }

    public int tier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(PIPE) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(PIPE, false);
    }

    @Override protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PIPE);
    }

    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(
            BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
            net.minecraft.world.phys.shapes.CollisionContext context) {
        if (!state.getValue(PIPE)) return net.minecraft.world.phys.shapes.Shapes.block();
        return level.getBlockEntity(pos) instanceof PanBlockEntity device
                ? net.claustra01.clayium.logistics.PipeShapes.forDevice(device)
                : net.minecraft.world.phys.shapes.Shapes.block();
    }

    @Override protected net.minecraft.world.phys.shapes.VoxelShape getOcclusionShape(
            BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return net.claustra01.clayium.logistics.PipeShapes.occlusionShape(state, PIPE);
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return kind == Kind.CORE || kind == Kind.ADAPTER || kind == Kind.DUPLICATOR
                ? new PanBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(
                type, PanRegistration.BLOCK_ENTITY.get(), PanBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof PanBlockEntity pan) {
            serverPlayer.openMenu(pan, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(oldState.getBlock())) {
            PanNetwork.invalidate(level);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PanBlockEntity pan) {
                if (kind == Kind.DUPLICATOR) Containers.dropContents(level, pos, pan.dropInventory());
                if (kind == Kind.ADAPTER) Containers.dropContents(level, pos, pan.dropAdapterSubItems());
            }
            PanNetwork.invalidate(level);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && kind == Kind.ADAPTER
                && level.getBlockEntity(pos) instanceof PanBlockEntity pan) {
            pan.adapterNeighborChanged();
        }
    }

    public enum Kind {
        CABLE,
        CORE,
        ADAPTER,
        DUPLICATOR
    }
}
