/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import javax.annotation.Nullable;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.item.ClayConfiguratorItem;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.claustra01.clayium.world.item.RawClayCraftingToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Common block-state and interaction shell; processing remains in dedicated devices. */
public abstract class AbstractTieredIoMachineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");
    private static final VoxelShape PIPE_CENTER = box(5, 5, 5, 11, 11, 11);
    private final ClayTier tier;

    protected AbstractTieredIoMachineBlock(Properties properties, ClayTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PIPE, false));
    }

    public final ClayTier tier() {
        return tier;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, PIPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(PIPE)) return Shapes.block();
        VoxelShape shape = PIPE_CENTER;
        if (level.getBlockEntity(pos) instanceof ConfigurableItemDevice device) {
            for (Direction side : Direction.values()) if (device.pipeConnects(side)) shape = Shapes.or(shape, arm(side));
        }
        return shape;
    }

    private static VoxelShape arm(Direction side) {
        return switch (side) {
            case DOWN -> box(5, 0, 5, 11, 5, 11);
            case UP -> box(5, 11, 5, 11, 16, 11);
            case NORTH -> box(5, 5, 0, 11, 11, 5);
            case SOUTH -> box(5, 5, 11, 11, 11, 16);
            case WEST -> box(0, 5, 5, 5, 11, 11);
            case EAST -> box(11, 5, 5, 16, 11, 11);
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof net.minecraft.world.MenuProvider provider) {
            serverPlayer.openMenu(provider, data -> data.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof ClayConfiguratorItem || stack.getItem() instanceof ClayFilterItem
                || stack.getItem() instanceof RawClayCraftingToolItem) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level instanceof ServerLevel
                && level.getBlockEntity(pos) instanceof net.minecraft.world.Container container) {
            Containers.dropContents(level, pos, container);
        }
        super.onRemove(state, level, pos, next, moving);
    }
}
