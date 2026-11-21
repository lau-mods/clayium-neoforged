package net.claustra01.clayium.multiblock;

import javax.annotation.Nullable;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class MultiblockControllerBlock extends ClayMachineBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    private final MultiblockMachineType machineType;

    public MultiblockControllerBlock(Properties properties, MultiblockMachineType machineType) {
        super(properties, machineType, machineType.minimumTier());
        this.machineType = machineType;
        registerDefaultState(defaultBlockState().setValue(FORMED, false));
    }

    public MultiblockMachineType machineType() {
        return machineType;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiblockControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(
                blockEntityType,
                MultiblockRegistration.CONTROLLER_BLOCK_ENTITY.get(),
                MultiblockControllerBlockEntity::serverTick);
    }

}
