/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import net.minecraft.world.level.block.Block;

/** Ranked shell segment used to enclose a CA Reactor coil ring. */
public final class CAReactorHullBlock extends Block {
    private final int rank;

    public CAReactorHullBlock(Properties properties, int rank) {
        super(properties);
        if (rank < 1 || rank > 10) {
            throw new IllegalArgumentException("CA Reactor hull rank must be in [1, 10]");
        }
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    /** Original metadata rank used by the reactor equations. */
    public int rankIndex() {
        return rank - 1;
    }
}
