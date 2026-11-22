package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.logistics.BufferBlock;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.BufferConfiguration;
import net.claustra01.clayium.logistics.BufferMenu;
import net.claustra01.clayium.logistics.filter.ItemFilterSettings;
import net.claustra01.clayium.logistics.filter.FilterMenu;
import net.claustra01.clayium.logistics.filter.FilterDuplicatorItem;
import net.claustra01.clayium.logistics.filter.UnifiedItemFilterItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class LogisticsRegistration {
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemFilterSettings>> ITEM_FILTER_SETTINGS =
            ClayiumRegistries.DATA_COMPONENT_TYPES.registerComponentType(
                    "item_filter",
                    builder -> builder.persistent(ItemFilterSettings.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(ItemFilterSettings.CODEC)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BufferConfiguration>> BUFFER_CONFIGURATION =
            ClayiumRegistries.DATA_COMPONENT_TYPES.registerComponentType(
                    "buffer_configuration",
                    builder -> builder.persistent(BufferConfiguration.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(BufferConfiguration.CODEC)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FILTER_COPY =
            ClayiumRegistries.DATA_COMPONENT_TYPES.registerComponentType(
                    "filter_copy",
                    builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Map<Integer, BufferDefinition> BUFFERS;
    public static final List<DeferredBlock<BufferBlock>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> BLOCK_ITEMS;

    static {
        Map<Integer, BufferDefinition> buffers = new LinkedHashMap<>();
        List<DeferredBlock<BufferBlock>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        for (int tier = 4; tier <= 13; tier++) {
            int bufferTier = tier;
            String id = "buffer_tier_" + tier;
            DeferredBlock<BufferBlock> block = ClayiumRegistries.BLOCKS.register(
                    id,
                    () -> new BufferBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(3.0F + bufferTier * 0.25F, 5.0F + bufferTier)
                                    .sound(SoundType.STONE),
                            bufferTier));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    id,
                    () -> new FoundationBlockItem(
                            block.get(), new Item.Properties().component(
                                    FoundationDataComponentRegistration.TIER.get(), bufferTier)));
            buffers.put(bufferTier, new BufferDefinition(bufferTier, id, block, item));
            blocks.add(block);
            items.add(item);
        }
        BUFFERS = Collections.unmodifiableMap(buffers);
        BLOCKS = List.copyOf(blocks);
        BLOCK_ITEMS = List.copyOf(items);
    }

    public static final DeferredItem<UnifiedItemFilterItem> ITEM_FILTER = ClayiumRegistries.ITEMS.register(
            "white_list_item_filter",
            () -> new UnifiedItemFilterItem(new Item.Properties().stacksTo(1)
                    .component(ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.ALLOW_ALL)));
    public static final DeferredItem<FilterDuplicatorItem> FILTER_DUPLICATOR = ClayiumRegistries.ITEMS.register(
            "item_filter_duplicator",
            () -> new FilterDuplicatorItem(new Item.Properties().stacksTo(1)
                    .component(FoundationDataComponentRegistration.TIER.get(), 7)
                    .component(ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.ALLOW_ALL)
                    .component(FILTER_COPY.get(), false)));
    public static final List<DeferredItem<? extends Item>> ITEMS = List.of(ITEM_FILTER, FILTER_DUPLICATOR);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BufferBlockEntity>> BUFFER_BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "buffer",
                    () -> BlockEntityType.Builder.of(
                                    BufferBlockEntity::new,
                                    BLOCKS.stream().map(DeferredBlock::get)
                                            .toArray(net.minecraft.world.level.block.Block[]::new))
                            .build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<BufferMenu>> BUFFER_MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "buffer",
                    () -> IMenuTypeExtension.create((containerId, inventory, data) ->
                            new BufferMenu(containerId, inventory, data.readBlockPos())));

    public static final DeferredHolder<MenuType<?>, MenuType<FilterMenu>> FILTER_MENU =
            ClayiumRegistries.MENU_TYPES.register("item_filter", () -> new MenuType<>(FilterMenu::new,
                    net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

    public record BufferDefinition(
            int tier,
            String id,
            DeferredBlock<BufferBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    private LogisticsRegistration() {
    }
}
