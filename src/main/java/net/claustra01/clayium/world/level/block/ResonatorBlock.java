/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.world.level.block.Block;

/** A passive CA resonator. Nearby resonators multiply a CA machine's resonance. */
public final class ResonatorBlock extends Block {
    private final ClayTier tier;
    private final double resonance;

    public ResonatorBlock(Properties properties, ClayTier tier, double resonance) {
        super(properties);
        if (resonance <= 1.0D || !Double.isFinite(resonance)) {
            throw new IllegalArgumentException("Resonance must be finite and greater than one");
        }
        this.tier = tier;
        this.resonance = resonance;
    }

    public ClayTier tier() {
        return tier;
    }

    public double resonance() {
        return resonance;
    }
}
