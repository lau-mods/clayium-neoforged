package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.augment.MachineAugmentBlock;
import net.claustra01.clayium.machine.augment.MachineAugmentBlock.AugmentKind;
import net.claustra01.clayium.machine.ca.CaCollectorBlock;
import net.claustra01.clayium.machine.ca.CaCollectorBlockEntity;
import net.claustra01.clayium.machine.ca.CaSeries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class CaRegistration {
    private static final String[] MATERIAL_NAMES = {"antimatter", "pure_antimatter", "oec", "opa"};
    private static final double[] RESONANCE = {1.08D, 1.10D, 2.0D, 20.0D};
    private static final int[] STORAGE = {1, 3, 7, 63};

    public static final Map<String, MachineDefinition> MACHINES;
    public static final Map<String, AugmentDefinition> AUGMENTS;
    public static final List<DeferredBlock<? extends Block>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;
    public static final DeferredBlock<CaCollectorBlock> RESONATING_COLLECTOR;
    public static final DeferredItem<FoundationBlockItem> RESONATING_COLLECTOR_ITEM;

    static {
        Map<String, MachineDefinition> machines = new LinkedHashMap<>();
        Map<String, AugmentDefinition> augments = new LinkedHashMap<>();
        List<DeferredBlock<? extends Block>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();

        for (CaSeries series : CaSeries.values()) {
            OriginalRegistryCatalog.MachineFamily family = OriginalRegistryCatalog.MACHINE_FAMILIES.stream()
                    .filter(candidate -> candidate.namedIdStem().equals(series.recipeMethod().id()))
                    .findFirst().orElseThrow();
            for (int tier : series.tiers()) {
                String id = OriginalRegistryCatalog.machineId(family, tier);
                DeferredBlock<ClayMachineBlock> block = ClayiumRegistries.BLOCKS.register(
                        id,
                        () -> new ClayMachineBlock(machineProperties(tier), series, tier));
                DeferredItem<FoundationBlockItem> item = blockItem(id, block, tier);
                machines.put(id, new MachineDefinition(id, series, tier, block));
                blocks.add(block);
                items.add(item);
            }
        }

        RESONATING_COLLECTOR = ClayiumRegistries.BLOCKS.register(
                "ca_resonating_collector", () -> new CaCollectorBlock(machineProperties(10)));
        RESONATING_COLLECTOR_ITEM = blockItem("ca_resonating_collector", RESONATING_COLLECTOR, 10);
        blocks.add(RESONATING_COLLECTOR);
        items.add(RESONATING_COLLECTOR_ITEM);

        for (int index = 0; index < MATERIAL_NAMES.length; index++) {
            int rank = index + 1;
            int tier = index + 10;
            String material = MATERIAL_NAMES[index];
            registerAugment(augments, blocks, items, material + "_resonator", material,
                    AugmentKind.RESONATOR, rank, tier, RESONANCE[index], 0);
            registerAugment(augments, blocks, items, material + "_overclocker", material,
                    AugmentKind.OVERCLOCKER, rank, tier, TierManager.overclockerFactor(rank), 0);
            registerAugment(augments, blocks, items, material + "_energy_storage_upgrade",
                    material, AugmentKind.ENERGY_STORAGE,
                    rank, tier, 1.0D, STORAGE[index]);
        }

        MACHINES = Collections.unmodifiableMap(machines);
        AUGMENTS = Collections.unmodifiableMap(augments);
        BLOCKS = List.copyOf(blocks);
        ITEMS = List.copyOf(items);
    }

    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>,
            net.minecraft.world.inventory.MenuType<net.claustra01.clayium.logistics.NormalInventoryMenu>>
            COLLECTOR_MENU = ClayiumRegistries.MENU_TYPES.register(
                    "ca_resonating_collector",
                    () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id, inventory, data) ->
                            new net.claustra01.clayium.logistics.NormalInventoryMenu(
                                    CaRegistration.COLLECTOR_MENU.get(), id, inventory, data.readBlockPos())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CaCollectorBlockEntity>> COLLECTOR_BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "ca_resonating_collector",
                    () -> BlockEntityType.Builder.of(CaCollectorBlockEntity::new, RESONATING_COLLECTOR.get())
                            .build(null));

    private static void registerAugment(
            Map<String, AugmentDefinition> augments,
            List<DeferredBlock<? extends Block>> blocks,
            List<DeferredItem<FoundationBlockItem>> items,
            String id,
            String material,
            AugmentKind kind,
            int rank,
            int tier,
            double factor,
            int additionalFuel) {
        DeferredBlock<MachineAugmentBlock> block = ClayiumRegistries.BLOCKS.register(
                id,
                () -> new MachineAugmentBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.METAL)
                                .strength(2.0F, 2.0F)
                                .sound(SoundType.METAL),
                        kind,
                        factor,
                        additionalFuel));
        DeferredItem<FoundationBlockItem> item = blockItem(id, block, tier);
        String legacyMaterial = material.replace("pure_antimatter", "pureantimatter");
        String texture = switch (kind) {
            case RESONATOR -> "resonator-" + legacyMaterial;
            case OVERCLOCKER -> "overclocker-" + legacyMaterial;
            case ENERGY_STORAGE -> "estorageupgrade-" + legacyMaterial;
        };
        augments.put(id, new AugmentDefinition(id, texture, block));
        blocks.add(block);
        items.add(item);
    }

    private static BlockBehaviour.Properties machineProperties(int tier) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0F + tier * 0.25F, 5.0F + tier)
                .sound(SoundType.STONE);
    }

    private static DeferredItem<FoundationBlockItem> blockItem(
            String id, DeferredBlock<? extends Block> block, int tier) {
        return ClayiumRegistries.ITEMS.register(
                id,
                () -> new FoundationBlockItem(
                        block.get(),
                        new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier)));
    }

    public record MachineDefinition(
            String id,
            CaSeries series,
            int tier,
            DeferredBlock<ClayMachineBlock> block) {
    }

    public record AugmentDefinition(
            String id,
            String texture,
            DeferredBlock<MachineAugmentBlock> block) {
    }

    private CaRegistration() {
    }
}
