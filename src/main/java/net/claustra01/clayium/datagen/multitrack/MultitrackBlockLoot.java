package net.claustra01.clayium.datagen.multitrack;

import java.util.Set;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class MultitrackBlockLoot extends BlockLootSubProvider {
    public MultitrackBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }
    @Override protected void generate() { MultitrackRegistration.BLOCKS.forEach(block -> dropSelf(block.get())); }
    @Override protected Iterable<Block> getKnownBlocks() {
        return MultitrackRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
