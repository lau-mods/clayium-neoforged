package net.claustra01.clayium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.ClayContainerBlock.FacingMode;
import net.claustra01.clayium.block.ClayContainerBlock.OverlayMode;
import net.claustra01.clayium.block.ClayCraftingTableBlock;
import net.claustra01.clayium.block.ClayMarkerBlock;
import net.claustra01.clayium.block.LaserReflectorBlock;
import net.claustra01.clayium.block.PANAdapterBlock;
import net.claustra01.clayium.block.PANDuplicatorBlock;
import net.claustra01.clayium.block.StorageContainerBlock;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.StorageContentData;
import net.claustra01.clayium.loot.ClayOreBonusLootFunction;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.util.ClayiumTooltips;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SemanticParityTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void specialBlocksPreserveShapesSoundsFacingsAndStates() {
        Block craftingTable = ClayiumBlocks.byPath("clay_crafting_table").get();
        assertTrue(craftingTable instanceof ClayCraftingTableBlock);
        AABB ctBox = craftingTable.defaultBlockState()
                .getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty())
                .bounds();
        assertEquals(0.0, ctBox.minY, 1e-6);
        assertEquals(0.25, ctBox.maxY, 1e-6);

        for (String markerId : List.of(
                "clay_marker",
                "clay_open_pit_marker",
                "clay_ground_leveling_marker",
                "clay_prism_marker")) {
            Block marker = ClayiumBlocks.byPath(markerId).get();
            assertTrue(marker instanceof ClayMarkerBlock, markerId);
            AABB box = marker.defaultBlockState()
                    .getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty())
                    .bounds();
            assertEquals(5.0 / 16.0, box.minX, 1e-6);
            assertEquals(11.0 / 16.0, box.maxX, 1e-6);
            assertEquals(5.0 / 16.0, box.minY, 1e-6);
            assertEquals(11.0 / 16.0, box.maxZ, 1e-6);
        }

        Block reflector = ClayiumBlocks.byPath("laser_reflector").get();
        assertTrue(reflector instanceof LaserReflectorBlock);
        assertEquals(SoundType.GLASS, reflector.defaultBlockState().getSoundType());
        AABB northBox = LaserReflectorBlock.shapeForFacing(Direction.NORTH).bounds();
        assertEquals(4.0 / 16.0, northBox.minX, 1e-6);
        assertEquals(2.0 / 16.0, northBox.minZ, 1e-6);
        assertEquals(14.0 / 16.0, northBox.maxZ, 1e-6);
        AABB upBox = LaserReflectorBlock.shapeForFacing(Direction.UP).bounds();
        assertEquals(2.0 / 16.0, upBox.minY, 1e-6);
        assertEquals(14.0 / 16.0, upBox.maxY, 1e-6);

        ClayContainerBlock workTable = (ClayContainerBlock) ClayiumBlocks.byPath("clay_work_table").get();
        assertEquals(SoundType.STONE, workTable.defaultBlockState().getSoundType());
        assertEquals(2.0F, workTable.defaultDestroyTime(), 1e-6);

        ClayContainerBlock chunkLoader = (ClayContainerBlock) ClayiumBlocks.byPath("clay_chunk_loader").get();
        assertEquals(6.0F, chunkLoader.defaultDestroyTime(), 1e-6);
        assertEquals(OverlayMode.ALL_FACES, chunkLoader.getOverlayMode());

        ClayContainerBlock panCore = (ClayContainerBlock) ClayiumBlocks.byPath("pan_core").get();
        assertEquals(FacingMode.HORIZONTAL, panCore.getFacingMode());

        PANAdapterBlock panAdapter = (PANAdapterBlock) ClayiumBlocks.byPath("antimatter_pan_adapter").get();
        assertEquals(FacingMode.ALL, panAdapter.getFacingMode());
        assertEquals(OverlayMode.ALL_FACES, panAdapter.getOverlayMode());
        assertEquals(11, panAdapter.getTier(new ItemStack(panAdapter)));

        PANDuplicatorBlock basicDup = (PANDuplicatorBlock) ClayiumBlocks.byPath("basic_pan_duplicator").get();
        PANDuplicatorBlock ultDup = (PANDuplicatorBlock) ClayiumBlocks.byPath("ultimate_pan_duplicator").get();
        PANDuplicatorBlock opaDup = (PANDuplicatorBlock) ClayiumBlocks.byPath("opa_pan_duplicator").get();
        assertEquals(FacingMode.HORIZONTAL, basicDup.getFacingMode());
        assertEquals("machinehull-3", basicDup.getBaseTexture());
        assertEquals(OverlayMode.PAN_DUPLICATOR, basicDup.getOverlayMode());
        assertEquals(11, basicDup.getTier(new ItemStack(basicDup)));
        assertEquals(12, ultDup.getTier(new ItemStack(ultDup)));
        assertEquals(13, opaDup.getTier(new ItemStack(opaDup)));

        ClayContainerBlock solar = (ClayContainerBlock) ClayiumBlocks.byPath("solar_clay_fabricator_mk1").get();
        assertEquals(FacingMode.ALL, solar.getFacingMode());
        assertEquals(OverlayMode.TOP_ONLY, solar.getOverlayMode());

        ClayContainerBlock miner = (ClayContainerBlock) ClayiumBlocks.byPath("miner").get();
        ClayContainerBlock activator = (ClayContainerBlock) ClayiumBlocks.byPath("activator").get();
        ClayContainerBlock areaCollector = (ClayContainerBlock) ClayiumBlocks.byPath("area_collector").get();
        assertEquals(FacingMode.ALL, miner.getFacingMode());
        assertEquals("az91dhull", miner.getBaseTexture());
        assertEquals(OverlayMode.FRONT_AND_BACK_MINER, miner.getOverlayMode());
        assertEquals("az91dhull", activator.getBaseTexture());
        assertEquals("zk60ahull", areaCollector.getBaseTexture());
        assertTrue(miner.hasActiveOverlay());
        assertTrue(activator.hasActiveOverlay());
        assertTrue(areaCollector.hasActiveOverlay());

        List<String> expectedActive = List.of(
                "clay_blast_furnace",
                "clay_reactor",
                "antimatter_ca_reactor_core",
                "pure_antimatter_ca_reactor_core",
                "oec_ca_reactor_core",
                "opa_ca_reactor_core",
                "clay_water_wheel",
                "dense_clay_water_wheel",
                "miner",
                "area_collector",
                "area_miner",
                "advanced_area_miner",
                "area_replacer",
                "activator",
                "area_activator"
        );
        for (String id : expectedActive) {
            Block b = ClayiumBlocks.byPath(id).get();
            assertTrue(b.defaultBlockState().hasProperty(ClayContainerBlock.ACTIVE), "Expected ACTIVE property on " + id);
        }

        Block storage = ClayiumBlocks.byPath("storage_container").get();
        assertTrue(storage instanceof StorageContainerBlock);
        assertTrue(storage.defaultBlockState().hasProperty(StorageContainerBlock.UPGRADED));
        ItemStack normalStorage = new ItemStack(storage);
        assertFalse(StorageContainerBlock.isUpgradedStack(normalStorage));
        ItemStack upgradedStorage = new ItemStack(storage);
        upgradedStorage.set(
                ClayiumDataComponents.STORAGE_CONTENT.get(),
                new StorageContentData(Items.AIR, 0L, Integer.MAX_VALUE)
        );
        assertTrue(StorageContainerBlock.isUpgradedStack(upgradedStorage));
    }

    @Test
    void clayToolsDurabilitiesSpeedsAndHarvestMatchOriginal() {
        ItemStack shovel = new ItemStack(ClayiumItems.byPath("clay_shovel").get());
        ItemStack pickaxe = new ItemStack(ClayiumItems.byPath("clay_pickaxe").get());
        ItemStack steelShovel = new ItemStack(ClayiumItems.byPath("clay_steel_shovel").get());
        ItemStack steelPickaxe = new ItemStack(ClayiumItems.byPath("clay_steel_pickaxe").get());
        ItemStack rollingPin = new ItemStack(ClayiumItems.byPath("clay_rolling_pin").get());

        assertEquals(500, shovel.getMaxDamage());
        assertEquals(500, pickaxe.getMaxDamage());
        assertEquals(10000, steelShovel.getMaxDamage());
        assertEquals(10000, steelPickaxe.getMaxDamage());
        assertFalse(rollingPin.isRepairable());

        BlockState clayOre = ClayiumBlocks.byPath("clay_ore").get().defaultBlockState();
        assertEquals(32.0F, pickaxe.getDestroySpeed(clayOre), 1e-6);
        assertEquals(12.0F, shovel.getDestroySpeed(clayOre), 1e-6);
        assertTrue(shovel.isCorrectToolForDrops(clayOre));
        assertEquals(32.0F, shovel.getDestroySpeed(Blocks.CLAY.defaultBlockState()), 1e-6);
        assertEquals(6.0F, steelPickaxe.getDestroySpeed(Blocks.AIR.defaultBlockState()), 1e-6);
        assertEquals(6.0F, steelShovel.getDestroySpeed(Blocks.AIR.defaultBlockState()), 1e-6);
    }

    @Test
    void clayOreDropMathMatchesOriginalToolFortuneMultipliers() {
        RandomSource seq = new FixedRandom(0, 0, 1);
        assertEquals(4, ClayOreBonusLootFunction.computeDropCount(seq, 0, false, false, 1.0));
        // With clay shovel at fortune 0: effectiveFortune = (0 + 1) * 3 = 3; bonus roll nextInt(5) -> 4 => 4 + 1 = 5 -> 4 * 5 = 20, roll 3 => 4 * 4 = 16
        RandomSource seqShovel = new FixedRandom(0, 0, 4);
        assertEquals(16, ClayOreBonusLootFunction.computeDropCount(seqShovel, 0, true, false, 1.0));
        // With clay pickaxe at fortune 1: effectiveFortune = (1 + 1) * 4 = 8; nextInt(10) -> 9 => bonus 8 -> 4 * 9 = 36
        RandomSource seqPick = new FixedRandom(0, 0, 9);
        assertEquals(36, ClayOreBonusLootFunction.computeDropCount(seqPick, 1, false, true, 1.0));
    }

    @Test
    void tooltipsFoldDetailsOnShiftAndFormatUpgradeFactorsSafely() {
        Map<String, String> entries = Map.of(
                "block.clayium_neoforged.clay_crafting_table.tooltip.line1", "Summary line",
                "block.clayium_neoforged.clay_crafting_table.tooltip.line2", "__DETAIL__",
                "block.clayium_neoforged.clay_crafting_table.tooltip.line3", "Hidden unless shift",
                "tooltip.HoldShiftForDetails", "--Hold Shift--",
                "tooltip.Resonator.resonance", "Resonance : x%1$4.3g"
        );
        Language mockLang = new Language() {
            @Override
            public String getOrDefault(String id, String defaultValue) {
                return entries.getOrDefault(id, defaultValue);
            }

            @Override
            public boolean has(String id) {
                return entries.containsKey(id);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }

            @Override
            public FormattedCharSequence getVisualOrder(FormattedText text) {
                return FormattedCharSequence.EMPTY;
            }
        };

        List<Component> folded = new ArrayList<>();
        ClayiumTooltips.appendLocalizedTooltip(folded, "block.clayium_neoforged.clay_crafting_table.tooltip", false, mockLang);
        assertEquals(2, folded.size());

        List<Component> expanded = new ArrayList<>();
        ClayiumTooltips.appendLocalizedTooltip(expanded, "block.clayium_neoforged.clay_crafting_table.tooltip", true, mockLang);
        assertEquals(2, expanded.size());

        // %1$4.3g must format cleanly via String.format without Minecraft TranslatableFormatException
        String formatted = String.format(java.util.Locale.ROOT, entries.get("tooltip.Resonator.resonance"), 1.08);
        assertEquals("Resonance : x1.08", formatted.trim());
    }

    @Test
    void generatedAssetsAndTagsReflectSemanticEquivalence() throws IOException {
        Path projectDir = Path.of(System.getProperty("clayium.projectDir", "."));
        Path gen = projectDir.resolve("src/generated/resources");

        // Verify translucent render_type on semi-transparent models
        for (String modelName : List.of(
                "tinted_metal_block.json",
                "tinted_metal_chest.json",
                "pan_cable_core.json",
                "laser_reflector.json",
                "storage_container.json",
                "storage_container_upgraded.json",
                "vacuum_container.json",
                "antimatter_energy_storage_upgrade.json")) {
            JsonObject model = JsonParser.parseString(
                    Files.readString(gen.resolve("assets/clayium_neoforged/models/block/" + modelName))
            ).getAsJsonObject();
            assertEquals("minecraft:translucent", model.get("render_type").getAsString(), modelName);
        }

        // Verify 2-layer hull preservation on storage_container, auto_crafter, solar, and buffer item models
        JsonObject crafter = JsonParser.parseString(
                Files.readString(gen.resolve("assets/clayium_neoforged/models/block/advanced_auto_crafter.json"))
        ).getAsJsonObject();
        assertEquals("clayium_neoforged:block/machinehull-4", crafter.getAsJsonObject("textures").get("base").getAsString());
        assertEquals(2, crafter.getAsJsonArray("elements").size());

        JsonObject bufferItem = JsonParser.parseString(
                Files.readString(gen.resolve("assets/clayium_neoforged/models/item/basic_buffer.json"))
        ).getAsJsonObject();
        assertEquals("clayium_neoforged:block/basic_buffer_item", bufferItem.get("parent").getAsString());

        // Verify fluid capsule layer0=mask, layer1=frame
        JsonObject capsule = JsonParser.parseString(
                Files.readString(gen.resolve("assets/clayium_neoforged/models/item/fluid_capsule_1000.json"))
        ).getAsJsonObject();
        JsonObject capsuleTex = capsule.getAsJsonObject("textures");
        assertEquals("clayium_neoforged:item/capsule1000_mask", capsuleTex.get("layer0").getAsString());
        assertEquals("clayium_neoforged:item/capsule1000", capsuleTex.get("layer1").getAsString());

        // Verify ClayTreeLeaves drops no sapling and ClayOre uses clay_ore_bonus
        String leavesLoot = Files.readString(gen.resolve("data/clayium_neoforged/loot_table/blocks/clay_tree_leaves.json"));
        assertFalse(leavesLoot.contains("clay_tree_sapling"));
        assertTrue(leavesLoot.contains("clay_dust"));
        assertTrue(leavesLoot.contains("dense_clay_dust"));

        String oreLoot = Files.readString(gen.resolve("data/clayium_neoforged/loot_table/blocks/clay_ore.json"));
        assertTrue(oreLoot.contains("clayium_neoforged:clay_ore_bonus"));

        // Verify tool/block tags
        Set<String> pickaxeItems = readTagValues(gen.resolve("data/minecraft/tags/item/pickaxes.json"));
        assertTrue(pickaxeItems.contains("clayium_neoforged:clay_pickaxe"));
        assertTrue(pickaxeItems.contains("clayium_neoforged:clay_steel_pickaxe"));

        Set<String> shovelBlocks = readTagValues(gen.resolve("data/minecraft/tags/block/mineable/shovel.json"));
        assertTrue(shovelBlocks.contains("clayium_neoforged:clay_crafting_table"));
        assertTrue(shovelBlocks.contains("clayium_neoforged:clay_marker"));
        assertFalse(shovelBlocks.contains("clayium_neoforged:clay_work_table"));
    }

    private static Set<String> readTagValues(Path path) throws IOException {
        JsonArray arr = JsonParser.parseString(Files.readString(path)).getAsJsonObject().getAsJsonArray("values");
        Set<String> out = new HashSet<>();
        for (JsonElement el : arr) {
            out.add(el.getAsString());
        }
        return out;
    }

    private static final class FixedRandom implements RandomSource {
        private final int[] sequence;
        private int idx;

        FixedRandom(int... sequence) {
            this.sequence = sequence;
        }

        @Override
        public int nextInt(int bound) {
            int val = this.sequence[Math.min(this.idx, this.sequence.length - 1)];
            this.idx++;
            return Math.floorMod(val, bound);
        }

        @Override
        public RandomSource fork() {
            return this;
        }

        @Override
        public net.minecraft.world.level.levelgen.PositionalRandomFactory forkPositional() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSeed(long seed) {}

        @Override
        public int nextInt() {
            return nextInt(Integer.MAX_VALUE);
        }

        @Override
        public long nextLong() {
            return nextInt();
        }

        @Override
        public boolean nextBoolean() {
            return false;
        }

        @Override
        public float nextFloat() {
            return 0.0F;
        }

        @Override
        public double nextDouble() {
            return 0.0;
        }

        @Override
        public double nextGaussian() {
            return 0.0;
        }
    }
}
