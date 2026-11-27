package net.claustra01.clayium.datagen;

import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.ClayOreBlock;
import net.claustra01.clayium.block.ClayTreeLeavesBlock;
import net.claustra01.clayium.block.ClayTreeLogBlock;
import net.claustra01.clayium.block.ClayTreeSaplingBlock;
import net.claustra01.clayium.block.CompressedClayBlock;
import net.claustra01.clayium.block.MachineHullBlock;
import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.block.MetalMaterialBlock;
import net.claustra01.clayium.block.PanCableBlock;
import net.claustra01.clayium.block.QuartzCrucibleBlock;
import net.claustra01.clayium.block.SiliconeColoredBlock;
import net.claustra01.clayium.block.UpgradeBlock;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ClayiumBlockTagsProvider extends BlockTagsProvider {
    public ClayiumBlockTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var shovel = tag(BlockTags.MINEABLE_WITH_SHOVEL);
        var stoneTool = tag(BlockTags.NEEDS_STONE_TOOL);
        var beaconBase = tag(BlockTags.BEACON_BASE_BLOCKS);
        var leaves = tag(BlockTags.LEAVES);
        var logs = tag(BlockTags.LOGS);
        var saplings = tag(BlockTags.SAPLINGS);
        var ores = tag(Tags.Blocks.ORES);
        var storageBlocks = tag(Tags.Blocks.STORAGE_BLOCKS);
        var clayOres = tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/clay")));
        var denseClayOres = tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/dense_clay")));
        var largeDenseClayOres = tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/large_dense_clay")));

        for (var entry : ClayiumBlocks.all().entrySet()) {
            String path = entry.getKey();
            Block block = entry.getValue().get();

            if (block instanceof CompressedClayBlock) {
                shovel.add(block);
            } else if ("raw_clay_machine_hull".equals(path)) {
                shovel.add(block);
            } else if (block instanceof ClayOreBlock oreBlock) {
                pickaxe.add(block);
                stoneTool.add(block);
                ores.add(block);
                switch (oreBlock.getVariantIndex()) {
                    case 1 -> denseClayOres.add(block);
                    case 2 -> largeDenseClayOres.add(block);
                    default -> clayOres.add(block);
                }
            } else if (block instanceof MetalMaterialBlock metalBlock) {
                pickaxe.add(block);
                beaconBase.add(block);
                storageBlocks.add(block);
                tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + metalBlock.getMaterial().idPrefix())))
                        .add(block);
                if ("aluminium".equals(metalBlock.getMaterial().idPrefix())) {
                    tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/aluminum")))
                            .add(block);
                }
            } else if (block instanceof MetalChestBlock || block instanceof SiliconeColoredBlock) {
                pickaxe.add(block);
                if (block instanceof SiliconeColoredBlock) {
                    tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/silicone"))).add(block);
                }
            } else if (block instanceof ClayTreeLeavesBlock) {
                leaves.add(block);
            } else if (block instanceof ClayTreeLogBlock) {
                logs.add(block);
                shovel.add(block);
            } else if (block instanceof ClayTreeSaplingBlock) {
                saplings.add(block);
            } else if (block instanceof MachineHullBlock
                    || block instanceof UpgradeBlock
                    || block instanceof ClayContainerBlock
                    || block instanceof PanCableBlock
                    || block instanceof QuartzCrucibleBlock
                    || path.contains("ca_reactor_")) {
                pickaxe.add(block);
            }
        }
    }
}
