package net.claustra01.clayium.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Generates data-driven worldgen definitions for Clay Tree and Overworld Clay Ore veins.
 */
public class ClayiumWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CLAY_TREE_CF =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ClayiumMod.id("clay_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> CLAY_ORE_VEIN_CF =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ClayiumMod.id("clay_ore_vein"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_DENSE_CLAY_ORE_VEIN_CF =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ClayiumMod.id("large_dense_clay_ore_vein"));

    public static final ResourceKey<PlacedFeature> CLAY_ORE_VEIN_PF =
            ResourceKey.create(Registries.PLACED_FEATURE, ClayiumMod.id("clay_ore_vein"));
    public static final ResourceKey<PlacedFeature> LARGE_DENSE_CLAY_ORE_VEIN_PF =
            ResourceKey.create(Registries.PLACED_FEATURE, ClayiumMod.id("large_dense_clay_ore_vein"));

    public static final ResourceKey<BiomeModifier> OVERWORLD_CLAY_ORES_BM =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ClayiumMod.id("overworld_clay_ores"));

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ClayiumWorldGenProvider::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, ClayiumWorldGenProvider::bootstrapPlacedFeatures)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ClayiumWorldGenProvider::bootstrapBiomeModifiers);

    public ClayiumWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(ClayiumMod.MOD_ID));
    }

    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(
                CLAY_TREE_CF,
                new ConfiguredFeature<>(
                        Feature.TREE,
                        new TreeConfiguration.TreeConfigurationBuilder(
                                BlockStateProvider.simple(ClayiumBlocks.byPath("clay_tree_log").get()),
                                new StraightTrunkPlacer(5, 2, 0),
                                BlockStateProvider.simple(ClayiumBlocks.byPath("clay_tree_leaves").get()),
                                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                                new TwoLayersFeatureSize(1, 0, 1)
                        ).ignoreVines().build()
                )
        );
        context.register(
                CLAY_ORE_VEIN_CF,
                new ConfiguredFeature<>(ClayiumFeatures.CLAY_COMPOUND_ORE.get(), NoneFeatureConfiguration.INSTANCE)
        );
        context.register(
                LARGE_DENSE_CLAY_ORE_VEIN_CF,
                new ConfiguredFeature<>(ClayiumFeatures.LARGE_DENSE_CLAY_ORE_FEATURE.get(), NoneFeatureConfiguration.INSTANCE)
        );
    }

    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(
                CLAY_ORE_VEIN_PF,
                new PlacedFeature(
                        configured.getOrThrow(CLAY_ORE_VEIN_CF),
                        List.of(
                                CountPlacement.of(8),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(24), VerticalAnchor.absolute(87)),
                                BiomeFilter.biome()
                        )
                )
        );
        context.register(
                LARGE_DENSE_CLAY_ORE_VEIN_PF,
                new PlacedFeature(
                        configured.getOrThrow(LARGE_DENSE_CLAY_ORE_VEIN_CF),
                        List.of(
                                CountPlacement.of(2),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(15)),
                                BiomeFilter.biome()
                        )
                )
        );
    }

    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        context.register(
                OVERWORLD_CLAY_ORES_BM,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(
                                placed.getOrThrow(CLAY_ORE_VEIN_PF),
                                placed.getOrThrow(LARGE_DENSE_CLAY_ORE_VEIN_PF)
                        ),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );
    }
}
