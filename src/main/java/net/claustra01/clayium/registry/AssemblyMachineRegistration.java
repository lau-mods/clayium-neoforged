package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.series.AssemblySeries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public final class AssemblyMachineRegistration {
    public static final Map<String, MachineDefinition> MACHINES;
    public static final Map<Integer, DeferredBlock<Block>> MACHINE_HULLS;
    public static final List<DeferredBlock<? extends Block>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<String, MachineDefinition> machines = new LinkedHashMap<>();
        Map<Integer, DeferredBlock<Block>> machineHulls = new LinkedHashMap<>();
        List<DeferredBlock<? extends Block>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        for (AssemblySeries series : AssemblySeries.values()) {
            for (int tier : series.tiers()) {
                OriginalRegistryCatalog.MachineFamily family = OriginalRegistryCatalog.MACHINE_FAMILIES.stream()
                        .filter(candidate -> candidate.namedIdStem().equals(series.recipeMethod().id()))
                        .findFirst().orElseThrow();
                String id = OriginalRegistryCatalog.machineId(family, tier);
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
        for (HullDefinition definition : List.of(
                new HullDefinition(3, "simple_machine_hull"),
                new HullDefinition(4, "basic_machine_hull"),
                new HullDefinition(5, "advanced_machine_hull"),
                new HullDefinition(6, "precision_machine_hull"),
                new HullDefinition(7, "clay_steel_machine_hull"),
                new HullDefinition(8, "clayium_machine_hull"),
                new HullDefinition(9, "ultimate_machine_hull"),
                new HullDefinition(10, "antimatter_machine_hull"))) {
            DeferredBlock<Block> block = ClayiumRegistries.BLOCKS.registerSimpleBlock(
                    definition.id(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.0F + definition.tier() * 0.25F, 5.0F + definition.tier())
                            .sound(SoundType.STONE));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    definition.id(),
                    () -> new FoundationBlockItem(
                            block.get(),
                            new Item.Properties().component(
                                    FoundationDataComponentRegistration.TIER.get(), definition.tier())));
            machineHulls.put(definition.tier(), block);
            blocks.add(block);
            items.add(item);
        }
        MACHINES = Collections.unmodifiableMap(machines);
        MACHINE_HULLS = Collections.unmodifiableMap(machineHulls);
        BLOCKS = List.copyOf(blocks);
        ITEMS = List.copyOf(items);
    }

    public record MachineDefinition(
            String id,
            AssemblySeries series,
            int tier,
            DeferredBlock<ClayMachineBlock> block) {
    }

    private record HullDefinition(int tier, String id) {
    }

    private AssemblyMachineRegistration() {
    }
}
