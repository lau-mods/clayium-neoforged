package net.claustra01.clayium.workbench;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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

public final class ClayCraftingBoardBlock extends BaseEntityBlock {
    public static final MapCodec<ClayCraftingBoardBlock> CODEC = simpleCodec(ClayCraftingBoardBlock::new);
    private static final VoxelShape SHAPE = box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);

    public ClayCraftingBoardBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClayCraftingBoardBlockEntity(pos, state);
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
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ClayCraftingBoardBlockEntity board) {
            player.openMenu(board, buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeVarInt(board.adjacentInventory() == null ? 0
                        : Math.min(board.adjacentInventory().getSlots(), 27));
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}
