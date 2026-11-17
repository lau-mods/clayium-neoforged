package net.claustra01.clayium.registry;

import java.util.List;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.worldgen.CompositeClayOreFeature;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class WorldgenRegistration {
    public static final DeferredBlock<Block> CLAY_ORE = registerOre("clay_ore", Blocks.STONE, true);
    public static final DeferredBlock<Block> DEEPSLATE_CLAY_ORE = registerOre(
            "deepslate_clay_ore", Blocks.DEEPSLATE_IRON_ORE, true);
    public static final DeferredBlock<Block> DENSE_CLAY_ORE = registerOre("dense_clay_ore", Blocks.STONE, false);
    public static final DeferredBlock<Block> DEEPSLATE_DENSE_CLAY_ORE = registerOre(
            "deepslate_dense_clay_ore", Blocks.DEEPSLATE_IRON_ORE, false);
    public static final DeferredBlock<Block> LARGE_DENSE_CLAY_ORE = registerOre(
            "large_dense_clay_ore", Blocks.STONE, false);
    public static final DeferredBlock<Block> DEEPSLATE_LARGE_DENSE_CLAY_ORE = registerOre(
            "deepslate_large_dense_clay_ore", Blocks.DEEPSLATE_IRON_ORE, false);

    public static final List<DeferredBlock<Block>> BLOCKS = List.of(
            CLAY_ORE,
            DEEPSLATE_CLAY_ORE,
            DENSE_CLAY_ORE,
            DEEPSLATE_DENSE_CLAY_ORE,
            LARGE_DENSE_CLAY_ORE,
            DEEPSLATE_LARGE_DENSE_CLAY_ORE);
    public static final List<DeferredItem<BlockItem>> ITEMS = List.of(
            registerItem("clay_ore", CLAY_ORE, 1),
            registerItem("deepslate_clay_ore", DEEPSLATE_CLAY_ORE, 1),
            registerItem("dense_clay_ore", DENSE_CLAY_ORE, 2),
            registerItem("deepslate_dense_clay_ore", DEEPSLATE_DENSE_CLAY_ORE, 2),
            registerItem("large_dense_clay_ore", LARGE_DENSE_CLAY_ORE, 2),
            registerItem("deepslate_large_dense_clay_ore", DEEPSLATE_LARGE_DENSE_CLAY_ORE, 2));

    public static final DeferredHolder<Feature<?>, CompositeClayOreFeature> COMPOSITE_CLAY_ORE =
            ClayiumRegistries.FEATURES.register("composite_clay_ore", CompositeClayOreFeature::new);

    private static DeferredBlock<Block> registerOre(String id, Block propertiesSource, boolean dropsExperience) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(propertiesSource)
                .strength(3.0F, 5.0F)
                .requiresCorrectToolForDrops();
        if (dropsExperience) {
            return ClayiumRegistries.BLOCKS.register(
                    id, () -> new DropExperienceBlock(UniformInt.of(0, 1), properties));
        }
        return ClayiumRegistries.BLOCKS.registerSimpleBlock(id, properties);
    }

    private static DeferredItem<BlockItem> registerItem(String id, DeferredBlock<Block> block, int tier) {
        return ClayiumRegistries.ITEMS.register(
                id,
                () -> new FoundationBlockItem(
                        block.get(),
                        new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier)));
    }

    private WorldgenRegistration() {
    }
}
