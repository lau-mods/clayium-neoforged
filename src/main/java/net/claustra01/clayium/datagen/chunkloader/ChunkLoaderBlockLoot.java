package net.claustra01.clayium.datagen.chunkloader;

import java.util.Set;
import net.claustra01.clayium.registry.ChunkLoaderRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class ChunkLoaderBlockLoot extends BlockLootSubProvider {
    public ChunkLoaderBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        dropSelf(ChunkLoaderRegistration.CHUNK_LOADER.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Set.of(ChunkLoaderRegistration.CHUNK_LOADER.get());
    }
}
