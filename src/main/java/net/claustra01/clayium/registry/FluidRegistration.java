package net.claustra01.clayium.registry;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.fluid.FluidFilterSettings;
import net.claustra01.clayium.fluid.FluidTransportBlock;
import net.claustra01.clayium.fluid.FluidTransportBlockEntity;
import net.claustra01.clayium.fluid.FluidTransportMenu;
import net.claustra01.clayium.fluid.FluidTransportSpecification;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
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

public final class FluidRegistration {
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidFilterSettings>> FLUID_FILTER_SETTINGS =
            ClayiumRegistries.DATA_COMPONENT_TYPES.registerComponentType(
                    "fluid_filter",
                    builder -> builder.persistent(FluidFilterSettings.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(FluidFilterSettings.CODEC)));

    public static final DeferredItem<Item> FLUID_FILTER = ClayiumRegistries.ITEMS.register(
            "fluid_filter",
            () -> new Item(new Item.Properties().stacksTo(1)
                    .component(FLUID_FILTER_SETTINGS.get(), FluidFilterSettings.ALLOW_ALL)));

    public static final Map<String, Definition> MACHINES;
    public static final List<DeferredBlock<FluidTransportBlock>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<String, Definition> machines = new LinkedHashMap<>();
        for (FluidTransportSpecification specification : FluidTransportSpecification.VALUES) {
            DeferredBlock<FluidTransportBlock> block = ClayiumRegistries.BLOCKS.register(
                    specification.id(),
                    () -> new FluidTransportBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(3.0F + specification.tier() * 0.25F, 5.0F + specification.tier())
                                    .sound(SoundType.STONE),
                            specification));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    specification.id(),
                    () -> new FoundationBlockItem(block.get(), new Item.Properties().component(
                            FoundationDataComponentRegistration.TIER.get(), specification.tier())));
            machines.put(specification.id(), new Definition(specification, block, item));
        }
        MACHINES = Collections.unmodifiableMap(machines);
        BLOCKS = MACHINES.values().stream().map(Definition::block).toList();
        ITEMS = MACHINES.values().stream().map(Definition::item).toList();
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidTransportBlockEntity>> BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "fluid_transport",
                    () -> BlockEntityType.Builder.of(
                                    FluidTransportBlockEntity::new,
                                    BLOCKS.stream().map(DeferredBlock::get)
                                            .toArray(net.minecraft.world.level.block.Block[]::new))
                            .build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<FluidTransportMenu>> MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "fluid_transport",
                    () -> IMenuTypeExtension.create((containerId, inventory, data) ->
                            new FluidTransportMenu(containerId, inventory, data.readBlockPos())));

    public record Definition(
            FluidTransportSpecification specification,
            DeferredBlock<FluidTransportBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    private FluidRegistration() {
    }
}
