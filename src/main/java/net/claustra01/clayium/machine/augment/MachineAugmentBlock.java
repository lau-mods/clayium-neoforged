package net.claustra01.clayium.machine.augment;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.claustra01.clayium.machine.ca.CaCollectorBlockEntity;

public final class MachineAugmentBlock extends Block {
    private final AugmentKind kind;
    private final double factor;
    private final int additionalFuel;

    public MachineAugmentBlock(
            Properties properties,
            AugmentKind kind,
            double factor,
            int additionalFuel) {
        super(properties);
        this.kind = kind;
        this.factor = factor;
        this.additionalFuel = additionalFuel;
    }

    public AugmentKind kind() {
        return kind;
    }

    public double factor() {
        return factor;
    }

    public int additionalFuel() {
        return additionalFuel;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (kind == AugmentKind.RESONATOR && !level.isClientSide) {
            invalidateCollectors(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (kind == AugmentKind.RESONATOR && !state.is(newState.getBlock()) && !level.isClientSide) {
            invalidateCollectors(level, pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    private static void invalidateCollectors(Level level, BlockPos pos) {
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            if (level.getBlockEntity(nearby) instanceof CaCollectorBlockEntity collector) {
                collector.invalidateResonance();
            }
        }
    }

    public enum AugmentKind {
        RESONATOR,
        OVERCLOCKER,
        ENERGY_STORAGE
    }
}
