package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Clayium container block with horizontal facing (metaMode=1).
 */
public class HorizontalClayContainerBlock extends ClayContainerBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public HorizontalClayContainerBlock(
            Properties properties,
            int tier,
            String baseTexture,
            @Nullable String overlayTexture,
            @Nullable String categoryKey,
            @Nullable String tooltipKey) {
        this(properties, tier, baseTexture, overlayTexture, categoryKey, tooltipKey, OverlayMode.FRONT_ONLY);
    }

    public HorizontalClayContainerBlock(
            Properties properties,
            int tier,
            String baseTexture,
            @Nullable String overlayTexture,
            @Nullable String categoryKey,
            @Nullable String tooltipKey,
            OverlayMode overlayMode) {
        super(properties, tier, baseTexture, overlayTexture, categoryKey, tooltipKey, overlayMode);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public FacingMode getFacingMode() {
        return FacingMode.HORIZONTAL;
    }

    @Override
    public Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
