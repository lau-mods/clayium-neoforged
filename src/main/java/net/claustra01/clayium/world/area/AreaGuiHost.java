package net.claustra01.clayium.world.area;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Block entities shown with the original GuiAreaMiner / GuiAreaActivator (GUI 16 and 24):
 * the Tier 6 block breaker and activator and the Tier 7-10 area machines.
 */
public interface AreaGuiHost {
    int BUTTON_START = 0;
    int BUTTON_STOP = 1;
    int BUTTON_LOOP = 2;
    int BUTTON_BOX = 3;
    int BUTTON_TARGET = 4;
    int BUTTON_RAY_TRACE = 5;
    int BUTTON_SNEAK = 6;

    ItemStackHandler inventory();

    /** Original tile tier; selects the GuiAreaMiner layout branch. */
    int guiTier();

    /** Original inventoryX / inventoryY. */
    int gridColumns();

    int gridRows();

    /** Tier 10 replacer: a second X*Y grid of replacement blocks below the outputs. */
    boolean hasReplacementGrid();

    int harvestFilterSlot();

    /** -1 when the GUI shows only the harvest filter (default GuiAreaMiner branch). */
    int fortuneFilterSlot();

    int silkTouchFilterSlot();

    /** Energy slot drawn outside the window, or -1 without stored CE. */
    int fuelSlot();

    boolean isActivator();

    long storedEnergy();

    long laserEnergy();

    AreaMachineBlockEntity.ActivatorTarget activatorTarget();

    boolean activatorRayTrace();

    boolean activatorSneaking();

    boolean applyControl(int action);

    BlockPos getBlockPos();

    boolean isRemoved();

    Level getLevel();
}
