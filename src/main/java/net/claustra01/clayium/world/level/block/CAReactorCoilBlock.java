/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.world.level.block.Block;

/** Tiered coil segment used by the CA Reactor's bounded ring validator. */
public final class CAReactorCoilBlock extends Block {
    private final ClayTier tier;

    public CAReactorCoilBlock(Properties properties, ClayTier tier) {
        super(properties);
        this.tier = tier;
    }

    public ClayTier tier() {
        return tier;
    }
}
