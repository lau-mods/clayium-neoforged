package net.claustra01.clayium.logistics;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.claustra01.clayium.tool.BufferConfigurationToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class BufferBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FILTERED = BooleanProperty.create("filtered");
    public static final EnumProperty<BufferBlockEntity.FaceMode> DOWN = EnumProperty.create("down", BufferBlockEntity.FaceMode.class);
    public static final EnumProperty<BufferBlockEntity.FaceMode> UP = EnumProperty.create("up", BufferBlockEntity.FaceMode.class);
    public static final EnumProperty<BufferBlockEntity.FaceMode> NORTH = EnumProperty.create("north", BufferBlockEntity.FaceMode.class);
    public static final EnumProperty<BufferBlockEntity.FaceMode> SOUTH = EnumProperty.create("south", BufferBlockEntity.FaceMode.class);
    public static final EnumProperty<BufferBlockEntity.FaceMode> WEST = EnumProperty.create("west", BufferBlockEntity.FaceMode.class);
    public static final EnumProperty<BufferBlockEntity.FaceMode> EAST = EnumProperty.create("east", BufferBlockEntity.FaceMode.class);
    public static final java.util.List<EnumProperty<BufferBlockEntity.FaceMode>> FACES = java.util.List.of(DOWN, UP, NORTH, SOUTH, WEST, EAST);

    private final int tier;

    public BufferBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(FILTERED, false)
                .setValue(DOWN, BufferBlockEntity.FaceMode.OUTPUT)
                .setValue(UP, BufferBlockEntity.FaceMode.INPUT)
                .setValue(NORTH, BufferBlockEntity.FaceMode.INPUT)
                .setValue(SOUTH, BufferBlockEntity.FaceMode.INPUT)
                .setValue(WEST, BufferBlockEntity.FaceMode.INPUT)
                .setValue(EAST, BufferBlockEntity.FaceMode.INPUT));
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
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, FILTERED, DOWN, UP, NORTH, SOUTH, WEST, EAST);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof BufferBlockEntity buffer) {
            serverPlayer.openMenu(buffer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            net.minecraft.world.InteractionHand hand,
            BlockHitResult hitResult) {
        if (stack.is(WorkbenchRegistration.DIRECTION_MEMORY_CARD.get())
                && level.getBlockEntity(pos) instanceof BufferBlockEntity buffer) {
            if (!level.isClientSide) {
                if (player.isShiftKeyDown()) {
                    BufferConfigurationToolItem.copyFrom(stack, buffer);
                } else {
                    BufferConfigurationToolItem.pasteTo(stack, buffer);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(LogisticsRegistration.ITEM_FILTER.get())
                && level.getBlockEntity(pos) instanceof BufferBlockEntity buffer) {
            if (!level.isClientSide) {
                if (!buffer.filterStack().isEmpty()) {
                    return ItemInteractionResult.FAIL;
                }
                buffer.setFilterStack(stack.copyWithCount(1));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BufferBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(blockEntityType, LogisticsRegistration.BUFFER_BLOCK_ENTITY.get(),
                        BufferBlockEntity::serverTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BufferBlockEntity buffer) {
            Containers.dropContents(level, pos, buffer.dropInventory());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
