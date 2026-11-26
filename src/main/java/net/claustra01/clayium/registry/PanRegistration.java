package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.pan.PanBlock;
import net.claustra01.clayium.pan.PanCableBlock;
import net.claustra01.clayium.pan.PanBlockEntity;
import net.claustra01.clayium.pan.PanMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class PanRegistration {
    public static final Map<String, Definition> DEFINITIONS;
    public static final List<DeferredBlock<PanBlock>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;
    public static final DeferredBlock<PanBlock> PAN_CABLE;
    public static final DeferredBlock<PanBlock> PAN_CORE;

    static {
        Map<String, Definition> definitions = new LinkedHashMap<>();
        List<DeferredBlock<PanBlock>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        PAN_CABLE = register(definitions, blocks, items, "pan_cable", PanBlock.Kind.CABLE, 11);
        PAN_CORE = register(definitions, blocks, items, "pan_core", PanBlock.Kind.CORE, 11);
        for (int tier = 10; tier <= 13; tier++) {
            register(definitions, blocks, items, "pan_adapter_tier_" + tier, PanBlock.Kind.ADAPTER, tier);
        }
        for (int tier = 4; tier <= 13; tier++) {
            register(definitions, blocks, items, "pan_duplicator_tier_" + tier,
                    PanBlock.Kind.DUPLICATOR, tier);
        }
        DEFINITIONS = Collections.unmodifiableMap(definitions);
        BLOCKS = List.copyOf(blocks);
        ITEMS = List.copyOf(items);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PanBlockEntity>> BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "pan",
                    () -> BlockEntityType.Builder.of(PanBlockEntity::new,
                                    DEFINITIONS.values().stream()
                                            .filter(definition -> definition.kind() == PanBlock.Kind.CORE
                                                    || definition.kind() == PanBlock.Kind.ADAPTER
                                                    || definition.kind() == PanBlock.Kind.DUPLICATOR)
                                            .map(definition -> (Block)definition.block().get())
                                            .toArray(Block[]::new))
                            .build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<PanMenu>> MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "pan", () -> IMenuTypeExtension.create((id, inventory, data) ->
                            new PanMenu(id, inventory, data.readBlockPos())));

    private static DeferredBlock<PanBlock> register(
            Map<String, Definition> definitions,
            List<DeferredBlock<PanBlock>> blocks,
            List<DeferredItem<FoundationBlockItem>> items,
            String id,
            PanBlock.Kind kind,
            int tier) {
        DeferredBlock<PanBlock> block = ClayiumRegistries.BLOCKS.register(id, () -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F + tier * 0.25F, 5.0F + tier)
                    .sound(SoundType.METAL);
            return kind == PanBlock.Kind.CABLE
                    ? new PanCableBlock(properties, tier) : new PanBlock(properties, kind, tier);
        });
        DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                id, () -> new FoundationBlockItem(block.get(), new Item.Properties()
                        .component(FoundationDataComponentRegistration.TIER.get(), tier)));
        definitions.put(id, new Definition(id, kind, tier, block));
        blocks.add(block);
        items.add(item);
        return block;
    }

    public record Definition(String id, PanBlock.Kind kind, int tier, DeferredBlock<PanBlock> block) {
    }

    private PanRegistration() {
    }
}
