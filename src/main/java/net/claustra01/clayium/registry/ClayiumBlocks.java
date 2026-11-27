package net.claustra01.clayium.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.ClayOreBlock;
import net.claustra01.clayium.block.ClayTreeLeavesBlock;
import net.claustra01.clayium.block.ClayTreeLogBlock;
import net.claustra01.clayium.block.ClayTreeSaplingBlock;
import net.claustra01.clayium.block.CompressedClayBlock;
import net.claustra01.clayium.block.DirectionalClayContainerBlock;
import net.claustra01.clayium.block.HorizontalClayContainerBlock;
import net.claustra01.clayium.block.MachineHullBlock;
import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.block.MetalMaterialBlock;
import net.claustra01.clayium.block.PanCableBlock;
import net.claustra01.clayium.block.QuartzCrucibleBlock;
import net.claustra01.clayium.block.SiliconeColoredBlock;
import net.claustra01.clayium.block.TieredBlock;
import net.claustra01.clayium.block.UpgradeBlock;
import net.claustra01.clayium.block.UpgradeBlock.UpgradeKind;
import net.claustra01.clayium.material.CMaterial;
import net.claustra01.clayium.material.CMaterials;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers all 427 Clayium blocks matching original CBlocks definitions.
 */
public final class ClayiumBlocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(ClayiumMod.MOD_ID);
    private static final Map<String, DeferredBlock<? extends Block>> ALL_BLOCKS = new LinkedHashMap<>();

    static {
        registerAll();
    }

    private ClayiumBlocks() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }

    public static Map<String, DeferredBlock<? extends Block>> all() {
        return Collections.unmodifiableMap(ALL_BLOCKS);
    }

    public static DeferredBlock<? extends Block> byPath(String path) {
        DeferredBlock<? extends Block> block = ALL_BLOCKS.get(path);
        if (block == null) {
            throw new IllegalArgumentException("Unknown Clayium block path: " + path);
        }
        return block;
    }

    private static <T extends Block> DeferredBlock<T> reg(String path, Supplier<T> factory) {
        DeferredBlock<T> holder = REGISTRY.register(path, factory);
        ALL_BLOCKS.put(path, holder);
        return holder;
    }

    private static Properties stoneMachineProps() {
        return Properties.of().mapColor(MapColor.METAL).strength(2.0F, 5.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static Properties clayProps(float hardness) {
        return Properties.of().mapColor(MapColor.CLAY).strength(hardness, 1.0F).sound(SoundType.GRAVEL);
    }

    private static Properties metalProps() {
        return Properties.of().mapColor(MapColor.METAL).strength(2.0F, 2.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static void regContainer(String path, int tier, int metaMode, String baseTex, String overlayTex, String category, String tooltipKey) {
        reg(path, () -> switch (metaMode) {
            case 1 -> new HorizontalClayContainerBlock(stoneMachineProps(), tier, baseTex, overlayTex, category, tooltipKey);
            case 2 -> new DirectionalClayContainerBlock(stoneMachineProps(), tier, baseTex, overlayTex, category, tooltipKey);
            default -> new ClayContainerBlock(stoneMachineProps(), tier, baseTex, overlayTex, category, tooltipKey);
        });
    }

    private static void regMetalBlock(String path, CMaterial mat, int tier, String tex, boolean layered) {
        reg(path, () -> new MetalMaterialBlock(metalProps(), mat, tier, tex, layered));
    }

    private static void regMetalChest(String path, CMaterial mat, int cols, int rows, int pages) {
        reg(path, () -> new MetalChestBlock(metalProps().noOcclusion(), mat, cols, rows, pages));
    }

    private static void regSilicone(String path, DyeColor color) {
        reg(path, () -> new SiliconeColoredBlock(metalProps(), color));
    }

    private static void regUpgrade(String path, int tier, String tex, UpgradeKind kind, double factor, int storage) {
        reg(path, () -> new UpgradeBlock(stoneMachineProps(), tier, tex, kind, factor, storage));
    }

    private static void registerAll() {
        reg("clay_ore", () -> new ClayOreBlock(Properties.of().mapColor(MapColor.STONE).strength(3.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops(), 0, "clayore-0"));
        reg("dense_clay_ore", () -> new ClayOreBlock(Properties.of().mapColor(MapColor.STONE).strength(3.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops(), 1, "clayore-1"));
        reg("large_dense_clay_ore", () -> new ClayOreBlock(Properties.of().mapColor(MapColor.STONE).strength(3.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops(), 2, "clayore-2"));
        reg("dense_clay", () -> new CompressedClayBlock(clayProps(1.0F), 0));
        reg("compressed_clay", () -> new CompressedClayBlock(clayProps(1.0F), 1));
        reg("industrial_clay", () -> new CompressedClayBlock(clayProps(1.0F), 2));
        reg("advanced_industrial_clay", () -> new CompressedClayBlock(clayProps(1.0F), 3));
        reg("energetic_clay", () -> new CompressedClayBlock(clayProps(1.0F), 4));
        reg("compressed_energetic_clay", () -> new CompressedClayBlock(clayProps(1.0F), 5));
        reg("double_compressed_energetic_clay", () -> new CompressedClayBlock(clayProps(1.0F), 6));
        reg("triple_compressed_energetic_clay", () -> new CompressedClayBlock(clayProps(1.0F), 7));
        reg("quadruple_compressed_energetic_clay", () -> new CompressedClayBlock(clayProps(1.0F), 8));
        reg("quintuple_compressed_energetic_clay", () -> new CompressedClayBlock(clayProps(1.0F), 9));
        reg("sextuple_compressed_energetic_clay", () -> new CompressedClayBlock(clayProps(1.0F), 10));
        reg("septuple_compressed_energetic_clay", () -> new CompressedClayBlock(clayProps(1.0F), 11));
        reg("octuple_compressed_energetic_clay", () -> new CompressedClayBlock(clayProps(1.0F), 12));
        reg("raw_clay_machine_hull", () -> new MachineHullBlock(clayProps(1.0F), 1, "rawclaymachinehull-0"));
        reg("clay_machine_hull", () -> new MachineHullBlock(metalProps(), 1, "machinehull-0"));
        reg("dense_clay_machine_hull", () -> new MachineHullBlock(metalProps(), 2, "machinehull-1"));
        reg("simple_machine_hull", () -> new MachineHullBlock(metalProps(), 3, "machinehull-2"));
        reg("basic_machine_hull", () -> new MachineHullBlock(metalProps(), 4, "machinehull-3"));
        reg("advanced_machine_hull", () -> new MachineHullBlock(metalProps(), 5, "machinehull-4"));
        reg("precision_machine_hull", () -> new MachineHullBlock(metalProps(), 6, "machinehull-5"));
        reg("clay_steel_machine_hull", () -> new MachineHullBlock(metalProps(), 7, "machinehull-6"));
        reg("clayium_machine_hull", () -> new MachineHullBlock(metalProps(), 8, "machinehull-7"));
        reg("ultimate_machine_hull", () -> new MachineHullBlock(metalProps(), 9, "machinehull-8"));
        reg("antimatter_machine_hull", () -> new MachineHullBlock(metalProps(), 10, "machinehull-9"));
        reg("pure_antimatter_machine_hull", () -> new MachineHullBlock(metalProps(), 11, "machinehull-10"));
        reg("oec_machine_hull", () -> new MachineHullBlock(metalProps(), 12, "machinehull-11"));
        reg("opa_machine_hull", () -> new MachineHullBlock(metalProps(), 13, "machinehull-12"));
        reg("az91d_alloy_hull", () -> new MachineHullBlock(metalProps(), 6, "az91dhull"));
        reg("zk60a_alloy_hull", () -> new MachineHullBlock(metalProps(), 6, "zk60ahull"));
        regContainer("clay_work_table", 0, 0, "clayworktable", "clayworktable", "crafting", null);
        regContainer("clay_crafting_table", 0, 0, "compressedclay-0", "claycraftingtable", "crafting", null);
        regContainer("elemental_milling_machine", 1, 1, "machinehull-0", "millingmachine", "machine", null);
        regContainer("energetic_clay_condenser", 3, 1, "machinehull-2", "eccondenser", "machine", null);
        regContainer("energetic_clay_condenser_mk2", 4, 1, "machinehull-3", "eccondenser", "machine", null);
        regContainer("creative_ce_source", 13, 2, "machinehull-12", "creativeenergy", "machine", null);
        regContainer("auto_clay_condenser", 5, 1, "machinehull-4", "autoclaycondenser", "machine", null);
        regContainer("auto_clay_condenser_mk2", 7, 1, "machinehull-6", "condenser", "machine", null);
        reg("quartz_crucible", () -> new QuartzCrucibleBlock(Properties.of().mapColor(MapColor.QUARTZ).strength(0.2F, 0.2F).sound(SoundType.GLASS).noOcclusion()));
        regContainer("solar_clay_fabricator_mk1", 5, 1, "machinehull-4", "solar", "machine", null);
        regContainer("solar_clay_fabricator_mk2", 6, 1, "machinehull-5", "solar", "machine", null);
        regContainer("lithium_solar_clay_fabricator", 7, 1, "machinehull-6", "solar", "machine", null);
        regContainer("clay_fabricator_mk1", 8, 1, "machinehull-7", "clayfabricator", "machine", null);
        regContainer("clay_fabricator_mk2", 9, 1, "machinehull-8", "clayfabricator", "machine", null);
        regContainer("clay_fabricator_mk3", 13, 1, "machinehull-12", "clayfabricator", "machine", null);
        regContainer("chemical_metal_separator", 6, 1, "machinehull-5", "chemicalmetalseparator", "machine", null);
        regContainer("clay_blast_furnace", 6, 1, "machinehull-5", "blastfurnace", "multiblock", null);
        regContainer("clay_reactor", 7, 1, "machinehull-6", "reactor", "multiblock", null);
        regContainer("ca_collector", 10, 1, "machinehull-9", "cacollector", "machine", null);
        regContainer("energetic_clay_decomposer", 13, 1, "machinehull-12", "decomposer", "machine", null);
        regContainer("storage_container", 6, 1, "storagecontainerside", "storagecontainer", "storage", null);
        regContainer("vacuum_container", 6, 1, "vacuumcontainerside", "vacuumcontainer", "storage", null);
        regContainer("auto_trader", 8, 1, "az91dhull", "autotradertop", "machine", null);
        reg("clay_marker", () -> new TieredBlock(clayProps(0.5F), 7, "compressedclay-6"));
        reg("clay_open_pit_marker", () -> new TieredBlock(clayProps(0.5F), 8, "compressedclay-7"));
        reg("clay_ground_leveling_marker", () -> new TieredBlock(clayProps(0.5F), 8, "compressedclay-8"));
        reg("clay_prism_marker", () -> new TieredBlock(clayProps(0.5F), 8, "compressedclay-9"));
        regContainer("miner", 6, 1, "az91dhull", "areaminer", "area", null);
        regContainer("area_collector", 7, 1, "machinehull-6", "areacollector", "area", null);
        regContainer("area_miner", 8, 1, "zk60ahull", "areaminer", "area", null);
        regContainer("advanced_area_miner", 9, 1, "zk60ahull", "advareaminer", "area", null);
        regContainer("area_replacer", 10, 1, "zk60ahull", "areareplacer", "area", null);
        regContainer("activator", 6, 1, "machinehull-5", "areaactivator", "area", null);
        regContainer("area_activator", 8, 1, "machinehull-7", "areaactivator", "area", null);
        regContainer("clay_water_wheel", 1, 1, "machinehull-0", "waterwheel", "generator", null);
        regContainer("dense_clay_water_wheel", 2, 1, "machinehull-1", "waterwheel", "generator", null);
        regContainer("laser_reflector", 7, 2, "machinehull-6", "laserreflector", "laser", null);
        reg("clay_tree_sapling", () -> new ClayTreeSaplingBlock(Properties.ofFullCopy(Blocks.OAK_SAPLING)));
        reg("clay_tree_leaves", () -> new ClayTreeLeavesBlock(Properties.ofFullCopy(Blocks.OAK_LEAVES)));
        reg("clay_tree_log", () -> new ClayTreeLogBlock(Properties.of().mapColor(MapColor.CLAY).strength(1.5F).sound(SoundType.GRAVEL)));
        regContainer("clay_chunk_loader", 6, 0, "zk60ahull", "chunkloader", "utility", null);
        regContainer("pan_core", 11, 0, "machinehull-10", "pancore", "pan", null);
        reg("pan_cable", () -> new PanCableBlock(stoneMachineProps().noOcclusion()));
        regContainer("clay_bending_machine", 1, 1, "machinehull-0", "bendingmachine", "machine", null);
        regContainer("dense_clay_bending_machine", 2, 1, "machinehull-1", "bendingmachine", "machine", null);
        regContainer("simple_bending_machine", 3, 1, "machinehull-2", "bendingmachine", "machine", null);
        regContainer("basic_bending_machine", 4, 1, "machinehull-3", "bendingmachine", "machine", null);
        regContainer("advanced_bending_machine", 5, 1, "machinehull-4", "bendingmachine", "machine", null);
        regContainer("precision_bending_machine", 6, 1, "machinehull-5", "bendingmachine", "machine", null);
        regContainer("clay_steel_bending_machine", 7, 1, "machinehull-6", "bendingmachine", "machine", null);
        regContainer("ultimate_bending_machine", 9, 1, "machinehull-8", "bendingmachine", "machine", null);
        regContainer("clay_wire_drawing_machine", 1, 1, "machinehull-0", "wiredrawingmachine", "machine", null);
        regContainer("dense_clay_wire_drawing_machine", 2, 1, "machinehull-1", "wiredrawingmachine", "machine", null);
        regContainer("simple_wire_drawing_machine", 3, 1, "machinehull-2", "wiredrawingmachine", "machine", null);
        regContainer("basic_wire_drawing_machine", 4, 1, "machinehull-3", "wiredrawingmachine", "machine", null);
        regContainer("clay_pipe_drawing_machine", 1, 1, "machinehull-0", "pipedrawingmachine", "machine", null);
        regContainer("dense_clay_pipe_drawing_machine", 2, 1, "machinehull-1", "pipedrawingmachine", "machine", null);
        regContainer("simple_pipe_drawing_machine", 3, 1, "machinehull-2", "pipedrawingmachine", "machine", null);
        regContainer("basic_pipe_drawing_machine", 4, 1, "machinehull-3", "pipedrawingmachine", "machine", null);
        regContainer("clay_cutting_machine", 1, 1, "machinehull-0", "cuttingmachine", "machine", null);
        regContainer("dense_clay_cutting_machine", 2, 1, "machinehull-1", "cuttingmachine", "machine", null);
        regContainer("simple_cutting_machine", 3, 1, "machinehull-2", "cuttingmachine", "machine", null);
        regContainer("basic_cutting_machine", 4, 1, "machinehull-3", "cuttingmachine", "machine", null);
        regContainer("clay_lathe", 1, 1, "machinehull-0", "lathe", "machine", null);
        regContainer("dense_clay_lathe", 2, 1, "machinehull-1", "lathe", "machine", null);
        regContainer("simple_lathe", 3, 1, "machinehull-2", "lathe", "machine", null);
        regContainer("basic_lathe", 4, 1, "machinehull-3", "lathe", "machine", null);
        regContainer("clay_cobblestone_generator", 1, 2, "machinehull-0", "cobblestonegenerator", "machine", null);
        regContainer("dense_clay_cobblestone_generator", 2, 2, "machinehull-1", "cobblestonegenerator", "machine", null);
        regContainer("simple_cobblestone_generator", 3, 2, "machinehull-2", "cobblestonegenerator", "machine", null);
        regContainer("basic_cobblestone_generator", 4, 2, "machinehull-3", "cobblestonegenerator", "machine", null);
        regContainer("advanced_cobblestone_generator", 5, 2, "machinehull-4", "cobblestonegenerator", "machine", null);
        regContainer("precision_cobblestone_generator", 6, 2, "machinehull-5", "cobblestonegenerator", "machine", null);
        regContainer("clay_steel_cobblestone_generator", 7, 2, "machinehull-6", "cobblestonegenerator", "machine", null);
        regContainer("dense_clay_condenser", 2, 1, "machinehull-1", "condenser", "machine", null);
        regContainer("simple_condenser", 3, 1, "machinehull-2", "condenser", "machine", null);
        regContainer("basic_condenser", 4, 1, "machinehull-3", "condenser", "machine", null);
        regContainer("advanced_condenser", 5, 1, "machinehull-4", "condenser", "machine", null);
        regContainer("antimatter_condenser", 10, 1, "machinehull-9", "condenser", "machine", null);
        regContainer("dense_clay_grinder", 2, 1, "machinehull-1", "grinder", "machine", null);
        regContainer("simple_grinder", 3, 1, "machinehull-2", "grinder", "machine", null);
        regContainer("basic_grinder", 4, 1, "machinehull-3", "grinder", "machine", null);
        regContainer("advanced_grinder", 5, 1, "machinehull-4", "grinder", "machine", null);
        regContainer("precision_grinder", 6, 1, "machinehull-5", "grinder", "machine", null);
        regContainer("antimatter_grinder", 10, 1, "machinehull-9", "grinder", "machine", null);
        regContainer("dense_clay_decomposer", 2, 1, "machinehull-1", "decomposer", "machine", null);
        regContainer("simple_decomposer", 3, 1, "machinehull-2", "decomposer", "machine", null);
        regContainer("basic_decomposer", 4, 1, "machinehull-3", "decomposer", "machine", null);
        regContainer("simple_milling_machine", 3, 1, "machinehull-2", "millingmachine", "machine", null);
        regContainer("basic_milling_machine", 4, 1, "machinehull-3", "millingmachine", "machine", null);
        regContainer("simple_assembler", 3, 1, "machinehull-2", "assembler", "machine", null);
        regContainer("basic_assembler", 4, 1, "machinehull-3", "assembler", "machine", null);
        regContainer("precision_assembler", 6, 1, "machinehull-5", "assembler", "machine", null);
        regContainer("antimatter_assembler", 10, 1, "machinehull-9", "assembler", "machine", null);
        regContainer("simple_inscriber", 3, 1, "machinehull-2", "inscriber", "machine", null);
        regContainer("basic_inscriber", 4, 1, "machinehull-3", "inscriber", "machine", null);
        regContainer("simple_centrifuge", 3, 1, "machinehull-2", "centrifuge", "machine", null);
        regContainer("basic_centrifuge", 4, 1, "machinehull-3", "centrifuge", "machine", null);
        regContainer("advanced_centrifuge", 5, 1, "machinehull-4", "centrifuge", "machine", null);
        regContainer("precision_centrifuge", 6, 1, "machinehull-5", "centrifuge", "machine", null);
        regContainer("basic_smelter", 4, 1, "machinehull-3", "smelter", "machine", null);
        regContainer("advanced_smelter", 5, 1, "machinehull-4", "smelter", "machine", null);
        regContainer("precision_smelter", 6, 1, "machinehull-5", "smelter", "machine", null);
        regContainer("clay_steel_smelter", 7, 1, "machinehull-6", "smelter", "machine", null);
        regContainer("clayium_smelter", 8, 1, "machinehull-7", "smelter", "machine", null);
        regContainer("ultimate_smelter", 9, 1, "machinehull-8", "smelter", "machine", null);
        regContainer("basic_buffer", 4, 2, "machinehull-3", null, "buffer", null);
        regContainer("advanced_buffer", 5, 2, "machinehull-4", null, "buffer", null);
        regContainer("precision_buffer", 6, 2, "machinehull-5", null, "buffer", null);
        regContainer("clay_steel_buffer", 7, 2, "machinehull-6", null, "buffer", null);
        regContainer("clayium_buffer", 8, 2, "machinehull-7", null, "buffer", null);
        regContainer("ultimate_buffer", 9, 2, "machinehull-8", null, "buffer", null);
        regContainer("antimatter_buffer", 10, 2, "machinehull-9", null, "buffer", null);
        regContainer("pure_antimatter_buffer", 11, 2, "machinehull-10", null, "buffer", null);
        regContainer("oec_buffer", 12, 2, "machinehull-11", null, "buffer", null);
        regContainer("opa_buffer", 13, 2, "machinehull-12", null, "buffer", null);
        regContainer("basic_multitrack_buffer", 4, 2, "machinehull-3", null, "buffer", null);
        regContainer("advanced_multitrack_buffer", 5, 2, "machinehull-4", null, "buffer", null);
        regContainer("precision_multitrack_buffer", 6, 2, "machinehull-5", null, "buffer", null);
        regContainer("clay_steel_multitrack_buffer", 7, 2, "machinehull-6", null, "buffer", null);
        regContainer("clayium_multitrack_buffer", 8, 2, "machinehull-7", null, "buffer", null);
        regContainer("ultimate_multitrack_buffer", 9, 2, "machinehull-8", null, "buffer", null);
        regContainer("antimatter_multitrack_buffer", 10, 2, "machinehull-9", null, "buffer", null);
        regContainer("pure_antimatter_multitrack_buffer", 11, 2, "machinehull-10", null, "buffer", null);
        regContainer("oec_multitrack_buffer", 12, 2, "machinehull-11", null, "buffer", null);
        regContainer("opa_multitrack_buffer", 13, 2, "machinehull-12", null, "buffer", null);
        regContainer("basic_chemical_reactor", 4, 1, "machinehull-3", "chemicalreactor", "machine", null);
        regContainer("advanced_chemical_reactor", 5, 1, "machinehull-4", "chemicalreactor", "machine", null);
        regContainer("clayium_chemical_reactor", 8, 1, "machinehull-7", "chemicalreactor", "machine", null);
        regContainer("basic_salt_extractor", 4, 2, "machinehull-3", "saltextractor", "machine", null);
        regContainer("advanced_salt_extractor", 5, 2, "machinehull-4", "saltextractor", "machine", null);
        regContainer("precision_salt_extractor", 6, 2, "machinehull-5", "saltextractor", "machine", null);
        regContainer("clay_steel_salt_extractor", 7, 2, "machinehull-6", "saltextractor", "machine", null);
        regContainer("precision_alloy_smelter", 6, 1, "machinehull-5", "alloysmelter", "machine", null);
        regContainer("advanced_clay_interface", 5, 0, "machinehull-4", "interface", "interface", null);
        regContainer("precision_clay_interface", 6, 0, "machinehull-5", "interface", "interface", null);
        regContainer("clay_steel_clay_interface", 7, 0, "machinehull-6", "interface", "interface", null);
        regContainer("clayium_clay_interface", 8, 0, "machinehull-7", "interface", "interface", null);
        regContainer("ultimate_clay_interface", 9, 0, "machinehull-8", "interface", "interface", null);
        regContainer("antimatter_clay_interface", 10, 0, "machinehull-9", "interface", "interface", null);
        regContainer("pure_antimatter_clay_interface", 11, 0, "machinehull-10", "interface", "interface", null);
        regContainer("oec_clay_interface", 12, 0, "machinehull-11", "interface", "interface", null);
        regContainer("opa_clay_interface", 13, 0, "machinehull-12", "interface", "interface", null);
        regContainer("advanced_redstone_interface", 5, 0, "machinehull-4", "redstoneinterface", "interface", null);
        regContainer("precision_redstone_interface", 6, 0, "machinehull-5", "redstoneinterface", "interface", null);
        regContainer("clay_steel_redstone_interface", 7, 0, "machinehull-6", "redstoneinterface", "interface", null);
        regContainer("clayium_redstone_interface", 8, 0, "machinehull-7", "redstoneinterface", "interface", null);
        regContainer("ultimate_redstone_interface", 9, 0, "machinehull-8", "redstoneinterface", "interface", null);
        regContainer("antimatter_redstone_interface", 10, 0, "machinehull-9", "redstoneinterface", "interface", null);
        regContainer("pure_antimatter_redstone_interface", 11, 0, "machinehull-10", "redstoneinterface", "interface", null);
        regContainer("oec_redstone_interface", 12, 0, "machinehull-11", "redstoneinterface", "interface", null);
        regContainer("opa_redstone_interface", 13, 0, "machinehull-12", "redstoneinterface", "interface", null);
        regContainer("precision_electrolysis_reactor", 6, 1, "machinehull-5", "electrolysisreactor", "machine", null);
        regContainer("clay_steel_electrolysis_reactor", 7, 1, "machinehull-6", "electrolysisreactor", "machine", null);
        regContainer("clayium_electrolysis_reactor", 8, 1, "machinehull-7", "electrolysisreactor", "machine", null);
        regContainer("ultimate_electrolysis_reactor", 9, 1, "machinehull-8", "electrolysisreactor", "machine", null);
        regContainer("clay_steel_clay_laser_interface", 7, 2, "machinehull-6", "laserinterface", "interface", null);
        regContainer("clayium_clay_laser_interface", 8, 2, "machinehull-7", "laserinterface", "interface", null);
        regContainer("ultimate_clay_laser_interface", 9, 2, "machinehull-8", "laserinterface", "interface", null);
        regContainer("antimatter_clay_laser_interface", 10, 2, "machinehull-9", "laserinterface", "interface", null);
        regContainer("pure_antimatter_clay_laser_interface", 11, 2, "machinehull-10", "laserinterface", "interface", null);
        regContainer("oec_clay_laser_interface", 12, 2, "machinehull-11", "laserinterface", "interface", null);
        regContainer("opa_clay_laser_interface", 13, 2, "machinehull-12", "laserinterface", "interface", null);
        regContainer("clay_steel_distributor", 7, 2, "machinehull-6", "distributor", "machine", null);
        regContainer("clayium_distributor", 8, 2, "machinehull-7", "distributor", "machine", null);
        regContainer("ultimate_distributor", 9, 2, "machinehull-8", "distributor", "machine", null);
        regContainer("advanced_auto_crafter", 5, 1, "machinehull-4", "autocraftertop", "machine", null);
        regContainer("precision_auto_crafter", 6, 1, "machinehull-5", "autocraftertop", "machine", null);
        regContainer("clay_steel_auto_crafter", 7, 1, "machinehull-6", "autocraftertop", "machine", null);
        regContainer("clayium_auto_crafter", 8, 1, "machinehull-7", "autocraftertop", "machine", null);
        regContainer("ultimate_auto_crafter", 9, 1, "machinehull-8", "autocraftertop", "machine", null);
        regContainer("clay_steel_matter_transformer", 7, 1, "machinehull-6", "transformer", "machine", null);
        regContainer("clayium_matter_transformer", 8, 1, "machinehull-7", "transformer", "machine", null);
        regContainer("ultimate_matter_transformer", 9, 1, "machinehull-8", "transformer", "machine", null);
        regContainer("antimatter_matter_transformer", 10, 1, "machinehull-9", "transformer", "machine", null);
        regContainer("pure_antimatter_matter_transformer", 11, 1, "machinehull-10", "transformer", "machine", null);
        regContainer("oec_matter_transformer", 12, 1, "machinehull-11", "transformer", "machine", null);
        regContainer("ultimate_ca_condenser", 9, 1, "machinehull-8", "cacondenser", "machine", null);
        regContainer("antimatter_ca_condenser", 10, 1, "machinehull-9", "cacondenser", "machine", null);
        regContainer("pure_antimatter_ca_condenser", 11, 1, "machinehull-10", "cacondenser", "machine", null);
        regContainer("ultimate_ca_injector", 9, 1, "machinehull-8", "cainjector", "machine", null);
        regContainer("antimatter_ca_injector", 10, 1, "machinehull-9", "cainjector", "machine", null);
        regContainer("pure_antimatter_ca_injector", 11, 1, "machinehull-10", "cainjector", "machine", null);
        regContainer("oec_ca_injector", 12, 1, "machinehull-11", "cainjector", "machine", null);
        regContainer("opa_ca_injector", 13, 1, "machinehull-12", "cainjector", "machine", null);
        regContainer("antimatter_ca_reactor_core", 10, 1, "machinehull-9", "careactorcore", "multiblock", null);
        regContainer("pure_antimatter_ca_reactor_core", 11, 1, "machinehull-10", "careactorcore", "multiblock", null);
        regContainer("oec_ca_reactor_core", 12, 1, "machinehull-11", "careactorcore", "multiblock", null);
        regContainer("opa_ca_reactor_core", 13, 1, "machinehull-12", "careactorcore", "multiblock", null);
        regContainer("clay_steel_clay_energy_laser", 7, 2, "machinehull-6", "energylaser", "laser", null);
        regContainer("clayium_clay_energy_laser", 8, 2, "machinehull-7", "energylaser", "laser", null);
        regContainer("ultimate_clay_energy_laser", 9, 2, "machinehull-8", "energylaser", "laser", null);
        regContainer("antimatter_clay_energy_laser", 10, 2, "machinehull-9", "energylaser", "laser", null);
        regContainer("antimatter_pan_adapter", 10, 2, "machinehull-9", "panadapter", "pan", null);
        regContainer("pure_antimatter_pan_adapter", 11, 2, "machinehull-10", "panadapter", "pan", null);
        regContainer("oec_pan_adapter", 12, 2, "machinehull-11", "panadapter", "pan", null);
        regContainer("opa_pan_adapter", 13, 2, "machinehull-12", "panadapter", "pan", null);
        regContainer("basic_pan_duplicator", 4, 2, "pancasing", "panduplicator", "pan", null);
        regContainer("advanced_pan_duplicator", 5, 2, "pancasing", "panduplicator", "pan", null);
        regContainer("precision_pan_duplicator", 6, 2, "pancasing", "panduplicator", "pan", null);
        regContainer("clay_steel_pan_duplicator", 7, 2, "pancasing", "panduplicator", "pan", null);
        regContainer("clayium_pan_duplicator", 8, 2, "pancasing", "panduplicator", "pan", null);
        regContainer("ultimate_pan_duplicator", 9, 2, "pancasing", "panduplicator", "pan", null);
        regContainer("antimatter_pan_duplicator", 10, 2, "pancasing", "panduplicator", "pan", null);
        regContainer("pure_antimatter_pan_duplicator", 11, 2, "pancasing", "panduplicator", "pan", null);
        regContainer("oec_pan_duplicator", 12, 2, "pancasing", "panduplicator", "pan", null);
        regContainer("opa_pan_duplicator", 13, 2, "pancasing", "panduplicator", "pan", null);
        regContainer("basic_fluid_translator", 4, 2, "machinehull-3", null, "fluid", null);
        regContainer("advanced_fluid_translator", 5, 2, "machinehull-4", null, "fluid", null);
        regContainer("precision_fluid_translator", 6, 2, "machinehull-5", null, "fluid", null);
        regContainer("clay_steel_fluid_translator", 7, 2, "machinehull-6", null, "fluid", null);
        regContainer("clayium_fluid_translator", 8, 2, "machinehull-7", null, "fluid", null);
        regContainer("ultimate_fluid_translator", 9, 2, "machinehull-8", null, "fluid", null);
        regContainer("antimatter_fluid_translator", 10, 2, "machinehull-9", null, "fluid", null);
        regContainer("pure_antimatter_fluid_translator", 11, 2, "machinehull-10", null, "fluid", null);
        regContainer("oec_fluid_translator", 12, 2, "machinehull-11", null, "fluid", null);
        regContainer("opa_fluid_translator", 13, 2, "machinehull-12", null, "fluid", null);
        regContainer("advanced_fluid_transfer_machine", 5, 1, "machinehull-4", "fluidtransfermachine", "fluid", null);
        regUpgrade("antimatter_resonator", 10, "resonator-antimatter", UpgradeKind.RESONATOR, 1.08, 0);
        regUpgrade("antimatter_overclocker", 10, "overclocker-antimatter", UpgradeKind.OVERCLOCKER, 1.5, 0);
        regUpgrade("antimatter_energy_storage_upgrade", 10, "estorageupgrade-antimatter", UpgradeKind.ENERGY_STORAGE, 1.0, 1);
        reg("antimatter_ca_reactor_coil", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(8.0F, 5.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 10, "careactorcoil-antimatter"));
        regUpgrade("pure_antimatter_resonator", 11, "resonator-pureantimatter", UpgradeKind.RESONATOR, 1.1, 0);
        regUpgrade("pure_antimatter_overclocker", 11, "overclocker-pureantimatter", UpgradeKind.OVERCLOCKER, 2.3, 0);
        regUpgrade("pure_antimatter_energy_storage_upgrade", 11, "estorageupgrade-pureantimatter", UpgradeKind.ENERGY_STORAGE, 1.0, 3);
        reg("pure_antimatter_ca_reactor_coil", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(8.0F, 5.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 11, "careactorcoil-pureantimatter"));
        regUpgrade("oec_resonator", 12, "resonator-oec", UpgradeKind.RESONATOR, 2.0, 0);
        regUpgrade("oec_overclocker", 12, "overclocker-oec", UpgradeKind.OVERCLOCKER, 3.5, 0);
        regUpgrade("oec_energy_storage_upgrade", 12, "estorageupgrade-oec", UpgradeKind.ENERGY_STORAGE, 1.0, 7);
        reg("oec_ca_reactor_coil", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(8.0F, 5.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 12, "careactorcoil-oec"));
        regUpgrade("opa_resonator", 13, "resonator-opa", UpgradeKind.RESONATOR, 20.0, 0);
        regUpgrade("opa_overclocker", 13, "overclocker-opa", UpgradeKind.OVERCLOCKER, 5.0, 0);
        regUpgrade("opa_energy_storage_upgrade", 13, "estorageupgrade-opa", UpgradeKind.ENERGY_STORAGE, 1.0, 63);
        reg("opa_ca_reactor_coil", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(8.0F, 5.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 13, "careactorcoil-opa"));
        reg("ca_reactor_hull_rank_1", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(4.0F, 25.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 10, "careactorhull-0"));
        reg("ca_reactor_hull_rank_2", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(4.0F, 25.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 11, "careactorhull-1"));
        reg("ca_reactor_hull_rank_3", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(4.0F, 25.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 11, "careactorhull-2"));
        reg("ca_reactor_hull_rank_4", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(4.0F, 25.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 11, "careactorhull-3"));
        reg("ca_reactor_hull_rank_5", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(4.0F, 25.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 11, "careactorhull-4"));
        reg("ca_reactor_hull_rank_6", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(4.0F, 25.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 12, "careactorhull-5"));
        reg("ca_reactor_hull_rank_7", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(4.0F, 25.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 12, "careactorhull-6"));
        reg("ca_reactor_hull_rank_8", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(4.0F, 25.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 12, "careactorhull-7"));
        reg("ca_reactor_hull_rank_9", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(4.0F, 25.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 12, "careactorhull-8"));
        reg("ca_reactor_hull_rank_10", () -> new TieredBlock(Properties.of().mapColor(MapColor.METAL).strength(4.0F, 25.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 13, "careactorhull-9"));
        regMetalBlock("impure_silicon_block", CMaterials.IMPURE_SILICON, 5, "impuresilicon", false);
        regMetalBlock("silicone_block", CMaterials.SILICONE, 5, "silicone", false);
        regMetalBlock("silicon_block", CMaterials.SILICON, 5, "silicon", false);
        regMetalBlock("aluminium_block", CMaterials.ALUMINIUM, 6, "aluminium", false);
        regMetalBlock("clay_steel_block", CMaterials.CLAY_STEEL, 7, "claysteel", false);
        regMetalBlock("clayium_block", CMaterials.CLAYIUM, 8, "clayium", false);
        regMetalBlock("ultimate_alloy_block", CMaterials.ULTIMATE_ALLOY, 9, "ultimatealloy", false);
        regMetalBlock("antimatter_block", CMaterials.ANTIMATTER, 10, "antimatter", false);
        regMetalBlock("pure_antimatter_block", CMaterials.PURE_ANTIMATTER, 11, "pureantimatter", false);
        regMetalBlock("octuple_energetic_clay_block", CMaterials.OCTUPLE_CLAY, 12, "oec", false);
        regMetalBlock("octuple_pure_antimatter_block", CMaterials.COMPRESSED_PURE_ANTIMATTER_8, 13, "opa", false);
        regSilicone("white_silicone_block", DyeColor.WHITE);
        regSilicone("orange_silicone_block", DyeColor.ORANGE);
        regSilicone("magenta_silicone_block", DyeColor.MAGENTA);
        regSilicone("light_blue_silicone_block", DyeColor.LIGHT_BLUE);
        regSilicone("yellow_silicone_block", DyeColor.YELLOW);
        regSilicone("lime_silicone_block", DyeColor.LIME);
        regSilicone("pink_silicone_block", DyeColor.PINK);
        regSilicone("gray_silicone_block", DyeColor.GRAY);
        regSilicone("light_gray_silicone_block", DyeColor.LIGHT_GRAY);
        regSilicone("cyan_silicone_block", DyeColor.CYAN);
        regSilicone("purple_silicone_block", DyeColor.PURPLE);
        regSilicone("blue_silicone_block", DyeColor.BLUE);
        regSilicone("brown_silicone_block", DyeColor.BROWN);
        regSilicone("green_silicone_block", DyeColor.GREEN);
        regSilicone("red_silicone_block", DyeColor.RED);
        regSilicone("black_silicone_block", DyeColor.BLACK);
        regContainer("basic_rf_generator", 4, 2, "machinehull-3", "rfgenerator", "fe_generator", null);
        regContainer("advanced_rf_generator", 5, 2, "machinehull-4", "rfgenerator", "fe_generator", null);
        regContainer("precision_rf_generator", 6, 2, "machinehull-5", "rfgenerator", "fe_generator", null);
        regContainer("clay_steel_rf_generator", 7, 2, "machinehull-6", "rfgenerator", "fe_generator", null);
        regContainer("clayium_rf_generator", 8, 2, "machinehull-7", "rfgenerator", "fe_generator", null);
        regContainer("ultimate_rf_generator", 9, 2, "machinehull-8", "rfgenerator", "fe_generator", null);
        regContainer("antimatter_rf_generator", 10, 2, "machinehull-9", "rfgenerator", "fe_generator", null);
        regContainer("pure_antimatter_rf_generator", 11, 2, "machinehull-10", "rfgenerator", "fe_generator", null);
        regContainer("oec_rf_generator", 12, 2, "machinehull-11", "rfgenerator", "fe_generator", null);
        regContainer("opa_rf_generator", 13, 2, "machinehull-12", "rfgenerator", "fe_generator", null);
        regMetalBlock("magnesium_block", CMaterials.MAGNESIUM, -1, "metalblock", true);
        regMetalBlock("sodium_block", CMaterials.SODIUM, -1, "metalblock", true);
        regMetalBlock("lithium_block", CMaterials.LITHIUM, -1, "metalblock", true);
        regMetalBlock("zirconium_block", CMaterials.ZIRCONIUM, -1, "metalblock", true);
        regMetalBlock("zinc_block", CMaterials.ZINC, -1, "metalblock", true);
        regMetalBlock("manganese_block", CMaterials.MANGANESE, -1, "metalblock", true);
        regMetalBlock("calcium_block", CMaterials.CALCIUM, -1, "metalblock", true);
        regMetalBlock("potassium_block", CMaterials.POTASSIUM, -1, "metalblock", true);
        regMetalBlock("nickel_block", CMaterials.NICKEL, -1, "metalblock", true);
        regMetalBlock("beryllium_block", CMaterials.BERYLLIUM, -1, "metalblock", true);
        regMetalBlock("lead_block", CMaterials.LEAD, -1, "metalblock", true);
        regMetalBlock("hafnium_block", CMaterials.HAFNIUM, -1, "metalblock", true);
        regMetalBlock("chrome_block", CMaterials.CHROME, -1, "metalblock", true);
        regMetalBlock("titanium_block", CMaterials.TITANIUM, -1, "metalblock", true);
        regMetalBlock("strontium_block", CMaterials.STRONTIUM, -1, "metalblock", true);
        regMetalBlock("barium_block", CMaterials.BARIUM, -1, "metalblock", true);
        regMetalBlock("az91d_block", CMaterials.AZ91D_ALLOY, -1, "metalblock", true);
        regMetalBlock("zk60a_block", CMaterials.ZK60A_ALLOY, -1, "metalblock", true);
        regMetalBlock("rubidium_block", CMaterials.RUBIDIUM, -1, "metalblock", true);
        regMetalBlock("caesium_block", CMaterials.CAESIUM, -1, "metalblock", true);
        regMetalBlock("francium_block", CMaterials.FRANCIUM, -1, "metalblock", true);
        regMetalBlock("radium_block", CMaterials.RADIUM, -1, "metalblock", true);
        regMetalBlock("actinium_block", CMaterials.ACTINIUM, -1, "metalblock", true);
        regMetalBlock("thorium_block", CMaterials.THORIUM, -1, "metalblock", true);
        regMetalBlock("protactinium_block", CMaterials.PROTACTINIUM, -1, "metalblock", true);
        regMetalBlock("uranium_block", CMaterials.URANIUM, -1, "metalblock", true);
        regMetalBlock("neptunium_block", CMaterials.NEPTUNIUM, -1, "metalblock", true);
        regMetalBlock("plutonium_block", CMaterials.PLUTONIUM, -1, "metalblock", true);
        regMetalBlock("americium_block", CMaterials.AMERICIUM, -1, "metalblock", true);
        regMetalBlock("curium_block", CMaterials.CURIUM, -1, "metalblock", true);
        regMetalBlock("lanthanum_block", CMaterials.LANTHANUM, -1, "metalblock", true);
        regMetalBlock("cerium_block", CMaterials.CERIUM, -1, "metalblock", true);
        regMetalBlock("praseodymium_block", CMaterials.PRASEODYMIUM, -1, "metalblock", true);
        regMetalBlock("neodymium_block", CMaterials.NEODYMIUM, -1, "metalblock", true);
        regMetalBlock("promethium_block", CMaterials.PROMETHIUM, -1, "metalblock", true);
        regMetalBlock("samarium_block", CMaterials.SAMARIUM, -1, "metalblock", true);
        regMetalBlock("europium_block", CMaterials.EUROPIUM, -1, "metalblock", true);
        regMetalBlock("vanadium_block", CMaterials.VANADIUM, -1, "metalblock", true);
        regMetalBlock("cobalt_block", CMaterials.COBALT, -1, "metalblock", true);
        regMetalBlock("palladium_block", CMaterials.PALLADIUM, -1, "metalblock", true);
        regMetalBlock("platinum_block", CMaterials.PLATINUM, -1, "metalblock", true);
        regMetalBlock("iridium_block", CMaterials.IRIDIUM, -1, "metalblock", true);
        regMetalBlock("osmium_block", CMaterials.OSMIUM, -1, "metalblock", true);
        regMetalBlock("rhenium_block", CMaterials.RHENIUM, -1, "metalblock", true);
        regMetalBlock("tantalum_block", CMaterials.TANTALUM, -1, "metalblock", true);
        regMetalBlock("tungsten_block", CMaterials.TUNGSTEN, -1, "metalblock", true);
        regMetalBlock("molybdenum_block", CMaterials.MOLYBDENUM, -1, "metalblock", true);
        regMetalBlock("antimony_block", CMaterials.ANTIMONY, -1, "metalblock", true);
        regMetalBlock("bismuth_block", CMaterials.BISMUTH, -1, "metalblock", true);
        regMetalBlock("brass_block", CMaterials.BRASS, -1, "metalblock", true);
        regMetalBlock("electrum_block", CMaterials.ELECTRUM, -1, "metalblock", true);
        regMetalBlock("invar_block", CMaterials.INVAR, -1, "metalblock", true);
        regMetalBlock("steel_block", CMaterials.STEEL, -1, "metalblock", true);
        regMetalBlock("copper_block", CMaterials.COPPER, -1, "metalblock", true);
        regMetalBlock("tin_block", CMaterials.TIN, -1, "metalblock", true);
        regMetalBlock("silver_block", CMaterials.SILVER, -1, "metalblock", true);
        regMetalBlock("bronze_block", CMaterials.BRONZE, -1, "metalblock", true);
        regMetalChest("silicon_chest", CMaterials.SILICON, 9, 5, 1);
        regMetalChest("aluminium_chest", CMaterials.ALUMINIUM, 9, 6, 1);
        regMetalChest("clay_steel_chest", CMaterials.CLAY_STEEL, 9, 8, 1);
        regMetalChest("clayium_chest", CMaterials.CLAYIUM, 13, 8, 1);
        regMetalChest("ultimate_alloy_chest", CMaterials.ULTIMATE_ALLOY, 13, 8, 3);
        regMetalChest("magnesium_chest", CMaterials.MAGNESIUM, 9, 6, 1);
        regMetalChest("sodium_chest", CMaterials.SODIUM, 9, 6, 1);
        regMetalChest("lithium_chest", CMaterials.LITHIUM, 9, 7, 1);
        regMetalChest("zirconium_chest", CMaterials.ZIRCONIUM, 9, 7, 1);
        regMetalChest("zinc_chest", CMaterials.ZINC, 9, 6, 1);
        regMetalChest("manganese_chest", CMaterials.MANGANESE, 9, 6, 1);
        regMetalChest("calcium_chest", CMaterials.CALCIUM, 9, 6, 1);
        regMetalChest("potassium_chest", CMaterials.POTASSIUM, 9, 6, 1);
        regMetalChest("nickel_chest", CMaterials.NICKEL, 9, 6, 1);
        regMetalChest("beryllium_chest", CMaterials.BERYLLIUM, 10, 8, 1);
        regMetalChest("lead_chest", CMaterials.LEAD, 9, 6, 1);
        regMetalChest("hafnium_chest", CMaterials.HAFNIUM, 9, 8, 1);
        regMetalChest("chrome_chest", CMaterials.CHROME, 13, 8, 1);
        regMetalChest("titanium_chest", CMaterials.TITANIUM, 13, 8, 1);
        regMetalChest("strontium_chest", CMaterials.STRONTIUM, 10, 8, 1);
        regMetalChest("barium_chest", CMaterials.BARIUM, 10, 8, 1);
        regMetalChest("az91d_chest", CMaterials.AZ91D_ALLOY, 13, 8, 1);
        regMetalChest("zk60a_chest", CMaterials.ZK60A_ALLOY, 13, 8, 1);
        regMetalChest("rubidium_chest", CMaterials.RUBIDIUM, 13, 3, 1);
        regMetalChest("caesium_chest", CMaterials.CAESIUM, 13, 3, 1);
        regMetalChest("francium_chest", CMaterials.FRANCIUM, 13, 4, 1);
        regMetalChest("radium_chest", CMaterials.RADIUM, 13, 4, 1);
        regMetalChest("actinium_chest", CMaterials.ACTINIUM, 13, 5, 1);
        regMetalChest("thorium_chest", CMaterials.THORIUM, 13, 5, 1);
        regMetalChest("protactinium_chest", CMaterials.PROTACTINIUM, 13, 6, 1);
        regMetalChest("uranium_chest", CMaterials.URANIUM, 13, 6, 2);
        regMetalChest("neptunium_chest", CMaterials.NEPTUNIUM, 13, 6, 3);
        regMetalChest("plutonium_chest", CMaterials.PLUTONIUM, 13, 6, 4);
        regMetalChest("americium_chest", CMaterials.AMERICIUM, 13, 6, 5);
        regMetalChest("curium_chest", CMaterials.CURIUM, 13, 6, 6);
        regMetalChest("lanthanum_chest", CMaterials.LANTHANUM, 13, 2, 2);
        regMetalChest("cerium_chest", CMaterials.CERIUM, 13, 2, 4);
        regMetalChest("praseodymium_chest", CMaterials.PRASEODYMIUM, 13, 2, 6);
        regMetalChest("neodymium_chest", CMaterials.NEODYMIUM, 13, 2, 8);
        regMetalChest("promethium_chest", CMaterials.PROMETHIUM, 13, 4, 8);
        regMetalChest("samarium_chest", CMaterials.SAMARIUM, 13, 6, 8);
        regMetalChest("europium_chest", CMaterials.EUROPIUM, 13, 8, 8);
        regMetalChest("vanadium_chest", CMaterials.VANADIUM, 4, 8, 1);
        regMetalChest("cobalt_chest", CMaterials.COBALT, 11, 6, 1);
        regMetalChest("palladium_chest", CMaterials.PALLADIUM, 11, 8, 1);
        regMetalChest("platinum_chest", CMaterials.PLATINUM, 13, 8, 2);
        regMetalChest("iridium_chest", CMaterials.IRIDIUM, 13, 8, 3);
        regMetalChest("osmium_chest", CMaterials.OSMIUM, 13, 8, 4);
        regMetalChest("rhenium_chest", CMaterials.RHENIUM, 13, 8, 5);
        regMetalChest("tantalum_chest", CMaterials.TANTALUM, 10, 8, 1);
        regMetalChest("tungsten_chest", CMaterials.TUNGSTEN, 13, 8, 1);
        regMetalChest("molybdenum_chest", CMaterials.MOLYBDENUM, 13, 8, 2);
        regMetalChest("antimony_chest", CMaterials.ANTIMONY, 9, 6, 1);
        regMetalChest("bismuth_chest", CMaterials.BISMUTH, 9, 6, 1);
        regMetalChest("brass_chest", CMaterials.BRASS, 9, 6, 1);
        regMetalChest("electrum_chest", CMaterials.ELECTRUM, 13, 8, 1);
        regMetalChest("invar_chest", CMaterials.INVAR, 9, 8, 1);
    }
}
