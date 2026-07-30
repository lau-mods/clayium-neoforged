/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.registry;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.world.inventory.ClayWorkTableMenu;
import net.claustra01.clayium.world.inventory.MachineMenu;
import net.claustra01.clayium.world.level.block.ClayWorkTableBlock;
import net.claustra01.clayium.world.level.block.MachineBlock;
import net.claustra01.clayium.world.level.block.WaterWheelBlock;
import net.claustra01.clayium.world.level.block.entity.ClayWorkTableBlockEntity;
import net.claustra01.clayium.world.level.block.entity.MachineBlockEntity;
import net.claustra01.clayium.world.level.block.entity.WaterWheelBlockEntity;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Clayium.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Clayium.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Clayium.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, Clayium.MODID);

    public static final DeferredBlock<ClayWorkTableBlock> CLAY_WORK_TABLE = BLOCKS.registerBlock(
            "clay_work_table",
            ClayWorkTableBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.CLAY)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 2.0F)
                    .sound(SoundType.STONE));

    public static final DeferredBlock<Block> CLAY_ORE = registerBlock(
            "clay_ore", BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 5.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> DENSE_CLAY_ORE = registerBlock(
            "dense_clay_ore", BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4.0F, 6.0F).sound(SoundType.DEEPSLATE));
    public static final DeferredBlock<Block> LARGE_DENSE_CLAY_ORE = registerBlock(
            "large_dense_clay_ore", BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(5.0F, 7.0F).sound(SoundType.DEEPSLATE));
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

    public static final DeferredBlock<MachineBlock> CLAY_BENDING_MACHINE = BLOCKS.register(
            "clay_bending_machine",
            () -> new MachineBlock(machineProperties(), ClayiumMachineIds.CLAY_BENDING_MACHINE, ClayTier.CLAY));
    public static final DeferredBlock<MachineBlock> ELEMENTAL_MILLING_MACHINE = BLOCKS.register(
            "elemental_milling_machine",
            () -> new MachineBlock(machineProperties(), ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE, ClayTier.CLAY));
    public static final DeferredBlock<WaterWheelBlock> CLAY_WATER_WHEEL = BLOCKS.register(
            "clay_water_wheel",
            () -> new WaterWheelBlock(machineProperties()));

    public static final DeferredItem<BlockItem> CLAY_WORK_TABLE_ITEM = ITEMS.register(
            "clay_work_table",
            () -> new BlockItem(CLAY_WORK_TABLE.get(), new Item.Properties()));

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
    public static final DeferredItem<BlockItem> CLAY_BENDING_MACHINE_ITEM =
            registerBlockItem("clay_bending_machine", CLAY_BENDING_MACHINE);
    public static final DeferredItem<BlockItem> ELEMENTAL_MILLING_MACHINE_ITEM =
            registerBlockItem("elemental_milling_machine", ELEMENTAL_MILLING_MACHINE);
    public static final DeferredItem<BlockItem> CLAY_WATER_WHEEL_ITEM =
            registerBlockItem("clay_water_wheel", CLAY_WATER_WHEEL);

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
    public static final DeferredItem<Item> CLAY_WHEEL = item("clay_wheel");
    public static final DeferredItem<Item> DENSE_CLAY_PLATE = item("dense_clay_plate");
    public static final DeferredItem<Item> DENSE_CLAY_STICK = item("dense_clay_stick");
    public static final DeferredItem<Item> DENSE_CLAY_GEAR = item("dense_clay_gear");
    public static final DeferredItem<Item> CLAY_CIRCUIT_BOARD = item("clay_circuit_board");
    public static final DeferredItem<Item> RAW_CLAY_ROLLING_PIN = item("raw_clay_rolling_pin");
    public static final DeferredItem<Item> RAW_CLAY_SLICER = item("raw_clay_slicer");
    public static final DeferredItem<Item> RAW_CLAY_SPATULA = item("raw_clay_spatula");
    public static final DeferredItem<Item> CLAY_ROLLING_PIN = durableItem("clay_rolling_pin", 60);
    public static final DeferredItem<Item> CLAY_SLICER = durableItem("clay_slicer", 60);
    public static final DeferredItem<Item> CLAY_SPATULA = durableItem("clay_spatula", 36);
    public static final DeferredItem<ShovelItem> CLAY_SHOVEL = ITEMS.register(
            "clay_shovel",
            () -> new ShovelItem(
                    Tiers.WOOD,
                    new Item.Properties().attributes(ShovelItem.createAttributes(Tiers.WOOD, 1.5F, -3.0F))));
    public static final DeferredItem<PickaxeItem> CLAY_PICKAXE = ITEMS.register(
            "clay_pickaxe",
            () -> new PickaxeItem(
                    Tiers.STONE,
                    new Item.Properties().attributes(PickaxeItem.createAttributes(Tiers.STONE, 1.0F, -2.8F))));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayWorkTableBlockEntity>>
            CLAY_WORK_TABLE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "clay_work_table",
                    () -> BlockEntityType.Builder.of(
                            ClayWorkTableBlockEntity::new,
                            CLAY_WORK_TABLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineBlockEntity>>
            MACHINE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "machine",
                    () -> BlockEntityType.Builder.of(
                            MachineBlockEntity::new,
                            CLAY_BENDING_MACHINE.get(),
                            ELEMENTAL_MILLING_MACHINE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterWheelBlockEntity>>
            WATER_WHEEL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "clay_water_wheel",
                    () -> BlockEntityType.Builder.of(
                            WaterWheelBlockEntity::new,
                            CLAY_WATER_WHEEL.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<ClayWorkTableMenu>> CLAY_WORK_TABLE_MENU =
            MENU_TYPES.register(
                    "clay_work_table",
                    () -> new MenuType<>(ClayWorkTableMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> MACHINE_MENU =
            MENU_TYPES.register(
                    "machine",
                    () -> new MenuType<>(MachineMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ClayiumRegistries() {
    }

    private static DeferredBlock<Block> registerBlock(String name, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new Block(properties));
    }

    private static <T extends Block> DeferredItem<BlockItem> registerBlockItem(
            String name,
            DeferredBlock<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static DeferredItem<Item> item(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
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
    }
}
