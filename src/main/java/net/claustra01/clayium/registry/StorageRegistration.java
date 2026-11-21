package net.claustra01.clayium.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.storage.MetalChestBlock;
import net.claustra01.clayium.storage.MetalChestBlockEntity;
import net.claustra01.clayium.storage.MetalChestItem;
import net.claustra01.clayium.storage.MetalChestMenu;
import net.claustra01.clayium.storage.MetalChestType;
import net.claustra01.clayium.storage.StorageContainerBlock;
import net.claustra01.clayium.storage.StorageContainerBlockEntity;
import net.claustra01.clayium.storage.StorageContainerMenu;
import net.claustra01.clayium.storage.VoidContainerBlock;
import net.claustra01.clayium.storage.VoidContainerBlockEntity;
import net.claustra01.clayium.storage.VoidContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class StorageRegistration {
    public static final DeferredBlock<StorageContainerBlock> STORAGE_CONTAINER = ClayiumRegistries.BLOCKS.register(
            "storage_container", () -> new StorageContainerBlock(containerProperties()));
    public static final DeferredItem<FoundationBlockItem> STORAGE_CONTAINER_ITEM = ClayiumRegistries.ITEMS.register(
            "storage_container", () -> new FoundationBlockItem(STORAGE_CONTAINER.get(), new Item.Properties()));
    public static final DeferredBlock<VoidContainerBlock> VOID_CONTAINER = ClayiumRegistries.BLOCKS.register(
            "void_container", () -> new VoidContainerBlock(containerProperties()));
    public static final DeferredItem<FoundationBlockItem> VOID_CONTAINER_ITEM = ClayiumRegistries.ITEMS.register(
            "void_container", () -> new FoundationBlockItem(VOID_CONTAINER.get(), new Item.Properties()));

    public static final Map<String, MetalChestDefinition> METAL_CHESTS;
    public static final List<DeferredBlock<? extends net.minecraft.world.level.block.Block>> BLOCKS;
    public static final List<DeferredItem<? extends Item>> ITEMS;

    static {
        Map<String, MetalChestDefinition> definitions = new LinkedHashMap<>();
        java.util.ArrayList<DeferredBlock<? extends net.minecraft.world.level.block.Block>> blocks =
                new java.util.ArrayList<>(List.of(STORAGE_CONTAINER, VOID_CONTAINER));
        java.util.ArrayList<DeferredItem<? extends Item>> items =
                new java.util.ArrayList<>(List.of(STORAGE_CONTAINER_ITEM, VOID_CONTAINER_ITEM));
        for (MetalChestType type : MetalChestType.ALL) {
            DeferredBlock<MetalChestBlock> block = ClayiumRegistries.BLOCKS.register(
                    type.id(), () -> new MetalChestBlock(containerProperties().noOcclusion(), type));
            DeferredItem<MetalChestItem> item = ClayiumRegistries.ITEMS.register(
                    type.id(), () -> new MetalChestItem(block.get(), new Item.Properties()));
            definitions.put(type.id(), new MetalChestDefinition(type, block, item));
            blocks.add(block);
            items.add(item);
        }
        METAL_CHESTS = Collections.unmodifiableMap(definitions);
        BLOCKS = List.copyOf(blocks);
        ITEMS = List.copyOf(items);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StorageContainerBlockEntity>>
            STORAGE_CONTAINER_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "storage_container", () -> BlockEntityType.Builder.of(
                            StorageContainerBlockEntity::new, STORAGE_CONTAINER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidContainerBlockEntity>>
            VOID_CONTAINER_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "void_container", () -> BlockEntityType.Builder.of(
                            VoidContainerBlockEntity::new, VOID_CONTAINER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MetalChestBlockEntity>>
            METAL_CHEST_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "metal_chest", () -> BlockEntityType.Builder.of(
                            MetalChestBlockEntity::new,
                            METAL_CHESTS.values().stream().map(definition -> definition.block().get())
                                    .toArray(net.minecraft.world.level.block.Block[]::new)).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<StorageContainerMenu>> STORAGE_CONTAINER_MENU =
            ClayiumRegistries.MENU_TYPES.register("storage_container", () -> IMenuTypeExtension.create(
                    (containerId, inventory, data) -> new StorageContainerMenu(
                            containerId, inventory, data.readBlockPos())));
    public static final DeferredHolder<MenuType<?>, MenuType<VoidContainerMenu>> VOID_CONTAINER_MENU =
            ClayiumRegistries.MENU_TYPES.register("void_container", () -> IMenuTypeExtension.create(
                    (containerId, inventory, data) -> new VoidContainerMenu(
                            containerId, inventory, data.readBlockPos())));
    public static final DeferredHolder<MenuType<?>, MenuType<MetalChestMenu>> METAL_CHEST_MENU =
            ClayiumRegistries.MENU_TYPES.register("metal_chest", () -> IMenuTypeExtension.create(
                    (containerId, inventory, data) -> new MetalChestMenu(
                            containerId, inventory, data.readBlockPos())));

    public record MetalChestDefinition(
            MetalChestType type,
            DeferredBlock<MetalChestBlock> block,
            DeferredItem<MetalChestItem> item) {
    }

    private static BlockBehaviour.Properties containerProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(4.0F, 8.0F).sound(SoundType.METAL);
    }

    private StorageRegistration() {
    }
}
