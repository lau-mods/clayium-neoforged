package net.claustra01.clayium.datagen.miner;

import java.util.Set;
import net.claustra01.clayium.registry.MinerRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class MinerBlockLoot extends BlockLootSubProvider {
    public MinerBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        MinerRegistration.BLOCKS.forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return MinerRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
