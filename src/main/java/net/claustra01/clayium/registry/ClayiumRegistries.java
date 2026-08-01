/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.registry;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.machine.Phase4MachineCatalog;
import net.claustra01.clayium.machine.Phase4ItemCatalog;
import net.claustra01.clayium.machine.Phase6MachineCatalog;
import net.claustra01.clayium.machine.Phase6ItemCatalog;
import net.claustra01.clayium.machine.SpecialMachineKind;
import net.claustra01.clayium.logistics.Phase5LogisticsCatalog;
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
import net.claustra01.clayium.world.level.block.CobblestoneGeneratorBlock;
import net.claustra01.clayium.world.level.block.MachineBlock;
import net.claustra01.clayium.world.level.block.LogisticsBlock;
import net.claustra01.clayium.world.level.block.WaterWheelBlock;
import net.claustra01.clayium.world.level.block.entity.ClayWorkTableBlockEntity;
import net.claustra01.clayium.world.level.block.entity.ClayCraftingTableBlockEntity;
import net.claustra01.clayium.world.level.block.entity.CobblestoneGeneratorBlockEntity;
import net.claustra01.clayium.world.level.block.entity.MachineBlockEntity;
import net.claustra01.clayium.world.level.block.entity.LogisticsBlockEntity;
import net.claustra01.clayium.world.item.ClayConfiguratorItem;
import net.claustra01.clayium.world.item.ClayCraftingToolItem;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.claustra01.clayium.world.item.ClayPickaxeItem;
import net.claustra01.clayium.world.item.ClayShovelItem;
import net.claustra01.clayium.world.item.ClayToolTier;
import net.claustra01.clayium.world.item.RawClayCraftingToolItem;
import net.claustra01.clayium.world.inventory.ItemFilterMenu;
import net.claustra01.clayium.world.inventory.FluidBufferMenu;
import net.claustra01.clayium.world.inventory.SaltExtractorMenu;
import net.claustra01.clayium.world.inventory.SpecialMachineMenu;
import net.claustra01.clayium.world.level.block.FluidBufferBlock;
import net.claustra01.clayium.world.level.block.SaltExtractorBlock;
import net.claustra01.clayium.world.level.block.QuartzCrucibleBlock;
import net.claustra01.clayium.world.level.block.SpecialMachineBlock;
import net.claustra01.clayium.world.level.block.entity.FluidBufferBlockEntity;
import net.claustra01.clayium.world.level.block.entity.SaltExtractorBlockEntity;
import net.claustra01.clayium.world.level.block.entity.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.world.level.block.entity.SpecialMachineBlockEntity;
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
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
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
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 5.0F)
                            .sound(SoundType.STONE)));
    public static final DeferredBlock<ClayOreBlock> DENSE_CLAY_ORE = BLOCKS.register(
            "dense_clay_ore",
            () -> new ClayOreBlock(
                    false,
                    UniformInt.of(0, 0),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 5.0F)
                            .sound(SoundType.STONE)));
    public static final DeferredBlock<ClayOreBlock> LARGE_DENSE_CLAY_ORE = BLOCKS.register(
            "large_dense_clay_ore",
            () -> new ClayOreBlock(
                    false,
                    UniformInt.of(0, 0),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 5.0F)
                            .sound(SoundType.STONE)));
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
    public static final Map<String, DeferredBlock<MachineBlock>> PHASE4_MACHINE_BLOCKS =
            registerPhase4MachineBlocks();
    public static final Map<String, DeferredBlock<MachineBlock>> PHASE6_MACHINE_BLOCKS =
            registerPhase6MachineBlocks();
    public static final Map<String, DeferredBlock<LogisticsBlock>> PHASE5_LOGISTICS_BLOCKS =
            registerPhase5LogisticsBlocks();
    public static final Map<String, DeferredBlock<FluidBufferBlock>> FLUID_BUFFER_BLOCKS = registerFluidBuffers();
    public static final Map<String, DeferredBlock<SaltExtractorBlock>> SALT_EXTRACTOR_BLOCKS = registerSaltExtractors();
    public static final DeferredBlock<QuartzCrucibleBlock> QUARTZ_CRUCIBLE = BLOCKS.registerBlock(
            "quartz_crucible", QuartzCrucibleBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(0.2F).sound(SoundType.GLASS));
    public static final Map<String, DeferredBlock<SpecialMachineBlock>> SPECIAL_MACHINE_BLOCKS =
            registerSpecialMachines();

    public static final DeferredItem<BlockItem> CLAY_WORK_TABLE_ITEM = ITEMS.register(
            "clay_work_table",
            () -> new BlockItem(CLAY_WORK_TABLE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> CLAY_CRAFTING_TABLE_ITEM =
            registerBlockItem("clay_crafting_table", CLAY_CRAFTING_TABLE);

    public static final DeferredItem<BlockItem> CLAY_ORE_ITEM = registerBlockItem("clay_ore", CLAY_ORE);
    public static final DeferredItem<BlockItem> DENSE_CLAY_ORE_ITEM = registerBlockItem("dense_clay_ore", DENSE_CLAY_ORE);
    public static final DeferredItem<BlockItem> LARGE_DENSE_CLAY_ORE_ITEM =
            registerBlockItem("large_dense_clay_ore", LARGE_DENSE_CLAY_ORE);
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
    public static final Map<String, DeferredItem<BlockItem>> PHASE4_MACHINE_ITEMS =
            registerPhase4MachineItems();
    public static final Map<String, DeferredItem<BlockItem>> PHASE6_MACHINE_ITEMS =
            registerBlockItems(PHASE6_MACHINE_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> PHASE5_LOGISTICS_ITEMS =
            registerBlockItems(PHASE5_LOGISTICS_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> FLUID_BUFFER_ITEMS = registerBlockItems(FLUID_BUFFER_BLOCKS);
    public static final Map<String, DeferredItem<BlockItem>> SALT_EXTRACTOR_ITEMS = registerBlockItems(SALT_EXTRACTOR_BLOCKS);
    public static final DeferredItem<BlockItem> QUARTZ_CRUCIBLE_ITEM = registerBlockItem("quartz_crucible", QUARTZ_CRUCIBLE);
    public static final Map<String, DeferredItem<BlockItem>> SPECIAL_MACHINE_ITEMS = registerBlockItems(SPECIAL_MACHINE_BLOCKS);

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
    public static final Map<String, DeferredItem<Item>> PHASE4_ITEMS = registerPhase4Items();
    public static final Map<String, DeferredItem<Item>> PHASE6_ITEMS = registerPhase6Items();
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
                            PHASE5_LOGISTICS_BLOCKS.values().stream()
                                    .map(DeferredBlock::get)
                                    .toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidBufferBlockEntity>> FLUID_BUFFER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("fluid_buffer", () -> BlockEntityType.Builder.of(
                    FluidBufferBlockEntity::new, FLUID_BUFFER_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SaltExtractorBlockEntity>> SALT_EXTRACTOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("salt_extractor", () -> BlockEntityType.Builder.of(
                    SaltExtractorBlockEntity::new, SALT_EXTRACTOR_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuartzCrucibleBlockEntity>> QUARTZ_CRUCIBLE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("quartz_crucible", () -> BlockEntityType.Builder.of(
                    QuartzCrucibleBlockEntity::new, QUARTZ_CRUCIBLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpecialMachineBlockEntity>> SPECIAL_MACHINE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("special_machine", () -> BlockEntityType.Builder.of(
                    SpecialMachineBlockEntity::new,
                    SPECIAL_MACHINE_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
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
    public static final DeferredHolder<MenuType<?>, MenuType<SpecialMachineMenu>> SPECIAL_MACHINE_MENU =
            MENU_TYPES.register("special_machine", () -> IMenuTypeExtension.create(SpecialMachineMenu::new));
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

    private static Map<String, DeferredBlock<MachineBlock>> registerPhase4MachineBlocks() {
        Map<String, DeferredBlock<MachineBlock>> blocks = new LinkedHashMap<>();
        for (Phase4MachineCatalog.Entry entry : Phase4MachineCatalog.ENTRIES) {
            blocks.put(
                    entry.blockId(),
                    BLOCKS.register(
                            entry.blockId(),
                            () -> new MachineBlock(machineProperties(), entry.machineId(), entry.tier())));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredItem<BlockItem>> registerPhase4MachineItems() {
        Map<String, DeferredItem<BlockItem>> items = new LinkedHashMap<>();
        PHASE4_MACHINE_BLOCKS.forEach((id, block) -> items.put(id, registerBlockItem(id, block)));
        return Map.copyOf(items);
    }

    private static Map<String, DeferredBlock<MachineBlock>> registerPhase6MachineBlocks() {
        Map<String, DeferredBlock<MachineBlock>> blocks = new LinkedHashMap<>();
        for (Phase6MachineCatalog.Entry entry : Phase6MachineCatalog.ENTRIES) {
            blocks.put(entry.blockId(), BLOCKS.register(entry.blockId(),
                    () -> new MachineBlock(machineProperties(), entry.machineId(), entry.tier())));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<LogisticsBlock>> registerPhase5LogisticsBlocks() {
        Map<String, DeferredBlock<LogisticsBlock>> blocks = new LinkedHashMap<>();
        for (Phase5LogisticsCatalog.Entry entry : Phase5LogisticsCatalog.ENTRIES) {
            blocks.put(entry.blockId(), BLOCKS.register(
                    entry.blockId(),
                    () -> new LogisticsBlock(
                            machineProperties(), entry.kind(), entry.tier().progressionIndex())));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<FluidBufferBlock>> registerFluidBuffers() {
        Map<String, DeferredBlock<FluidBufferBlock>> blocks=new LinkedHashMap<>();
        for(ClayTier tier : new ClayTier[]{ClayTier.BASIC,ClayTier.ADVANCED,ClayTier.PRECISION}) {
            String id=tier.id()+"_fluid_buffer";
            blocks.put(id,BLOCKS.register(id,()->new FluidBufferBlock(machineProperties(),tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<CobblestoneGeneratorBlock>> registerCobblestoneGenerators() {
        Map<String, DeferredBlock<CobblestoneGeneratorBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.CLAY, ClayTier.DENSE_CLAY, ClayTier.SIMPLE,
                ClayTier.BASIC, ClayTier.ADVANCED, ClayTier.PRECISION}) {
            String id = tier.id() + "_cobblestone_generator";
            blocks.put(id, BLOCKS.register(id, () -> new CobblestoneGeneratorBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<SaltExtractorBlock>> registerSaltExtractors() {
        Map<String, DeferredBlock<SaltExtractorBlock>> blocks = new LinkedHashMap<>();
        for (ClayTier tier : new ClayTier[]{ClayTier.BASIC, ClayTier.ADVANCED, ClayTier.PRECISION}) {
            String id = tier.id() + "_salt_extractor";
            blocks.put(id, BLOCKS.register(id, () -> new SaltExtractorBlock(machineProperties(), tier)));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredBlock<SpecialMachineBlock>> registerSpecialMachines() {
        Map<String, DeferredBlock<SpecialMachineBlock>> blocks = new LinkedHashMap<>();
        registerSpecial(blocks, "advanced_auto_clay_condenser", SpecialMachineKind.AUTO_CLAY_CONDENSER, ClayTier.ADVANCED);
        registerSpecial(blocks, "advanced_auto_crafter", SpecialMachineKind.AUTO_CRAFTER, ClayTier.ADVANCED);
        registerSpecial(blocks, "precision_auto_crafter", SpecialMachineKind.AUTO_CRAFTER, ClayTier.PRECISION);
        registerSpecial(blocks, "precision_chemical_metal_separator", SpecialMachineKind.CHEMICAL_METAL_SEPARATOR, ClayTier.PRECISION);
        return Map.copyOf(blocks);
    }

    private static void registerSpecial(Map<String, DeferredBlock<SpecialMachineBlock>> blocks, String id,
                                        SpecialMachineKind kind, ClayTier tier) {
        blocks.put(id, BLOCKS.register(id, () -> new SpecialMachineBlock(machineProperties(), kind, tier)));
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
            "precision_machine_hull"
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

    private static Map<String, DeferredItem<Item>> registerPhase4Items() {
        Map<String, DeferredItem<Item>> items = new LinkedHashMap<>();
        for (Phase4ItemCatalog.Entry entry : Phase4ItemCatalog.ENTRIES) {
            items.put(entry.id(), item(entry.id()));
        }
        return Map.copyOf(items);
    }

    private static Map<String, DeferredItem<Item>> registerPhase6Items() {
        Map<String, DeferredItem<Item>> items = new LinkedHashMap<>();
        for (Phase6ItemCatalog.Entry entry : Phase6ItemCatalog.ENTRIES) {
            items.put(entry.id(), item(entry.id()));
        }
        return Map.copyOf(items);
    }

    private static Block[] allMachineBlocks() {
        Block[] blocks = new Block[PHASE4_MACHINE_BLOCKS.size() + PHASE6_MACHINE_BLOCKS.size() + 2];
        blocks[0] = CLAY_BENDING_MACHINE.get();
        blocks[1] = ELEMENTAL_MILLING_MACHINE.get();
        int index = 2;
        for (DeferredBlock<MachineBlock> block : PHASE4_MACHINE_BLOCKS.values()) {
            blocks[index++] = block.get();
        }
        for (DeferredBlock<MachineBlock> block : PHASE6_MACHINE_BLOCKS.values()) {
            blocks[index++] = block.get();
        }
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
