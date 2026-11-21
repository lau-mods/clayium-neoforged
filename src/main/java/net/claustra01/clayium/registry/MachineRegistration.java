package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.ClayMachineMenu;
import net.claustra01.clayium.machine.series.MachineSeries;
import net.claustra01.clayium.machine.series.ChemicalSeries;
import net.claustra01.clayium.machine.series.ProcessingSeries;
import net.claustra01.clayium.machine.series.SmeltingSeries;
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

public final class MachineRegistration {
    public static final Map<String, MachineDefinition> MACHINES;
    public static final List<DeferredBlock<ClayMachineBlock>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<String, MachineDefinition> machines = new LinkedHashMap<>();
        List<DeferredBlock<ClayMachineBlock>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        List<MachineSeries> seriesList = new ArrayList<>();
        Collections.addAll(seriesList, ProcessingSeries.values());
        Collections.addAll(seriesList, SmeltingSeries.values());
        Collections.addAll(seriesList, ChemicalSeries.values());
        for (MachineSeries series : seriesList) {
            for (int tier : series.tiers()) {
                String id = series.registrationId(tier);
                DeferredBlock<ClayMachineBlock> block = ClayiumRegistries.BLOCKS.register(
                        id,
                        () -> new ClayMachineBlock(
                                BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(3.0F + tier * 0.25F, 5.0F + tier)
                                        .sound(SoundType.STONE),
                                series,
                                tier));
                DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                        id,
                        () -> new FoundationBlockItem(
                                block.get(),
                                new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier)));
                machines.put(id, new MachineDefinition(id, series, tier, block));
                blocks.add(block);
                items.add(item);
            }
        }
        MACHINES = Collections.unmodifiableMap(machines);
        BLOCKS = List.copyOf(blocks);
        ITEMS = List.copyOf(items);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayMachineBlockEntity>> MACHINE_BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "processing_machine",
                    () -> BlockEntityType.Builder.of(
                                    ClayMachineBlockEntity::new,
                                    java.util.stream.Stream.of(
                                                    BLOCKS.stream(),
                                                    CaRegistration.MACHINES.values().stream()
                                                            .map(CaRegistration.MachineDefinition::block),
                                                    FormingMachineRegistration.BLOCKS.stream(),
                                                    EnvironmentMachineRegistration.SALT_EXTRACTOR_BLOCKS.stream(),
                                                    AssemblyMachineRegistration.MACHINES.values().stream()
                                                            .map(AssemblyMachineRegistration.MachineDefinition::block))
                                            .flatMap(java.util.function.Function.identity())
                                            .map(DeferredBlock::get)
                                            .toArray(net.minecraft.world.level.block.Block[]::new))
                            .build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ClayMachineMenu>> MACHINE_MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "processing_machine",
                    () -> IMenuTypeExtension.create((containerId, inventory, data) ->
                            new ClayMachineMenu(containerId, inventory, data.readBlockPos())));

    public record MachineDefinition(
            String id,
            MachineSeries series,
            int tier,
            DeferredBlock<ClayMachineBlock> block) {
    }

    private MachineRegistration() {
    }
}
