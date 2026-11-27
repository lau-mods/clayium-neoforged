package mods.clayium.block.tile;

import mods.clayium.entity.RayTraceMemory;

public interface IRayTracer {
    void setRayTraceMemory(RayTraceMemory var1);

    boolean acceptRayTraceMemory(RayTraceMemory var1);

    RayTraceMemory getRayTraceMemory();
}
