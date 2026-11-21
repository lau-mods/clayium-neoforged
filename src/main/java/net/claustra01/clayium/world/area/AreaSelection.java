package net.claustra01.clayium.world.area;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

final class AreaSelection {
    private static final String POSITION = "clayium_area_marker";
    private static final String DIMENSION = "clayium_area_marker_dimension";

    static void remember(Player player, Level level, BlockPos position) {
        player.getPersistentData().putLong(POSITION, position.asLong());
        player.getPersistentData().putString(DIMENSION, level.dimension().location().toString());
    }

    static BlockPos recalled(Player player, Level level) {
        if (!level.dimension().location().toString().equals(player.getPersistentData().getString(DIMENSION))) {
            return null;
        }
        return player.getPersistentData().contains(POSITION)
                ? BlockPos.of(player.getPersistentData().getLong(POSITION)) : null;
    }

    static void clear(Player player) {
        player.getPersistentData().remove(POSITION);
        player.getPersistentData().remove(DIMENSION);
    }

    private AreaSelection() {
    }
}
