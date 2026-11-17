package net.claustra01.clayium.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public record ClayOreConfiguration(
        List<Target> targets,
        int primarySize,
        int denseSize,
        float largeChance,
        float discardChanceOnAirExposure,
        boolean independentLarge) implements FeatureConfiguration {
    public static final Codec<ClayOreConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Target.CODEC.listOf().fieldOf("targets").forGetter(ClayOreConfiguration::targets),
                    Codec.intRange(1, 64).fieldOf("primary_size").forGetter(ClayOreConfiguration::primarySize),
                    Codec.intRange(0, 64).fieldOf("dense_size").forGetter(ClayOreConfiguration::denseSize),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("large_chance").forGetter(ClayOreConfiguration::largeChance),
                    Codec.floatRange(0.0F, 1.0F)
                            .fieldOf("discard_chance_on_air_exposure")
                            .forGetter(ClayOreConfiguration::discardChanceOnAirExposure),
                    Codec.BOOL.fieldOf("independent_large").forGetter(ClayOreConfiguration::independentLarge))
            .apply(instance, ClayOreConfiguration::new));

    public record Target(RuleTest target, BlockState primaryState, BlockState denseState, BlockState largeState) {
        public static final Codec<Target> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        RuleTest.CODEC.fieldOf("target").forGetter(Target::target),
                        BlockState.CODEC.fieldOf("primary_state").forGetter(Target::primaryState),
                        BlockState.CODEC.fieldOf("dense_state").forGetter(Target::denseState),
                        BlockState.CODEC.fieldOf("large_state").forGetter(Target::largeState))
                .apply(instance, Target::new));
    }
}
