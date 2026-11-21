package net.claustra01.clayium.datagen.claytree;

import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.claustra01.clayium.world.claytree.ClayTreeSaplingBlock;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

public final class ClayTreeWorldgen {
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        TreeConfiguration configuration = new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ClayTreeRegistration.CLAY_TREE_LOG.get()),
                new StraightTrunkPlacer(5, 2, 0),
                BlockStateProvider.simple(ClayTreeRegistration.CLAY_TREE_LEAF.get()),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 0, 1))
                .ignoreVines()
                .build();
        context.register(ClayTreeSaplingBlock.CLAY_TREE, new ConfiguredFeature<>(Feature.TREE, configuration));
    }

    private ClayTreeWorldgen() {
    }
}
