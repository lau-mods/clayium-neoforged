package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleBlock;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.machine.environment.CobblestoneGeneratorBlock;
import net.claustra01.clayium.machine.environment.CobblestoneGeneratorBlockEntity;
import net.claustra01.clayium.machine.environment.EnvironmentSeries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class EnvironmentMachineRegistration {
    public static final Map<String, SaltExtractorDefinition> SALT_EXTRACTORS;
    public static final List<DeferredBlock<ClayMachineBlock>> SALT_EXTRACTOR_BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> SALT_EXTRACTOR_ITEMS;

    public static final List<CobblestoneGeneratorDefinition> COBBLESTONE_GENERATORS = List.of(
            cobblestoneGenerator("clay_cobblestone_generator", 1),
            cobblestoneGenerator("dense_clay_cobblestone_generator", 2),
            cobblestoneGenerator("simple_cobblestone_generator", 3),
            cobblestoneGenerator("basic_cobblestone_generator", 4),
            cobblestoneGenerator("advanced_cobblestone_generator", 5),
            cobblestoneGenerator("precision_cobblestone_generator", 6),
            cobblestoneGenerator("clay_steel_cobblestone_generator", 7));

    public static final DeferredBlock<QuartzCrucibleBlock> QUARTZ_CRUCIBLE = ClayiumRegistries.BLOCKS.register(
            "quartz_crucible",
            () -> new QuartzCrucibleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(4.0F, 8.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()));
    public static final DeferredItem<FoundationBlockItem> QUARTZ_CRUCIBLE_ITEM = ClayiumRegistries.ITEMS.register(
            "quartz_crucible",
            () -> new FoundationBlockItem(
                    QUARTZ_CRUCIBLE.get(),
                    new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), 5)));

    static {
        Map<String, SaltExtractorDefinition> definitions = new LinkedHashMap<>();
        List<DeferredBlock<ClayMachineBlock>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        for (int tier : EnvironmentSeries.SALT_EXTRACTOR.tiers()) {
            String id = EnvironmentSeries.SALT_EXTRACTOR.registrationId(tier);
            DeferredBlock<ClayMachineBlock> block = ClayiumRegistries.BLOCKS.register(
                    id,
                    () -> new ClayMachineBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(3.0F + tier * 0.25F, 5.0F + tier)
                                    .sound(SoundType.STONE),
                            EnvironmentSeries.SALT_EXTRACTOR,
                            tier));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    id,
                    () -> new FoundationBlockItem(
                            block.get(),
                            new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier)));
            definitions.put(id, new SaltExtractorDefinition(id, tier, block, item));
            blocks.add(block);
            items.add(item);
        }
        SALT_EXTRACTORS = Collections.unmodifiableMap(definitions);
        SALT_EXTRACTOR_BLOCKS = List.copyOf(blocks);
        SALT_EXTRACTOR_ITEMS = List.copyOf(items);
    }

    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>,
            net.minecraft.world.inventory.MenuType<net.claustra01.clayium.logistics.NormalInventoryMenu>>
            COBBLESTONE_GENERATOR_MENU = ClayiumRegistries.MENU_TYPES.register(
                    "cobblestone_generator",
                    () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id, inventory, data) ->
                            new net.claustra01.clayium.logistics.NormalInventoryMenu(
                                    EnvironmentMachineRegistration.COBBLESTONE_GENERATOR_MENU.get(),
                                    id, inventory, data.readBlockPos())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CobblestoneGeneratorBlockEntity>>
            COBBLESTONE_GENERATOR_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "cobblestone_generator",
                    () -> BlockEntityType.Builder.of(
                                    CobblestoneGeneratorBlockEntity::new,
                                    COBBLESTONE_GENERATORS.stream().map(CobblestoneGeneratorDefinition::block)
                                            .map(DeferredBlock::get)
                                            .toArray(net.minecraft.world.level.block.Block[]::new))
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuartzCrucibleBlockEntity>>
            QUARTZ_CRUCIBLE_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "quartz_crucible",
                    () -> BlockEntityType.Builder.of(QuartzCrucibleBlockEntity::new, QUARTZ_CRUCIBLE.get())
                            .build(null));

    private static CobblestoneGeneratorDefinition cobblestoneGenerator(String id, int tier) {
        DeferredBlock<CobblestoneGeneratorBlock> block = ClayiumRegistries.BLOCKS.register(
                id,
                () -> new CobblestoneGeneratorBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.STONE)
                                .strength(3.0F + tier * 0.25F, 5.0F + tier)
                                .sound(SoundType.STONE),
                        tier));
        DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                id,
                () -> new FoundationBlockItem(
                        block.get(),
                        new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier)));
        return new CobblestoneGeneratorDefinition(id, tier, block, item);
    }

    public static List<DeferredItem<FoundationBlockItem>> allItems() {
        return java.util.stream.Stream.concat(
                        java.util.stream.Stream.concat(
                                COBBLESTONE_GENERATORS.stream().map(CobblestoneGeneratorDefinition::item),
                                SALT_EXTRACTOR_ITEMS.stream()),
                        java.util.stream.Stream.of(QUARTZ_CRUCIBLE_ITEM))
                .toList();
    }

    public record CobblestoneGeneratorDefinition(
            String id,
            int tier,
            DeferredBlock<CobblestoneGeneratorBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    public record SaltExtractorDefinition(
            String id,
            int tier,
            DeferredBlock<ClayMachineBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    private EnvironmentMachineRegistration() {
    }
}
