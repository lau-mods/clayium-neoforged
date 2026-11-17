package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.series.FormingSeries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public final class FormingMachineRegistration {
    public static final Map<String, MachineDefinition> MACHINES;
    public static final List<DeferredBlock<ClayMachineBlock>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<String, MachineDefinition> machines = new LinkedHashMap<>();
        List<DeferredBlock<ClayMachineBlock>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        for (FormingSeries series : FormingSeries.values()) {
            for (int tier : series.tiers()) {
                OriginalRegistryCatalog.MachineFamily family = OriginalRegistryCatalog.MACHINE_FAMILIES.stream()
                        .filter(candidate -> candidate.legacyName().equals(series.legacyName()))
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
        MACHINES = Collections.unmodifiableMap(machines);
        BLOCKS = List.copyOf(blocks);
        ITEMS = List.copyOf(items);
    }

    public record MachineDefinition(
            String id,
            FormingSeries series,
            int tier,
            DeferredBlock<ClayMachineBlock> block) {
    }

    private FormingMachineRegistration() {
    }
}
