package net.claustra01.clayium.datagen.worldgen;

import java.util.List;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.claustra01.clayium.worldgen.ClayOreConfiguration;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class WorldgenDataProvider {
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAIN_MIDDLE = configured("clay_ore_main_middle");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAIN_DEEP = configured("clay_ore_main_deep");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_MIDDLE = configured("large_dense_clay_ore_middle");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_DEEP = configured("large_dense_clay_ore_deep");

    public static final ResourceKey<PlacedFeature> MAIN_MIDDLE_PLACED = placed("clay_ore_main_middle");
    public static final ResourceKey<PlacedFeature> MAIN_DEEP_PLACED = placed("clay_ore_main_deep");
    public static final ResourceKey<PlacedFeature> LARGE_MIDDLE_PLACED = placed("large_dense_clay_ore_middle");
    public static final ResourceKey<PlacedFeature> LARGE_DEEP_PLACED = placed("large_dense_clay_ore_deep");
    public static final ResourceKey<BiomeModifier> ADD_CLAY_ORES = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS, ClayiumMod.id("add_clay_ores"));

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, WorldgenDataProvider::bootstrapConfigured)
            .add(Registries.PLACED_FEATURE, WorldgenDataProvider::bootstrapPlaced)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, WorldgenDataProvider::bootstrapBiomeModifiers);

    public static DatapackBuiltinEntriesProvider create(net.neoforged.neoforge.data.event.GatherDataEvent event) {
        return new DatapackBuiltinEntriesProvider(
                event.getGenerator().getPackOutput(), event.getLookupProvider(), BUILDER, Set.of(ClayiumMod.MOD_ID));
    }

    private static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        register(context, MAIN_MIDDLE, composite(24, 10, 0.5F, false));
        register(context, MAIN_DEEP, composite(24, 10, 0.5F, false));
        register(context, LARGE_MIDDLE, composite(6, 0, 0.0F, true));
        register(context, LARGE_DEEP, composite(6, 0, 0.0F, true));
    }

    private static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        registerPlaced(context, MAIN_MIDDLE_PLACED, configured.getOrThrow(MAIN_MIDDLE), 8, 16, 95);
        registerPlaced(context, MAIN_DEEP_PLACED, configured.getOrThrow(MAIN_DEEP), 3, -48, 15);
        registerPlaced(context, LARGE_MIDDLE_PLACED, configured.getOrThrow(LARGE_MIDDLE), 1, 8, 23);
        registerPlaced(context, LARGE_DEEP_PLACED, configured.getOrThrow(LARGE_DEEP), 1, -48, -9);
    }

    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        context.register(
                ADD_CLAY_ORES,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(List.of(
                                placed.getOrThrow(MAIN_MIDDLE_PLACED),
                                placed.getOrThrow(MAIN_DEEP_PLACED),
                                placed.getOrThrow(LARGE_MIDDLE_PLACED),
                                placed.getOrThrow(LARGE_DEEP_PLACED))),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
    }

    private static ClayOreConfiguration composite(int primarySize, int denseSize, float largeChance, boolean independentLarge) {
        return new ClayOreConfiguration(
                List.of(
                        new ClayOreConfiguration.Target(
                                new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                                WorldgenRegistration.CLAY_ORE.get().defaultBlockState(),
                                WorldgenRegistration.DENSE_CLAY_ORE.get().defaultBlockState(),
                                WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get().defaultBlockState()),
                        new ClayOreConfiguration.Target(
                                new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                                WorldgenRegistration.DEEPSLATE_CLAY_ORE.get().defaultBlockState(),
                                WorldgenRegistration.DEEPSLATE_DENSE_CLAY_ORE.get().defaultBlockState(),
                                WorldgenRegistration.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get().defaultBlockState())),
                primarySize,
                denseSize,
                largeChance,
                0.0F,
                independentLarge);
    }

    private static void register(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            ClayOreConfiguration configuration) {
        context.register(key, new ConfiguredFeature<>(WorldgenRegistration.COMPOSITE_CLAY_ORE.get(), configuration));
    }

    private static void registerPlaced(
            BootstrapContext<PlacedFeature> context,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> configured,
            int count,
            int minimumY,
            int maximumY) {
        context.register(
                key,
                new PlacedFeature(
                        configured,
                        List.of(
                                CountPlacement.of(count),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(minimumY), VerticalAnchor.absolute(maximumY)),
                                BiomeFilter.biome())));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ClayiumMod.id(path));
    }

    private static ResourceKey<PlacedFeature> placed(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ClayiumMod.id(path));
    }

    private WorldgenDataProvider() {
    }
}
