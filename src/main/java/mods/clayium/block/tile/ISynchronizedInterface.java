package mods.clayium.block.tile;

import net.minecraft.world.World;

public interface ISynchronizedInterface {
    boolean setCoreBlockCoord(int var1, int var2, int var3);

    boolean setCoreBlockDimension(int var1);

    int getCoreBlockXCoord();

    int getCoreBlockYCoord();

    int getCoreBlockZCoord();

    World getCoreWorld();

    boolean isSynced();

    boolean acceptCoordChanger();
}
