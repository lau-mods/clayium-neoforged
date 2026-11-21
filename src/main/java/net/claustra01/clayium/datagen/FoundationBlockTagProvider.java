package net.claustra01.clayium.datagen;

import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.claustra01.clayium.registry.FoundationBlockRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.FormingMachineRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.claustra01.clayium.registry.WaterWheelRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.claustra01.clayium.world.claytree.ClayTreeLaserConversion;
import net.claustra01.clayium.tool.advanced.ClayToolProperties;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class FoundationBlockTagProvider extends BlockTagsProvider {
    public FoundationBlockTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                FoundationBlockRegistration.DENSE_CLAY.get(),
                FoundationBlockRegistration.COMPRESSED_CLAY.get(),
                FoundationBlockRegistration.CLAYIUM_BLOCK.get(),
                WorkbenchRegistration.CLAY_WORK_TABLE.get(),
                WorkbenchRegistration.CLAY_CRAFTING_BOARD.get(),
                WorkbenchRegistration.RAW_CLAY_MACHINE_HULL.get(),
                WorkbenchRegistration.CLAY_MACHINE_HULL.get());
        java.util.stream.Stream.concat(
                MachineRegistration.BLOCKS.stream(), FormingMachineRegistration.BLOCKS.stream())
                .forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(WorldgenRegistration.BLOCKS.stream()
                .map(block -> block.get()).toArray(Block[]::new));
        tag(ClayToolProperties.CLAY_ORES).add(WorldgenRegistration.BLOCKS.stream()
                .map(block -> block.get()).toArray(Block[]::new));
        tag(ClayToolProperties.ORDINARY_CLAY_ORES).add(
                WorldgenRegistration.CLAY_ORE.get(), WorldgenRegistration.DEEPSLATE_CLAY_ORE.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(WaterWheelRegistration.BLOCKS.stream()
                .map(block -> block.get()).toArray(Block[]::new));
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(AutoCrafterRegistration.BLOCKS.stream()
                .map(block -> block.get()).toArray(Block[]::new));
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MultiblockRegistration.CONTROLLERS.values().stream()
                .map(block -> (Block)block.get()).toArray(Block[]::new));
        MaterialRegistration.MATERIAL_OWNED_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        tag(BlockTags.NEEDS_STONE_TOOL).add(
                WorldgenRegistration.CLAY_ORE.get(),
                WorldgenRegistration.DEEPSLATE_CLAY_ORE.get(),
                WorldgenRegistration.DENSE_CLAY_ORE.get(),
                WorldgenRegistration.DEEPSLATE_DENSE_CLAY_ORE.get(),
                WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get(),
                WorldgenRegistration.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get(),
                FoundationBlockRegistration.CLAYIUM_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(ClayTreeRegistration.CLAY_TREE_LOG.get());
        tag(BlockTags.LOGS).add(ClayTreeRegistration.CLAY_TREE_LOG.get());
        tag(BlockTags.LOGS_THAT_BURN).add(ClayTreeRegistration.CLAY_TREE_LOG.get());
        tag(BlockTags.LEAVES).add(ClayTreeRegistration.CLAY_TREE_LEAF.get());
        tag(BlockTags.SAPLINGS).add(ClayTreeRegistration.CLAY_TREE_SAPLING.get());
        tag(ClayTreeLaserConversion.LASER_TREE_SOURCES).add(
                Blocks.OAK_SAPLING,
                Blocks.SPRUCE_SAPLING,
                Blocks.BIRCH_SAPLING,
                Blocks.JUNGLE_SAPLING,
                Blocks.ACACIA_SAPLING,
                Blocks.DARK_OAK_SAPLING,
                Blocks.CHERRY_SAPLING,
                Blocks.MANGROVE_PROPAGULE,
                Blocks.AZALEA,
                Blocks.FLOWERING_AZALEA);
    }
}
