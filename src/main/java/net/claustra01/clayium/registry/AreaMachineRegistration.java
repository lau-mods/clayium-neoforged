package net.claustra01.clayium.registry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.world.area.AreaMachineBlock;
import net.claustra01.clayium.world.area.AreaMachineBlockEntity;
import net.claustra01.clayium.world.area.AreaMachineType;
import net.claustra01.clayium.world.area.AreaMarkerBlock;
import net.claustra01.clayium.world.area.AreaMarkerBlockEntity;
import net.claustra01.clayium.world.area.AreaMarkerType;
import net.claustra01.clayium.world.area.AreaMachineMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class AreaMachineRegistration {
    public static final Map<AreaMarkerType, MarkerDefinition> MARKERS;
    public static final Map<AreaMachineType, MachineDefinition> MACHINES;
    public static final List<DeferredBlock<? extends Block>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<AreaMarkerType, MarkerDefinition> markers = new EnumMap<>(AreaMarkerType.class);
        for (AreaMarkerType markerType : AreaMarkerType.values()) {
            DeferredBlock<AreaMarkerBlock> block = ClayiumRegistries.BLOCKS.register(
                    markerType.id(),
                    () -> new AreaMarkerBlock(
                            BlockBehaviour.Properties.of().mapColor(MapColor.CLAY)
                                    .strength(1.5F, 3.0F).sound(SoundType.STONE).noOcclusion(),
                            markerType));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    markerType.id(),
                    () -> new FoundationBlockItem(block.get(), new Item.Properties().component(
                            FoundationDataComponentRegistration.TIER.get(), markerTier(markerType))));
            markers.put(markerType, new MarkerDefinition(markerType, block, item));
        }
        MARKERS = Collections.unmodifiableMap(markers);

        Map<AreaMachineType, MachineDefinition> machines = new EnumMap<>(AreaMachineType.class);
        for (AreaMachineType machineType : AreaMachineType.values()) {
            DeferredBlock<AreaMachineBlock> block = ClayiumRegistries.BLOCKS.register(
                    machineType.id(),
                    () -> new AreaMachineBlock(
                            BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                                    .strength(3.0F + machineType.tier() * 0.25F, 5.0F + machineType.tier())
                                    .sound(SoundType.STONE),
                            machineType));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    machineType.id(),
                    () -> new FoundationBlockItem(block.get(), new Item.Properties().component(
                            FoundationDataComponentRegistration.TIER.get(), machineType.tier())));
            machines.put(machineType, new MachineDefinition(machineType, block, item));
        }
        MACHINES = Collections.unmodifiableMap(machines);
        List<DeferredBlock<? extends Block>> blocks = new ArrayList<>();
        MARKERS.values().forEach(definition -> blocks.add(definition.block()));
        MACHINES.values().forEach(definition -> blocks.add(definition.block()));
        BLOCKS = List.copyOf(blocks);
        ITEMS = java.util.stream.Stream.concat(
                MARKERS.values().stream().map(MarkerDefinition::item),
                MACHINES.values().stream().map(MachineDefinition::item)).toList();
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AreaMarkerBlockEntity>> MARKER_BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "area_marker",
                    () -> BlockEntityType.Builder.of(
                            AreaMarkerBlockEntity::new,
                            MARKERS.values().stream().map(definition -> definition.block().get())
                                    .toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AreaMachineBlockEntity>> MACHINE_BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "area_machine",
                    () -> BlockEntityType.Builder.of(
                            AreaMachineBlockEntity::new,
                            MACHINES.values().stream().map(definition -> definition.block().get())
                                    .toArray(Block[]::new)).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<AreaMachineMenu>> MENU =
            ClayiumRegistries.MENU_TYPES.register("area_machine",
                    () -> IMenuTypeExtension.create((containerId, inventory, data) ->
                            new AreaMachineMenu(containerId, inventory, data.readBlockPos())));

    private static int markerTier(AreaMarkerType markerType) {
        return switch (markerType) {
            case NORMAL -> 7;
            case OPEN_PIT, GROUND_LEVELING, PRISM -> 8;
        };
    }

    public record MarkerDefinition(
            AreaMarkerType type,
            DeferredBlock<AreaMarkerBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    public record MachineDefinition(
            AreaMachineType type,
            DeferredBlock<AreaMachineBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    private AreaMachineRegistration() {
    }
}
