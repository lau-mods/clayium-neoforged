/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.registry;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.ClayWorkTableMenu;
import net.claustra01.clayium.world.level.block.ClayWorkTableBlock;
import net.claustra01.clayium.world.level.block.entity.ClayWorkTableBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Blocks;
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
            BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE));
    public static final DeferredItem<BlockItem> CLAY_WORK_TABLE_ITEM = ITEMS.register(
            "clay_work_table",
            () -> new BlockItem(CLAY_WORK_TABLE.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayWorkTableBlockEntity>>
            CLAY_WORK_TABLE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "clay_work_table",
                    () -> BlockEntityType.Builder.of(
                            ClayWorkTableBlockEntity::new,
                            CLAY_WORK_TABLE.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<ClayWorkTableMenu>> CLAY_WORK_TABLE_MENU =
            MENU_TYPES.register(
                    "clay_work_table",
                    () -> new MenuType<>(ClayWorkTableMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ClayiumRegistries() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
    }
}
