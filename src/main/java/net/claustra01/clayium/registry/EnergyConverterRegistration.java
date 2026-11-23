package net.claustra01.clayium.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.energy.fe.EnergyConverterBlock;
import net.claustra01.clayium.energy.fe.EnergyConverterBlockEntity;
import net.claustra01.clayium.energy.fe.EnergyConverterSpecification;
import net.claustra01.clayium.energy.fe.EnergyConverterMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class EnergyConverterRegistration {
    public static final Map<Integer, ConverterDefinition> CONVERTERS;
    public static final List<DeferredBlock<EnergyConverterBlock>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<Integer, ConverterDefinition> converters = new LinkedHashMap<>();
        for (EnergyConverterSpecification specification : EnergyConverterSpecification.VALUES) {
            DeferredBlock<EnergyConverterBlock> block = ClayiumRegistries.BLOCKS.register(
                    specification.id(),
                    () -> new EnergyConverterBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(3.0F + specification.tier() * 0.25F, 5.0F + specification.tier())
                                    .sound(SoundType.STONE),
                            specification));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    specification.id(),
                    () -> new FoundationBlockItem(
                            block.get(),
                            new Item.Properties().component(
                                    FoundationDataComponentRegistration.TIER.get(), specification.tier())));
            converters.put(specification.tier(), new ConverterDefinition(specification, block, item));
        }
        CONVERTERS = Collections.unmodifiableMap(converters);
        BLOCKS = CONVERTERS.values().stream().map(ConverterDefinition::block).toList();
        ITEMS = CONVERTERS.values().stream().map(ConverterDefinition::item).toList();
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyConverterBlockEntity>> BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "energy_converter",
                    () -> BlockEntityType.Builder.of(
                                    EnergyConverterBlockEntity::new,
                                    BLOCKS.stream().map(DeferredBlock::get)
                                            .toArray(net.minecraft.world.level.block.Block[]::new))
                            .build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<EnergyConverterMenu>> MENU =
            ClayiumRegistries.MENU_TYPES.register("energy_converter",
                    () -> IMenuTypeExtension.create((id, inventory, data) ->
                            new EnergyConverterMenu(id, inventory, data.readBlockPos())));

    public record ConverterDefinition(
            EnergyConverterSpecification specification,
            DeferredBlock<EnergyConverterBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    private EnergyConverterRegistration() {
    }
}
