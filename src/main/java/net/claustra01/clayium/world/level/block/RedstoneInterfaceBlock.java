/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.entity.RedstoneInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Orientation-independent redstone controller interface. */
public final class RedstoneInterfaceBlock extends BaseEntityBlock {
    public static final MapCodec<RedstoneInterfaceBlock> CODEC = simpleCodec(
            properties -> new RedstoneInterfaceBlock(properties, ClayTier.ADVANCED));
    private final ClayTier tier;

    public RedstoneInterfaceBlock(Properties properties, ClayTier tier) { super(properties); this.tier = tier; }
    public ClayTier tier() { return tier; }
    @Override protected MapCodec<? extends RedstoneInterfaceBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public boolean isSignalSource(BlockState state) { return true; }
    @Override public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof RedstoneInterfaceBlockEntity device ? device.redstoneSignal() : 0;
    }
    @Override public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                          Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RedstoneInterfaceBlockEntity device) {
            player.displayClientMessage(device.cycleMode(player.isShiftKeyDown()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneInterfaceBlockEntity(pos, state);
    }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                ClayiumRegistries.REDSTONE_INTERFACE_BLOCK_ENTITY.get(),
                (l, p, s, device) -> device.serverTick());
    }
}
