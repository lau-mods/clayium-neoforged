/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.world.level.block.entity.ClayCraftingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Original quarter-block-height persistent crafting table. */
public final class ClayCraftingTableBlock extends BaseEntityBlock {
    public static final MapCodec<ClayCraftingTableBlock> CODEC = simpleCodec(ClayCraftingTableBlock::new);
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 4, 16);

    public ClayCraftingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ClayCraftingTableBlockEntity table) {
            player.openMenu(table);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level instanceof ServerLevel
                && level.getBlockEntity(pos) instanceof ClayCraftingTableBlockEntity table) {
            Containers.dropContents(level, pos, table);
        }
        super.onRemove(state, level, pos, next, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClayCraftingTableBlockEntity(pos, state);
    }
}
