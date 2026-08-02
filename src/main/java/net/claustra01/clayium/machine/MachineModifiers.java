/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import net.claustra01.clayium.world.level.block.MachineModifierBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/** Resolves the six directly adjacent passive machine modifiers. */
public final class MachineModifiers {
    public record Snapshot(double overclockFactor, int energySlotLimit) {
        public static final Snapshot DEFAULT = new Snapshot(1.0D, 1);

        /** Original per-tick CE scaling after the ten-work-step overclock threshold. */
        public double energyFactor() {
            return overclockFactor <= 10.0D
                    ? overclockFactor
                    : 10.0D * Math.pow(overclockFactor / 10.0D, 1.5D);
        }
    }

    private MachineModifiers() {}

    public static Snapshot scan(Level level, BlockPos origin) {
        double factor = 1.0D;
        int storage = 1;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(origin.relative(direction)).getBlock() instanceof MachineModifierBlock modifier) {
                if (modifier.kind() == MachineModifierBlock.Kind.OVERCLOCKER) factor *= modifier.overclockFactor();
                else storage += modifier.additionalStorageUnits();
            }
        }
        return new Snapshot(Math.min(15_625.0D, factor), Math.min(64, storage));
    }
}
