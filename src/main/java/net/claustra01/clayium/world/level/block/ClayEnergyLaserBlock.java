/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.entity.ClayEnergyLaserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class ClayEnergyLaserBlock extends AbstractTieredIoMachineBlock {
    public static final MapCodec<ClayEnergyLaserBlock> CODEC = simpleCodec(
            properties -> new ClayEnergyLaserBlock(properties, ClayTier.CLAY_STEEL));
    public ClayEnergyLaserBlock(Properties properties, ClayTier tier) { super(properties, tier); }
    @Override protected MapCodec<? extends ClayEnergyLaserBlock> codec() { return CODEC; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClayEnergyLaserBlockEntity(pos, state);
    }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ClayiumRegistries.CLAY_ENERGY_LASER_BLOCK_ENTITY.get(),
                (l, p, s, laser) -> laser.serverTick());
    }
}
