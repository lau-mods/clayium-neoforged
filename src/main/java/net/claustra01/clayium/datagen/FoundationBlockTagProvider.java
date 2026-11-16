package net.claustra01.clayium.datagen;

import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.FoundationBlockRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
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
                FoundationBlockRegistration.CLAY_ORE.get(),
                FoundationBlockRegistration.CLAYIUM_BLOCK.get(),
                FoundationBlockRegistration.CLAY_BENDING_MACHINE.get());
        MaterialRegistration.MATERIAL_OWNED_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        tag(BlockTags.NEEDS_STONE_TOOL).add(
                FoundationBlockRegistration.CLAY_ORE.get(),
                FoundationBlockRegistration.CLAYIUM_BLOCK.get());
    }
}
