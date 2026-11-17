package net.claustra01.clayium.worldgen;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

public final class CompositeClayOreFeature extends Feature<ClayOreConfiguration> {
    public CompositeClayOreFeature() {
        super(ClayOreConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<ClayOreConfiguration> context) {
        ClayOreConfiguration configuration = context.config();
        BlockPos center = context.origin().offset(8, 0, 8);
        if (configuration.independentLarge()) {
            return Feature.ORE.place(
                    oreConfiguration(configuration, ClayOreConfiguration.Target::largeState, configuration.primarySize()),
                    context.level(),
                    context.chunkGenerator(),
                    context.random(),
                    center);
        }

        boolean placed = Feature.ORE.place(
                oreConfiguration(configuration, ClayOreConfiguration.Target::primaryState, configuration.primarySize()),
                context.level(),
                context.chunkGenerator(),
                context.random(),
                center);
        if (configuration.denseSize() > 0) {
            List<OreConfiguration.TargetBlockState> denseTargets = configuration.targets().stream()
                    .map(target -> OreConfiguration.target(
                            new BlockMatchTest(target.primaryState().getBlock()), target.denseState()))
                    .toList();
            Feature.ORE.place(
                    new OreConfiguration(denseTargets, configuration.denseSize(), configuration.discardChanceOnAirExposure()),
                    context.level(),
                    context.chunkGenerator(),
                    context.random(),
                    center);
        }
        BlockState state = context.level().getBlockState(center);
        configuration.targets().stream()
                .filter(target -> state.is(target.denseState().getBlock()))
                .findFirst()
                .filter(target -> context.random().nextFloat() < configuration.largeChance())
                .ifPresent(target -> context.level().setBlock(center, target.largeState(), 2));
        return placed;
    }

    private static OreConfiguration oreConfiguration(
            ClayOreConfiguration configuration,
            java.util.function.Function<ClayOreConfiguration.Target, BlockState> state,
            int size) {
        List<OreConfiguration.TargetBlockState> targets = configuration.targets().stream()
                .map(target -> OreConfiguration.target(target.target(), state.apply(target)))
                .toList();
        return new OreConfiguration(targets, size, configuration.discardChanceOnAirExposure());
    }
}
