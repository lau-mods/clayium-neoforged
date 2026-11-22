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
    public static final Map<String, HullDefinition> MACHINE_HULL_DEFINITIONS;
    public static final List<DeferredBlock<? extends Block>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<String, MachineDefinition> machines = new LinkedHashMap<>();
        Map<Integer, DeferredBlock<Block>> machineHulls = new LinkedHashMap<>();
        Map<String, HullDefinition> hullDefinitions = new LinkedHashMap<>();
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
        registerHull(2, "dense_clay_machine_hull", "machinehull-1", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(3, "simple_machine_hull", "machinehull-2", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(4, "basic_machine_hull", "machinehull-3", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(5, "advanced_machine_hull", "machinehull-4", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(6, "precision_machine_hull", "machinehull-5", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(7, "clay_steel_machine_hull", "machinehull-6", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(8, "clayium_machine_hull", "machinehull-7", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(9, "ultimate_machine_hull", "machinehull-8", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(10, "antimatter_machine_hull", "machinehull-9", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(11, "pure_antimatter_machine_hull", "machinehull-10", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(12, "oec_machine_hull", "machinehull-11", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(13, "opa_machine_hull", "machinehull-12", true, machineHulls, hullDefinitions, blocks, items);
        registerHull(6, "az91_d_alloy_hull", "az91dhull", false, machineHulls, hullDefinitions, blocks, items);
        registerHull(6, "zk60_a_alloy_hull", "zk60ahull", false, machineHulls, hullDefinitions, blocks, items);
        MACHINES = Collections.unmodifiableMap(machines);
        MACHINE_HULLS = Collections.unmodifiableMap(machineHulls);
        MACHINE_HULL_DEFINITIONS = Collections.unmodifiableMap(hullDefinitions);
        BLOCKS = List.copyOf(blocks);
        ITEMS = List.copyOf(items);
    }

    public record MachineDefinition(
            String id,
            AssemblySeries series,
            int tier,
            DeferredBlock<ClayMachineBlock> block) {
    }

    private static void registerHull(
            int tier, String id, String texture, boolean primary,
            Map<Integer, DeferredBlock<Block>> machineHulls,
            Map<String, HullDefinition> hullDefinitions,
            List<DeferredBlock<? extends Block>> blocks,
            List<DeferredItem<FoundationBlockItem>> items) {
        DeferredBlock<Block> block = ClayiumRegistries.BLOCKS.registerSimpleBlock(
                id,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(3.0F + tier * 0.25F, 5.0F + tier)
                        .sound(SoundType.STONE));
        DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                id,
                () -> new FoundationBlockItem(block.get(),
                        new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier)));
        hullDefinitions.put(id, new HullDefinition(tier, id, texture, primary, block));
        if (primary) {
            machineHulls.put(tier, block);
        }
        blocks.add(block);
        items.add(item);
    }

    public record HullDefinition(
            int tier,
            String id,
            String texture,
            boolean primary,
            DeferredBlock<Block> block) {
    }

    private AssemblyMachineRegistration() {
    }
}
