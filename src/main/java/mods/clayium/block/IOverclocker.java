package mods.clayium.block;

import net.minecraft.world.IBlockAccess;

public interface IOverclocker {
    double getOverclockFactor(IBlockAccess var1, int var2, int var3, int var4);
}
