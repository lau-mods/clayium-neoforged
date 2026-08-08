/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.logistics.LogisticsKind;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.data.StorageContents;
import net.claustra01.clayium.world.level.block.entity.LogisticsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.claustra01.clayium.world.item.ClayConfiguratorItem;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.claustra01.clayium.world.item.RawClayCraftingToolItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LogisticsBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");
    private static final VoxelShape PIPE_CENTER = Block.box(5, 5, 5, 11, 11, 11);
    public static final MapCodec<LogisticsBlock> CODEC =
            simpleCodec(properties -> new LogisticsBlock(properties, LogisticsKind.BUFFER, 4));
    private final LogisticsKind kind;
    private final int tier;

    public LogisticsBlock(Properties properties, LogisticsKind kind, int tier) {
        super(properties);
        this.kind = kind;
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PIPE, false));
    }

    public LogisticsKind kind() {
        return kind;
    }

    public int tier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, PIPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(PIPE, false);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(PIPE)) {
            return Shapes.block();
        }
        VoxelShape shape = PIPE_CENTER;
        if (level.getBlockEntity(pos) instanceof LogisticsBlockEntity logistics) {
            for (Direction side : Direction.values()) {
                if (logistics.pipeConnects(side)) {
                    shape = Shapes.or(shape, pipeArm(side));
                }
            }
        }
        return shape;
    }

    private static VoxelShape pipeArm(Direction side) {
        return switch (side) {
            case DOWN -> Block.box(5, 0, 5, 11, 5, 11);
            case UP -> Block.box(5, 11, 5, 11, 16, 11);
            case NORTH -> Block.box(5, 5, 0, 11, 11, 5);
            case SOUTH -> Block.box(5, 5, 11, 11, 11, 16);
            case WEST -> Block.box(0, 5, 5, 5, 11, 11);
            case EAST -> Block.box(11, 5, 5, 16, 11, 11);
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (stack.getItem() instanceof ClayConfiguratorItem
                || stack.getItem() instanceof ClayFilterItem
                || stack.getItem() instanceof RawClayCraftingToolItem) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LogisticsBlockEntity logistics) {
            player.openMenu(logistics, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock())
                && level instanceof ServerLevel
                && level.getBlockEntity(pos) instanceof LogisticsBlockEntity logistics) {
            if (kind == LogisticsKind.STORAGE_CONTAINER) {
                for (int slot : new int[]{LogisticsBlockEntity.STORAGE_INPUT_SLOT,
                        LogisticsBlockEntity.CONTAINER_FILTER_SLOT}) {
                    ItemStack stack = logistics.getItem(slot);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + .5,
                                pos.getZ() + .5, stack.copy());
                    }
                }
            } else {
                Containers.dropContents(level, pos, logistics);
            }
        }
        super.onRemove(state, level, pos, next, moving);
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                                      @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (kind == LogisticsKind.STORAGE_CONTAINER
                && level.getBlockEntity(pos) instanceof LogisticsBlockEntity storage) {
            storage.setStorageCapacity(stack.getOrDefault(
                    ClayiumDataComponents.STORAGE_CAPACITY.get(), LogisticsBlockEntity.DEFAULT_STORAGE_CAPACITY));
            StorageContents contents = stack.get(ClayiumDataComponents.STORAGE_CONTENTS.get());
            if (contents != null) storage.restoreStoredContents(contents.item(), contents.count());
        }
    }

    @Override protected java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        java.util.List<ItemStack> drops = super.getDrops(state, params);
        if (kind != LogisticsKind.STORAGE_CONTAINER) return drops;
        BlockEntity entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(entity instanceof LogisticsBlockEntity storage)) return drops;
        for (ItemStack drop : drops) {
            if (drop.is(this.asItem())) {
                drop.set(ClayiumDataComponents.STORAGE_CAPACITY.get(), storage.storageCapacity());
                if (storage.storedCount() > 0 && !storage.storedItem().isEmpty()) {
                    drop.set(ClayiumDataComponents.STORAGE_CONTENTS.get(),
                            new StorageContents(storage.storedItem(), storage.storedCount()));
                }
            }
        }
        return drops;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LogisticsBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(
                type, ClayiumRegistries.LOGISTICS_BLOCK_ENTITY.get(), LogisticsBlockEntity::serverTick);
    }
}
