package net.claustra01.clayium.registry;

import java.util.List;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.world.claytree.ClayTreeSaplingBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ClayTreeRegistration {
    public static final DeferredBlock<ClayTreeSaplingBlock> CLAY_TREE_SAPLING = ClayiumRegistries.BLOCKS.register(
            "clay_tree_sapling",
            () -> new ClayTreeSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));
    public static final DeferredBlock<LeavesBlock> CLAY_TREE_LEAF = ClayiumRegistries.BLOCKS.register(
            "clay_tree_leaf",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)));
    public static final DeferredBlock<RotatedPillarBlock> CLAY_TREE_LOG = ClayiumRegistries.BLOCKS.register(
            "clay_tree_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).strength(1.5F)));

    public static final DeferredItem<FoundationBlockItem> CLAY_TREE_SAPLING_ITEM = registerItem(
            "clay_tree_sapling", CLAY_TREE_SAPLING);
    public static final DeferredItem<FoundationBlockItem> CLAY_TREE_LEAF_ITEM = registerItem(
            "clay_tree_leaf", CLAY_TREE_LEAF);
    public static final DeferredItem<FoundationBlockItem> CLAY_TREE_LOG_ITEM = registerItem(
            "clay_tree_log", CLAY_TREE_LOG);

    public static final List<DeferredBlock<? extends Block>> BLOCKS = List.of(
            CLAY_TREE_SAPLING, CLAY_TREE_LEAF, CLAY_TREE_LOG);
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS = List.of(
            CLAY_TREE_SAPLING_ITEM, CLAY_TREE_LEAF_ITEM, CLAY_TREE_LOG_ITEM);

    private static DeferredItem<FoundationBlockItem> registerItem(
            String id, DeferredBlock<? extends Block> block) {
        return ClayiumRegistries.ITEMS.register(
                id,
                () -> new FoundationBlockItem(
                        block.get(),
                        new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), 7)));
    }

    private ClayTreeRegistration() {
    }
}
