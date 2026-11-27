package net.claustra01.clayium.blockentity;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Common server sync and tool interaction contract for Clayium BlockEntities.
 */
public interface IGeneralInterface {
    void markForSync();

    void markForInstantSync();

    boolean acceptInterfaceSync();

    default boolean isUsableTool(ItemStack stack, Player player, Direction hitFace) {
        return false;
    }

    default boolean onToolUse(ItemStack stack, Player player, Direction hitFace) {
        return false;
    }
}
