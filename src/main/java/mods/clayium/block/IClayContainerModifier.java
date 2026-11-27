package mods.clayium.block;

import mods.clayium.block.tile.TileClayContainer;
import net.minecraft.world.IBlockAccess;

public interface IClayContainerModifier {
    void modifyClayContainer(IBlockAccess var1, int var2, int var3, int var4, TileClayContainer var5);
}
