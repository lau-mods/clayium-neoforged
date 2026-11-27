package mods.clayium.pan;

import net.minecraft.world.World;

public interface IPANAdapterConversionFactory {
    boolean match(World var1, int var2, int var3, int var4);

    IPANConversion getConversion(IPANAdapter var1);
}
