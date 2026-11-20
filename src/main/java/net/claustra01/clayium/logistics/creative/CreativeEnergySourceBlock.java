package net.claustra01.clayium.logistics.creative;

import javax.annotation.Nullable;
import net.claustra01.clayium.logistics.BufferBlock;
import net.claustra01.clayium.registry.CreativeSourceRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class CreativeEnergySourceBlock extends BufferBlock {
    public CreativeEnergySourceBlock(BlockBehaviour.Properties properties) {
        super(properties, 13);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeEnergySourceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(
                        blockEntityType,
                        CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE_BLOCK_ENTITY.get(),
                        CreativeEnergySourceBlockEntity::serverTick);
    }
}
