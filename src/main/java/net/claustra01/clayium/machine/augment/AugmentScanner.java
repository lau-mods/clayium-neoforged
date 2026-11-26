package net.claustra01.clayium.machine.augment;

import net.claustra01.clayium.machine.augment.MachineAugmentBlock.AugmentKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class AugmentScanner {
    public static double resonance(Level level, BlockPos center) {
        double resonance = 1.0D;
        for (BlockPos position : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            MachineAugmentBlock augment = augment(level.getBlockState(position), AugmentKind.RESONATOR);
            if (augment != null) {
                resonance *= augment.factor();
            }
        }
        return resonance;
    }

    public static double overclockFactor(Level level, BlockPos center) {
        double factor = 1.0D;
        for (Direction direction : Direction.values()) {
            MachineAugmentBlock augment = augment(
                    level.getBlockState(center.relative(direction)), AugmentKind.OVERCLOCKER);
            if (augment != null) {
                factor *= augment.factor();
            }
        }
        return factor;
    }

    public static int fuelLimit(Level level, BlockPos center) {
        int limit = 1;
        for (Direction direction : Direction.values()) {
            MachineAugmentBlock augment = augment(
                    level.getBlockState(center.relative(direction)), AugmentKind.ENERGY_STORAGE);
            if (augment != null) {
                limit = Math.min(64, limit + augment.additionalFuel());
            }
        }
        return limit;
    }

    private static MachineAugmentBlock augment(BlockState state, AugmentKind kind) {
        return state.getBlock() instanceof MachineAugmentBlock augment && augment.kind() == kind
                ? augment : null;
    }

    private AugmentScanner() {
    }
}
