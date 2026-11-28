package net.claustra01.clayium.blockentity;

import net.minecraft.world.phys.AABB;

/**
 * Contract for area marker BlockEntities supplying selection boxes to nearby Area Machines.
 */
public interface IAxisAlignedBBProvider {
    boolean hasAxisAlignedBB();

    AABB getAxisAlignedBB();

    void setAxisAlignedBBToMachine();
}
