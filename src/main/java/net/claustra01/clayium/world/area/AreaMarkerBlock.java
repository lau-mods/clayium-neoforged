package net.claustra01.clayium.world.area;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

public final class AreaMarkerBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = box(5, 5, 5, 11, 11, 11);

    private final AreaMarkerType markerType;

    public AreaMarkerBlock(Properties properties, AreaMarkerType markerType) {
        super(properties);
        this.markerType = markerType;
    }

    public AreaMarkerType markerType() {
        return markerType;
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
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof AreaMarkerBlockEntity marker) {
            marker.setOwner(player.getUUID());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AreaMarkerBlockEntity marker
                && (marker.owner() == null || marker.owner().equals(player.getUUID()))) {
            BlockPos first = AreaSelection.recalled(player, level);
            if (first != null && !first.equals(pos) && level.isLoaded(first)
                    && level.getBlockEntity(first) instanceof AreaMarkerBlockEntity other
                    && other.markerType() == marker.markerType()
                    && (other.owner() == null || other.owner().equals(player.getUUID()))
                    && Math.abs(first.getX() - pos.getX()) <= 64
                    && Math.abs(first.getY() - pos.getY()) <= 64
                    && Math.abs(first.getZ() - pos.getZ()) <= 64) {
                marker.select(first);
                other.select(pos);
            }
            AreaSelection.remember(player, level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AreaMarkerBlockEntity(pos, state);
    }
}
