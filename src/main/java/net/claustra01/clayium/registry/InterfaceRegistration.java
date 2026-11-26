package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.machine.iface.InterfaceBlock;
import net.claustra01.clayium.machine.iface.InterfaceBlockEntity;
import net.claustra01.clayium.machine.iface.InterfaceType;
import net.claustra01.clayium.machine.iface.SynchronizedTarget;
import net.claustra01.clayium.tool.Synchronizer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class InterfaceRegistration {
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SynchronizedTarget>> SYNCHRONIZED_TARGET =
            ClayiumRegistries.DATA_COMPONENT_TYPES.registerComponentType(
                    "synchronized_target",
                    builder -> builder.persistent(SynchronizedTarget.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(SynchronizedTarget.CODEC)));

    public static final Map<String, InterfaceDefinition> INTERFACES;
    public static final List<DeferredBlock<InterfaceBlock>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<String, InterfaceDefinition> interfaces = new LinkedHashMap<>();
        List<DeferredBlock<InterfaceBlock>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        register(interfaces, blocks, items, InterfaceType.ITEM, 5, 13);
        register(interfaces, blocks, items, InterfaceType.REDSTONE, 5, 13);
        register(interfaces, blocks, items, InterfaceType.LASER, 7, 13);
        INTERFACES = Collections.unmodifiableMap(interfaces);
        BLOCKS = List.copyOf(blocks);
        ITEMS = List.copyOf(items);
    }

    public static final DeferredItem<Synchronizer> SYNCHRONIZER = ClayiumRegistries.ITEMS.register(
            "synchronizer",
            () -> new Synchronizer(new Item.Properties().stacksTo(1)
                    .component(FoundationDataComponentRegistration.TIER.get(), 5)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InterfaceBlockEntity>> BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "interface",
                    () -> BlockEntityType.Builder.of(
                            InterfaceBlockEntity::new,
                            BLOCKS.stream().map(DeferredBlock::get)
                                    .toArray(net.minecraft.world.level.block.Block[]::new)).build(null));

    private static void register(
            Map<String, InterfaceDefinition> interfaces,
            List<DeferredBlock<InterfaceBlock>> blocks,
            List<DeferredItem<FoundationBlockItem>> items,
            InterfaceType type,
            int minimumTier,
            int maximumTier) {
        for (int tier = minimumTier; tier <= maximumTier; tier++) {
            int interfaceTier = tier;
            String id = type.idStem() + "_tier_" + tier;
            DeferredBlock<InterfaceBlock> block = ClayiumRegistries.BLOCKS.register(
                    id,
                    () -> new InterfaceBlock(
                            BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                                    .strength(3.0F + interfaceTier * 0.25F, 5.0F + interfaceTier)
                                    .sound(SoundType.STONE),
                            type,
                            interfaceTier));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    id,
                    () -> new FoundationBlockItem(
                            block.get(),
                            new Item.Properties().component(
                                    FoundationDataComponentRegistration.TIER.get(), interfaceTier)));
            interfaces.put(id, new InterfaceDefinition(interfaceTier, id, type, block, item));
            blocks.add(block);
            items.add(item);
        }
    }

    public record InterfaceDefinition(
            int tier,
            String id,
            InterfaceType type,
            DeferredBlock<InterfaceBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    private InterfaceRegistration() {
    }
}
