package net.claustra01.clayium.gametest.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.claustra01.clayium.datagen.worldgen.WorldgenDataProvider;
import net.claustra01.clayium.worldgen.ClayOreConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.BiomeTags;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WorldgenGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 200)
    public static void compositeOreGenerationIsNestedAndHostAware(GameTestHelper helper) {
        ClayOreConfiguration defaults = mainConfiguration(helper);
        ClayOreConfiguration primaryOnly = new ClayOreConfiguration(
                defaults.targets(), defaults.primarySize(), 0, 0.0F, 0.0F, false);
        ClayOreConfiguration allUpgrades = new ClayOreConfiguration(
                defaults.targets(), defaults.primarySize(), defaults.denseSize(), 1.0F, 0.0F, false);

        BlockPos baselineOrigin = new BlockPos(1600, 32, 1600);
        BlockPos stoneOrigin = baselineOrigin.offset(40, 0, 0);
        BlockPos deepslateOrigin = baselineOrigin.offset(80, 0, 0);
        fillHost(helper, baselineOrigin, Blocks.STONE);
        fillHost(helper, stoneOrigin, Blocks.STONE);
        fillHost(helper, deepslateOrigin, Blocks.DEEPSLATE);

        place(helper, primaryOnly, baselineOrigin, 741_852L);
        place(helper, allUpgrades, stoneOrigin, 741_852L);
        place(helper, allUpgrades, deepslateOrigin, 741_852L);

        Set<BlockPos> baseline = relativeOrePositions(helper, baselineOrigin, Set.of(WorldgenRegistration.CLAY_ORE.get()));
        Set<BlockPos> nested = relativeOrePositions(
                helper,
                stoneOrigin,
                Set.of(
                        WorldgenRegistration.CLAY_ORE.get(),
                        WorldgenRegistration.DENSE_CLAY_ORE.get(),
                        WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get()));
        require(!baseline.isEmpty(), "The fixed seed did not generate a primary vein");
        require(nested.equals(baseline), "Dense generation escaped the primary vein footprint");
        require(count(helper, stoneOrigin, WorldgenRegistration.DENSE_CLAY_ORE.get()) > 0,
                "The primary vein has no dense interior");
        require(count(helper, stoneOrigin, WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get()) == 1,
                "The guaranteed large conversion did not replace exactly one dense center block");
        require(count(helper, deepslateOrigin, WorldgenRegistration.CLAY_ORE.get()) == 0
                        && count(helper, deepslateOrigin, WorldgenRegistration.DENSE_CLAY_ORE.get()) == 0,
                "Stone variants generated in a deepslate host");
        require(count(helper, deepslateOrigin, WorldgenRegistration.DEEPSLATE_CLAY_ORE.get()) > 0
                        && count(helper, deepslateOrigin, WorldgenRegistration.DEEPSLATE_DENSE_CLAY_ORE.get()) > 0
                        && count(helper, deepslateOrigin, WorldgenRegistration.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get()) == 1,
                "Deepslate variants were not selected through the deepslate replaceable tag");
        BlockPos blockedOrigin = baselineOrigin.offset(120, 0, 0);
        fillHost(helper, blockedOrigin, Blocks.STONE);
        helper.getLevel().setBlock(blockedOrigin.offset(8, 0, 8), Blocks.BEDROCK.defaultBlockState(), 2);
        place(helper, allUpgrades, blockedOrigin, 741_852L);
        require(count(helper, blockedOrigin, WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get()) == 0,
                "A non-dense center was upgraded to large ore");

        ClayOreConfiguration halfChance = new ClayOreConfiguration(
                defaults.targets(), defaults.primarySize(), defaults.denseSize(), 0.5F, 0.0F, false);
        BlockPos halfOrigin = baselineOrigin.offset(160, 0, 0);
        fillHost(helper, halfOrigin, Blocks.STONE);
        RecordingRandomSource random = new RecordingRandomSource(741_852L);
        WorldgenRegistration.COMPOSITE_CLAY_ORE.get().place(
                halfChance, helper.getLevel(), helper.getLevel().getChunkSource().getGenerator(), random, halfOrigin);
        require(relativeOrePositions(helper, halfOrigin, Set.of(
                        WorldgenRegistration.CLAY_ORE.get(), WorldgenRegistration.DENSE_CLAY_ORE.get(),
                        WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get())).equals(baseline),
                "The half-chance roll changed the primary vein footprint");
        require(count(helper, halfOrigin, WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get())
                        == (random.lastFloat < 0.5F ? 1 : 0),
                "The large ore did not follow the fixed-seed 50% roll");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void fourOreLayersUseSpecifiedTrialsSizesAndHeights(GameTestHelper helper) {
        assertConfigured(helper, "clay_ore_main_middle", 24, 10, 0.5F, false);
        assertConfigured(helper, "clay_ore_main_deep", 24, 10, 0.5F, false);
        assertConfigured(helper, "large_dense_clay_ore_middle", 6, 0, 0.0F, true);
        assertConfigured(helper, "large_dense_clay_ore_deep", 6, 0, 0.0F, true);
        assertPlaced(helper, "clay_ore_main_middle", 8, 16, 95);
        assertPlaced(helper, "clay_ore_main_deep", 3, -48, 15);
        assertPlaced(helper, "large_dense_clay_ore_middle", 1, 8, 23);
        assertPlaced(helper, "large_dense_clay_ore_deep", 1, -48, -9);
        List<String> ids = List.of("clay_ore_main_middle", "clay_ore_main_deep",
                "large_dense_clay_ore_middle", "large_dense_clay_ore_deep");
        int[] minimum = {16, -48, 8, -48};
        int[] maximum = {95, 15, 23, -9};
        for (int index = 0; index < ids.size(); index++) {
            BlockPos origin = new BlockPos(4096 + index * 64, 0, 4096);
            ServerLevel level = helper.getLevel();
            level.getChunk(origin.getX() >> 4, origin.getZ() >> 4);
            Block host = index % 2 == 0 ? Blocks.STONE : Blocks.DEEPSLATE;
            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    for (int y = minimum[index] - 10; y <= maximum[index] + 10; y++) {
                        level.setBlock(origin.offset(x, y, z), host.defaultBlockState(), 2);
                    }
                }
            }
            PlacedFeature placed = withoutBiomeFilter(helper, ids.get(index));
            placed.place(level, level.getChunkSource().getGenerator(),
                    RandomSource.create(81_204L + index), origin);
            int ores = 0;
            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    for (int y = minimum[index] - 10; y <= maximum[index] + 10; y++) {
                        BlockState state = level.getBlockState(origin.offset(x, y, z));
                        if (WorldgenRegistration.BLOCKS.stream().anyMatch(block -> state.is(block.get()))) {
                            ores++;
                            require(state.is(index % 2 == 0 ? WorldgenRegistration.CLAY_ORE.get()
                                            : WorldgenRegistration.DEEPSLATE_CLAY_ORE.get())
                                            || state.is(index % 2 == 0 ? WorldgenRegistration.DENSE_CLAY_ORE.get()
                                                    : WorldgenRegistration.DEEPSLATE_DENSE_CLAY_ORE.get())
                                            || state.is(index % 2 == 0 ? WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get()
                                                    : WorldgenRegistration.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get()),
                                    "Ore used the wrong host variant in " + ids.get(index));
                            require(y >= level.getMinBuildHeight() && y < level.getMaxBuildHeight(),
                                    "Ore escaped the world build range: " + ids.get(index));
                        }
                    }
                }
            }
            require(ores > 0, "The fixed-seed layer placed no ores: " + ids.get(index));
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void worldgenIsOverworldOnlyAndDatapackDriven(GameTestHelper helper) {
        require(helper.getLevel().dimension().equals(Level.OVERWORLD), "The standard GameTest level is not the overworld");
        require(helper.getLevel().getMinBuildHeight() == -64 && helper.getLevel().getMaxBuildHeight() == 320,
                "Unexpected standard overworld build limits");
        var modifier = helper.getLevel().registryAccess()
                .registryOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS)
                .getOrThrow(WorldgenDataProvider.ADD_CLAY_ORES);
        require(modifier instanceof BiomeModifiers.AddFeaturesBiomeModifier, "Missing add-features biome modifier");
        var added = (BiomeModifiers.AddFeaturesBiomeModifier) modifier;
        require(added.biomes() instanceof HolderSet.Named<?> named
                        && named.key().equals(BiomeTags.IS_OVERWORLD),
                "Clay ores are not restricted to overworld biomes");
        require(added.features().size() == 4, "The biome modifier must add exactly four layers");
        require(added.features().stream().map(holder -> holder.unwrapKey().orElseThrow().location())
                        .collect(java.util.stream.Collectors.toSet()).equals(Set.of(
                                WorldgenDataProvider.MAIN_MIDDLE_PLACED.location(),
                                WorldgenDataProvider.MAIN_DEEP_PLACED.location(),
                                WorldgenDataProvider.LARGE_MIDDLE_PLACED.location(),
                                WorldgenDataProvider.LARGE_DEEP_PLACED.location())),
                "The biome modifier uses the wrong placed features");
        require(added.step() == net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
                "Clay ores use the wrong generation step");

        ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        ServerLevel end = helper.getLevel().getServer().getLevel(Level.END);
        require(nether != null && end != null, "The standard Nether and End dimensions are missing");
        require(countGeneratedOres(nether, new ChunkPos(37, 37)) == 0,
                "Clay ores generated in a new Nether chunk");
        require(countGeneratedOres(end, new ChunkPos(37, 37)) == 0,
                "Clay ores generated in a new End chunk");

        BlockPos origin = new BlockPos(5120, 0, 5120);
        fillChunkHost(helper.getLevel(), origin, 40, Blocks.STONE);
        PlacedFeature changed = overridePlacement(helper, "clay_ore_main_middle", 4, 40);
        changed.place(helper.getLevel(), helper.getLevel().getChunkSource().getGenerator(),
                RandomSource.create(320_041L), origin);
        Set<BlockPos> original = orePositions(helper.getLevel(), origin, 40);
        require(!original.isEmpty(), "Datapack-style placed feature did not create ore");
        for (BlockPos position : original) {
            require(position.getY() >= 30 && position.getY() <= 50,
                    "Overridden height was not applied to a new chunk");
        }
        helper.getLevel().getChunk((origin.getX() >> 4) + 1, origin.getZ() >> 4);
        helper.getLevel().getChunk(origin.getX() >> 4, origin.getZ() >> 4);
        require(orePositions(helper.getLevel(), origin, 40).equals(original),
                "Requesting a generated chunk rewrote its ore blocks");
        BlockPos emptyOrigin = origin.offset(64, 0, 0);
        fillChunkHost(helper.getLevel(), emptyOrigin, 40, Blocks.STONE);
        overridePlacement(helper, "clay_ore_main_middle", 0, 40).place(
                helper.getLevel(), helper.getLevel().getChunkSource().getGenerator(),
                RandomSource.create(320_041L), emptyOrigin);
        require(orePositions(helper.getLevel(), emptyOrigin, 40).isEmpty(),
                "A zero-count datapack override still generated ore");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void oreDropsMatchRanksFortuneAndSilkTouch(GameTestHelper helper) {
        ItemStack normalTool = new ItemStack(Items.DIAMOND_PICKAXE);
        ItemStack fortuneTool = new ItemStack(Items.DIAMOND_PICKAXE);
        ItemStack silkTool = new ItemStack(Items.DIAMOND_PICKAXE);
        Holder<Enchantment> fortune = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        Holder<Enchantment> silkTouch = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        fortuneTool.enchant(fortune, 3);
        silkTool.enchant(silkTouch, 1);

        assertClayDrops(helper, WorldgenRegistration.CLAY_ORE.get(), normalTool, fortuneTool, silkTool);
        assertClayDrops(helper, WorldgenRegistration.DEEPSLATE_CLAY_ORE.get(), normalTool, fortuneTool, silkTool);
        assertSelfDrops(helper, WorldgenRegistration.DENSE_CLAY_ORE.get(), normalTool, fortuneTool, silkTool);
        assertSelfDrops(helper, WorldgenRegistration.DEEPSLATE_DENSE_CLAY_ORE.get(), normalTool, fortuneTool, silkTool);
        assertSelfDrops(helper, WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get(), normalTool, fortuneTool, silkTool);
        assertSelfDrops(helper, WorldgenRegistration.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get(), normalTool, fortuneTool, silkTool);

        for (var ore : WorldgenRegistration.BLOCKS) {
            BlockState state = ore.get().defaultBlockState();
            require(state.is(BlockTags.NEEDS_STONE_TOOL), ore.get() + " lost the stone-pickaxe rank");
            require(!new ItemStack(Items.WOODEN_PICKAXE).isCorrectToolForDrops(state),
                    ore.get() + " can be harvested below stone tier");
            require(new ItemStack(Items.STONE_PICKAXE).isCorrectToolForDrops(state),
                    ore.get() + " cannot be harvested at stone tier");
            require(state.getDestroySpeed(helper.getLevel(), helper.absolutePos(BlockPos.ZERO)) == 3.0F,
                    ore.get() + " lost the original hardness");
            require(ore.get().getExplosionResistance() == 5.0F,
                    ore.get() + " lost the original explosion resistance");
        }
        helper.succeed();
    }

    private static void assertClayDrops(
            GameTestHelper helper,
            Block block,
            ItemStack normalTool,
            ItemStack fortuneTool,
            ItemStack silkTool) {
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        int normalTotal = 0;
        int fortuneTotal = 0;
        for (int attempt = 0; attempt < 32; attempt++) {
            int normal = dropCount(block, helper, position.offset(attempt, 0, 0), normalTool, Items.CLAY_BALL);
            require(normal >= 4 && normal <= 16, "Clay drop is outside the original 4-16 range for " + block);
            normalTotal += normal;
            fortuneTotal += dropCount(block, helper, position.offset(attempt, 0, 1), fortuneTool, Items.CLAY_BALL);
        }
        require(fortuneTotal > normalTotal, "Fortune did not increase aggregate drops for " + block);
        require(isSingleSelfDrop(block, helper, position, silkTool), "Silk Touch did not preserve " + block);
    }

    private static void assertSelfDrops(
            GameTestHelper helper,
            Block block,
            ItemStack normalTool,
            ItemStack fortuneTool,
            ItemStack silkTool) {
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        require(isSingleSelfDrop(block, helper, position, normalTool), "The dense ore did not drop itself: " + block);
        require(isSingleSelfDrop(block, helper, position, fortuneTool), "Fortune changed the dense ore drop: " + block);
        require(isSingleSelfDrop(block, helper, position, silkTool), "Silk Touch did not preserve " + block);
    }

    private static int dropCount(Block block, GameTestHelper helper, BlockPos position, ItemStack tool, Item expected) {
        List<ItemStack> drops = Block.getDrops(block.defaultBlockState(), helper.getLevel(), position, null, null, tool);
        require(drops.stream().allMatch(stack -> stack.is(expected)), "Unexpected resource dropped from " + block);
        return drops.stream().mapToInt(ItemStack::getCount).sum();
    }

    private static boolean isSingleSelfDrop(Block block, GameTestHelper helper, BlockPos position, ItemStack tool) {
        List<ItemStack> drops = Block.getDrops(block.defaultBlockState(), helper.getLevel(), position, null, null, tool);
        return drops.size() == 1 && drops.getFirst().is(block.asItem()) && drops.getFirst().getCount() == 1;
    }

    private static void assertConfigured(
            GameTestHelper helper, String id, int primarySize, int denseSize, float chance, boolean independent) {
        ClayOreConfiguration config = configuration(helper, id);
        require(config.primarySize() == primarySize, "Wrong primary size for " + id);
        require(config.denseSize() == denseSize, "Wrong dense size for " + id);
        require(config.largeChance() == chance, "Wrong large chance for " + id);
        require(config.independentLarge() == independent, "Wrong generation mode for " + id);
        require(config.discardChanceOnAirExposure() == 0.0F, "Exposure suppression must be disabled");
        var encoded = ClayOreConfiguration.CODEC.encodeStart(
                        RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess()), config)
                .getOrThrow(message -> new GameTestAssertException(message));
        JsonArray targets = encoded.getAsJsonObject().getAsJsonArray("targets");
        require(targets.size() == 2
                        && targets.get(0).getAsJsonObject().getAsJsonObject("target").get("tag").getAsString()
                                .equals("minecraft:stone_ore_replaceables")
                        && targets.get(1).getAsJsonObject().getAsJsonObject("target").get("tag").getAsString()
                                .equals("minecraft:deepslate_ore_replaceables"),
                "Ore host targets are not the vanilla replaceable tags");
    }

    private static void assertPlaced(GameTestHelper helper, String id, int count, int minimumY, int maximumY) {
        PlacedFeature placed = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE)
                .getOrThrow(ResourceKey.create(Registries.PLACED_FEATURE, ClayiumMod.id(id)));
        var encoded = PlacedFeature.DIRECT_CODEC.encodeStart(
                        RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess()), placed)
                .getOrThrow(message -> new GameTestAssertException(message));
        JsonArray placements = encoded.getAsJsonObject().getAsJsonArray("placement");
        require(placements.size() == 4, "Wrong placement modifier count for " + id);
        require(placements.get(0).getAsJsonObject().get("count").getAsInt() == count, "Wrong attempts per chunk for " + id);
        require(placements.get(1).getAsJsonObject().get("type").getAsString().equals("minecraft:in_square"),
                "Missing in-square placement for " + id);
        JsonObject height = placements.get(2).getAsJsonObject().getAsJsonObject("height");
        require(height.getAsJsonObject("min_inclusive").get("absolute").getAsInt() == minimumY
                        && height.getAsJsonObject("max_inclusive").get("absolute").getAsInt() == maximumY,
                "Wrong inclusive height range for " + id);
        require(placements.get(3).getAsJsonObject().get("type").getAsString().equals("minecraft:biome"),
                "Missing biome placement filter for " + id);
        require(minimumY >= helper.getLevel().getMinBuildHeight()
                        && maximumY < helper.getLevel().getMaxBuildHeight(),
                "Layer exceeds the overworld build range: " + id);
    }

    private static ClayOreConfiguration mainConfiguration(GameTestHelper helper) {
        return configuration(helper, "clay_ore_main_middle");
    }

    private static ClayOreConfiguration configuration(GameTestHelper helper, String id) {
        ConfiguredFeature<?, ?> feature = helper.getLevel().registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, ClayiumMod.id(id)));
        require(feature.feature() == WorldgenRegistration.COMPOSITE_CLAY_ORE.get(), "Wrong feature type for " + id);
        return (ClayOreConfiguration) feature.config();
    }

    private static PlacedFeature withoutBiomeFilter(GameTestHelper helper, String id) {
        PlacedFeature loaded = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE)
                .getOrThrow(ResourceKey.create(Registries.PLACED_FEATURE, ClayiumMod.id(id)));
        var ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        JsonObject json = PlacedFeature.DIRECT_CODEC.encodeStart(ops, loaded)
                .getOrThrow(message -> new GameTestAssertException(message)).getAsJsonObject();
        json.getAsJsonArray("placement").remove(3);
        return PlacedFeature.DIRECT_CODEC.parse(ops, json)
                .getOrThrow(message -> new GameTestAssertException(message));
    }

    private static PlacedFeature overridePlacement(GameTestHelper helper, String id, int count, int y) {
        PlacedFeature loaded = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE)
                .getOrThrow(ResourceKey.create(Registries.PLACED_FEATURE, ClayiumMod.id(id)));
        var ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        JsonObject json = PlacedFeature.DIRECT_CODEC.encodeStart(ops, loaded)
                .getOrThrow(message -> new GameTestAssertException(message)).getAsJsonObject();
        JsonArray modifiers = json.getAsJsonArray("placement");
        modifiers.get(0).getAsJsonObject().addProperty("count", count);
        JsonObject anchor = new JsonObject();
        anchor.addProperty("absolute", y);
        JsonObject height = modifiers.get(2).getAsJsonObject().getAsJsonObject("height");
        height.add("min_inclusive", anchor.deepCopy());
        height.add("max_inclusive", anchor);
        modifiers.remove(3);
        return PlacedFeature.DIRECT_CODEC.parse(ops, json)
                .getOrThrow(message -> new GameTestAssertException("Cannot decode placed-feature override: " + message));
    }

    private static void fillChunkHost(ServerLevel level, BlockPos origin, int y, Block block) {
        level.getChunk(origin.getX() >> 4, origin.getZ() >> 4);
        for (int x = -8; x < 40; x++) {
            for (int z = -8; z < 40; z++) {
                for (int offsetY = -10; offsetY <= 10; offsetY++) {
                    level.setBlock(origin.offset(x, y + offsetY, z), block.defaultBlockState(), 2);
                }
            }
        }
    }

    private static Set<BlockPos> orePositions(ServerLevel level, BlockPos origin, int y) {
        Set<Block> ores = new HashSet<>(WorldgenRegistration.BLOCKS.stream().map(block -> block.get()).toList());
        Set<BlockPos> positions = new HashSet<>();
        for (int x = -8; x < 40; x++) {
            for (int z = -8; z < 40; z++) {
                for (int offsetY = -10; offsetY <= 10; offsetY++) {
                    BlockPos position = origin.offset(x, y + offsetY, z);
                    if (ores.contains(level.getBlockState(position).getBlock())) {
                        positions.add(position);
                    }
                }
            }
        }
        return positions;
    }

    private static void place(GameTestHelper helper, ClayOreConfiguration configuration, BlockPos origin, long seed) {
        WorldgenRegistration.COMPOSITE_CLAY_ORE.get().place(
                configuration,
                helper.getLevel(),
                helper.getLevel().getChunkSource().getGenerator(),
                RandomSource.create(seed),
                origin);
    }

    private static void fillHost(GameTestHelper helper, BlockPos origin, Block block) {
        for (int x = -8; x <= 24; x++) {
            for (int y = -8; y <= 8; y++) {
                for (int z = -8; z <= 24; z++) {
                    helper.getLevel().setBlock(origin.offset(x, y, z), block.defaultBlockState(), 2);
                }
            }
        }
    }

    private static Set<BlockPos> relativeOrePositions(GameTestHelper helper, BlockPos origin, Set<Block> ores) {
        Set<BlockPos> positions = new HashSet<>();
        for (int x = -8; x <= 24; x++) {
            for (int y = -8; y <= 8; y++) {
                for (int z = -8; z <= 24; z++) {
                    if (ores.contains(helper.getLevel().getBlockState(origin.offset(x, y, z)).getBlock())) {
                        positions.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return positions;
    }

    private static int count(GameTestHelper helper, BlockPos origin, Block block) {
        return relativeOrePositions(helper, origin, Set.of(block)).size();
    }

    private static int countGeneratedOres(ServerLevel level, ChunkPos chunkPos) {
        level.getChunk(chunkPos.x, chunkPos.z);
        int count = 0;
        Set<Block> ores = new HashSet<>(WorldgenRegistration.BLOCKS.stream().map(block -> block.get()).toList());
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    if (ores.contains(level.getBlockState(new BlockPos(x, y, z)).getBlock())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static final class RecordingRandomSource implements RandomSource {
        private final RandomSource delegate;
        private float lastFloat;

        private RecordingRandomSource(long seed) {
            this.delegate = RandomSource.create(seed);
        }

        @Override
        public RandomSource fork() {
            return delegate.fork();
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            return delegate.forkPositional();
        }

        @Override
        public void setSeed(long seed) {
            delegate.setSeed(seed);
        }

        @Override
        public int nextInt() {
            return delegate.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            return delegate.nextInt(bound);
        }

        @Override
        public long nextLong() {
            return delegate.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            return delegate.nextBoolean();
        }

        @Override
        public float nextFloat() {
            lastFloat = delegate.nextFloat();
            return lastFloat;
        }

        @Override
        public double nextDouble() {
            return delegate.nextDouble();
        }

        @Override
        public double nextGaussian() {
            return delegate.nextGaussian();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private WorldgenGameTests() {
    }
}
