package net.claustra01.clayium.world;

import com.mojang.serialization.Codec;
import java.util.List;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

/**
 * Generates deep-level {@code large_dense_clay_ore} veins (default size 6, Y 10..15).
 */
public class LargeDenseClayOreFeature extends Feature<NoneFeatureConfiguration> {
    public LargeDenseClayOreFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int size = ClayiumConfig.LARGE_DENSE_CLAY_ORE_VEIN_SIZE.get();
        if (size <= 0) {
            return false;
        }
        Block largeDenseClayOre = ClayiumBlocks.byPath("large_dense_clay_ore").get();
        OreConfiguration config = new OreConfiguration(
                List.of(
                        OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), largeDenseClayOre.defaultBlockState()),
                        OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), largeDenseClayOre.defaultBlockState())
                ),
                size
        );
        return Feature.ORE.place(config, context.level(), context.chunkGenerator(), context.random(), context.origin());
    }
}
