/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.entity.ChemicalMetalSeparatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class ChemicalMetalSeparatorBlock extends AbstractTieredIoMachineBlock {
    public static final MapCodec<ChemicalMetalSeparatorBlock> CODEC = simpleCodec(
            properties -> new ChemicalMetalSeparatorBlock(properties, ClayTier.PRECISION));

    public ChemicalMetalSeparatorBlock(Properties properties, ClayTier tier) { super(properties, tier); }
    @Override protected MapCodec<? extends ChemicalMetalSeparatorBlock> codec() { return CODEC; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChemicalMetalSeparatorBlockEntity(pos, state);
    }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                ClayiumRegistries.CHEMICAL_METAL_SEPARATOR_BLOCK_ENTITY.get(), (l, p, s, machine) -> machine.serverTick());
    }
}
