/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.entity.AutoClayCondenserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class AutoClayCondenserBlock extends AbstractTieredIoMachineBlock {
    public static final MapCodec<AutoClayCondenserBlock> CODEC = simpleCodec(
            properties -> new AutoClayCondenserBlock(properties, ClayTier.ADVANCED));

    public AutoClayCondenserBlock(Properties properties, ClayTier tier) { super(properties, tier); }
    @Override protected MapCodec<? extends AutoClayCondenserBlock> codec() { return CODEC; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AutoClayCondenserBlockEntity(pos, state);
    }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                ClayiumRegistries.AUTO_CLAY_CONDENSER_BLOCK_ENTITY.get(), (l, p, s, machine) -> machine.serverTick());
    }
}
