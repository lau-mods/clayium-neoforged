/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.item.ClayConfiguratorItem;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.claustra01.clayium.world.item.RawClayCraftingToolItem;
import net.claustra01.clayium.world.level.block.entity.CobblestoneGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CobblestoneGeneratorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");
    private static final VoxelShape PIPE_CENTER = Block.box(5, 5, 5, 11, 11, 11);
    public static final MapCodec<CobblestoneGeneratorBlock> CODEC = simpleCodec(
            properties -> new CobblestoneGeneratorBlock(properties, ClayTier.CLAY));
    private final ClayTier tier;

    public CobblestoneGeneratorBlock(Properties properties, ClayTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PIPE, false));
    }

    public ClayTier tier() { return tier; }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> b) { b.add(FACING, PIPE); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(PIPE, false);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(PIPE)) return Shapes.block();
        VoxelShape shape = PIPE_CENTER;
        if (level.getBlockEntity(pos) instanceof CobblestoneGeneratorBlockEntity generator) {
            for (Direction side : Direction.values()) {
                if (generator.pipeConnects(side)) shape = Shapes.or(shape, pipeArm(side));
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof ClayConfiguratorItem || stack.getItem() instanceof ClayFilterItem
                || stack.getItem() instanceof RawClayCraftingToolItem) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CobblestoneGeneratorBlockEntity generator) player.openMenu(generator, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level instanceof ServerLevel
                && level.getBlockEntity(pos) instanceof CobblestoneGeneratorBlockEntity generator) Containers.dropContents(level, pos, generator);
        super.onRemove(state, level, pos, next, moving);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CobblestoneGeneratorBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ClayiumRegistries.COBBLESTONE_GENERATOR_BLOCK_ENTITY.get(), CobblestoneGeneratorBlockEntity::serverTick);
    }
}
