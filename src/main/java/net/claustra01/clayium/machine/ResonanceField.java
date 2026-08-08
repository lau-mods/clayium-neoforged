/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import net.claustra01.clayium.world.level.block.ResonatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Original 5x5x5 CA resonance field calculation. */
public final class ResonanceField {
    public static final int RADIUS = 2;
    private static final double MAX_RESONANCE = 1.0E100D;

    private ResonanceField() {}

    public static double at(Level level, BlockPos center) {
        if (level == null) {
            return 1.0D;
        }
        double value = 1.0D;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    cursor.setWithOffset(center, x, y, z);
                    if (level.getBlockState(cursor).getBlock() instanceof ResonatorBlock resonator) {
                        value = Math.min(MAX_RESONANCE, value * resonator.resonance());
                    }
                }
            }
        }
        return value;
    }
}
