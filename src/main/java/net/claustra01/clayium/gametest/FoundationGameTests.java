package net.claustra01.clayium.gametest;

import net.claustra01.clayium.datagen.waterwheel.WaterWheelLanguageEntries;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.claustra01.clayium.registry.CaRegistration;
import net.claustra01.clayium.registry.CaReactorRegistration;
import net.claustra01.clayium.registry.AdvancedToolRegistration;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.FormingMachineRegistration;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.claustra01.clayium.registry.FluidRegistration;
import net.claustra01.clayium.datagen.LegacyTranslations;
import net.claustra01.clayium.datagen.autocrafter.AutoCrafterLanguageEntries;
import net.claustra01.clayium.datagen.ca.CaLanguageEntries;
import net.claustra01.clayium.datagen.careactor.CaReactorLanguageEntries;
import net.claustra01.clayium.datagen.environment.EnvironmentLanguageEntries;
import net.claustra01.clayium.datagen.clayproduction.ClayProductionLanguageEntries;
import net.claustra01.clayium.datagen.chunkloader.ChunkLoaderLanguageEntries;
import net.claustra01.clayium.datagen.area.AreaLanguageEntries;
import net.claustra01.clayium.datagen.advancedtool.AdvancedToolLanguageEntries;
import net.claustra01.clayium.datagen.creative.CreativeSourceLanguageEntries;
import net.claustra01.clayium.datagen.compat.CompatLanguageEntries;
import net.claustra01.clayium.datagen.energyconverter.EnergyConverterLanguageEntries;
import net.claustra01.clayium.datagen.gadget.GadgetTranslations;
import net.claustra01.clayium.datagen.iface.InterfaceLanguageEntries;
import net.claustra01.clayium.datagen.fluid.FluidLanguageEntries;
import net.claustra01.clayium.datagen.logistics.LogisticsLanguageEntries;
import net.claustra01.clayium.datagen.laser.LaserLanguageEntries;
import net.claustra01.clayium.datagen.multitrack.MultitrackLanguageEntries;
import net.claustra01.clayium.datagen.multiblock.MultiblockLanguageEntries;
import net.claustra01.clayium.datagen.trade.TradeLanguageEntries;
import net.claustra01.clayium.datagen.miner.MinerLanguageEntries;
import net.claustra01.clayium.datagen.storage.StorageLanguageEntries;
import net.claustra01.clayium.datagen.weapon.WeaponTranslations;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.claustra01.clayium.registry.OriginalClassInventory;
import net.claustra01.clayium.registry.OriginalRegistryCatalog;
import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.claustra01.clayium.registry.ChunkLoaderRegistration;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.claustra01.clayium.registry.CreativeSourceRegistration;
import net.claustra01.clayium.registry.FoundationDataComponentRegistration;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.LaserRegistration;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.claustra01.clayium.registry.TradeRegistration;
import net.claustra01.clayium.registry.MinerRegistration;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.claustra01.clayium.registry.WeaponRegistration;
import net.claustra01.clayium.registry.WaterWheelRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.datagen.pan.PanLanguageEntries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoundationGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void namedRegistriesUseClayiumNamespace(GameTestHelper helper) {
        require(ModList.get().isLoaded(ClayiumMod.MOD_ID), "Clayium was not loaded");
        Set<ResourceLocation> expectedBlocks = new HashSet<>();
        Set<ResourceLocation> expectedItems = new HashSet<>();
        for (OriginalRegistryCatalog.RegisteredEntry entry : OriginalRegistryCatalog.REGISTERED_ENTRIES) {
            ResourceLocation id = ClayiumMod.id(entry.namedId());
            require(expectedItems.add(id), "Duplicate item ledger ID: " + id);
            if (entry.block()) {
                require(expectedBlocks.add(id), "Duplicate block ledger ID: " + id);
                require(BuiltInRegistries.BLOCK.containsKey(id), "Ledger block is absent: " + id);
            }
            require(BuiltInRegistries.ITEM.containsKey(id), "Ledger item is absent: " + id);
        }
        for (String idPath : MaterialRegistration.COMPRESSION_BLOCKS.keySet()) {
            ResourceLocation id = ClayiumMod.id(idPath);
            expectedBlocks.add(id);
            expectedItems.add(id);
        }
        for (String idPath : MaterialRegistration.MATERIAL_ITEMS.keySet()) {
            expectedItems.add(ClayiumMod.id(idPath));
        }
        for (String idPath : MaterialRegistration.MATERIAL_BLOCKS.keySet()) {
            expectedBlocks.add(ClayiumMod.id(idPath));
        }
        expectedBlocks.add(MaterialRegistration.OEC_BLOCK.getId());
        expectedItems.add(MaterialRegistration.OEC_BLOCK_ITEM.getId());
        MaterialRegistration.METAL_BLOCKS.keySet().forEach(idPath -> {
            expectedBlocks.add(ClayiumMod.id(idPath));
            expectedItems.add(ClayiumMod.id(idPath));
        });
        MaterialRegistration.SILICONE_BLOCKS.keySet().forEach(idPath -> {
            expectedBlocks.add(ClayiumMod.id(idPath));
            expectedItems.add(ClayiumMod.id(idPath));
        });
        MaterialRegistration.SHARDS.keySet().forEach(idPath -> expectedItems.add(ClayiumMod.id(idPath)));
        for (String idPath : MaterialRegistration.COMPONENTS.keySet()) {
            expectedItems.add(ClayiumMod.id(idPath));
        }
        for (String idPath : MachineRegistration.MACHINES.keySet()) {
            ResourceLocation id = ClayiumMod.id(idPath);
            expectedBlocks.add(id);
            expectedItems.add(id);
        }
        for (String idPath : FormingMachineRegistration.MACHINES.keySet()) {
            ResourceLocation id = ClayiumMod.id(idPath);
            expectedBlocks.add(id);
            expectedItems.add(id);
        }
        FormingMachineRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        FormingMachineRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        AssemblyMachineRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        AssemblyMachineRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        AutoCrafterRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        AutoCrafterRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        MinerRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        MinerRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        AreaMachineRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        AreaMachineRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        expectedBlocks.add(ChunkLoaderRegistration.CHUNK_LOADER.getId());
        expectedItems.add(ChunkLoaderRegistration.CHUNK_LOADER_ITEM.getId());
        EnergyConverterRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        EnergyConverterRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        InterfaceRegistration.INTERFACES.values().forEach(definition -> {
            expectedBlocks.add(definition.block().getId());
            expectedItems.add(definition.item().getId());
        });
        expectedItems.add(InterfaceRegistration.SYNCHRONIZER.getId());
        FluidRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        FluidRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        expectedItems.add(FluidRegistration.FLUID_FILTER.getId());
        LogisticsRegistration.BUFFERS.values().forEach(definition -> {
            expectedBlocks.add(definition.block().getId());
            expectedItems.add(definition.item().getId());
        });
        expectedBlocks.add(CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE.getId());
        expectedItems.add(CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE_ITEM.getId());
        EnvironmentMachineRegistration.COBBLESTONE_GENERATORS.forEach(definition -> {
            expectedBlocks.add(definition.block().getId());
            expectedItems.add(definition.item().getId());
        });
        EnvironmentMachineRegistration.SALT_EXTRACTORS.values().forEach(definition -> {
            expectedBlocks.add(definition.block().getId());
            expectedItems.add(definition.item().getId());
        });
        expectedBlocks.add(EnvironmentMachineRegistration.QUARTZ_CRUCIBLE.getId());
        expectedItems.add(EnvironmentMachineRegistration.QUARTZ_CRUCIBLE_ITEM.getId());
        MultitrackRegistration.MULTITRACK_BUFFERS.values().forEach(definition -> {
            expectedBlocks.add(definition.block().getId());
            expectedItems.add(definition.item().getId());
        });
        MultitrackRegistration.DISTRIBUTORS.values().forEach(definition -> {
            expectedBlocks.add(definition.block().getId());
            expectedItems.add(definition.item().getId());
        });
        StorageRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        StorageRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        ClayProductionRegistration.BLOCKS.values().forEach(block -> {
            expectedBlocks.add(block.getId());
            expectedItems.add(block.getId());
        });
        expectedBlocks.add(TradeRegistration.AUTO_TRADER.getId());
        expectedItems.add(TradeRegistration.AUTO_TRADER_ITEM.getId());
        WorldgenRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        WorldgenRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        LaserRegistration.LASER_BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        LaserRegistration.LASER_ITEMS.forEach(item -> expectedItems.add(item.getId()));
        expectedBlocks.add(LaserRegistration.LASER_REFLECTOR.getId());
        expectedItems.add(LaserRegistration.LASER_REFLECTOR_ITEM.getId());
        MultiblockRegistration.CONTROLLERS.values().forEach(block -> expectedBlocks.add(block.getId()));
        MultiblockRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        ClayTreeRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        ClayTreeRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        CaRegistration.MACHINES.values().forEach(definition -> {
            expectedBlocks.add(definition.block().getId());
        });
        expectedBlocks.add(CaRegistration.RESONATING_COLLECTOR.getId());
        CaRegistration.AUGMENTS.values().forEach(definition -> {
            expectedBlocks.add(definition.block().getId());
        });
        CaRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        CaReactorRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        CaReactorRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        PanRegistration.BLOCKS.forEach(block -> expectedBlocks.add(block.getId()));
        PanRegistration.ITEMS.forEach(item -> expectedItems.add(item.getId()));
        Set<String> workbenchOriginalKeys = Set.of(
                "tile.blockClayWorkTable.name", "tile.blockClayCraftingTable.name",
                "tile.blockRawClayMachineHull.0.name", "tile.blockMachineHull.0.name",
                "item.itemRawClayCraftingTools.RollingPin.name", "item.itemRawClayCraftingTools.Slicer.name",
                "item.itemRawClayCraftingTools.Spatula.name", "item.itemClayRollingPin.name",
                "item.itemClaySlicer.name", "item.itemClaySpatula.name", "item.itemClayWrench.name",
                "item.itemClayPipingTools.IO.name", "item.itemClayPipingTools.Piping.name",
                "item.itemClayPipingTools.Memory.name", "item.itemDirectionMemory.name");
        for (OriginalRegistryCatalog.OriginalVariety variety : OriginalRegistryCatalog.ORIGINAL_VARIETIES) {
            if (workbenchOriginalKeys.contains(variety.legacyId())) {
                ResourceLocation id = ClayiumMod.id(variety.namedId());
                expectedItems.add(id);
                if (variety.kind().equals("block")) {
                    expectedBlocks.add(id);
                }
            }
        }
        WeaponRegistration.ALL.forEach(item -> expectedItems.add(item.getId()));
        AdvancedToolRegistration.ALL.forEach(item -> expectedItems.add(item.getId()));
        require(clayiumIds(BuiltInRegistries.BLOCK).equals(expectedBlocks), "Block registry differs from original ledger");
        require(clayiumIds(BuiltInRegistries.ITEM).equals(expectedItems), "Item registry differs from original ledger");
        require(clayiumIds(BuiltInRegistries.BLOCK_ENTITY_TYPE).equals(Set.of(
                        ClayiumMod.id("clay_work_table"), ClayiumMod.id("clay_crafting_board"),
                        ClayiumMod.id("processing_machine"), ClayiumMod.id("water_wheel"),
                        ClayiumMod.id("buffer"), ClayiumMod.id("cobblestone_generator"),
                        ClayiumMod.id("quartz_crucible"), ClayiumMod.id("multitrack_buffer"),
                        ClayiumMod.id("distributor"), ClayiumMod.id("clay_production_machine"),
                        ClayiumMod.id("energy_converter"), ClayiumMod.id("storage_container"),
                        ClayiumMod.id("void_container"), ClayiumMod.id("metal_chest"),
                        ClayiumMod.id("interface"),
                        ClayiumMod.id("fluid_transport"), ClayiumMod.id("creative_ce_source"),
                        ClayiumMod.id("auto_crafter"), ClayiumMod.id("basic_block_operation"),
                        ClayiumMod.id("area_marker"), ClayiumMod.id("area_machine"),
                        ClayiumMod.id("clay_energy_laser"), ClayiumMod.id("laser_reflector"),
                        ClayiumMod.id("chunk_loader"), ClayiumMod.id("multiblock_controller"),
                        ClayiumMod.id("ca_resonating_collector"), ClayiumMod.id("ca_reactor_core"),
                        ClayiumMod.id("pan"),
                        TradeRegistration.BLOCK_ENTITY.getId())),
                "Block entity registrations differ from registered blocks");
        require(clayiumIds(BuiltInRegistries.ENTITY_TYPE).equals(Set.of(
                        WeaponRegistration.CLAY_BALL.getId(), WeaponRegistration.TELEPORT_BALL.getId())),
                "Entity registry differs from the weapon ledger");
        for (OriginalRegistryCatalog.ExcludedEntry entry : OriginalRegistryCatalog.EXCLUDED_ENTRIES) {
            ResourceLocation id = ClayiumMod.id(entry.legacyId());
            require(!BuiltInRegistries.ITEM.containsKey(id), "Excluded item was registered: " + id);
            require(!BuiltInRegistries.BLOCK.containsKey(id), "Excluded block was registered: " + id);
        }
        require(BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(ClayiumMod.id("tier")), "Tier component is missing");
        require(FoundationDataComponentRegistration.TIER.isBound(), "Tier component holder is unbound");
        require(BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(ClayiumMod.id("synchronized_target")),
                "Synchronized target component is missing");
        require(InterfaceRegistration.SYNCHRONIZED_TARGET.isBound(),
                "Synchronized target component holder is unbound");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalRegistryCatalogTracksAllMachineVariants(GameTestHelper helper) {
        Set<String> originalKeys = new HashSet<>();
        for (OriginalRegistryCatalog.OriginalVariety variety : OriginalRegistryCatalog.ORIGINAL_VARIETIES) {
            require(originalKeys.add(variety.legacyId()), "Duplicate original variety: " + variety.legacyId());
            require(variety.namedId().matches("[a-z][a-z0-9_]*")
                    || variety.namedId().equals("minecraft:clay")
                    || variety.namedId().equals("minecraft:clay_ball"),
                    "Unnamed registration target: " + variety.legacyId());
        }
        for (OriginalRegistryCatalog.RegisteredEntry entry : OriginalRegistryCatalog.REGISTERED_ENTRIES) {
            String kind = entry.block() ? "block" : "item";
            require(OriginalRegistryCatalog.ORIGINAL_VARIETIES.stream().anyMatch(variety ->
                            variety.kind().equals(kind) && variety.namedId().equals(entry.namedId())),
                    "Registered variety lacks an original ledger entry: " + entry.namedId());
        }
        Set<String> machineIds = new HashSet<>();
        for (OriginalRegistryCatalog.MachineFamily family : OriginalRegistryCatalog.MACHINE_FAMILIES) {
            for (int tier : family.tiers()) {
                require(machineIds.add(OriginalRegistryCatalog.machineId(family, tier)),
                        "Machine ledger has a duplicate named ID");
            }
        }
        require(machineIds.contains("clay_bending_machine"), "Registered machine is absent from machine ledger");
        require(OriginalClassInventory.EXCLUDED_FIELDS.containsKey("itemsCapsule"),
                "The original capsule array must have an explicit exclusion reason");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalPngBytesAreUnchanged(GameTestHelper helper) {
        try (InputStream manifestStream = resource("assets/clayium/original_png.sha256");
                BufferedReader manifest = new BufferedReader(new InputStreamReader(manifestStream, StandardCharsets.UTF_8))) {
            List<String> entries = manifest.lines().toList();
            require(entries.size() == 416, "Expected 416 original PNG hashes, got " + entries.size());
            for (String entry : entries) {
                String expectedHash = entry.substring(0, 64);
                String path = entry.substring(66);
                try (InputStream image = resource("assets/clayium/" + path)) {
                    String actualHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(image.readAllBytes()));
                    require(actualHash.equals(expectedHash), "Original PNG bytes changed: " + path);
                }
            }
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new GameTestAssertException("Unable to verify original PNG resources: " + exception.getMessage());
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void englishAndJapaneseTranslationsIncludeTierAndUnits(GameTestHelper helper) {
        JsonObject english = language("en_us");
        JsonObject japanese = language("ja_jp");
        for (String locale : List.of("en_us", "ja_jp")) {
            JsonObject language = locale.equals("ja_jp") ? japanese : english;
            var original = locale.equals("ja_jp") ? LegacyTranslations.JAPANESE : LegacyTranslations.ENGLISH;
            for (var translation : original.entrySet()) {
                require(language.has(translation.getKey()), "Translation is missing: " + translation.getKey());
                require(language.get(translation.getKey()).getAsString().equals(translation.getValue()),
                        "Translation differs from original: " + translation.getKey());
            }
            for (OriginalRegistryCatalog.RegisteredEntry entry : OriginalRegistryCatalog.REGISTERED_ENTRIES) {
                String key = entry.block() ? "block.clayium." + entry.namedId() : "item.clayium." + entry.namedId();
                require(language.has(key), "Registered object's visible name is missing: " + key);
            }
            Set<String> expectedKeys = new HashSet<>(original.keySet());
            expectedKeys.addAll(GadgetTranslations.forLocale(locale).keySet());
            expectedKeys.addAll(WeaponTranslations.forLocale(locale).keySet());
            var compatibility = new HashMap<String, String>();
            CompatLanguageEntries.add(locale, compatibility::put);
            expectedKeys.addAll(compatibility.keySet());
            var logistics = new HashMap<String, String>();
            LogisticsLanguageEntries.add(locale, logistics::put);
            expectedKeys.addAll(logistics.keySet());
            var creativeSource = new HashMap<String, String>();
            CreativeSourceLanguageEntries.add(locale, creativeSource::put);
            expectedKeys.addAll(creativeSource.keySet());
            EnvironmentLanguageEntries.forLocale(locale).keySet().forEach(
                    id -> expectedKeys.add("block.clayium." + id));
            var multitrack = new HashMap<String, String>();
            MultitrackLanguageEntries.add(locale, multitrack::put);
            expectedKeys.addAll(multitrack.keySet());
            var clayProduction = new HashMap<String, String>();
            ClayProductionLanguageEntries.add(locale, clayProduction::put);
            expectedKeys.addAll(clayProduction.keySet());
            var energyConverters = new HashMap<String, String>();
            EnergyConverterLanguageEntries.add(locale, energyConverters::put);
            expectedKeys.addAll(energyConverters.keySet());
            var waterWheels = new HashMap<String, String>();
            WaterWheelLanguageEntries.add(locale, waterWheels::put);
            expectedKeys.addAll(waterWheels.keySet());
            var storage = new HashMap<String, String>();
            StorageLanguageEntries.add(locale, storage::put);
            expectedKeys.addAll(storage.keySet());
            var interfaces = new HashMap<String, String>();
            InterfaceLanguageEntries.add(locale, interfaces::put);
            expectedKeys.addAll(interfaces.keySet());
            var fluidTransport = new HashMap<String, String>();
            FluidLanguageEntries.add(locale, fluidTransport::put);
            expectedKeys.addAll(fluidTransport.keySet());
            var trade = new HashMap<String, String>();
            TradeLanguageEntries.add(locale, trade::put);
            expectedKeys.addAll(trade.keySet());
            var autoCrafters = new HashMap<String, String>();
            AutoCrafterLanguageEntries.add(locale, autoCrafters::put);
            expectedKeys.addAll(autoCrafters.keySet());
            var lasers = new HashMap<String, String>();
            LaserLanguageEntries.add(locale, lasers::put);
            expectedKeys.addAll(lasers.keySet());
            var ca = new HashMap<String, String>();
            CaLanguageEntries.add(locale, ca::put);
            expectedKeys.addAll(ca.keySet());
            var caReactor = new HashMap<String, String>();
            CaReactorLanguageEntries.add(locale, caReactor::put);
            expectedKeys.addAll(caReactor.keySet());
            var pan = new HashMap<String, String>();
            PanLanguageEntries.add(locale, pan::put);
            expectedKeys.addAll(pan.keySet());
            var miners = new HashMap<String, String>();
            MinerLanguageEntries.add(locale, miners::put);
            expectedKeys.addAll(miners.keySet());
            var area = new HashMap<String, String>();
            AreaLanguageEntries.add(locale, area::put);
            expectedKeys.addAll(area.keySet());
            var advancedTools = new HashMap<String, String>();
            AdvancedToolLanguageEntries.add(locale, advancedTools::put);
            expectedKeys.addAll(advancedTools.keySet());
            var chunkLoader = new HashMap<String, String>();
            ChunkLoaderLanguageEntries.add(locale, chunkLoader::put);
            expectedKeys.addAll(chunkLoader.keySet());
            expectedKeys.addAll(MultiblockLanguageEntries.forLocale(locale).keySet());
            for (var tier : net.claustra01.clayium.material.CompressionTier.values()) {
                expectedKeys.add("block.clayium." + tier.id());
            }
            for (var variant : net.claustra01.clayium.material.ClayMaterials.VARIANTS) {
                if (!variant.id().startsWith("minecraft:")) {
                    expectedKeys.add((variant.shape() == net.claustra01.clayium.material.MaterialShape.BLOCK
                            ? "block.clayium." : "item.clayium.") + variant.id());
                }
            }
            for (String id : MaterialRegistration.COMPONENTS.keySet()) {
                expectedKeys.add("item.clayium." + id);
            }
            MaterialRegistration.METAL_BLOCKS.keySet().forEach(id ->
                    expectedKeys.add("block.clayium." + id));
            MaterialRegistration.SILICONE_BLOCKS.keySet().forEach(id ->
                    expectedKeys.add("block.clayium." + id));
            MaterialRegistration.SHARDS.keySet().forEach(id ->
                    expectedKeys.add("item.clayium." + id));
            expectedKeys.add("block.clayium.oec_block");
            expectedKeys.add("item.clayium.impure_osmium_ingot");
            expectedKeys.add("tooltip.clayium.ce_value");
            expectedKeys.add("tooltip.clayium.clay_units");
            for (String id : MachineRegistration.MACHINES.keySet()) {
                expectedKeys.add("block.clayium." + id);
            }
            for (String id : FormingMachineRegistration.MACHINES.keySet()) {
                expectedKeys.add("block.clayium." + id);
            }
            expectedKeys.add("block.clayium.elemental_milling_machine");
            for (String id : AssemblyMachineRegistration.MACHINES.keySet()) {
                expectedKeys.add("block.clayium." + id);
            }
            AssemblyMachineRegistration.MACHINE_HULL_DEFINITIONS.keySet().forEach(id ->
                    expectedKeys.add("block.clayium." + id));
            for (WaterWheelRegistration.WaterWheelDefinition definition : WaterWheelRegistration.DEFINITIONS) {
                expectedKeys.add("block.clayium." + definition.id());
            }
            for (String state : List.of("idle", "running", "output_full", "waiting_for_energy", "recipe_changed")) {
                expectedKeys.add("gui.clayium.machine.state." + state);
            }
            expectedKeys.add("gui.clayium.machine.faces");
            expectedKeys.add("gui.clayium.machine.required_energy");
            expectedKeys.add("gui.clayium.machine.inventory");
            expectedKeys.add("gui.clayium.machine.empty");
            ClayTreeRegistration.BLOCKS.forEach(block ->
                    expectedKeys.add("block.clayium." + block.getId().getPath()));
            require(language.keySet().equals(expectedKeys),
                    "Generated language keys differ from the registered objects and tooltips");
        }
        for (OriginalRegistryCatalog.RegisteredEntry entry : OriginalRegistryCatalog.REGISTERED_ENTRIES) {
            Item item = BuiltInRegistries.ITEM.get(ClayiumMod.id(entry.namedId()));
            ItemStack stack = item.getDefaultInstance();
            List<Component> tooltip = new ArrayList<>();
            item.appendHoverText(stack, Item.TooltipContext.EMPTY, tooltip, TooltipFlag.NORMAL);
            Integer tier = stack.get(FoundationDataComponentRegistration.TIER.get());
            if (tier != null) {
                require(tooltip.contains(Component.translatable("tooltip.clayium.tier", tier)),
                        "Tier tooltip is absent: " + entry.namedId());
            }
            String description = "tooltip.clayium." + entry.namedId() + ".line1";
            if (english.has(description)) {
                require(tooltip.contains(Component.translatable(description)),
                        "Original description is absent: " + entry.namedId());
            }
        }
        helper.succeed();
    }

    private static JsonObject language(String locale) {
        try (InputStream stream = resource("assets/clayium/lang/" + locale + ".json");
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) {
            throw new GameTestAssertException("Unable to read " + locale + " translations: " + exception.getMessage());
        }
    }

    private static InputStream resource(String path) {
        InputStream stream = FoundationGameTests.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new GameTestAssertException("Missing resource: " + path);
        }
        return stream;
    }

    private static Set<ResourceLocation> clayiumIds(Registry<?> registry) {
        Set<ResourceLocation> ids = new HashSet<>();
        for (ResourceLocation id : registry.keySet()) {
            if (id.getNamespace().equals(ClayiumMod.MOD_ID)) {
                require(!id.getPath().matches("(?:^|.*/)\\d+(?:/|$)"), "Numeric legacy ID found: " + id);
                ids.add(id);
            }
        }
        return ids;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private FoundationGameTests() {
    }
}
