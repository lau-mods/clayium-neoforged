package net.claustra01.clayium.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

/** A block entity shown with the original ContainerNormalInventory (GUI 11 / GUI 18). */
public interface NormalInventoryHost {
    ItemStackHandler normalInventory();

    NormalInventoryLayout normalLayout();

    /** ContainerCACollector adds 14 px below the grid for the resonance line. */
    default int extraMachineHeight() {
        return 0;
    }

    /** Resonance shown by GuiClayContainerTemp for TileCAMachines, or NaN when not shown. */
    default double displayedResonance() {
        return Double.NaN;
    }

    BlockPos getBlockPos();

    boolean isRemoved();

    Level getLevel();
}
