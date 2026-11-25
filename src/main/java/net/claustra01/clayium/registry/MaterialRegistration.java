package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.item.FoundationItem;
import net.claustra01.clayium.material.ClayMaterials;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.material.MaterialBlockDefinition;
import net.claustra01.clayium.material.MaterialShape;
import net.claustra01.clayium.material.SiliconeBlockDefinition;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public final class MaterialRegistration {
    public static final DeferredBlock<Block> OEC_BLOCK;
    public static final DeferredItem<FoundationBlockItem> OEC_BLOCK_ITEM;
    public static final List<ComponentDefinition> COMPONENT_DEFINITIONS = List.of(
            component("clay_circuit", "claycircuit", 1),
            component("clay_circuit_board", "claycircuitboard", 1),
            component("simple_circuit", "simplecircuit", 3),
            component("basic_circuit", "basiccircuit", 4),
            component("advanced_circuit", "advancedcircuit", 5),
            component("precision_circuit", "precisioncircuit", 6),
            component("integrated_circuit", "integratedcircuit", 7),
            component("clay_core", "claycore", 6),
            component("clay_brain", "claybrain", 7),
            component("clay_spirit", "clayspirit", 8),
            component("clay_soul", "claysoul", 9),
            component("clay_anima", "clayanima", 10),
            component("clay_psyche", "claypsyche", 11),
            component("synchronous_parts", "synchronousparts", 7),
            component("teleportation_parts", "teleportationparts", 8),
            component("manipulator_mk_i", "manipulator", 8),
            component("manipulator_mk_ii", "manipulator2", 9),
            component("manipulator_mk_iii", "manipulator3", 10),
            component("cee_board", "ceeboard", 7),
            component("cee_circuit", "ceecircuit", 8),
            component("clay_energy_excitor", "cee", 9),
            component("laser_parts", "laserparts", 7),
            component("antimatter_seed", "antimatterseed", 10));

    public static final Map<String, DeferredBlock<? extends Block>> COMPRESSION_BLOCKS;
    public static final Map<String, DeferredBlock<? extends Block>> MATERIAL_BLOCKS;
    public static final Map<String, Supplier<? extends Item>> MATERIAL_ITEMS;
    public static final Map<String, DeferredItem<? extends Item>> COMPONENTS;
    public static final Map<String, DeferredBlock<? extends Block>> METAL_BLOCKS;
    public static final Map<String, DeferredBlock<? extends Block>> SILICONE_BLOCKS;
    public static final Map<String, DeferredItem<? extends Item>> SHARDS;
    public static final List<DeferredBlock<? extends Block>> MATERIAL_OWNED_BLOCKS;
    public static final List<DeferredItem<? extends Item>> CREATIVE_TAB_ITEMS;

    static {
        Map<String, DeferredBlock<? extends Block>> compressionBlocks = new LinkedHashMap<>();
        Map<String, DeferredItem<? extends Item>> compressionItems = new LinkedHashMap<>();
        Map<String, DeferredBlock<? extends Block>> materialBlocks = new LinkedHashMap<>();
        Map<String, Supplier<? extends Item>> materialItems = new LinkedHashMap<>();
        Map<String, DeferredItem<? extends Item>> components = new LinkedHashMap<>();
        Map<String, DeferredBlock<? extends Block>> metalBlocks = new LinkedHashMap<>();
        Map<String, DeferredBlock<? extends Block>> siliconeBlocks = new LinkedHashMap<>();
        Map<String, DeferredItem<? extends Item>> shards = new LinkedHashMap<>();
        List<DeferredBlock<? extends Block>> materialOwnedBlocks = new ArrayList<>();
        List<DeferredItem<? extends Item>> creativeTabItems = new ArrayList<>();

        compressionBlocks.put(CompressionTier.DENSE_CLAY.id(), FoundationBlockRegistration.DENSE_CLAY);
        compressionItems.put(CompressionTier.DENSE_CLAY.id(), FoundationItemRegistration.DENSE_CLAY);
        compressionBlocks.put(CompressionTier.COMPRESSED_CLAY.id(), FoundationBlockRegistration.COMPRESSED_CLAY);
        compressionItems.put(CompressionTier.COMPRESSED_CLAY.id(), FoundationItemRegistration.COMPRESSED_CLAY);
        for (CompressionTier compressionTier : CompressionTier.values()) {
            if (compressionBlocks.containsKey(compressionTier.id())) {
                continue;
            }
            DeferredBlock<Block> block = registerBlock(compressionTier.id(), compressionTier.originalMetadata() + 2.5F);
            DeferredItem<FoundationBlockItem> item = registerBlockItem(
                    compressionTier.id(), block, compressionTier.originalMetadata());
            compressionBlocks.put(compressionTier.id(), block);
            compressionItems.put(compressionTier.id(), item);
            materialOwnedBlocks.add(block);
            creativeTabItems.add(item);
        }

        for (var variant : ClayMaterials.VARIANTS) {
            if (variant.id().equals("minecraft:clay") || variant.id().equals("minecraft:clay_ball")) {
                continue;
            }
            if (variant.shape() == MaterialShape.BLOCK) {
                DeferredBlock<? extends Block> block;
                DeferredItem<? extends Item> item;
                if (compressionBlocks.containsKey(variant.id())) {
                    block = compressionBlocks.get(variant.id());
                    item = compressionItems.get(variant.id());
                } else if (variant.id().equals("clayium_block")) {
                    block = FoundationBlockRegistration.CLAYIUM_BLOCK;
                    item = FoundationItemRegistration.CLAYIUM_BLOCK;
                } else {
                    DeferredBlock<Block> registeredBlock = registerBlock(variant.id(), 3.0F + variant.tier());
                    DeferredItem<FoundationBlockItem> registeredItem = registerBlockItem(
                            variant.id(), registeredBlock, variant.tier());
                    block = registeredBlock;
                    item = registeredItem;
                    materialOwnedBlocks.add(block);
                    creativeTabItems.add(item);
                }
                materialBlocks.put(variant.id(), block);
                materialItems.put(variant.id(), item);
            } else if (variant.id().equals("industrial_clay_dust")) {
                materialItems.put(variant.id(), FoundationItemRegistration.INDUSTRIAL_CLAY_DUST);
            } else if (variant.id().equals("copper_ingot")) {
                materialItems.put(variant.id(), () -> Items.COPPER_INGOT);
            } else {
                DeferredItem<FoundationItem> item = ClayiumRegistries.ITEMS.register(
                        variant.id(), () -> new FoundationItem(itemProperties(variant.tier())));
                materialItems.put(variant.id(), item);
                creativeTabItems.add(item);
            }
        }

        // The original blockMaterial metadata 9 OEC block is distinct from the
        // blockCompressedClay metadata 12 material shape.
        DeferredBlock<Block> oecBlock = registerBlock("oec_block", 15.0F);
        DeferredItem<FoundationBlockItem> oecBlockItem = registerBlockItem("oec_block", oecBlock, 12);
        OEC_BLOCK = oecBlock;
        OEC_BLOCK_ITEM = oecBlockItem;
        materialOwnedBlocks.add(oecBlock);
        creativeTabItems.add(oecBlockItem);

        DeferredItem<FoundationItem> impureOsmium = ClayiumRegistries.ITEMS.register(
                "impure_osmium_ingot", () -> new FoundationItem(itemProperties(-1)));
        materialItems.put("impure_osmium_ingot", impureOsmium);
        creativeTabItems.add(impureOsmium);

        for (ComponentDefinition definition : COMPONENT_DEFINITIONS) {
            DeferredItem<FoundationItem> item = ClayiumRegistries.ITEMS.register(
                    definition.id(), () -> new FoundationItem(itemProperties(definition.tier())));
            components.put(definition.id(), item);
            creativeTabItems.add(item);
        }

        for (MaterialBlockDefinition definition : MaterialBlockDefinition.ORIGINAL_METAL_BLOCKS) {
            DeferredBlock<Block> block = ClayiumRegistries.BLOCKS.registerSimpleBlock(
                    definition.id(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(2.0F, 2.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE));
            DeferredItem<FoundationBlockItem> item = registerBlockItem(
                    definition.id(), block, definition.tier());
            metalBlocks.put(definition.id(), block);
            materialOwnedBlocks.add(block);
            creativeTabItems.add(item);
        }

        for (SiliconeBlockDefinition definition : SiliconeBlockDefinition.ORIGINAL_COLORS) {
            DeferredBlock<Block> block = ClayiumRegistries.BLOCKS.registerSimpleBlock(
                    definition.id(),
                    BlockBehaviour.Properties.of()
                            .mapColor(definition.color().getMapColor())
                            .strength(2.0F, 2.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE));
            DeferredItem<FoundationBlockItem> item = registerBlockItem(definition.id(), block, ClayMaterials.VARIANTS_BY_ID.get("silicone_block").tier());
            siliconeBlocks.put(definition.id(), block);
            materialOwnedBlocks.add(block);
            creativeTabItems.add(item);
        }

        for (String id : List.of(
                "compressed_clay_shard", "industrial_clay_shard", "advanced_industrial_clay_shard")) {
            DeferredItem<FoundationItem> item = ClayiumRegistries.ITEMS.register(
                    id, () -> new FoundationItem(new Item.Properties()));
            shards.put(id, item);
            creativeTabItems.add(item);
        }

        COMPRESSION_BLOCKS = immutableMap(compressionBlocks);
        MATERIAL_BLOCKS = immutableMap(materialBlocks);
        MATERIAL_ITEMS = immutableMap(materialItems);
        COMPONENTS = immutableMap(components);
        METAL_BLOCKS = immutableMap(metalBlocks);
        SILICONE_BLOCKS = immutableMap(siliconeBlocks);
        SHARDS = immutableMap(shards);
        MATERIAL_OWNED_BLOCKS = List.copyOf(materialOwnedBlocks);
        CREATIVE_TAB_ITEMS = List.copyOf(creativeTabItems);
    }

    private static DeferredBlock<Block> registerBlock(String id, float hardness) {
        return ClayiumRegistries.BLOCKS.registerSimpleBlock(
                id,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.CLAY)
                        .strength(hardness, hardness + 2.0F)
                        .sound(SoundType.STONE));
    }

    private static DeferredItem<FoundationBlockItem> registerBlockItem(
            String id, DeferredBlock<? extends Block> block, int tier) {
        return ClayiumRegistries.ITEMS.register(
                id, () -> new FoundationBlockItem(block.get(), itemProperties(tier)));
    }

    private static Item.Properties itemProperties(int tier) {
        return tier < 0 ? new Item.Properties()
                : new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier);
    }

    private static ComponentDefinition component(String id, String texture, int tier) {
        return new ComponentDefinition(id, texture, tier);
    }

    public static boolean isVanillaBacked(String id) {
        return id.equals("copper_ingot");
    }

    private static <T> Map<String, T> immutableMap(Map<String, T> map) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    public record ComponentDefinition(String id, String texture, int tier) {
    }

    private MaterialRegistration() {
    }
}
