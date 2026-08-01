/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.item;

import net.claustra01.clayium.Clayium;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Shared block classification for the Clayium ore tools. */
public final class ClayOreToolHelper {
    public static final TagKey<Block> CLAY_ORES = BlockTags.create(Clayium.id("clay_ores"));

    private ClayOreToolHelper() {
    }

    public static boolean isClayOre(BlockState state) {
        return state.is(CLAY_ORES);
    }
}
