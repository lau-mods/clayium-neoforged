/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.registry;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.machine.ManufacturingMachineCatalog;
import net.claustra01.clayium.machine.ClayComponentCatalog;
import net.claustra01.clayium.machine.SpecializedMachineCatalog;
import net.claustra01.clayium.machine.MaterialCatalog;
import net.claustra01.clayium.logistics.LogisticsCatalog;
import net.claustra01.clayium.logistics.LogisticsKind;
import net.claustra01.clayium.storage.MetalStorageCatalog;
import java.util.LinkedHashMap;
import java.util.Map;
import net.claustra01.clayium.world.inventory.ClayWorkTableMenu;
import net.claustra01.clayium.world.inventory.ClayCraftingTableMenu;
import net.claustra01.clayium.world.inventory.CobblestoneGeneratorMenu;
import net.claustra01.clayium.world.inventory.MachineMenu;
import net.claustra01.clayium.world.inventory.LogisticsMenu;
import net.claustra01.clayium.world.level.block.ClayWorkTableBlock;
import net.claustra01.clayium.world.level.block.ClayCraftingTableBlock;
import net.claustra01.clayium.world.level.block.ClayOreBlock;
import net.claustra01.clayium.world.level.block.ColoredSiliconeBlock;
import net.claustra01.clayium.world.level.block.CobblestoneGeneratorBlock;
import net.claustra01.clayium.world.level.block.MachineBlock;
import net.claustra01.clayium.world.level.block.LogisticsBlock;
import net.claustra01.clayium.world.level.block.MachineInterfaceBlock;
import net.claustra01.clayium.world.level.block.RedstoneInterfaceBlock;
import net.claustra01.clayium.world.level.block.WaterWheelBlock;
import net.claustra01.clayium.world.level.block.entity.ClayWorkTableBlockEntity;
import net.claustra01.clayium.world.level.block.entity.ClayCraftingTableBlockEntity;
import net.claustra01.clayium.world.level.block.entity.CobblestoneGeneratorBlockEntity;
import net.claustra01.clayium.world.level.block.entity.MachineBlockEntity;
import net.claustra01.clayium.world.level.block.entity.LogisticsBlockEntity;
import net.claustra01.clayium.world.level.block.entity.MachineInterfaceBlockEntity;
import net.claustra01.clayium.world.level.block.entity.RedstoneInterfaceBlockEntity;
import net.claustra01.clayium.world.item.ClayConfiguratorItem;
import net.claustra01.clayium.world.item.ClayCraftingToolItem;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.claustra01.clayium.world.item.ClayPickaxeItem;
import net.claustra01.clayium.world.item.ClayShovelItem;
import net.claustra01.clayium.world.item.ClayToolTier;
import net.claustra01.clayium.world.item.RawClayCraftingToolItem;
import net.claustra01.clayium.world.item.StorageContainerBlockItem;
import net.claustra01.clayium.world.item.ClaySteelPickaxeItem;
import net.claustra01.clayium.world.item.ClaySteelShovelItem;
import net.claustra01.clayium.world.item.ClaySteelToolTier;
import net.claustra01.clayium.world.inventory.ItemFilterMenu;
import net.claustra01.clayium.world.inventory.FluidBufferMenu;
import net.claustra01.clayium.world.inventory.SaltExtractorMenu;
import net.claustra01.clayium.world.inventory.AutoClayCondenserMenu;
import net.claustra01.clayium.world.inventory.AutoCrafterMenu;
import net.claustra01.clayium.world.inventory.ChemicalMetalSeparatorMenu;
import net.claustra01.clayium.world.level.block.FluidBufferBlock;
import net.claustra01.clayium.world.level.block.SaltExtractorBlock;
import net.claustra01.clayium.world.level.block.QuartzCrucibleBlock;
import net.claustra01.clayium.world.level.block.AutoClayCondenserBlock;
import net.claustra01.clayium.world.level.block.AutoCrafterBlock;
import net.claustra01.clayium.world.level.block.ChemicalMetalSeparatorBlock;
import net.claustra01.clayium.world.level.block.ClayEnergyLaserBlock;
import net.claustra01.clayium.world.level.block.ClayLaserInterfaceBlock;
import net.claustra01.clayium.world.level.block.LaserReflectorBlock;
import net.claustra01.clayium.world.level.block.ClayEnergyConverterBlock;
import net.claustra01.clayium.world.level.block.MachineModifierBlock;
import net.claustra01.clayium.world.level.block.ResonatorBlock;
import net.claustra01.clayium.world.level.block.CAReactorCoilBlock;
import net.claustra01.clayium.world.level.block.CAReactorHullBlock;
import net.claustra01.clayium.world.level.block.ResonatingCollectorBlock;
import net.claustra01.clayium.world.level.block.PanAdapterBlock;
import net.claustra01.clayium.world.level.block.PanCableBlock;
import net.claustra01.clayium.world.level.block.PanCoreBlock;
import net.claustra01.clayium.world.level.block.PanDuplicatorBlock;
import net.claustra01.clayium.world.level.block.entity.FluidBufferBlockEntity;
import net.claustra01.clayium.world.level.block.entity.SaltExtractorBlockEntity;
import net.claustra01.clayium.world.level.block.entity.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.world.level.block.entity.AutoClayCondenserBlockEntity;
import net.claustra01.clayium.world.level.block.entity.AutoCrafterBlockEntity;
import net.claustra01.clayium.world.level.block.entity.ChemicalMetalSeparatorBlockEntity;
import net.claustra01.clayium.world.level.block.entity.ClayEnergyLaserBlockEntity;
import net.claustra01.clayium.world.level.block.entity.ClayLaserInterfaceBlockEntity;
import net.claustra01.clayium.world.level.block.entity.LaserReflectorBlockEntity;
import net.claustra01.clayium.world.level.block.entity.ClayEnergyConverterBlockEntity;
import net.claustra01.clayium.world.level.block.entity.ResonatingCollectorBlockEntity;
import net.claustra01.clayium.world.level.block.entity.PanAdapterBlockEntity;
import net.claustra01.clayium.world.level.block.entity.PanCoreBlockEntity;
import net.claustra01.clayium.world.level.block.entity.PanDuplicatorBlockEntity;
import net.claustra01.clayium.world.inventory.ClayEnergyLaserMenu;
import net.claustra01.clayium.world.inventory.ClayEnergyConverterMenu;
import net.claustra01.clayium.world.inventory.ResonatingCollectorMenu;
import net.claustra01.clayium.world.inventory.PanAdapterMenu;
import net.claustra01.clayium.world.inventory.PanCoreMenu;
import net.claustra01.clayium.world.inventory.PanDuplicatorMenu;
import net.claustra01.clayium.world.inventory.MetalChestMenu;
import net.claustra01.clayium.world.level.block.MetalChestBlock;
import net.claustra01.clayium.world.level.block.DecorativeMetalBlock;
import net.claustra01.clayium.world.level.block.entity.MetalChestBlockEntity;
import net.claustra01.clayium.world.level.block.entity.WaterWheelBlockEntity;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public final class ClayiumRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Clayium.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Clayium.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Clayium.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, Clayium.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Clayium.MODID);

    public static final DeferredBlock<ClayWorkTableBlock> CLAY_WORK_TABLE = BLOCKS.registerBlock(
            "clay_work_table",
            ClayWorkTableBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.CLAY)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 2.0F)
                    .sound(SoundType.STONE));
    public static final DeferredBlock<ClayCraftingTableBlock> CLAY_CRAFTING_TABLE = BLOCKS.registerBlock(
            "clay_crafting_table",
            ClayCraftingTableBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.CLAY)
                    .strength(1.0F, 4.0F)
                    .sound(SoundType.GRAVEL));

    public static final DeferredBlock<ClayOreBlock> CLAY_ORE = BLOCKS.register(
            "clay_ore",
            () -> new ClayOreBlock(
                    true,
                    UniformInt.of(0, 1),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<ClayOreBlock> DEEPSLATE_CLAY_ORE = BLOCKS.register(
            "deepslate_clay_ore",
            () -> new ClayOreBlock(
                    true,
                    UniformInt.of(0, 1),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final DeferredBlock<ClayOreBlock> DENSE_CLAY_ORE = BLOCKS.register(
            "dense_clay_ore",
            () -> new ClayOreBlock(
                    false,
                    UniformInt.of(0, 0),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<ClayOreBlock> DEEPSLATE_DENSE_CLAY_ORE = BLOCKS.register(
            "deepslate_dense_clay_ore",
            () -> new ClayOreBlock(
                    false,
                    UniformInt.of(0, 0),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final DeferredBlock<ClayOreBlock> LARGE_DENSE_CLAY_ORE = BLOCKS.register(
            "large_dense_clay_ore",
            () -> new ClayOreBlock(
                    false,
                    UniformInt.of(0, 0),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<ClayOreBlock> DEEPSLATE_LARGE_DENSE_CLAY_ORE = BLOCKS.register(
            "deepslate_large_dense_clay_ore",
            () -> new ClayOreBlock(
                    false,
                    UniformInt.of(0, 0),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final DeferredBlock<Block> DENSE_CLAY = registerBlock(
            "dense_clay", BlockBehaviour.Properties.of()
                    .mapColor(MapColor.CLAY).strength(2.5F, 4.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> COMPRESSED_CLAY = registerBlock(
            "compressed_clay", BlockBehaviour.Properties.of()
                    .mapColor(MapColor.CLAY).strength(3.5F, 5.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> RAW_CLAY_MACHINE_HULL = registerBlock(
            "raw_clay_machine_hull", BlockBehaviour.Properties.of()
                    .mapColor(MapColor.CLAY).strength(2.0F, 3.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> CLAY_MACHINE_HULL = registerBlock(
            "clay_machine_hull", BlockBehaviour.Properties.of()
                    .mapColor(MapColor.CLAY).strength(3.0F, 5.0F).sound(SoundType.STONE));
    public static final Map<String, DeferredBlock<Block>> COMPRESSED_CLAY_BLOCKS =
            registerCompressedClayBlocks();
    public static final Map<String, DeferredBlock<Block>> MACHINE_HULL_BLOCKS =
            registerMachineHullBlocks();

    public static final DeferredBlock<MachineBlock> CLAY_BENDING_MACHINE = BLOCKS.register(
            "clay_bending_machine",
            () -> new MachineBlock(machineProperties(), ClayiumMachineIds.CLAY_BENDING_MACHINE, ClayTier.CLAY));
    public static final DeferredBlock<MachineBlock> ELEMENTAL_MILLING_MACHINE = BLOCKS.register(
            "elemental_milling_machine",
            () -> new MachineBlock(machineProperties(), ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE, ClayTier.CLAY));
    public static final DeferredBlock<WaterWheelBlock> CLAY_WATER_WHEEL = BLOCKS.register(
            "clay_water_wheel",
            () -> new WaterWheelBlock(machineProperties()));
    public static final DeferredBlock<WaterWheelBlock> DENSE_CLAY_WATER_WHEEL = BLOCKS.register(
            "dense_clay_water_wheel",
            () -> new WaterWheelBlock(machineProperties(), ClayTier.DENSE_CLAY));
    public static final Map<String, DeferredBlock<CobblestoneGeneratorBlock>> COBBLESTONE_GENERATOR_BLOCKS =
            registerCobblestoneGenerators();
    public static final Map<String, DeferredBlock<MachineBlock>> MANUFACTURING_MACHINE_BLOCKS =
            registerManufacturingMachineBlocks();
    public static final Map<String, DeferredBlock<MachineBlock>> SPECIALIZED_MACHINE_BLOCKS =
            registerSpecializedMachineBlocks();
    public static final DeferredBlock<MachineBlock> CLAY_BLAST_FURNACE = BLOCKS.register(
            "clay_blast_furnace",
            () -> new MachineBlock(machineProperties(), ClayiumMachineIds.CLAY_BLAST_FURNACE, ClayTier.PRECISION));
    public static final Map<String, DeferredBlock<LogisticsBlock>> LOGISTICS_BLOCKS =
            registerLogisticsBlocks();
    public static final Map<String, DeferredBlock<MachineInterfaceBlock>> MACHINE_INTERFACE_BLOCKS =
            registerMachineInterfaces();
    public static final Map<String, DeferredBlock<RedstoneInterfaceBlock>> REDSTONE_INTERFACE_BLOCKS =
            registerRedstoneInterfaces();
    public static final Map<String, DeferredBlock<Block>> MATERIAL_BLOCKS = registerMaterialBlocks();
    public static final Map<String, DeferredBlock<DecorativeMetalBlock>> DECORATIVE_METAL_BLOCKS =
            registerDecorativeMetalBlocks();
    public static final Map<String, DeferredBlock<ColoredSiliconeBlock>> COLORED_SILICONE_BLOCKS =
            registerColoredSiliconeBlocks();
    public static final Map<String, DeferredBlock<Block>> OTHER_HULL_BLOCKS = registerOtherHullBlocks();
    public static final Map<String, DeferredBlock<FluidBufferBlock>> FLUID_BUFFER_BLOCKS = registerFluidBuffers();
    public static final Map<String, DeferredBlock<SaltExtractorBlock>> SALT_EXTRACTOR_BLOCKS = registerSaltExtractors();
    public static final DeferredBlock<QuartzCrucibleBlock> QUARTZ_CRUCIBLE = BLOCKS.registerBlock(
            "quartz_crucible", QuartzCrucibleBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(0.2F).sound(SoundType.GLASS));
    public static final DeferredBlock<AutoClayCondenserBlock> ADVANCED_AUTO_CLAY_CONDENSER = BLOCKS.register(
            "advanced_auto_clay_condenser",
            () -> new AutoClayCondenserBlock(machineProperties(), ClayTier.ADVANCED));
    public static final Map<String, DeferredBlock<AutoCrafterBlock>> AUTO_CRAFTER_BLOCKS = registerAutoCrafters();
    public static final DeferredBlock<ChemicalMetalSeparatorBlock> PRECISION_CHEMICAL_METAL_SEPARATOR = BLOCKS.register(
            "precision_chemical_metal_separator",
            () -> new ChemicalMetalSeparatorBlock(machineProperties(), ClayTier.PRECISION));
    public static final Map<String, DeferredBlock<ClayEnergyLaserBlock>> CLAY_ENERGY_LASER_BLOCKS = registerClayEnergyLasers();
    public static final Map<String, DeferredBlock<ClayLaserInterfaceBlock>> CLAY_LASER_INTERFACE_BLOCKS = registerClayLaserInterfaces();
    public static final DeferredBlock<LaserReflectorBlock> LASER_REFLECTOR = BLOCKS.register(
            "laser_reflector", () -> new LaserReflectorBlock(machineProperties()));
    public static final DeferredBlock<MachineBlock> CLAY_REACTOR = BLOCKS.register(
            "clay_reactor", () -> new MachineBlock(machineProperties(), ClayiumMachineIds.CLAY_REACTOR, ClayTier.CLAY_STEEL));
    public static final DeferredBlock<AutoClayCondenserBlock> CLAY_STEEL_AUTO_CLAY_CONDENSER = BLOCKS.register(
            "clay_steel_auto_clay_condenser", () -> new AutoClayCondenserBlock(machineProperties(), ClayTier.CLAY_STEEL));
    public static final Map<String, DeferredBlock<ClayEnergyConverterBlock>> CLAY_ENERGY_CONVERTER_BLOCKS =
            registerClayEnergyConverters();
    public static final Map<String, DeferredBlock<MachineModifierBlock>> MACHINE_MODIFIER_BLOCKS =
            registerMachineModifiers();
    public static final Map<String, DeferredBlock<ResonatorBlock>> RESONATOR_BLOCKS = registerResonators();
    public static final Map<String, DeferredBlock<CAReactorCoilBlock>> CA_REACTOR_COIL_BLOCKS =
            registerCAReactorCoils();
    public static final Map<String, DeferredBlock<CAReactorHullBlock>> CA_REACTOR_HULL_BLOCKS =
            registerCAReactorHulls();
    public static final Map<String, DeferredBlock<MachineBlock>> CA_REACTOR_CORE_BLOCKS =
            registerCAReactorCores();
    public static final DeferredBlock<ResonatingCollectorBlock> RESONATING_COLLECTOR = BLOCKS.register(
            "resonating_collector", () -> new ResonatingCollectorBlock(machineProperties()));
    public static final DeferredBlock<PanCableBlock> PAN_CABLE = BLOCKS.register(
            "pan_cable", () -> new PanCableBlock(machineProperties()));
    public static final DeferredBlock<PanCoreBlock> PAN_CORE = BLOCKS.register(
            "pan_core", () -> new PanCoreBlock(machineProperties()));
    public static final Map<String, DeferredBlock<PanAdapterBlock>> PAN_ADAPTER_BLOCKS = registerPanAdapters();
    public static final Map<String, DeferredBlock<PanDuplicatorBlock>> PAN_DUPLICATOR_BLOCKS = registerPanDuplicators();
    public static final Map<String, DeferredBlock<MetalChestBlock>> METAL_CHEST_BLOCKS = registerMetalChests();

    public static final DeferredItem<BlockItem> CLAY_WORK_TABLE_ITEM = ITEMS.register(
            "clay_work_table",
            () -> new BlockItem(CLAY_WORK_TABLE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> CLAY_CRAFTING_TABLE_ITEM =
            registerBlockItem("clay_crafting_table", CLAY_CRAFTING_TABLE);

    public static final DeferredItem<BlockItem> CLAY_ORE_ITEM = registerBlockItem("clay_ore", CLAY_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_CLAY_ORE_ITEM =
            registerBlockItem("deepslate_clay_ore", DEEPSLATE_CLAY_ORE);
    public static final DeferredItem<BlockItem> DENSE_CLAY_ORE_ITEM = registerBlockItem("dense_clay_ore", DENSE_CLAY_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_DENSE_CLAY_ORE_ITEM =
            registerBlockItem("deepslate_dense_clay_ore", DEEPSLATE_DENSE_CLAY_ORE);
    public static final DeferredItem<BlockItem> LARGE_DENSE_CLAY_ORE_ITEM =
            registerBlockItem("large_dense_clay_ore", LARGE_DENSE_CLAY_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_LARGE_DENSE_CLAY_ORE_ITEM =
            registerBlockItem("deepslate_large_dense_clay_ore", DEEPSLATE_LARGE_DENSE_CLAY_ORE);
    public static final DeferredItem<BlockItem> DENSE_CLAY_ITEM = registerBlockItem("dense_clay", DENSE_CLAY);
    public static final DeferredItem<BlockItem> COMPRESSED_CLAY_ITEM = registerBlockItem("compressed_clay", COMPRESSED_CLAY);
    public static final DeferredItem<BlockItem> RAW_CLAY_MACHINE_HULL_ITEM =
            registerBlockItem("raw_clay_machine_hull", RAW_CLAY_MACHINE_HULL);
    public static final DeferredItem<BlockItem> CLAY_MACHINE_HULL_ITEM =
            registerBlockItem("clay_machine_hull", CLAY_MACHINE_HULL);
    public static final Map<String, DeferredItem<BlockItem>> COMPRESSED_CLAY_BLOCK_ITEMS =
            registerBlockItems(COMPRESSED_CLAY_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> MACHINE_HULL_BLOCK_ITEMS =
            registerBlockItems(MACHINE_HULL_BLOCKS);
    public static final DeferredItem<BlockItem> CLAY_BENDING_MACHINE_ITEM =
            registerBlockItem("clay_bending_machine", CLAY_BENDING_MACHINE);
    public static final DeferredItem<BlockItem> ELEMENTAL_MILLING_MACHINE_ITEM =
            registerBlockItem("elemental_milling_machine", ELEMENTAL_MILLING_MACHINE);
    public static final DeferredItem<BlockItem> CLAY_WATER_WHEEL_ITEM =
            registerBlockItem("clay_water_wheel", CLAY_WATER_WHEEL);
    public static final DeferredItem<BlockItem> DENSE_CLAY_WATER_WHEEL_ITEM =
            registerBlockItem("dense_clay_water_wheel", DENSE_CLAY_WATER_WHEEL);
    public static final Map<String, DeferredItem<BlockItem>> COBBLESTONE_GENERATOR_ITEMS =
            registerBlockItems(COBBLESTONE_GENERATOR_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> MANUFACTURING_MACHINE_ITEMS =
            registerManufacturingMachineItems();
    public static final Map<String, DeferredItem<BlockItem>> SPECIALIZED_MACHINE_ITEMS =
            registerBlockItems(SPECIALIZED_MACHINE_BLOCKS);
    public static final DeferredItem<BlockItem> CLAY_BLAST_FURNACE_ITEM =
            registerBlockItem("clay_blast_furnace", CLAY_BLAST_FURNACE);
    public static final Map<String, DeferredItem<BlockItem>> LOGISTICS_ITEMS =
            registerLogisticsBlockItems();
    public static final Map<String, DeferredItem<BlockItem>> MACHINE_INTERFACE_ITEMS =
            registerBlockItems(MACHINE_INTERFACE_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> REDSTONE_INTERFACE_ITEMS =
            registerBlockItems(REDSTONE_INTERFACE_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> MATERIAL_BLOCK_ITEMS = registerBlockItems(MATERIAL_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> DECORATIVE_METAL_BLOCK_ITEMS =
            registerBlockItems(DECORATIVE_METAL_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> COLORED_SILICONE_ITEMS =
            registerBlockItems(COLORED_SILICONE_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> OTHER_HULL_ITEMS = registerBlockItems(OTHER_HULL_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> FLUID_BUFFER_ITEMS = registerBlockItems(FLUID_BUFFER_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> SALT_EXTRACTOR_ITEMS = registerBlockItems(SALT_EXTRACTOR_BLOCKS);
    public static final DeferredItem<BlockItem> QUARTZ_CRUCIBLE_ITEM = registerBlockItem("quartz_crucible", QUARTZ_CRUCIBLE);
    public static final DeferredItem<BlockItem> ADVANCED_AUTO_CLAY_CONDENSER_ITEM =
            registerBlockItem("advanced_auto_clay_condenser", ADVANCED_AUTO_CLAY_CONDENSER);
    public static final Map<String, DeferredItem<BlockItem>> AUTO_CRAFTER_ITEMS = registerBlockItems(AUTO_CRAFTER_BLOCKS);
    public static final DeferredItem<BlockItem> PRECISION_CHEMICAL_METAL_SEPARATOR_ITEM =
            registerBlockItem("precision_chemical_metal_separator", PRECISION_CHEMICAL_METAL_SEPARATOR);
    public static final Map<String, DeferredItem<BlockItem>> CLAY_ENERGY_LASER_ITEMS = registerBlockItems(CLAY_ENERGY_LASER_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> CLAY_LASER_INTERFACE_ITEMS = registerBlockItems(CLAY_LASER_INTERFACE_BLOCKS);
    public static final DeferredItem<BlockItem> LASER_REFLECTOR_ITEM = registerBlockItem("laser_reflector", LASER_REFLECTOR);
    public static final DeferredItem<BlockItem> CLAY_REACTOR_ITEM = registerBlockItem("clay_reactor", CLAY_REACTOR);
    public static final DeferredItem<BlockItem> CLAY_STEEL_AUTO_CLAY_CONDENSER_ITEM =
            registerBlockItem("clay_steel_auto_clay_condenser", CLAY_STEEL_AUTO_CLAY_CONDENSER);
    public static final Map<String, DeferredItem<BlockItem>> CLAY_ENERGY_CONVERTER_ITEMS =
            registerBlockItems(CLAY_ENERGY_CONVERTER_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> MACHINE_MODIFIER_ITEMS =
            registerBlockItems(MACHINE_MODIFIER_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> RESONATOR_ITEMS = registerBlockItems(RESONATOR_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> CA_REACTOR_COIL_ITEMS =
            registerBlockItems(CA_REACTOR_COIL_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> CA_REACTOR_HULL_ITEMS =
            registerBlockItems(CA_REACTOR_HULL_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> CA_REACTOR_CORE_ITEMS =
            registerBlockItems(CA_REACTOR_CORE_BLOCKS);
    public static final DeferredItem<BlockItem> RESONATING_COLLECTOR_ITEM =
            registerBlockItem("resonating_collector", RESONATING_COLLECTOR);
    public static final DeferredItem<BlockItem> PAN_CABLE_ITEM = registerBlockItem("pan_cable", PAN_CABLE);
    public static final DeferredItem<BlockItem> PAN_CORE_ITEM = registerBlockItem("pan_core", PAN_CORE);
    public static final Map<String, DeferredItem<BlockItem>> PAN_ADAPTER_ITEMS = registerBlockItems(PAN_ADAPTER_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> PAN_DUPLICATOR_ITEMS = registerBlockItems(PAN_DUPLICATOR_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> METAL_CHEST_ITEMS = registerBlockItems(METAL_CHEST_BLOCKS);

    public static final DeferredItem<Item> CLAY_STICK = ITEMS.register(
            "clay_stick",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHORT_CLAY_STICK = item("short_clay_stick");
    public static final DeferredItem<Item> LARGE_CLAY_BALL = item("large_clay_ball");
    public static final DeferredItem<Item> CLAY_DISC = item("clay_disc");
    public static final DeferredItem<Item> SMALL_CLAY_DISC = item("small_clay_disc");
    public static final DeferredItem<Item> CLAY_PLATE = item("clay_plate");
    public static final DeferredItem<Item> LARGE_CLAY_PLATE = item("large_clay_plate");
    public static final DeferredItem<Item> CLAY_BLADE = item("clay_blade");
    public static final DeferredItem<Item> CLAY_CYLINDER = item("clay_cylinder");
    public static final DeferredItem<Item> CLAY_RING = item("clay_ring");
    public static final DeferredItem<Item> SMALL_CLAY_RING = item("small_clay_ring");
    public static final DeferredItem<Item> CLAY_GEAR = item("clay_gear");
    public static final DeferredItem<Item> DENSE_CLAY_PLATE = item("dense_clay_plate");
    public static final DeferredItem<Item> DENSE_CLAY_STICK = item("dense_clay_stick");
    public static final DeferredItem<Item> DENSE_CLAY_GEAR = item("dense_clay_gear");
    public static final DeferredItem<Item> CLAY_CIRCUIT_BOARD = item("clay_circuit_board");
    public static final DeferredItem<Item> RAW_CLAY_ROLLING_PIN = rawTool("raw_clay_rolling_pin");
    public static final DeferredItem<Item> RAW_CLAY_SLICER = rawTool("raw_clay_slicer");
    public static final DeferredItem<Item> RAW_CLAY_SPATULA = rawTool("raw_clay_spatula");
    public static final DeferredItem<Item> CLAY_ROLLING_PIN = ITEMS.register(
            "clay_rolling_pin", () -> new ClayCraftingToolItem(
                    new Item.Properties().durability(60), ClayConfiguratorItem.Mode.INSERT, 4));
    public static final DeferredItem<Item> CLAY_SLICER = ITEMS.register(
            "clay_slicer", () -> new ClayCraftingToolItem(
                    new Item.Properties().durability(60), ClayConfiguratorItem.Mode.EXTRACT, 3));
    public static final DeferredItem<Item> CLAY_SPATULA = ITEMS.register(
            "clay_spatula", () -> new ClayCraftingToolItem(
                    new Item.Properties().durability(36), ClayConfiguratorItem.Mode.PIPE, 2));
    public static final DeferredItem<Item> CLAY_WRENCH = ITEMS.register(
            "clay_wrench", () -> new ClayConfiguratorItem(
                    new Item.Properties().stacksTo(1), ClayConfiguratorItem.Mode.ROTATE));
    public static final DeferredItem<ClayShovelItem> CLAY_SHOVEL = ITEMS.register(
            "clay_shovel",
            () -> new ClayShovelItem(
                    new Item.Properties().attributes(ShovelItem.createAttributes(ClayToolTier.SHOVEL, 1.5F, -3.0F))));
    public static final DeferredItem<ClayPickaxeItem> CLAY_PICKAXE = ITEMS.register(
            "clay_pickaxe",
            () -> new ClayPickaxeItem(
                    new Item.Properties().attributes(PickaxeItem.createAttributes(ClayToolTier.PICKAXE, 1.0F, -2.8F))));
    public static final Map<String, DeferredItem<Item>> COMPONENT_ITEMS = registerComponentItems();
    public static final Map<String, DeferredItem<Item>> MATERIAL_ITEMS = registerMaterialItems();
    public static final DeferredItem<ClaySteelPickaxeItem> CLAY_STEEL_PICKAXE = ITEMS.register(
            "clay_steel_pickaxe", () -> new ClaySteelPickaxeItem(new Item.Properties()
                    .attributes(PickaxeItem.createAttributes(ClaySteelToolTier.INSTANCE, 1.0F, -2.8F))
                    .component(ClayiumDataComponents.CLAY_STEEL_TOOL_SETTINGS.get(),
                            net.claustra01.clayium.data.ClaySteelToolSettings.DEFAULT)));
    public static final DeferredItem<ClaySteelShovelItem> CLAY_STEEL_SHOVEL = ITEMS.register(
            "clay_steel_shovel", () -> new ClaySteelShovelItem(new Item.Properties()
                    .attributes(ShovelItem.createAttributes(ClaySteelToolTier.INSTANCE, 1.5F, -3.0F))
                    .component(ClayiumDataComponents.CLAY_STEEL_TOOL_SETTINGS.get(),
                            net.claustra01.clayium.data.ClaySteelToolSettings.DEFAULT)));
    public static final DeferredItem<ClayConfiguratorItem> CLAY_IO_TOOL = ITEMS.register(
            "clay_io_tool", () -> new ClayConfiguratorItem(
                    new Item.Properties().stacksTo(1), ClayConfiguratorItem.Mode.IO_COMBINED));
    public static final DeferredItem<ClayConfiguratorItem> CLAY_PIPING_TOOL = ITEMS.register(
            "clay_piping_tool", () -> new ClayConfiguratorItem(
                    new Item.Properties().stacksTo(1), ClayConfiguratorItem.Mode.PIPE_COMBINED));
    public static final DeferredItem<ClayConfiguratorItem> IO_MEMORY_CARD = ITEMS.register(
            "io_memory_card", () -> new ClayConfiguratorItem(new Item.Properties()
                    .stacksTo(1)
                    .component(ClayiumDataComponents.IO_MEMORY.get(), net.claustra01.clayium.data.IoMemory.DEFAULT),
                    ClayConfiguratorItem.Mode.MEMORY));
    public static final DeferredItem<ClayFilterItem> FILTER_DUPLICATOR =
            filter("filter_duplicator", ClayFilterItem.Kind.DUPLICATOR);
    public static final DeferredItem<ClayFilterItem> FILTER_WHITELIST =
            filter("filter_whitelist", ClayFilterItem.Kind.WHITELIST);
    public static final DeferredItem<ClayFilterItem> FILTER_BLACKLIST =
            filter("filter_blacklist", ClayFilterItem.Kind.BLACKLIST);
    public static final DeferredItem<ClayFilterItem> FILTER_FUZZY =
            filter("filter_fuzzy", ClayFilterItem.Kind.FUZZY);
    public static final DeferredItem<ClayFilterItem> FILTER_ITEM_TAG =
            filter("filter_item_tag", ClayFilterItem.Kind.ITEM_TAG);
    public static final DeferredItem<ClayFilterItem> FILTER_ITEM_NAME =
            filter("filter_item_name", ClayFilterItem.Kind.ITEM_NAME);
    public static final DeferredItem<ClayFilterItem> FILTER_TRANSLATION_KEY =
            filter("filter_translation_key", ClayFilterItem.Kind.TRANSLATION_KEY);
    public static final DeferredItem<ClayFilterItem> FILTER_UNIQUE_ID =
            filter("filter_unique_id", ClayFilterItem.Kind.UNIQUE_ID);
    public static final DeferredItem<ClayFilterItem> FILTER_MOD_ID =
            filter("filter_mod_id", ClayFilterItem.Kind.MOD_ID);
    public static final DeferredItem<ClayFilterItem> FILTER_ITEM_DAMAGE =
            filter("filter_item_damage", ClayFilterItem.Kind.ITEM_DAMAGE);
    public static final DeferredItem<ClayFilterItem> FILTER_BLOCK_STATE =
            filter("filter_block_state", ClayFilterItem.Kind.BLOCK_STATE);
    public static final DeferredItem<ClayFilterItem> FILTER_BLOCK_HARVESTABLE =
            filter("filter_block_harvestable", ClayFilterItem.Kind.BLOCK_HARVESTABLE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayWorkTableBlockEntity>>
            CLAY_WORK_TABLE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "clay_work_table",
                    () -> BlockEntityType.Builder.of(
                            ClayWorkTableBlockEntity::new,
                            CLAY_WORK_TABLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayCraftingTableBlockEntity>>
            CLAY_CRAFTING_TABLE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "clay_crafting_table",
                    () -> BlockEntityType.Builder.of(
                            ClayCraftingTableBlockEntity::new,
                            CLAY_CRAFTING_TABLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineBlockEntity>>
            MACHINE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "machine",
                    () -> BlockEntityType.Builder.of(
                            MachineBlockEntity::new,
                            allMachineBlocks()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterWheelBlockEntity>>
            WATER_WHEEL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "clay_water_wheel",
                    () -> BlockEntityType.Builder.of(
                            WaterWheelBlockEntity::new,
                            CLAY_WATER_WHEEL.get(),
                            DENSE_CLAY_WATER_WHEEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CobblestoneGeneratorBlockEntity>>
            COBBLESTONE_GENERATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "cobblestone_generator",
                    () -> BlockEntityType.Builder.of(
                            CobblestoneGeneratorBlockEntity::new,
                            COBBLESTONE_GENERATOR_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new))
                            .build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LogisticsBlockEntity>>
            LOGISTICS_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "logistics",
                    () -> BlockEntityType.Builder.of(
                            LogisticsBlockEntity::new,
                            LOGISTICS_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineInterfaceBlockEntity>>
            MACHINE_INTERFACE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("machine_interface",
                    () -> BlockEntityType.Builder.of(MachineInterfaceBlockEntity::new,
                            MACHINE_INTERFACE_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneInterfaceBlockEntity>>
            REDSTONE_INTERFACE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("redstone_interface",
                    () -> BlockEntityType.Builder.of(RedstoneInterfaceBlockEntity::new,
                            REDSTONE_INTERFACE_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidBufferBlockEntity>> FLUID_BUFFER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("fluid_buffer", () -> BlockEntityType.Builder.of(
                    FluidBufferBlockEntity::new, FLUID_BUFFER_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SaltExtractorBlockEntity>> SALT_EXTRACTOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("salt_extractor", () -> BlockEntityType.Builder.of(
                    SaltExtractorBlockEntity::new, SALT_EXTRACTOR_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuartzCrucibleBlockEntity>> QUARTZ_CRUCIBLE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("quartz_crucible", () -> BlockEntityType.Builder.of(
                    QuartzCrucibleBlockEntity::new, QUARTZ_CRUCIBLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoClayCondenserBlockEntity>>
            AUTO_CLAY_CONDENSER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("auto_clay_condenser",
                    () -> BlockEntityType.Builder.of(AutoClayCondenserBlockEntity::new,
                            ADVANCED_AUTO_CLAY_CONDENSER.get(), CLAY_STEEL_AUTO_CLAY_CONDENSER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoCrafterBlockEntity>>
            AUTO_CRAFTER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("auto_crafter",
                    () -> BlockEntityType.Builder.of(AutoCrafterBlockEntity::new,
                            AUTO_CRAFTER_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalMetalSeparatorBlockEntity>>
            CHEMICAL_METAL_SEPARATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("chemical_metal_separator",
                    () -> BlockEntityType.Builder.of(ChemicalMetalSeparatorBlockEntity::new,
                            PRECISION_CHEMICAL_METAL_SEPARATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayEnergyLaserBlockEntity>> CLAY_ENERGY_LASER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("clay_energy_laser", () -> BlockEntityType.Builder.of(
                    ClayEnergyLaserBlockEntity::new, CLAY_ENERGY_LASER_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayLaserInterfaceBlockEntity>> CLAY_LASER_INTERFACE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("clay_laser_interface", () -> BlockEntityType.Builder.of(
                    ClayLaserInterfaceBlockEntity::new, CLAY_LASER_INTERFACE_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserReflectorBlockEntity>> LASER_REFLECTOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("laser_reflector", () -> BlockEntityType.Builder.of(
                    LaserReflectorBlockEntity::new, LASER_REFLECTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayEnergyConverterBlockEntity>> CLAY_ENERGY_CONVERTER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("clay_energy_converter", () -> BlockEntityType.Builder.of(
                    ClayEnergyConverterBlockEntity::new,
                    CLAY_ENERGY_CONVERTER_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonatingCollectorBlockEntity>>
            RESONATING_COLLECTOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("resonating_collector",
                    () -> BlockEntityType.Builder.of(ResonatingCollectorBlockEntity::new,
                            RESONATING_COLLECTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PanAdapterBlockEntity>> PAN_ADAPTER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("pan_adapter", () -> BlockEntityType.Builder.of(PanAdapterBlockEntity::new,
                    PAN_ADAPTER_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PanCoreBlockEntity>> PAN_CORE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("pan_core", () -> BlockEntityType.Builder.of(PanCoreBlockEntity::new, PAN_CORE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PanDuplicatorBlockEntity>> PAN_DUPLICATOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("pan_duplicator", () -> BlockEntityType.Builder.of(PanDuplicatorBlockEntity::new,
                    PAN_DUPLICATOR_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MetalChestBlockEntity>> METAL_CHEST_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("metal_chest", () -> BlockEntityType.Builder.of(MetalChestBlockEntity::new,
                    METAL_CHEST_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<ClayWorkTableMenu>> CLAY_WORK_TABLE_MENU =
            MENU_TYPES.register(
                    "clay_work_table",
                    () -> new MenuType<>(ClayWorkTableMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<ClayCraftingTableMenu>> CLAY_CRAFTING_TABLE_MENU =
            MENU_TYPES.register(
                    "clay_crafting_table",
                    () -> IMenuTypeExtension.create((id, inventory, data) ->
                            new ClayCraftingTableMenu(id, inventory, data.readBoolean())));
    public static final DeferredHolder<MenuType<?>, MenuType<CobblestoneGeneratorMenu>> COBBLESTONE_GENERATOR_MENU =
            MENU_TYPES.register("cobblestone_generator", () -> IMenuTypeExtension.create(CobblestoneGeneratorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> MACHINE_MENU =
            MENU_TYPES.register(
                    "machine",
                    () -> new MenuType<>(MachineMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> ASSEMBLER_MACHINE_MENU =
            MENU_TYPES.register(
                    "assembler_machine",
                    () -> new MenuType<>(MachineMenu::assembler, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> CHEMICAL_MACHINE_MENU =
            MENU_TYPES.register(
                    "chemical_machine",
                    () -> new MenuType<>(MachineMenu::chemical, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> CENTRIFUGE_MACHINE_MENU_1 =
            MENU_TYPES.register(
                    "centrifuge_machine_1",
                    () -> new MenuType<>(MachineMenu::centrifugeTier3, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> CENTRIFUGE_MACHINE_MENU_2 =
            MENU_TYPES.register(
                    "centrifuge_machine_2",
                    () -> new MenuType<>(MachineMenu::centrifugeTier4, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> CENTRIFUGE_MACHINE_MENU_3 =
            MENU_TYPES.register(
                    "centrifuge_machine_3",
                    () -> new MenuType<>(MachineMenu::centrifugeTier5, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> CENTRIFUGE_MACHINE_MENU_4 =
            MENU_TYPES.register(
                    "centrifuge_machine_4",
                    () -> new MenuType<>(MachineMenu::centrifugeTier6, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<LogisticsMenu>> LOGISTICS_MENU =
            MENU_TYPES.register(
                    "logistics",
                    () -> IMenuTypeExtension.create(LogisticsMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ItemFilterMenu>> ITEM_FILTER_MENU =
            MENU_TYPES.register(
                    "item_filter",
                    () -> new MenuType<>(ItemFilterMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<FluidBufferMenu>> FLUID_BUFFER_MENU =
            MENU_TYPES.register("fluid_buffer", () -> IMenuTypeExtension.create(FluidBufferMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<SaltExtractorMenu>> SALT_EXTRACTOR_MENU =
            MENU_TYPES.register("salt_extractor", () -> IMenuTypeExtension.create(SaltExtractorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AutoClayCondenserMenu>> AUTO_CLAY_CONDENSER_MENU =
            MENU_TYPES.register("auto_clay_condenser", () -> IMenuTypeExtension.create(AutoClayCondenserMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AutoCrafterMenu>> AUTO_CRAFTER_MENU =
            MENU_TYPES.register("auto_crafter", () -> IMenuTypeExtension.create(AutoCrafterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ChemicalMetalSeparatorMenu>> CHEMICAL_METAL_SEPARATOR_MENU =
            MENU_TYPES.register("chemical_metal_separator", () -> IMenuTypeExtension.create(ChemicalMetalSeparatorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ClayEnergyLaserMenu>> CLAY_ENERGY_LASER_MENU =
            MENU_TYPES.register("clay_energy_laser", () -> IMenuTypeExtension.create(ClayEnergyLaserMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ClayEnergyConverterMenu>> CLAY_ENERGY_CONVERTER_MENU =
            MENU_TYPES.register("clay_energy_converter", () -> IMenuTypeExtension.create(ClayEnergyConverterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ResonatingCollectorMenu>> RESONATING_COLLECTOR_MENU =
            MENU_TYPES.register("resonating_collector", () -> IMenuTypeExtension.create(ResonatingCollectorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PanAdapterMenu>> PAN_ADAPTER_MENU =
            MENU_TYPES.register("pan_adapter", () -> IMenuTypeExtension.create(PanAdapterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PanCoreMenu>> PAN_CORE_MENU =
            MENU_TYPES.register("pan_core", () -> IMenuTypeExtension.create(PanCoreMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PanDuplicatorMenu>> PAN_DUPLICATOR_MENU =
            MENU_TYPES.register("pan_duplicator", () -> IMenuTypeExtension.create(PanDuplicatorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MetalChestMenu>> METAL_CHEST_MENU =
            MENU_TYPES.register("metal_chest", () -> IMenuTypeExtension.create(MetalChestMenu::new));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CLAYIUM_CREATIVE_TAB =
            CREATIVE_MODE_TABS.register(
                    "clayium",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + Clayium.MODID))
                            .icon(() -> new ItemStack(CLAY_WORK_TABLE_ITEM.get()))
                            .displayItems((parameters, output) ->
                                    ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                            .build());

    private ClayiumRegistries() {
    }

    private static DeferredBlock<Block> registerBlock(String name, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new Block(properties));
    }

    private static Map<String, DeferredBlock<MachineBlock>> registerManufacturingMachineBlocks() {
        Map<String, DeferredBlock<MachineBlock>> blocks = new LinkedHashMap<>();
        for (ManufacturingMachineCatalog.Entry entry : ManufacturingMachineCatalog.ENTRIES) {
            blocks.put(
                    entry.blockId(),
                    BLOCKS.register(
                            entry.blockId(),
                            () -> new MachineBlock(machineProperties(), entry.machineId(), entry.tier())));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredItem<BlockItem>> registerManufacturingMachineItems() {
        Map<String, DeferredItem<BlockItem>> items = new LinkedHashMap<>();
        MANUFACTURING_MACHINE_BLOCKS.forEach((id, block) -> items.put(id, registerBlockItem(id, block)));
        return Map.copyOf(items);
    }

    private static Map<String, DeferredBlock<MachineBlock>> registerSpecializedMachineBlocks() {
        Map<String, DeferredBlock<MachineBlock>> blocks = new LinkedHashMap<>();
        for (SpecializedMachineCatalog.Entry entry : SpecializedMachineCatalog.ENTRIES) {
            blocks.put(entry.blockId(), BLOCKS.register(entry.blockId(),
                    () -> new MachineBlock(machineProperties(), entry.machineId(), entry.tier())));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<ClayEnergyLaserBlock>> registerClayEnergyLasers() {
        Map<String, DeferredBlock<ClayEnergyLaserBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.CLAY_STEEL, ClayTier.CLAYIUM, ClayTier.ULTIMATE, ClayTier.ANTIMATTER}) {
            String id = tier.id() + "_clay_energy_laser";
            blocks.put(id, BLOCKS.register(id, () -> new ClayEnergyLaserBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<ClayLaserInterfaceBlock>> registerClayLaserInterfaces() {
        Map<String, DeferredBlock<ClayLaserInterfaceBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.CLAY_STEEL, ClayTier.CLAYIUM, ClayTier.ULTIMATE,
                ClayTier.ANTIMATTER, ClayTier.PURE_ANTIMATTER, ClayTier.OEC, ClayTier.OPA}) {
            String id = tier.id() + "_clay_laser_interface";
            blocks.put(id, BLOCKS.register(id, () -> new ClayLaserInterfaceBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<ClayEnergyConverterBlock>> registerClayEnergyConverters() {
        Map<String, DeferredBlock<ClayEnergyConverterBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.BASIC, ClayTier.ADVANCED, ClayTier.PRECISION,
                ClayTier.CLAY_STEEL, ClayTier.CLAYIUM, ClayTier.ULTIMATE}) {
            String id = tier.id() + "_clay_energy_converter";
            blocks.put(id, BLOCKS.register(id, () -> new ClayEnergyConverterBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<MachineModifierBlock>> registerMachineModifiers() {
        Map<String, DeferredBlock<MachineModifierBlock>> blocks = new LinkedHashMap<>();
        String[] names = {"antimatter", "pure_antimatter", "oec", "opa"};
        double[] factors = {1.5D, 2.3D, 3.5D, 5.0D};
        int[] storage = {1, 3, 7, 63};
        for (int index = 0; index < names.length; index++) {
            ClayTier tier = ClayTier.byLegacyIndex(index + 10);
            String overclocker = names[index] + "_overclocker";
            double factor = factors[index];
            blocks.put(overclocker, BLOCKS.register(overclocker, () -> new MachineModifierBlock(
                    machineProperties(), MachineModifierBlock.Kind.OVERCLOCKER, tier, factor, 0)));
            String upgrade = names[index] + "_energy_storage_upgrade";
            int units = storage[index];
            blocks.put(upgrade, BLOCKS.register(upgrade, () -> new MachineModifierBlock(
                    machineProperties(), MachineModifierBlock.Kind.ENERGY_STORAGE, tier, 1.0D, units)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<ResonatorBlock>> registerResonators() {
        Map<String, DeferredBlock<ResonatorBlock>> blocks = new LinkedHashMap<>();
        String[] names = {"antimatter", "pure_antimatter", "oec", "opa"};
        double[] resonance = {1.08D, 1.10D, 2.0D, 20.0D};
        for (int index = 0; index < names.length; index++) {
            String id = names[index] + "_resonator";
            ClayTier tier = ClayTier.byLegacyIndex(index + 10);
            double factor = resonance[index];
            blocks.put(id, BLOCKS.register(id, () -> new ResonatorBlock(machineProperties(), tier, factor)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<CAReactorCoilBlock>> registerCAReactorCoils() {
        Map<String, DeferredBlock<CAReactorCoilBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.ANTIMATTER, ClayTier.PURE_ANTIMATTER, ClayTier.OEC, ClayTier.OPA}) {
            String id = tier.id() + "_ca_reactor_coil";
            blocks.put(id, BLOCKS.register(id,
                    () -> new CAReactorCoilBlock(machineProperties().strength(8.0F, 5.0F), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<CAReactorHullBlock>> registerCAReactorHulls() {
        Map<String, DeferredBlock<CAReactorHullBlock>> blocks = new LinkedHashMap<>();
        for (int rank = 1; rank <= 10; rank++) {
            String id = "ca_reactor_hull_rank_" + rank;
            int registeredRank = rank;
            blocks.put(id, BLOCKS.register(id,
                    () -> new CAReactorHullBlock(machineProperties().strength(4.0F, 25.0F), registeredRank)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<MachineBlock>> registerCAReactorCores() {
        Map<String, DeferredBlock<MachineBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.ANTIMATTER, ClayTier.PURE_ANTIMATTER, ClayTier.OEC, ClayTier.OPA}) {
            String id = tier.id() + "_ca_reactor_core";
            blocks.put(id, BLOCKS.register(id,
                    () -> new MachineBlock(machineProperties(), ClayiumMachineIds.CA_REACTOR, tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<PanAdapterBlock>> registerPanAdapters() {
        Map<String, DeferredBlock<PanAdapterBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.ANTIMATTER, ClayTier.PURE_ANTIMATTER, ClayTier.OEC, ClayTier.OPA}) {
            String id = tier.id() + "_pan_adapter";
            blocks.put(id, BLOCKS.register(id, () -> new PanAdapterBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<PanDuplicatorBlock>> registerPanDuplicators() {
        Map<String, DeferredBlock<PanDuplicatorBlock>> blocks = new LinkedHashMap<>();
        for (int tier = 4; tier <= 13; tier++) {
            ClayTier clayTier = ClayTier.byLegacyIndex(tier);
            String id = clayTier.id() + "_pan_duplicator";
            blocks.put(id, BLOCKS.register(id, () -> new PanDuplicatorBlock(machineProperties(), clayTier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<LogisticsBlock>> registerLogisticsBlocks() {
        Map<String, DeferredBlock<LogisticsBlock>> blocks = new LinkedHashMap<>();
        for (LogisticsCatalog.Entry entry : LogisticsCatalog.ENTRIES) {
            blocks.put(entry.blockId(), BLOCKS.register(
                    entry.blockId(),
                    () -> new LogisticsBlock(
                            machineProperties(), entry.kind(), entry.tier().progressionIndex())));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<MachineInterfaceBlock>> registerMachineInterfaces() {
        Map<String, DeferredBlock<MachineInterfaceBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.ADVANCED, ClayTier.PRECISION, ClayTier.CLAY_STEEL,
                ClayTier.CLAYIUM, ClayTier.ULTIMATE, ClayTier.ANTIMATTER, ClayTier.PURE_ANTIMATTER,
                ClayTier.OEC, ClayTier.OPA}) {
            String id = tier.id() + "_clay_interface";
            blocks.put(id, BLOCKS.register(id, () -> new MachineInterfaceBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<RedstoneInterfaceBlock>> registerRedstoneInterfaces() {
        Map<String, DeferredBlock<RedstoneInterfaceBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.ADVANCED, ClayTier.PRECISION, ClayTier.CLAY_STEEL,
                ClayTier.CLAYIUM, ClayTier.ULTIMATE, ClayTier.ANTIMATTER, ClayTier.PURE_ANTIMATTER,
                ClayTier.OEC, ClayTier.OPA}) {
            String id = tier.id() + "_redstone_interface";
            blocks.put(id, BLOCKS.register(id, () -> new RedstoneInterfaceBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<Block>> registerMaterialBlocks() {
        Map<String, DeferredBlock<Block>> blocks = new LinkedHashMap<>();
        for (String id : new String[]{"impure_silicon", "silicone", "silicon", "aluminium", "clay_steel",
                "clayium", "ultimate_alloy", "antimatter", "pure_antimatter", "opa"}) {
            blocks.put(id + "_block", registerBlock(
                    id + "_block",
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                            .requiresCorrectToolForDrops().strength(3.0F, 5.0F).sound(SoundType.METAL)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<DecorativeMetalBlock>> registerDecorativeMetalBlocks() {
        Map<String, DeferredBlock<DecorativeMetalBlock>> blocks = new LinkedHashMap<>();
        for (MetalStorageCatalog.Metal definition : MetalStorageCatalog.METALS) {
            blocks.put(definition.blockId(), BLOCKS.register(definition.blockId(), () -> new DecorativeMetalBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 2.0F)
                            .sound(SoundType.METAL), definition)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<MetalChestBlock>> registerMetalChests() {
        Map<String, DeferredBlock<MetalChestBlock>> blocks = new LinkedHashMap<>();
        for (MetalStorageCatalog.Chest definition : MetalStorageCatalog.CHESTS) {
            blocks.put(definition.blockId(), BLOCKS.register(definition.blockId(), () -> new MetalChestBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 2.0F)
                            .sound(SoundType.METAL).noOcclusion(), definition)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<ColoredSiliconeBlock>> registerColoredSiliconeBlocks() {
        Map<String, DeferredBlock<ColoredSiliconeBlock>> blocks = new LinkedHashMap<>();
        for (DyeColor color : DyeColor.values()) {
            String id = color.getSerializedName() + "_silicone_block";
            blocks.put(id, BLOCKS.register(id, () -> new ColoredSiliconeBlock(
                    color,
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                            .requiresCorrectToolForDrops().strength(3.0F, 5.0F).sound(SoundType.METAL))));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<Block>> registerOtherHullBlocks() {
        Map<String, DeferredBlock<Block>> blocks = new LinkedHashMap<>();
        for (String id : new String[]{"az91d_machine_hull", "zk60a_machine_hull"}) {
            blocks.put(id, registerBlock(id, machineProperties()));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<FluidBufferBlock>> registerFluidBuffers() {
        Map<String, DeferredBlock<FluidBufferBlock>> blocks=new LinkedHashMap<>();
        for(ClayTier tier : new ClayTier[]{ClayTier.BASIC,ClayTier.ADVANCED,ClayTier.PRECISION,ClayTier.CLAY_STEEL,
                ClayTier.CLAYIUM,ClayTier.ULTIMATE,ClayTier.ANTIMATTER,ClayTier.PURE_ANTIMATTER,ClayTier.OEC,ClayTier.OPA}) {
            String id=tier.id()+"_fluid_buffer";
            blocks.put(id,BLOCKS.register(id,()->new FluidBufferBlock(machineProperties(),tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<CobblestoneGeneratorBlock>> registerCobblestoneGenerators() {
        Map<String, DeferredBlock<CobblestoneGeneratorBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.CLAY, ClayTier.DENSE_CLAY, ClayTier.SIMPLE,
                ClayTier.BASIC, ClayTier.ADVANCED, ClayTier.PRECISION, ClayTier.CLAY_STEEL}) {
            String id = tier.id() + "_cobblestone_generator";
            blocks.put(id, BLOCKS.register(id, () -> new CobblestoneGeneratorBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<SaltExtractorBlock>> registerSaltExtractors() {
        Map<String, DeferredBlock<SaltExtractorBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.BASIC, ClayTier.ADVANCED, ClayTier.PRECISION, ClayTier.CLAY_STEEL}) {
            String id = tier.id() + "_salt_extractor";
            blocks.put(id, BLOCKS.register(id, () -> new SaltExtractorBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<AutoCrafterBlock>> registerAutoCrafters() {
        Map<String, DeferredBlock<AutoCrafterBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.ADVANCED, ClayTier.PRECISION, ClayTier.CLAY_STEEL,
                ClayTier.CLAYIUM, ClayTier.ULTIMATE}) {
            String id = tier.id() + "_auto_crafter";
            blocks.put(id, BLOCKS.register(id, () -> new AutoCrafterBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<Block>> registerCompressedClayBlocks() {
        String[] ids = {
            "industrial_clay",
            "advanced_industrial_clay",
            "energetic_clay",
            "compressed_energetic_clay",
            "double_compressed_energetic_clay",
            "triple_compressed_energetic_clay",
            "quadruple_compressed_energetic_clay",
            "quintuple_compressed_energetic_clay",
            "sextuple_compressed_energetic_clay",
            "septuple_compressed_energetic_clay",
            "octuple_compressed_energetic_clay"
        };
        Map<String, DeferredBlock<Block>> blocks = new LinkedHashMap<>();
        for (String id : ids) {
            blocks.put(id, registerBlock(
                    id,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.CLAY)
                            .strength(3.5F, 5.0F)
                            .sound(SoundType.STONE)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<Block>> registerMachineHullBlocks() {
        String[] ids = {
            "dense_clay_machine_hull",
            "simple_machine_hull",
            "basic_machine_hull",
            "advanced_machine_hull",
            "precision_machine_hull",
            "clay_steel_machine_hull",
            "clayium_machine_hull",
            "ultimate_machine_hull",
            "antimatter_machine_hull",
            "pure_antimatter_machine_hull",
            "oec_machine_hull",
            "opa_machine_hull"
        };
        Map<String, DeferredBlock<Block>> blocks = new LinkedHashMap<>();
        for (String id : ids) {
            blocks.put(id, registerBlock(id, machineProperties()));
        }
        return Map.copyOf(blocks);
    }

    private static <T extends Block> Map<String, DeferredItem<BlockItem>> registerBlockItems(
            Map<String, DeferredBlock<T>> blocks) {
        Map<String, DeferredItem<BlockItem>> items = new LinkedHashMap<>();
        blocks.forEach((id, block) -> items.put(id, registerBlockItem(id, block)));
        return Map.copyOf(items);
    }

    private static Map<String, DeferredItem<BlockItem>> registerLogisticsBlockItems() {
        Map<String, DeferredItem<BlockItem>> items = new LinkedHashMap<>();
        LOGISTICS_BLOCKS.forEach((id, block) -> {
            DeferredItem<BlockItem> item = ITEMS.register(id, () -> id.equals("storage_container")
                    ? new StorageContainerBlockItem(block.get(), new Item.Properties())
                    : new BlockItem(block.get(), new Item.Properties()));
            items.put(id, item);
        });
        return Map.copyOf(items);
    }

    private static Map<String, DeferredItem<Item>> registerComponentItems() {
        Map<String, DeferredItem<Item>> items = new LinkedHashMap<>();
        for (ClayComponentCatalog.Entry entry : ClayComponentCatalog.ENTRIES) {
            items.put(entry.id(), item(entry.id()));
        }
        return Map.copyOf(items);
    }

    private static Map<String, DeferredItem<Item>> registerMaterialItems() {
        Map<String, DeferredItem<Item>> items = new LinkedHashMap<>();
        for (MaterialCatalog.Entry entry : MaterialCatalog.ENTRIES) {
            items.put(entry.id(), item(entry.id()));
        }
        return Map.copyOf(items);
    }

    private static Block[] allMachineBlocks() {
        Block[] blocks = new Block[MANUFACTURING_MACHINE_BLOCKS.size() + SPECIALIZED_MACHINE_BLOCKS.size()
                + CA_REACTOR_CORE_BLOCKS.size() + 4];
        blocks[0] = CLAY_BENDING_MACHINE.get();
        blocks[1] = ELEMENTAL_MILLING_MACHINE.get();
        int index = 2;
        for (DeferredBlock<MachineBlock> block : MANUFACTURING_MACHINE_BLOCKS.values()) {
            blocks[index++] = block.get();
        }
        for (DeferredBlock<MachineBlock> block : SPECIALIZED_MACHINE_BLOCKS.values()) {
            blocks[index++] = block.get();
        }
        for (DeferredBlock<MachineBlock> block : CA_REACTOR_CORE_BLOCKS.values()) {
            blocks[index++] = block.get();
        }
        blocks[index++] = CLAY_BLAST_FURNACE.get();
        blocks[index] = CLAY_REACTOR.get();
        return blocks;
    }

    private static <T extends Block> DeferredItem<BlockItem> registerBlockItem(
            String name,
            DeferredBlock<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static DeferredItem<Item> item(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static DeferredItem<Item> rawTool(String name) {
        return ITEMS.register(name, () -> new RawClayCraftingToolItem(new Item.Properties()));
    }

    private static DeferredItem<ClayFilterItem> filter(String name, ClayFilterItem.Kind kind) {
        return ITEMS.register(
                name,
                () -> new ClayFilterItem(new Item.Properties()
                        .stacksTo(1)
                        .component(
                                ClayiumDataComponents.FILTER_SETTINGS.get(),
                                net.claustra01.clayium.data.FilterSettings.DEFAULT), kind));
    }

    private static DeferredItem<Item> durableItem(String name, int durability) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().durability(durability)));
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.CLAY)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 5.0F)
                .sound(SoundType.STONE);
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
