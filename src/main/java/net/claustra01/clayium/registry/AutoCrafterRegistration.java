package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterBlock;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterBlockEntity;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterMenu;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterSpecification;
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

public final class AutoCrafterRegistration {
    public static final Map<Integer, Definition> AUTO_CRAFTERS;
    public static final List<DeferredBlock<AutoCrafterBlock>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<Integer, Definition> definitions = new LinkedHashMap<>();
        List<DeferredBlock<AutoCrafterBlock>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        OriginalRegistryCatalog.MachineFamily family = OriginalRegistryCatalog.MACHINE_FAMILIES.stream()
                .filter(candidate -> candidate.legacyName().equals("AutoCrafter"))
                .findFirst().orElseThrow();
        for (AutoCrafterSpecification specification : AutoCrafterSpecification.VALUES) {
            String id = OriginalRegistryCatalog.machineId(family, specification.tier());
            DeferredBlock<AutoCrafterBlock> block = ClayiumRegistries.BLOCKS.register(
                    id,
                    () -> new AutoCrafterBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(3.0F + specification.tier() * 0.25F, 5.0F + specification.tier())
                                    .sound(SoundType.STONE),
                            specification));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    id,
                    () -> new FoundationBlockItem(block.get(), new Item.Properties().component(
                            FoundationDataComponentRegistration.TIER.get(), specification.tier())));
            definitions.put(specification.tier(), new Definition(id, specification, block, item));
            blocks.add(block);
            items.add(item);
        }
        AUTO_CRAFTERS = Collections.unmodifiableMap(definitions);
        BLOCKS = List.copyOf(blocks);
        ITEMS = List.copyOf(items);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoCrafterBlockEntity>> BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "auto_crafter",
                    () -> BlockEntityType.Builder.of(
                                    AutoCrafterBlockEntity::new,
                                    BLOCKS.stream().map(DeferredBlock::get)
                                            .toArray(net.minecraft.world.level.block.Block[]::new))
                            .build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<AutoCrafterMenu>> MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "auto_crafter",
                    () -> IMenuTypeExtension.create((containerId, inventory, data) ->
                            new AutoCrafterMenu(containerId, inventory, data.readBlockPos())));

    public record Definition(
            String id,
            AutoCrafterSpecification specification,
            DeferredBlock<AutoCrafterBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    private AutoCrafterRegistration() {
    }
}
