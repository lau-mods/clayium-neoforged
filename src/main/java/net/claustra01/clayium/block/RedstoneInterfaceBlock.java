package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.RedstoneInterfaceBlockEntity;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Redstone Interface block emitting and reading redstone signals linked to a target machine.
 */
public class RedstoneInterfaceBlock extends ClayContainerBlock {
    public RedstoneInterfaceBlock(Properties properties, int tier, String baseTexture) {
        super(properties, tier, baseTexture, "redstoneinterface", "interface", "tooltip.RedstoneInterface", OverlayMode.ALL_FACES);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Non-pipable block; no PIPE_MODE or FACING property
    }

    @Override
    public boolean canBePipe() {
        return false;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof RedstoneInterfaceBlockEntity ri) {
            return ri.getProvidingWeakPower();
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneInterfaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ClayiumBlockEntities.REDSTONE_INTERFACE.get(), RedstoneInterfaceBlockEntity::serverTickStatic);
    }
}
