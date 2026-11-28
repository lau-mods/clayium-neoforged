package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.component.ClayiumDataComponentTypes.DirectionMemoryData;

/**
 * Contract for BlockEntities accepting ray-trace orientation vectors stored on Direction Memory Cards.
 */
public interface IRayTracer {
    boolean acceptRayTraceMemory(DirectionMemoryData data);

    void applyRayTraceMemory(DirectionMemoryData data);
}
