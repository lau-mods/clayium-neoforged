package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayWorkTableBlockEntity;
import net.claustra01.clayium.menu.ClayWorkTableMenu;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Clay Work Table (Tier 0 manual crafting block; does not enter piping mode).
 */
public class ClayWorkTableBlock extends ClayContainerBlock {
    public ClayWorkTableBlock(Properties properties) {
        super(properties, 0, "clayworktable", "clayworktable", "crafting", null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // No PIPE_MODE property
    }

    @Override
    public boolean canBePipe() {
        return false;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ClayWorkTableBlockEntity table) {
                for (ItemStack stack : table.getItems()) {
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                    }
                }
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof ClayWorkTableBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new ClayWorkTableMenu(id, inv, pos),
                            state.getBlock().getName()),
                    pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClayWorkTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ClayiumBlockEntities.CLAY_WORK_TABLE.get()) {
            return null;
        }
        return (lvl, p, st, be) -> {
            if (be instanceof ClayWorkTableBlockEntity table) {
                ClayWorkTableBlockEntity.serverTick(lvl, p, st, table);
            }
        };
    }
}
