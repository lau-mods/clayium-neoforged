package net.claustra01.clayium.world;

import com.mojang.serialization.Codec;
import java.util.List;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

/**
 * Generates Clayium compound clay ore veins: outer {@code clay_ore} vein (size 24),
 * optional inner {@code dense_clay_ore} vein (size 10 replacing {@code clay_ore}),
 * and a 50% core block upgrade to {@code large_dense_clay_ore} at {@code origin + (8, 0, 8)}.
 */
public class ClayCompoundOreFeature extends Feature<NoneFeatureConfiguration> {
    public ClayCompoundOreFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int outerSize = ClayiumConfig.CLAY_ORE_VEIN_SIZE.get();
        if (outerSize <= 0) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        Block clayOre = ClayiumBlocks.byPath("clay_ore").get();
        Block denseClayOre = ClayiumBlocks.byPath("dense_clay_ore").get();
        Block largeDenseClayOre = ClayiumBlocks.byPath("large_dense_clay_ore").get();

        OreConfiguration outerConfig = new OreConfiguration(
                List.of(
                        OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), clayOre.defaultBlockState()),
                        OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), clayOre.defaultBlockState())
                ),
                outerSize
        );
        boolean placed = Feature.ORE.place(outerConfig, level, context.chunkGenerator(), random, origin);

        if (ClayiumConfig.GENERATE_DENSE_CLAY_ORE_VEIN.get()) {
            int innerSize = ClayiumConfig.DENSE_CLAY_ORE_VEIN_SIZE.get();
            if (innerSize > 0) {
                OreConfiguration innerConfig = new OreConfiguration(
                        new BlockMatchTest(clayOre),
                        denseClayOre.defaultBlockState(),
                        innerSize
                );
                placed |= Feature.ORE.place(innerConfig, level, context.chunkGenerator(), random, origin);
            }
            if (random.nextInt(2) == 0) {
                BlockPos corePos = origin.offset(8, 0, 8);
                if (level.getBlockState(corePos).is(denseClayOre)) {
                    level.setBlock(corePos, largeDenseClayOre.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        return placed;
    }
}
