package net.claustra01.clayium.tool.advanced;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ClayToolProperties {
    public static final TagKey<Block> CLAY_ORES = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ClayiumMod.MOD_ID, "clay_ores"));
    public static final TagKey<Block> ORDINARY_CLAY_ORES = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ClayiumMod.MOD_ID, "ordinary_clay_ores"));

    public static boolean isClayOre(BlockState state) {
        return state.is(CLAY_ORES);
    }

    public static boolean isOrdinaryClayOre(BlockState state) {
        return state.is(ORDINARY_CLAY_ORES);
    }

    public static boolean isClayBlock(BlockState state) {
        return state.is(Blocks.CLAY) || state.is(BlockTags.TERRACOTTA);
    }

    private ClayToolProperties() {
    }
}
