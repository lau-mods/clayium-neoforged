package net.claustra01.clayium.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Contract for Interface BlockEntities (Clay Interface, Redstone Interface, Laser Interface)
 * linked to a core machine or multiblock controller.
 */
public interface ISynchronizedInterface extends IGeneralInterface {
    boolean setCoreBlockOffset(int dx, int dy, int dz);

    boolean setCoreBlockDimension(ResourceKey<Level> dimension);

    BlockPos getCoreBlockOffset();

    boolean isSynced();

    boolean acceptsCoordChanger();
}
