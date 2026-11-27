package mods.clayium.block.tile;

import net.minecraft.util.AxisAlignedBB;

public interface IAxisAlignedBBContainer {
    AxisAlignedBB getAxisAlignedBB();

    void setAxisAlignedBB(AxisAlignedBB var1);

    boolean hasAxisAlignedBB();

    int getBoxAppearance();
}
