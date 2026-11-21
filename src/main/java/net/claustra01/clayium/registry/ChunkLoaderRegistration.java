package net.claustra01.clayium.registry;

import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.world.chunkloading.ChunkLoaderBlock;
import net.claustra01.clayium.world.chunkloading.ChunkLoaderBlockEntity;
import net.claustra01.clayium.world.chunkloading.ChunkLoaderMenu;
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

public final class ChunkLoaderRegistration {
    public static final int TIER = 6;
    public static final String ID = "clay_3x3_chunk_loader";

    public static final DeferredBlock<ChunkLoaderBlock> CHUNK_LOADER = ClayiumRegistries.BLOCKS.register(
            ID,
            () -> new ChunkLoaderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(4.5F, 11.0F)
                    .sound(SoundType.STONE)));
    public static final DeferredItem<FoundationBlockItem> CHUNK_LOADER_ITEM = ClayiumRegistries.ITEMS.register(
            ID,
            () -> new FoundationBlockItem(
                    CHUNK_LOADER.get(),
                    new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), TIER)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChunkLoaderBlockEntity>> BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "chunk_loader",
                    () -> BlockEntityType.Builder.of(ChunkLoaderBlockEntity::new, CHUNK_LOADER.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ChunkLoaderMenu>> MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "chunk_loader",
                    () -> IMenuTypeExtension.create((containerId, inventory, data) ->
                            new ChunkLoaderMenu(containerId, inventory, data.readBlockPos())));

    private ChunkLoaderRegistration() {
    }
}
