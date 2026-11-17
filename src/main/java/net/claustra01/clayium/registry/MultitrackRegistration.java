package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.logistics.distributor.DistributorBlock;
import net.claustra01.clayium.logistics.distributor.DistributorBlockEntity;
import net.claustra01.clayium.logistics.distributor.DistributorMenu;
import net.claustra01.clayium.logistics.multitrack.MultitrackBufferBlock;
import net.claustra01.clayium.logistics.multitrack.MultitrackBufferBlockEntity;
import net.claustra01.clayium.logistics.multitrack.MultitrackBufferMenu;
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

public final class MultitrackRegistration {
    public static final Map<Integer, MultitrackDefinition> MULTITRACK_BUFFERS;
    public static final Map<Integer, DistributorDefinition> DISTRIBUTORS;
    public static final List<DeferredBlock<?>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> BLOCK_ITEMS;

    static {
        Map<Integer, MultitrackDefinition> multitracks = new LinkedHashMap<>();
        Map<Integer, DistributorDefinition> distributors = new LinkedHashMap<>();
        List<DeferredBlock<?>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        for (int tier = 4; tier <= 13; tier++) {
            int registeredTier = tier;
            String id = "multitrack_buffer_tier_" + tier;
            DeferredBlock<MultitrackBufferBlock> block = ClayiumRegistries.BLOCKS.register(
                    id, () -> new MultitrackBufferBlock(properties(registeredTier), registeredTier));
            DeferredItem<FoundationBlockItem> item = blockItem(id, block, registeredTier);
            multitracks.put(tier, new MultitrackDefinition(tier, id, block, item));
            blocks.add(block);
            items.add(item);
        }
        for (int tier = 7; tier <= 9; tier++) {
            int registeredTier = tier;
            String id = "distributor_tier_" + tier;
            DeferredBlock<DistributorBlock> block = ClayiumRegistries.BLOCKS.register(
                    id, () -> new DistributorBlock(properties(registeredTier), registeredTier));
            DeferredItem<FoundationBlockItem> item = blockItem(id, block, registeredTier);
            distributors.put(tier, new DistributorDefinition(tier, id, block, item));
            blocks.add(block);
            items.add(item);
        }
        MULTITRACK_BUFFERS = Collections.unmodifiableMap(multitracks);
        DISTRIBUTORS = Collections.unmodifiableMap(distributors);
        BLOCKS = List.copyOf(blocks);
        BLOCK_ITEMS = List.copyOf(items);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultitrackBufferBlockEntity>>
            MULTITRACK_BUFFER_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "multitrack_buffer",
                    () -> BlockEntityType.Builder.of(
                                    MultitrackBufferBlockEntity::new,
                                    MULTITRACK_BUFFERS.values().stream().map(definition -> definition.block().get())
                                            .toArray(net.minecraft.world.level.block.Block[]::new))
                            .build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DistributorBlockEntity>>
            DISTRIBUTOR_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "distributor",
                    () -> BlockEntityType.Builder.of(
                                    DistributorBlockEntity::new,
                                    DISTRIBUTORS.values().stream().map(definition -> definition.block().get())
                                            .toArray(net.minecraft.world.level.block.Block[]::new))
                            .build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<MultitrackBufferMenu>> MULTITRACK_BUFFER_MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "multitrack_buffer",
                    () -> IMenuTypeExtension.create((id, inventory, data) ->
                            new MultitrackBufferMenu(id, inventory, data.readBlockPos())));
    public static final DeferredHolder<MenuType<?>, MenuType<DistributorMenu>> DISTRIBUTOR_MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "distributor",
                    () -> IMenuTypeExtension.create((id, inventory, data) ->
                            new DistributorMenu(id, inventory, data.readBlockPos())));

    private static BlockBehaviour.Properties properties(int tier) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                .strength(3.0F + tier * 0.25F, 5.0F + tier).sound(SoundType.STONE);
    }

    private static <T extends net.minecraft.world.level.block.Block> DeferredItem<FoundationBlockItem> blockItem(
            String id, DeferredBlock<T> block, int tier) {
        return ClayiumRegistries.ITEMS.register(id, () -> new FoundationBlockItem(
                block.get(), new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier)));
    }

    public record MultitrackDefinition(int tier, String id, DeferredBlock<MultitrackBufferBlock> block,
                                       DeferredItem<FoundationBlockItem> item) {
    }

    public record DistributorDefinition(int tier, String id, DeferredBlock<DistributorBlock> block,
                                        DeferredItem<FoundationBlockItem> item) {
    }

    private MultitrackRegistration() {
    }
}
