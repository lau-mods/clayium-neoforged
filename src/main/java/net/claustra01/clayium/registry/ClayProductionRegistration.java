package net.claustra01.clayium.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.machine.clayproduction.ClayProductionBlock;
import net.claustra01.clayium.machine.clayproduction.ClayProductionBlockEntity;
import net.claustra01.clayium.machine.clayproduction.ClayProductionMenu;
import net.claustra01.clayium.machine.clayproduction.ClayProductionType;
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

public final class ClayProductionRegistration {
    public static final Map<ClayProductionType, DeferredBlock<ClayProductionBlock>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<ClayProductionType, DeferredBlock<ClayProductionBlock>> blocks = new LinkedHashMap<>();
        java.util.ArrayList<DeferredItem<FoundationBlockItem>> items = new java.util.ArrayList<>();
        for (ClayProductionType type : ClayProductionType.values()) {
            DeferredBlock<ClayProductionBlock> block = ClayiumRegistries.BLOCKS.register(
                    type.id(),
                    () -> new ClayProductionBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(3.0F + type.tier() * 0.25F, 5.0F + type.tier())
                                    .sound(SoundType.STONE),
                            type));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    type.id(),
                    () -> new FoundationBlockItem(block.get(), new Item.Properties()
                            .component(FoundationDataComponentRegistration.TIER.get(), type.tier())));
            blocks.put(type, block);
            items.add(item);
        }
        BLOCKS = Collections.unmodifiableMap(blocks);
        ITEMS = List.copyOf(items);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayProductionBlockEntity>> BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "clay_production_machine",
                    () -> BlockEntityType.Builder.of(
                                    ClayProductionBlockEntity::new,
                                    BLOCKS.values().stream().map(DeferredBlock::get)
                                            .toArray(net.minecraft.world.level.block.Block[]::new))
                            .build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ClayProductionMenu>> MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "clay_production_machine",
                    () -> IMenuTypeExtension.create((containerId, inventory, data) ->
                            new ClayProductionMenu(containerId, inventory, data.readBlockPos())));

    private ClayProductionRegistration() {
    }
}
