/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.entity.AutoCrafterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class AutoCrafterBlock extends AbstractTieredIoMachineBlock {
    public static final MapCodec<AutoCrafterBlock> CODEC = simpleCodec(
            properties -> new AutoCrafterBlock(properties, ClayTier.ADVANCED));

    public AutoCrafterBlock(Properties properties, ClayTier tier) { super(properties, tier); }
    @Override protected MapCodec<? extends AutoCrafterBlock> codec() { return CODEC; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AutoCrafterBlockEntity(pos, state);
    }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                ClayiumRegistries.AUTO_CRAFTER_BLOCK_ENTITY.get(), (l, p, s, machine) -> machine.serverTick());
    }
}
