/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.world.level.block.Block;

/** Passive machine modifier matching the original adjacent overclocker/storage blocks. */
public final class MachineModifierBlock extends Block {
    public enum Kind { OVERCLOCKER, ENERGY_STORAGE }

    private final Kind kind;
    private final ClayTier tier;
    private final double overclockFactor;
    private final int additionalStorageUnits;

    public MachineModifierBlock(
            Properties properties,
            Kind kind,
            ClayTier tier,
            double overclockFactor,
            int additionalStorageUnits) {
        super(properties);
        this.kind = kind;
        this.tier = tier;
        this.overclockFactor = overclockFactor;
        this.additionalStorageUnits = additionalStorageUnits;
    }

    public Kind kind() { return kind; }
    public ClayTier tier() { return tier; }
    public double overclockFactor() { return overclockFactor; }
    public int additionalStorageUnits() { return additionalStorageUnits; }
}
